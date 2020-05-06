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

package info.aduna.iteration;

/**
 * A CloseableIteration that converts an iteration over objects of type
 * <tt>S</tt> (the source type) to an iteration over objects of type
 * <tt>T</tt> (the target type).
 */
public abstract class ConvertingIteration<S, T, X extends Exception> extends CloseableIterationBase<T, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The source type iteration.
	 */
	private final Iteration<? extends S, ? extends X> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ConvertingIteration that operates on the supplied source
	 * type iteration.
	 * 
	 * @param iter
	 *        The source type iteration for this <tt>ConvertingIteration</tt>,
	 *        must not be <tt>null</tt>.
	 */
	public ConvertingIteration(Iteration<? extends S, ? extends X> iter) {
		assert iter != null;
		this.iter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Converts a source type object to a target type object.
	 */
	protected abstract T convert(S sourceObject)
		throws X;

	/**
	 * Checks whether the source type iteration contains more elements.
	 * 
	 * @return <tt>true</tt> if the source type iteration contains more
	 *         elements, <tt>false</tt> otherwise.
	 * @throws X
	 */
	public boolean hasNext()
		throws X
	{
		return iter.hasNext();
	}

	/**
	 * Returns the next element from the source type iteration.
	 * 
	 * @throws X
	 * @throws java.util.NoSuchElementException
	 *         If all elements have been returned.
	 * @throws IllegalStateException
	 *         If the iteration has been closed.
	 */
	public T next()
		throws X
	{
		return convert(iter.next());
	}

	/**
	 * Calls <tt>remove()</tt> on the underlying Iteration.
	 * 
	 * @throws UnsupportedOperationException
	 *         If the wrapped Iteration does not support the <tt>remove</tt>
	 *         operation.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed, or if {@link #next} has not yet
	 *         been called, or {@link #remove} has already been called after the
	 *         last call to {@link #next}.
	 */
	public void remove()
		throws X
	{
		iter.remove();
	}

	/**
	 * Closes this iteration as well as the wrapped iteration if it is a
	 * {@link CloseableIteration}.
	 */
	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		Iterations.closeCloseable(iter);
	}
}
