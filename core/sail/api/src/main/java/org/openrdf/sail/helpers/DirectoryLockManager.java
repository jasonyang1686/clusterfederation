/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.security.AccessControlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.sail.LockManager;
import org.openrdf.sail.SailLockedException;

/**
 * Used to create a lock in a directory.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class DirectoryLockManager implements LockManager {

	private static final String LOCK_DIR_NAME = "lock";

	private static final String LOCK_FILE_NAME = "locked";

	private static final String INFO_FILE_NAME = "process";

	private final Logger logger = LoggerFactory.getLogger(DirectoryLockManager.class);

	private final File dir;

	public DirectoryLockManager(File dir) {
		this.dir = dir;
	}

	public String getLocation() {
		return dir.toString();
	}

	private File getLockDir() {
		return new File(dir, LOCK_DIR_NAME);
	}

	/**
	 * Determines if the directory is locked.
	 * 
	 * @return <code>true</code> if the directory is already locked.
	 */
	public boolean isLocked() {
		return getLockDir().exists();
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock or null if the directory is already locked.
	 */
	public Lock tryLock() {
		File lockDir = getLockDir();

		if (lockDir.exists()) {
			removeInvalidLock(lockDir);
		}

		if (!lockDir.mkdir()) {
			return null;
		}

		Lock lock = null;

		try {
			File infoFile = new File(lockDir, INFO_FILE_NAME);
			File lockedFile = new File(lockDir, LOCK_FILE_NAME);

			RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			try {
				FileLock fileLock = raf.getChannel().lock();
				lock = createLock(raf, fileLock);
				sign(infoFile);
			}
			catch (IOException e) {
				if (lock != null) {
					// Also closes raf
					lock.release();
				}
				else {
					raf.close();
				}
				throw e;
			}
		}
		catch (IOException e) {
			logger.error(e.toString(), e);
		}

		return lock;
	}

	/**
	 * Creates a lock in a directory if it does not yet exist.
	 * 
	 * @return a newly acquired lock.
	 * @throws SailLockedException
	 *         if the directory is already locked.
	 */
	public Lock lockOrFail()
		throws SailLockedException
	{
		Lock lock = tryLock();

		if (lock != null) {
			return lock;
		}

		String requestedBy = getProcessName();
		String lockedBy = getLockedBy();

		if (lockedBy != null) {
			throw new SailLockedException(lockedBy, requestedBy, this);
		}

		lock = tryLock();
		if (lock != null) {
			return lock;
		}

		throw new SailLockedException(requestedBy);
	}

	/**
	 * Revokes a lock owned by another process.
	 * 
	 * @return <code>true</code> if a lock was successfully revoked.
	 */
	public boolean revokeLock() {
		File lockDir = getLockDir();
		File lockedFile = new File(lockDir, LOCK_FILE_NAME);
		File infoFile = new File(lockDir, INFO_FILE_NAME);
		lockedFile.delete();
		infoFile.delete();
		return lockDir.delete();
	}

	private void removeInvalidLock(File lockDir) {
		try {
			boolean revokeLock = false;

			File lockedFile = new File(lockDir, LOCK_FILE_NAME);
			RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
			try {
				FileLock fileLock = raf.getChannel().tryLock();

				if (fileLock != null) {
					logger.warn("Removing invalid lock {}", getLockedBy());
					fileLock.release();
					revokeLock = true;
				}
			}
			catch (OverlappingFileLockException exc) {
				// lock is still valid
			}
			finally {
				raf.close();
			}

			if (revokeLock) {
				revokeLock();
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
		}
	}

	private String getLockedBy() {
		try {
			File lockDir = getLockDir();
			File infoFile = new File(lockDir, INFO_FILE_NAME);
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			try {
				return reader.readLine();
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			logger.warn(e.toString(), e);
			return null;
		}
	}

	private Lock createLock(final RandomAccessFile raf, final FileLock fileLock) {
		return new Lock() {

			private Thread hook;
			{
				try {
					Thread hook = new Thread(new Runnable() {

						public void run() {
							delete();
						}
					});
					Runtime.getRuntime().addShutdownHook(hook);
					this.hook = hook;
				}
				catch (AccessControlException e) {
					// okay, just remember to close it yourself
				}
			}

			public boolean isActive() {
				return fileLock.isValid() || hook != null;
			}

			public void release() {
				try {
					if (hook != null) {
						Runtime.getRuntime().removeShutdownHook(hook);
						hook = null;
					}
				}
				catch (IllegalStateException e) {
					// already shutting down
				}
				catch (AccessControlException e) {
					logger.warn(e.toString(), e);
				}
				delete();
			}

			synchronized void delete() {
				try {
					if (raf.getChannel().isOpen()) {
						fileLock.release();
						raf.close();
					}
				}
				catch (IOException e) {
					logger.warn(e.toString(), e);
				}

				revokeLock();
			}
		};
	}

	private void sign(File infoFile)
		throws IOException
	{
		FileWriter out = new FileWriter(infoFile);
		try {
			out.write(getProcessName());
			out.flush();
		}
		finally {
			out.close();
		}
	}

	private String getProcessName() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}
}
