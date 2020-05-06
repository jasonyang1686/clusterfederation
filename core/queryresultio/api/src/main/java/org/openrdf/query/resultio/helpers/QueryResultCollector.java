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
package org.openrdf.query.resultio.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQueryResultHandler;
import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * An implementation of the {@link QueryResultHandler} interface that is able to
 * collect a single result from either Boolean or Tuple results simultaneously.
 * <p>
 * The {@link List}s that are returned by this interface are immutable.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class QueryResultCollector implements QueryResultHandler, TupleQueryResultHandler, BooleanQueryResultHandler {

	private boolean hasBooleanSet = false;

	private Boolean value = null;

	private boolean endQueryResultFound = false;

	private List<String> bindingNames = Collections.emptyList();

	private List<BindingSet> bindingSets = Collections.emptyList();

	private List<String> links = new ArrayList<String>();

	public QueryResultCollector() {
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		hasBooleanSet = true;
		this.value = value;
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		endQueryResultFound = false;
		this.bindingNames = Collections.unmodifiableList(new ArrayList<String>(bindingNames));
		bindingSets = new ArrayList<BindingSet>();
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		endQueryResultFound = false;
		bindingSets.add(bindingSet);
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		endQueryResultFound = true;
		// the binding sets cannot be modified after this point without a call to
		// startQueryResult which will reset the bindingsets
		bindingSets = Collections.unmodifiableList(bindingSets);
		// reset the start query result found variable at this point
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		this.links.addAll(linkUrls);
	}

	/**
	 * Determines whether {@link #handleBoolean(boolean)} was called for this
	 * collector.
	 * 
	 * @return True if there was a boolean handled by this collector.
	 * @since 2.7.0
	 */
	public boolean getHandledBoolean() {
		return hasBooleanSet;
	}

	/**
	 * If {@link #getHandledBoolean()} returns true this method returns the
	 * boolean that was last found using {@link #handleBoolean(boolean)}
	 * <p>
	 * If {@link #getHandledBoolean()} returns false this method throws a
	 * {@link QueryResultHandlerException} indicating that a response could not
	 * be provided.
	 * 
	 * @return The boolean value that was collected.
	 * @throws QueryResultHandlerException
	 *         If there was no boolean value collected.
	 * @since 2.7.0
	 */
	public boolean getBoolean()
		throws QueryResultHandlerException
	{
		if (!hasBooleanSet) {
			throw new QueryResultHandlerException("Did not collect a boolean value");
		}
		else {
			return this.value;
		}
	}

	/**
	 * Determines whether {@link #endQueryResult()} was called after the last
	 * calls to {@link #startQueryResult(List)} and optionally calls to
	 * {@link #handleSolution(BindingSet)}.
	 * 
	 * @return True if there was a call to {@link #endQueryResult()} after the
	 *         last calls to {@link #startQueryResult(List)} and
	 *         {@link #handleSolution(BindingSet)}.
	 * @since 2.7.0
	 */
	public boolean getHandledTuple() {
		return endQueryResultFound;
	}

	/**
	 * Returns a collection of binding names collected.
	 * 
	 * @return An immutable list of {@link String}s that were collected as the
	 *         binding names.
	 * @throws QueryResultHandlerException
	 *         If the tuple results set was not successfully collected, as
	 *         signalled by a call to {@link #endQueryResult()}.
	 * @since 2.7.0
	 */
	public List<String> getBindingNames()
		throws QueryResultHandlerException
	{
		if (!endQueryResultFound) {
			throw new QueryResultHandlerException("Did not successfully collect a tuple results set.");
		}
		else {
			return bindingNames;
		}
	}

	/**
	 * @return An immutable list of {@link BindingSet}s that were collected as
	 *         the tuple results.
	 * @throws QueryResultHandlerException
	 *         If the tuple results set was not successfully collected, as
	 *         signalled by a call to {@link #endQueryResult()}.
	 * @since 2.7.0
	 */
	public List<BindingSet> getBindingSets()
		throws QueryResultHandlerException
	{
		if (!endQueryResultFound) {
			throw new QueryResultHandlerException("Did not successfully collect a tuple results set.");
		}
		else {
			return bindingSets;
		}
	}

	/**
	 * @return A list of links accumulated from calls to
	 *         {@link #handleLinks(List)}.
	 * @since 2.7.0
	 */
	public List<String> getLinks() {
		return Collections.unmodifiableList(links);
	}
}
