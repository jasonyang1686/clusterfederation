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
package org.openrdf.query.resultio.sparqlxml;

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BNODE_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_FALSE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TRUE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HREF_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LINK_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_DATATYPE_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_LANG_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.NAMESPACE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.QNAME;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_SET_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.ROOT_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.URI_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.xml.XMLWriter;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SESAMEQNAME;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BasicQueryWriterSettings;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.query.resultio.QueryResultWriterBase;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.XMLWriterSettings;

/**
 * An abstract class to implement the base functionality for both
 * SPARQLBooleanXMLWriter and SPARQLResultsXMLWriter.
 * 
 * @author Peter Ansell
 */
abstract class SPARQLXMLWriterBase extends QueryResultWriterBase implements QueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * XMLWriter to write XML to.
	 */
	protected XMLWriter xmlWriter;

	protected boolean documentOpen = false;

	protected boolean headerOpen = false;

	protected boolean headerComplete = false;

	protected boolean tupleVariablesFound = false;

	/**
	 * Map with keys as namespace URI strings and the values as the shortened
	 * prefixes.
	 */
	private Map<String, String> namespaceTable = new HashMap<String, String>();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLXMLWriterBase(OutputStream out) {
		this(new XMLWriter(out));
	}

	public SPARQLXMLWriterBase(XMLWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
		this.xmlWriter.setPrettyPrint(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Enables/disables addition of indentation characters and newlines in the
	 * XML document. By default, pretty-printing is set to <tt>true</tt>. If set
	 * to <tt>false</tt>, no indentation and newlines are added to the XML
	 * document. This method has to be used before writing starts (that is,
	 * before {@link #startDocument} is called).
	 * 
	 * @deprecated Use {@link #getWriterConfig()}
	 *             .set(BasicWriterSettings.PRETTY_PRINT, prettyPrint) instead.
	 */
	@Deprecated
	public void setPrettyPrint(boolean prettyPrint) {
		getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, prettyPrint);
		xmlWriter.setPrettyPrint(prettyPrint);
	}

	protected void endDocument()
		throws IOException
	{
		xmlWriter.endTag(ROOT_TAG);

		xmlWriter.endDocument();

		tupleVariablesFound = false;
		headerOpen = false;
		headerComplete = false;
		documentOpen = false;
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		if (!headerOpen) {
			startHeader();
		}

		if (!headerComplete) {
			endHeader();
		}

		if (tupleVariablesFound) {
			throw new QueryResultHandlerException("Cannot call handleBoolean after startQueryResults");
		}

		try {
			if (value) {
				xmlWriter.textElement(BOOLEAN_TAG, BOOLEAN_TRUE);
			}
			else {
				xmlWriter.textElement(BOOLEAN_TAG, BOOLEAN_FALSE);
			}

			endDocument();
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void startDocument()
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			documentOpen = true;
			headerOpen = false;
			headerComplete = false;
			tupleVariablesFound = false;

			try {
				xmlWriter.setPrettyPrint(getWriterConfig().get(BasicWriterSettings.PRETTY_PRINT));

				if(getWriterConfig().get(XMLWriterSettings.INCLUDE_XML_PI)) {
					xmlWriter.startDocument();
				}
				
				xmlWriter.setAttribute("xmlns", NAMESPACE);

				if (getWriterConfig().get(BasicQueryWriterSettings.ADD_SESAME_QNAME)) {
					xmlWriter.setAttribute("xmlns:q", SESAMEQNAME.NAMESPACE);
				}

				for (String nextPrefix : namespaceTable.keySet()) {
					this.log.debug("Adding custom prefix for <{}> to map to <{}>", nextPrefix,
							namespaceTable.get(nextPrefix));
					xmlWriter.setAttribute("xmlns:" + namespaceTable.get(nextPrefix), nextPrefix);
				}
			}
			catch (IOException e) {
				throw new QueryResultHandlerException(e);
			}
		}
	}

	@Override
	public void handleStylesheet(String url)
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		try {
			xmlWriter.writeStylesheet(url);
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void startHeader()
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		if (!headerOpen) {
			try {
				xmlWriter.startTag(ROOT_TAG);

				xmlWriter.startTag(HEAD_TAG);

				headerOpen = true;
			}
			catch (IOException e) {
				throw new QueryResultHandlerException(e);
			}
		}
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		if (!headerOpen) {
			startHeader();
		}

		try {
			// Write link URLs
			for (String name : linkUrls) {
				xmlWriter.setAttribute(HREF_ATT, name);
				xmlWriter.emptyElement(LINK_TAG);
			}
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void endHeader()
		throws QueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
		}

		if (!headerOpen) {
			startHeader();
		}

		if (!headerComplete) {
			try {
				xmlWriter.endTag(HEAD_TAG);

				if (tupleVariablesFound) {
					// Write start of results, which must always exist, even if there
					// are no result bindings
					xmlWriter.startTag(RESULT_SET_TAG);
				}

				headerComplete = true;
			}
			catch (IOException e) {
				throw new QueryResultHandlerException(e);
			}
		}
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}
			if (!headerOpen) {
				startHeader();
			}

			tupleVariablesFound = true;
			// Write binding names
			for (String name : bindingNames) {
				xmlWriter.setAttribute(VAR_NAME_ATT, name);
				xmlWriter.emptyElement(VAR_TAG);
			}
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}

			if (!headerOpen) {
				startHeader();
			}

			if (!headerComplete) {
				endHeader();
			}

			if (!tupleVariablesFound) {
				throw new IllegalStateException(
						"Could not end query result as startQueryResult was not called first.");
			}

			xmlWriter.endTag(RESULT_SET_TAG);
			endDocument();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
			}

			if (!headerOpen) {
				startHeader();
			}

			if (!headerComplete) {
				endHeader();
			}

			if (!tupleVariablesFound) {
				throw new IllegalStateException("Must call startQueryResult before handleSolution");
			}

			xmlWriter.startTag(RESULT_TAG);

			for (Binding binding : bindingSet) {
				xmlWriter.setAttribute(BINDING_NAME_ATT, binding.getName());
				xmlWriter.startTag(BINDING_TAG);

				writeValue(binding.getValue());

				xmlWriter.endTag(BINDING_TAG);
			}

			xmlWriter.endTag(RESULT_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public final Collection<RioSetting<?>> getSupportedSettings() {
		Set<RioSetting<?>> result = new HashSet<RioSetting<?>>(super.getSupportedSettings());

		result.add(BasicWriterSettings.PRETTY_PRINT);
		result.add(BasicQueryWriterSettings.ADD_SESAME_QNAME);
		result.add(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL);
		result.add(BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL);

		return result;
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws QueryResultHandlerException
	{
		// we only support the addition of prefixes before the document is open
		// fail silently if namespaces are added after this point
		if (!documentOpen) {
			// SES-1751 : Do not allow overriding of the fixed sparql or
			// sesameqname prefixes
			if (!prefix.trim().isEmpty() && !prefix.trim().equals(SESAMEQNAME.PREFIX)) {
				this.log.debug("Handle namespace: Will map <{}> to <{}>", uri, prefix);
				// NOTE: The keys in the namespace table are the URIs and the values
				// are the prefixes
				this.namespaceTable.put(uri, prefix);
			}
			else {
				this.log.debug(
						"handleNamespace was ignored for either the empty prefix or the sesame qname prefix (q). Attempted to map: <{}> to <{}>",
						uri, prefix);
			}
		}
		else {
			this.log.warn("handleNamespace was ignored after startDocument: <{}> to <{}>", uri, prefix);
		}
	}

	private void writeValue(Value value)
		throws IOException
	{
		if (value instanceof URI) {
			writeURI((URI)value);
		}
		else if (value instanceof BNode) {
			writeBNode((BNode)value);
		}
		else if (value instanceof Literal) {
			writeLiteral((Literal)value);
		}
	}

	private boolean isQName(URI nextUri) {
		return namespaceTable.containsKey(nextUri.getNamespace());
	}

	/**
	 * Write a QName for the given URI if and only if the
	 * {@link BasicQueryWriterSettings#ADD_SESAME_QNAME} setting has been set to
	 * true. By default it is false, to ensure that this implementation stays
	 * within the specification by default.
	 * 
	 * @param nextUri
	 *        The prefixed URI to be written as a sesame qname attribute.
	 */
	private void writeQName(URI nextUri) {
		if (getWriterConfig().get(BasicQueryWriterSettings.ADD_SESAME_QNAME)) {
			xmlWriter.setAttribute(QNAME,
					namespaceTable.get(nextUri.getNamespace()) + ":" + nextUri.getLocalName());
		}
	}

	private void writeURI(URI uri)
		throws IOException
	{
		if (isQName(uri)) {
			writeQName(uri);
		}
		xmlWriter.textElement(URI_TAG, uri.toString());
	}

	private void writeBNode(BNode bNode)
		throws IOException
	{
		xmlWriter.textElement(BNODE_TAG, bNode.getID());
	}

	private void writeLiteral(Literal literal)
		throws IOException
	{
		if (literal.getLanguage() != null) {
			xmlWriter.setAttribute(LITERAL_LANG_ATT, literal.getLanguage());
			if (!rdfLangStringToLangLiteral() && literal.getDatatype() != null) {
				// Only enter this section if there is a datatype. Whatever datatype
				// it is, it should be RDF.LANGSTRING
				if (isQName(RDF.LANGSTRING)) {
					writeQName(RDF.LANGSTRING);
				}
				xmlWriter.setAttribute(LITERAL_DATATYPE_ATT, RDF.LANGSTRING.stringValue());
			}
		}
		// Only enter this section for non-language literals now, as the
		// rdf:langString datatype is handled implicitly above
		else if (literal.getDatatype() != null) {
			URI datatype = literal.getDatatype();
			if (!datatype.equals(XMLSchema.STRING) || !xsdStringToPlainLiteral()) {
				if (isQName(datatype)) {
					writeQName(datatype);
				}
				xmlWriter.setAttribute(LITERAL_DATATYPE_ATT, datatype.stringValue());
			}
		}

		xmlWriter.textElement(LITERAL_TAG, literal.getLabel());
	}

	private boolean xsdStringToPlainLiteral() {
		return getWriterConfig().get(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL);
	}

	private boolean rdfLangStringToLangLiteral() {
		return getWriterConfig().get(BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL);
	}
}
