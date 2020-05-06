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

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.QueryEvaluationException;

/**
 * If the primary cursor is empty, use the alternative cursor.
 * 
 * @author James Leigh
 */
public class AlternativeCursor<E> extends
		LookAheadIteration<E, QueryEvaluationException> {

	private CloseableIteration<? extends E, QueryEvaluationException> delegate;

	private final CloseableIteration<? extends E, QueryEvaluationException> primary;

	private final CloseableIteration<? extends E, QueryEvaluationException> alternative;

	public AlternativeCursor(
			CloseableIteration<? extends E, QueryEvaluationException> primary,
			CloseableIteration<? extends E, QueryEvaluationException> alternative) {
		super();
		this.alternative = alternative;
		this.primary = primary;
	}

	public void handleClose() throws QueryEvaluationException {
		primary.close();
		alternative.close();
	}

	public E getNextElement() throws QueryEvaluationException {
		if (delegate == null) {
			delegate = primary.hasNext() ? primary : alternative;
		}
		return delegate.hasNext() ? delegate.next() : null; // NOPMD
	}

	@Override
	public String toString() {
		String name = getClass().getName().replaceAll("^.*\\.|Cursor$", "");
		return name + "\n\t" + primary.toString() + "\n\t"
				+ alternative.toString();
	}
}
