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
package org.openrdf.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the <a href="http://www.w3.org/2004/02/skos/">Simple
 * Knowledge Organization System (SKOS)</a>.
 * 
 * @see <a href="http://www.w3.org/TR/skos-reference/">SKOS Simple Knowledge
 *      Organization System Reference</a>
 * @author Jeen Broekstra
 */
public class SKOS {

	/**
	 * The SKOS namespace: http://www.w3.org/2004/02/skos/core#
	 */
	public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

	/**
	 * The recommended prefix for the SKOS namespace: "skos"
	 */
	public static final String PREFIX = "skos";

	/**
	 * An immutable {@link Namespace} constant that represents the SKOS
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/* classes */

	/**
	 * The skos:Concept class
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#concepts">The
	 *      skos:Concept Class</a>
	 */
	public static final URI CONCEPT;

	/**
	 * The skos:ConceptScheme class
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#schemes">Concept
	 *      Schemes</a>
	 */
	public static final URI CONCEPT_SCHEME;

	/**
	 * The skos:Collection class
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#collections">Concept
	 *      Collections</a>
	 */
	public static final URI COLLECTION;

	/**
	 * The skos:OrderedCollection class
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#collections">Concept
	 *      Collections</a>
	 */
	public static final URI ORDERED_COLLECTION;

	/* lexical labels */

	/**
	 * The skos:prefLabel lexical label property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#labels">Lexical
	 *      Labels</a>
	 */
	public static final URI PREF_LABEL;

	/**
	 * The skos:altLabel lexical label property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#labels">Lexical
	 *      Labels</a>
	 */
	public static final URI ALT_LABEL;

	/**
	 * The skos:hiddenLabel lexical label property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#labels">Lexical
	 *      Labels</a>
	 */
	public static final URI HIDDEN_LABEL;

	/* Concept Scheme properties */

	/**
	 * The skos:inScheme relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#schemes">Concept
	 *      Schemes</a>
	 */
	public static final URI IN_SCHEME;

	/**
	 * The skos:hasTopConcept relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#schemes">Concept
	 *      Schemes</a>
	 */
	public static final URI HAS_TOP_CONCEPT;

	/**
	 * The skos:topConceptOf relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#schemes">Concept
	 *      Schemes</a>
	 */
	public static final URI TOP_CONCEPT_OF;

	/* collection properties */

	/**
	 * The skos:member relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#collections">Concept
	 *      Collections</a>
	 */
	public static final URI MEMBER;

	/**
	 * The skos:memberList relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#collections">Concept
	 *      Collections</a>
	 */
	public static final URI MEMBER_LIST;

	/* notation properties */

	/**
	 * The skos:notation property.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#notations">Notations</a>
	 */
	public static final URI NOTATION;

	/* documentation properties */

	/**
	 * The skos:changeNote property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */
	public static final URI CHANGE_NOTE;

	/**
	 * The skos:definition property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */
	public static final URI DEFINITION;

	/**
	 * The skos:editorialNote property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */
	public static final URI EDITORIAL_NOTE;

	/**
	 * The skos:example property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */

	public static final URI EXAMPLE;

	/**
	 * The skos:historyNote property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */
	public static final URI HISTORY_NOTE;

	/**
	 * The skos:note property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */
	public static final URI NOTE;

	/**
	 * The skos:scopeNote property.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#notes">Documentation
	 *      Properties (Note Properties)</a>
	 */
	public static final URI SCOPE_NOTE;

	/* semantic relations */

	/**
	 * The skos:broader relation.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#semantic-relations">SKOS
	 *      Simple Knowledge Organization System Reference - Semantic Relations
	 *      Section</a>
	 */
	public static final URI BROADER;

	/**
	 * The skos:broaderTransitive relation.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#semantic-relations">SKOS
	 *      Simple Knowledge Organization System Reference - Semantic Relations
	 *      Section</a>
	 */
	public static final URI BROADER_TRANSITIVE;

	/**
	 * The skos:narrower relation.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#semantic-relations">SKOS
	 *      Simple Knowledge Organization System Reference - Semantic Relations
	 *      Section</a>
	 */
	public static final URI NARROWER;

	/**
	 * The skos:narrowerTransitive relation.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#semantic-relations">SKOS
	 *      Simple Knowledge Organization System Reference - Semantic Relations
	 *      Section</a>
	 */
	public static final URI NARROWER_TRANSITIVE;

