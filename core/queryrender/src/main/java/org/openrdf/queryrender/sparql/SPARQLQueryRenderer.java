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
package org.openrdf.queryrender.sparql;

import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.queryrender.QueryRenderer;

/**
 * <p>
 * Implementation of the {@link QueryRenderer} interface which renders queries
 * into the SPARQL syntax.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class SPARQLQueryRenderer implements QueryRenderer {

	/**
	 * The query renderer
	 */
	private SparqlTupleExprRenderer mRenderer = new SparqlTupleExprRenderer();

	/**
	 * @inheritDoc
	 */
	public QueryLanguage getLanguage() {
		return QueryLanguage.SPARQL;
	}

	/**
	 * @inheritDoc
	 */
	public String render(final ParsedQuery theQuery)
		throws Exception
	{
		mRenderer.reset();

		StringBuffer aBody = new StringBuffer(mRenderer.render(theQuery.getTupleExpr()));

		boolean aFirst = true;

		StringBuffer aQuery = new StringBuffer();

		if (theQuery instanceof ParsedTupleQuery) {
			aQuery.append("select ");
		}
		else if (theQuery instanceof ParsedBooleanQuery) {
			aQuery.append("ask\n");
		}
		else {
			aQuery.append("construct ");
		}

		if (mRenderer.isDistinct()) {
			aQuery.append("distinct ");
		}

		if (mRenderer.isReduced() && theQuery instanceof ParsedTupleQuery) {
			aQuery.append("reduced ");
		}

		if (!mRenderer.getProjection().isEmpty() && !(theQuery instanceof ParsedBooleanQuery)) {

			aFirst = true;

			if (!(theQuery instanceof ParsedTupleQuery)) {
				aQuery.append(" {\n");
			}

			for (ProjectionElemList aList : mRenderer.getProjection()) {
				if (SparqlTupleExprRenderer.isSPOElemList(aList)) {
					if (!aFirst) {
						aQuery.append("\n");
					}
					else {
						aFirst = false;
					}

					aQuery.append("  ").append(mRenderer.renderPattern(mRenderer.toStatementPattern(aList)));
				}
				else {
					for (ProjectionElem aElem : aList.getElements()) {
						if (!aFirst) {
							aQuery.append(" ");
						}
						else {
							aFirst = false;
						}

						aQuery.append("?" + aElem.getSourceName());

						// SPARQL does not support this, its an artifact of copy and
						// paste from the serql stuff
						// aQuery.append(mRenderer.getExtensions().containsKey(aElem.getSourceName())
						// ?
						// mRenderer.renderValueExpr(mRenderer.getExtensions().get(aElem.getSourceName()))
						// : "?"+aElem.getSourceName());
						//
						// if (!aElem.getSourceName().equals(aElem.getTargetName()) ||
						// (mRenderer.getExtensions().containsKey(aElem.getTargetName())
						// &&
						// !mRenderer.getExtensions().containsKey(aElem.getSourceName())))
						// {
						// aQuery.append(" as ").append(mRenderer.getExtensions().containsKey(aElem.getTargetName())
						// ?
						// mRenderer.renderValueExpr(mRenderer.getExtensions().get(aElem.getTargetName()))
						// : aElem.getTargetName());
						// }
					}
				}
			}

			if (!(theQuery instanceof ParsedTupleQuery)) {
				aQuery.append("}");
			}

			aQuery.append("\n");
		}
		else if (mRenderer.getProjection().isEmpty()) {
			if (theQuery instanceof ParsedGraphQuery) {
				aQuery.append("{ }\n");
			}
			else if (theQuery instanceof ParsedTupleQuery) {
				aQuery.append("*\n");
			}
		}

		if (theQuery.getDataset() != null) {
			for (URI aURI : theQuery.getDataset().getDefaultGraphs()) {
				aQuery.append("from <").append(aURI).append(">\n");
			}

			for (URI aURI : theQuery.getDataset().getNamedGraphs()) {
				aQuery.append("from named <").append(aURI).append(">\n");
			}
		}

		if (aBody.length() > 0) {

			// this removes any superflous trailing commas, i think this is just an
			// artifact of this code's history
			// from initially being a serql renderer. i'll leave it for now, but i
			// think this is to be removed.
			// test cases to prove these things work would be lovely.
			if (aBody.toString().trim().lastIndexOf(",") == aBody.length() - 1) {
				aBody.setCharAt(aBody.lastIndexOf(","), ' ');
			}

			if (!(theQuery instanceof ParsedBooleanQuery)) {
				aQuery.append("where ");
			}

			aQuery.append("{\n");
			aQuery.append(aBody);
			aQuery.append("}");
		}

		if (!mRenderer.getOrdering().isEmpty()) {
			aQuery.append("\norder by ");

			aFirst = true;
			for (OrderElem aOrder : mRenderer.getOrdering()) {
				if (!aFirst) {
					aQuery.append(" ");
				}
				else {
					aFirst = false;
				}

				if (aOrder.isAscending()) {
					aQuery.append(mRenderer.renderValueExpr(aOrder.getExpr()));
				}
				else {
					aQuery.append("desc(");
					aQuery.append(mRenderer.renderValueExpr(aOrder.getExpr()));
					aQuery.append(")");
				}
			}
		}

		if (mRenderer.getLimit() != -1 && !(theQuery instanceof ParsedBooleanQuery)) {
			aQuery.append("\nlimit ").append(mRenderer.getLimit());
		}

		if (mRenderer.getOffset() != -1) {
			aQuery.append("\noffset ").append(mRenderer.getOffset());
		}

		return aQuery.toString();
	}
}
