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
package org.openrdf.sail.nativerdf.btree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import info.aduna.io.FileUtil;

/**
 * @author Arjohn Kampman
 */
public class BTreeTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static List<byte[]> TEST_VALUES = new ArrayList<byte[]>(256);

	private static List<byte[]> RANDOMIZED_TEST_VALUES = new ArrayList<byte[]>(256);

	static {
		for (int i = 0; i < 256; i++) {
			byte[] value = new byte[1];
			value[0] = (byte)i;
			TEST_VALUES.add(value);
		}

		RANDOMIZED_TEST_VALUES.addAll(TEST_VALUES);
		Collections.shuffle(RANDOMIZED_TEST_VALUES);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dir;

	private BTree btree;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		dir = FileUtil.createTempDir("btree");
		btree = new BTree(dir, "test", 85, 1);
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			btree.delete();
			FileUtil.deleteDir(dir);
		}
		finally {
			super.tearDown();
		}
	}

	public void testAddAscending()
		throws Exception
	{
		for (byte[] value : TEST_VALUES) {
			btree.insert(value);
		}
	}

	public void testAddDescending()
		throws Exception
	{
		for (int i = TEST_VALUES.size() - 1; i >= 0; i--) {
			btree.insert(TEST_VALUES.get(i));
		}
	}

	public void testAddRandom()
		throws Exception
	{
		for (byte[] value : RANDOMIZED_TEST_VALUES) {
			btree.insert(value);
		}
	}

	public void testRemoveAscending()
		throws Exception
	{
		testAddRandom();

		for (byte[] value : TEST_VALUES) {
			btree.remove(value);
		}
	}

	public void testRemoveDescending()
		throws Exception
	{
		testAddRandom();

		for (int i = TEST_VALUES.size() - 1; i >= 0; i--) {
			btree.remove(TEST_VALUES.get(i));
		}
	}

	public void testRemoveRandom()
		throws Exception
	{
		testAddAscending();

		for (byte[] value : RANDOMIZED_TEST_VALUES) {
			btree.remove(value);
		}
	}

	public void testConcurrentAccess()
		throws Exception
	{
		int meanIdx = TEST_VALUES.size() / 2;
		btree.insert(TEST_VALUES.get(meanIdx - 1));
		btree.insert(TEST_VALUES.get(meanIdx));
		btree.insert(TEST_VALUES.get(meanIdx + 1));

		RecordIterator iter1 = btree.iterateAll();
		iter1.next();

		RecordIterator iter2 = btree.iterateAll();
		iter2.next();
		iter2.next();
		iter2.next();

		for (byte[] value : TEST_VALUES) {
			btree.insert(value);
		}

		iter2.close();
		iter1.close();
	}

	public void testNewAndClear()
		throws Exception
	{
		btree.clear();
	}

	/* Test for SES-527
		public void testRootNodeSplit()
			throws Exception
		{
			// Fill the root node
			for (int i = 0; i < 15; i++) {
				btree.insert(TEST_VALUES.get(i));
			}

			// Fire up an iterator
			RecordIterator iter = btree.iterateAll();
			iter.next();

			// Force the root node to split
			btree.insert(TEST_VALUES.get(15));

			// Verify that the iterator returns all 15 elements
			int count = 0;
			while (iter.next() != null) {
				count++;
			}

			iter.close();
			
			assertEquals(15, count);
		}
	*/
}
