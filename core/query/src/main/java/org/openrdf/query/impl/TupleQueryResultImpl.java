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
package org.openrdf.query.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * A generic implementation of the {@link TupleQueryResult} interface.
 */
public class TupleQueryResultImpl extends IterationWrapper<BindingSet, QueryEvaluationException> implements
		TupleQueryResult
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final List<String> bindingNames;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public TupleQueryResultImpl(List<String> bindingNames, Iterable<? extends BindingSet> bindingSets) {
		this(bindingNames, bindingSets.iterator());
	}

	public TupleQueryResultImpl(List<String> bindingNames, Iterator<? extends BindingSet> bindingSetIter) {
		this(bindingNames, new CloseableIteratorIteration<BindingSet, QueryEvaluationException>(bindingSetIter));
	}

	/**
	 * Creates a query result object with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public TupleQueryResultImpl(List<String> bindingNames,
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingSetIter)
	{
		super(bindingSetIter);
		// Don't allow modifications to the binding names when it is accessed
		// through getBindingNames:
		this.bindingNames = Collections.unmodifiableList(bindingNames);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public List<String> getBindingNames()
		throws QueryEvaluationException
	{
		return bindingNames;
	}

}
