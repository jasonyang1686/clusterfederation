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
package org.openrdf.http.protocol.transaction;

/**
 * Interface defining tags and attribute names for the XML serialization of
 * transactions.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
interface TransactionXMLConstants {

	public static final String TRANSACTION_TAG = "transaction";

	public static final String ADD_STATEMENT_TAG = "add";

	public static final String REMOVE_STATEMENTS_TAG = "remove";

	public static final String REMOVE_NAMED_CONTEXT_STATEMENTS_TAG = "removeFromNamedContext";

	public static final String CLEAR_TAG = "clear";

	public static final String NULL_TAG = "null";

	public static final String URI_TAG = "uri";

	public static final String BNODE_TAG = "bnode";

	public static final String LITERAL_TAG = "literal";

	public static final String ENCODING_ATT = "encoding";

	public static final String LANG_ATT = "xml:lang";

	public static final String DATATYPE_ATT = "datatype";

	public static final String SET_NAMESPACE_TAG = "setNamespace";

	public static final String REMOVE_NAMESPACE_TAG = "removeNamespace";

	public static final String PREFIX_ATT = "prefix";

	public static final String NAME_ATT = "name";

	public static final String CLEAR_NAMESPACES_TAG = "clearNamespaces";

	public static final String CONTEXTS_TAG = "contexts";

	/**
	 * @since 2.7.0
	 */
	public static final String SPARQL_UPDATE_TAG = "sparql";

	/**
	 * @since 2.7.0
	 */
	public static final String UPDATE_STRING_TAG = "updateString";

	/**
	 * @since 2.7.0
	 */
	public static final String BASE_URI_ATT = "baseURI";

	/**
	 * @since 2.7.0
	 */
	public static final String INCLUDE_INFERRED_ATT = "includeInferred";

	/**
	 * @since 2.7.0
	 */
	public static final String DATASET_TAG = "dataset";

	/**
	 * @since 2.7.0
	 */
	public static final String GRAPH_TAG = "graph";

	/**
	 * @since 2.7.0
	 */
	public static final String DEFAULT_GRAPHS_TAG = "defaultGraphs";

	/**
	 * @since 2.7.0
	 */
	public static final String NAMED_GRAPHS_TAG = "namedGraphs";

	/**
	 * @since 2.7.0
	 */
	public static final String DEFAULT_REMOVE_GRAPHS_TAG = "defaultRemoveGraphs";

	/**
	 * @since 2.7.0
	 */
	public static final String DEFAULT_INSERT_GRAPH = "defaultInsertGraph";

	public static final String BINDINGS = "bindings";

	public static final String BINDING_URI = "binding_uri";

	public static final String BINDING_BNODE = "binding_bnode";

	public static final String BINDING_LITERAL = "binding_literal";

	public static final String LANGUAGE_ATT = "language";

	public static final String DATA_TYPE_ATT = "dataType";
}