	/**
	 * The skos:related relation.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#semantic-relations">SKOS
	 *      Simple Knowledge Organization System Reference - Semantic Relations
	 *      Section</a>
	 */
	public static final URI RELATED;

	/**
	 * The skos:semanticRelation relation.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/skos-reference/#semantic-relations">SKOS
	 *      Simple Knowledge Organization System Reference - Semantic Relations
	 *      Section</a>
	 */
	public static final URI SEMANTIC_RELATION;

	/* mapping properties */

	/**
	 * The skos:broadMatch relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#mapping">SKOS Simple
	 *      Knowledge Organization System Reference - Mapping Properties
	 *      Section</a>
	 */
	public static final URI BROAD_MATCH;

	/**
	 * The skos:closeMatch relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#mapping">SKOS Simple
	 *      Knowledge Organization System Reference - Mapping Properties
	 *      Section</a>
	 */
	public static final URI CLOSE_MATCH;

	/**
	 * The skos:exactMatch relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#mapping">SKOS Simple
	 *      Knowledge Organization System Reference - Mapping Properties
	 *      Section</a>
	 */
	public static final URI EXACT_MATCH;

	/**
	 * The skos:mappingRelation relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#mapping">SKOS Simple
	 *      Knowledge Organization System Reference - Mapping Properties
	 *      Section</a>
	 */
	public static final URI MAPPING_RELATION;

	/**
	 * The skos:narrowMatch relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#mapping">SKOS Simple
	 *      Knowledge Organization System Reference - Mapping Properties
	 *      Section</a>
	 */
	public static final URI NARROW_MATCH;

	/**
	 * The skos:relatedMatch relation.
	 * 
	 * @see <a href="http://www.w3.org/TR/skos-reference/#mapping">SKOS Simple
	 *      Knowledge Organization System Reference - Mapping Properties
	 *      Section</a>
	 */
	public static final URI RELATED_MATCH;

	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		CONCEPT = f.createURI(NAMESPACE, "Concept");
		CONCEPT_SCHEME = f.createURI(NAMESPACE, "ConceptScheme");
		COLLECTION = f.createURI(NAMESPACE, "Collection");
		ORDERED_COLLECTION = f.createURI(NAMESPACE, "OrderedCollection");

		PREF_LABEL = f.createURI(NAMESPACE, "prefLabel");
		ALT_LABEL = f.createURI(NAMESPACE, "altLabel");

		BROADER = f.createURI(NAMESPACE, "broader");
		NARROWER = f.createURI(NAMESPACE, "narrower");

		HAS_TOP_CONCEPT = f.createURI(NAMESPACE, "hasTopConcept");
		MEMBER = f.createURI(NAMESPACE, "member");

		HIDDEN_LABEL = f.createURI(NAMESPACE, "hiddenLabel");

		IN_SCHEME = f.createURI(NAMESPACE, "inScheme");

		TOP_CONCEPT_OF = f.createURI(NAMESPACE, "topConceptOf");

		MEMBER_LIST = f.createURI(NAMESPACE, "memberList");
		NOTATION = f.createURI(NAMESPACE, "notation");
		CHANGE_NOTE = f.createURI(NAMESPACE, "changeNote");
		DEFINITION = f.createURI(NAMESPACE, "definition");
		EDITORIAL_NOTE = f.createURI(NAMESPACE, "editorialNote");
		EXAMPLE = f.createURI(NAMESPACE, "example");
		HISTORY_NOTE = f.createURI(NAMESPACE, "historyNote");
		NOTE = f.createURI(NAMESPACE, "note");
		SCOPE_NOTE = f.createURI(NAMESPACE, "scopeNote");
		BROADER_TRANSITIVE = f.createURI(NAMESPACE, "broaderTransitive");
		NARROWER_TRANSITIVE = f.createURI(NAMESPACE, "narrowerTransitive");
		RELATED = f.createURI(NAMESPACE, "related");
		SEMANTIC_RELATION = f.createURI(NAMESPACE, "semanticRelation");
		BROAD_MATCH = f.createURI(NAMESPACE, "broadMatch");
		CLOSE_MATCH = f.createURI(NAMESPACE, "closeMatch");
		EXACT_MATCH = f.createURI(NAMESPACE, "exactMatch");
		MAPPING_RELATION = f.createURI(NAMESPACE, "mappingRelation");
		NARROW_MATCH = f.createURI(NAMESPACE, "narrowMatch");
		RELATED_MATCH = f.createURI(NAMESPACE, "relatedMatch");

	}
}
