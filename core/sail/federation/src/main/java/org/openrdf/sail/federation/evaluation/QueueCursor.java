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
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.LookAheadIteration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openrdf.query.QueryEvaluationException;

/**
 * Makes working with a queue easier by adding the methods {@link #done()} and
 * {@link #toss(Exception)} and automatically converting the exception into a
 * QueryEvaluationException with an appropriate stack trace.
 * 
 * @author James Leigh
 */
public class QueueCursor<E> extends
		LookAheadIteration<E, QueryEvaluationException> {

	private volatile boolean done;

	private BlockingQueue<E> queue;

	private final E afterLast = createAfterLast();

	private volatile Queue<Throwable> exceptions = new LinkedList<Throwable>();

	/**
	 * Creates an <tt>QueueCursor</tt> with the given (fixed) capacity and
	 * default access policy.
	 * 
	 * @param capacity
	 *            the capacity of this queue
	 */
	public QueueCursor(int capacity) {
		this(capacity, false);
	}

	/**
	 * Creates an <tt>QueueCursor</tt> with the given (fixed) capacity and the
	 * specified access policy.
	 * 
	 * @param capacity
	 *            the capacity of this queue
	 * @param fair
	 *            if <tt>true</tt> then queue accesses for threads blocked on
	 *            insertion or removal, are processed in FIFO order; if
	 *            <tt>false</tt> the access order is unspecified.
	 */
	public QueueCursor(int capacity, boolean fair) {
		super();
		this.queue = new ArrayBlockingQueue<E>(capacity, fair);
	}

	/**
	 * The next time {@link #next()} is called this exception will be thrown. If
	 * it is not a QueryEvaluationException or RuntimeException it will be
	 * wrapped in a QueryEvaluationException.
	 */
	public void toss(Exception exception) {
		synchronized (exceptions) {
			exceptions.add(exception);
		}
	}

	/**
	 * Adds another item to the queue, blocking while the queue is full.
	 */
	public void put(E item) throws InterruptedException {
		queue.put(item);
	}

	/**
	 * Indicates the method {@link #put(Object)} will not be called in the queue
	 * anymore.
	 */
	public void done() {
		done = true;
		try {
			queue.add(afterLast);
		} catch (IllegalStateException e) { // NOPMD
			// no thread is waiting on this queue anyway
		}
	}

	/**
	 * Returns the next item in the queue or throws an exception.
	 */
	@Override
	public E getNextElement() throws QueryEvaluationException {
		try {
			checkException();
			E take;
			if (done) {
				take = queue.poll();
			} else {
				take = queue.take();
				if (done) {
					done(); // in case the queue was full before
				}
			}
			if (isAfterLast(take)) {
				checkException();
				done(); // put afterLast back for others
				take = null; // NOPMD
			}
			return take;
		} catch (InterruptedException e) {
			checkException();
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public void handleClose() throws QueryEvaluationException {
		checkException();
	}

	public void checkException() throws QueryEvaluationException {
		synchronized (exceptions) {
			if (!exceptions.isEmpty()) {
				try {
					throw exceptions.remove();
				} catch (QueryEvaluationException e) {
					modifyStackTraceAndRethrow(e, 1);
				} catch (RuntimeException e) {
					modifyStackTraceAndRethrow(e, 0);
				} catch (Throwable e) { // NOPMD
					throw new QueryEvaluationException(e);
				}
			}
		}
	}

	private <X extends Exception> void modifyStackTraceAndRethrow(X exception,
			int firstIndex) throws X {
		List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
		stack.addAll(Arrays.asList(exception.getStackTrace()));
		StackTraceElement[] thisStack = new Throwable().getStackTrace(); // NOPMD
		stack.addAll(Arrays.asList(thisStack).subList(firstIndex,
				thisStack.length));
		exception.setStackTrace(stack.toArray(new StackTraceElement[stack
				.size()]));
		throw exception;
	}

	private boolean isAfterLast(E take) {
		return take == null || take == afterLast; // NOPMD
	}

	@SuppressWarnings("unchecked")
	private E createAfterLast() {
		return (E) new Object();
	}

}
