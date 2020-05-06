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

import java.io.IOException;
import java.io.OutputStream;

import info.aduna.xml.XMLUtil;
import info.aduna.xml.XMLWriter;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SPARQLUpdateOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.StatementOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;

/**
 * Serializes of an RDF transaction.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class TransactionWriter {

	public TransactionWriter() {
	}

	/**
	 * serialize the passed list of operations to the passed writer.
	 * 
	 * @param txn
	 *        the operations
	 * @param out
	 *        the output stream to write to
	 * @throws IllegalArgumentException
	 *         when one of the parameters is null
	 */
	public void serialize(Iterable<? extends TransactionOperation> txn, OutputStream out)
		throws IOException
	{
		assert txn != null : "operation list must not be null";
		assert out != null : "output stream must not be null";

		XMLWriter xmlWriter = new XMLWriter(out);
		xmlWriter.setPrettyPrint(true);

		xmlWriter.startDocument();
		xmlWriter.startTag(TransactionXMLConstants.TRANSACTION_TAG);

		for (TransactionOperation op : txn) {
			serialize(op, xmlWriter);
		}

		xmlWriter.endTag(TransactionXMLConstants.TRANSACTION_TAG);
		xmlWriter.endDocument();
	}

	/**
	 * Serializes the supplied operation.
	 * 
	 * @param op
	 *        The operation to serialize
	 */
	protected void serialize(TransactionOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		if (op instanceof AddStatementOperation) {
			serialize((AddStatementOperation)op, xmlWriter);
		}
		else if (op instanceof RemoveStatementsOperation) {
			serialize((RemoveStatementsOperation)op, xmlWriter);
		}
		else if (op instanceof ClearOperation) {
			serialize((ClearOperation)op, xmlWriter);
		}
		else if (op instanceof SetNamespaceOperation) {
			serialize((SetNamespaceOperation)op, xmlWriter);
		}
		else if (op instanceof RemoveNamespaceOperation) {
			serialize((RemoveNamespaceOperation)op, xmlWriter);
		}
		else if (op instanceof ClearNamespacesOperation) {
			serialize((ClearNamespacesOperation)op, xmlWriter);
		}
		else if (op instanceof SPARQLUpdateOperation) {
			serialize((SPARQLUpdateOperation)op, xmlWriter);
		}
		else if (op == null) {
			// ignore(?)
		}
		else {
			throw new IllegalArgumentException("Unknown operation type: " + op.getClass());
		}
	}

	protected void serialize(AddStatementOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.ADD_STATEMENT_TAG);
		serialize((StatementOperation)op, xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.ADD_STATEMENT_TAG);
	}

	protected void serialize(SPARQLUpdateOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		String baseURI = op.getBaseURI();
		if (baseURI != null) {
			xmlWriter.setAttribute(TransactionXMLConstants.BASE_URI_ATT, baseURI);
		}
		xmlWriter.setAttribute(TransactionXMLConstants.INCLUDE_INFERRED_ATT, op.isIncludeInferred());
		xmlWriter.startTag(TransactionXMLConstants.SPARQL_UPDATE_TAG);

		// serialize update string
		String updateString = op.getUpdateString();
		xmlWriter.textElement(TransactionXMLConstants.UPDATE_STRING_TAG, updateString);

		// serialize dataset definition (if any)
		Dataset dataset = op.getDataset();
		if (dataset != null) {
			xmlWriter.startTag(TransactionXMLConstants.DATASET_TAG);

			xmlWriter.startTag(TransactionXMLConstants.DEFAULT_GRAPHS_TAG);
			for (URI defaultGraph : dataset.getDefaultGraphs()) {
				xmlWriter.textElement(TransactionXMLConstants.GRAPH_TAG, defaultGraph.stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.DEFAULT_GRAPHS_TAG);

			xmlWriter.startTag(TransactionXMLConstants.NAMED_GRAPHS_TAG);
			for (URI namedGraph : dataset.getNamedGraphs()) {
				xmlWriter.textElement(TransactionXMLConstants.GRAPH_TAG, namedGraph.stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.NAMED_GRAPHS_TAG);

			xmlWriter.startTag(TransactionXMLConstants.DEFAULT_REMOVE_GRAPHS_TAG);
			for (URI defaultRemoveGraph : dataset.getDefaultRemoveGraphs()) {
				xmlWriter.textElement(TransactionXMLConstants.GRAPH_TAG, defaultRemoveGraph.stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.DEFAULT_REMOVE_GRAPHS_TAG);

			if (dataset.getDefaultInsertGraph() != null) {
				xmlWriter.textElement(TransactionXMLConstants.DEFAULT_INSERT_GRAPH,
						dataset.getDefaultInsertGraph().stringValue());
			}
			xmlWriter.endTag(TransactionXMLConstants.DATASET_TAG);
		}

		if (op.getBindings() != null && op.getBindings().length > 0) {
			xmlWriter.startTag(TransactionXMLConstants.BINDINGS);

			for (Binding binding : op.getBindings()) {
				if (binding.getName() != null && binding.getValue() != null
						&& binding.getValue().stringValue() != null)
				{
					if (binding.getValue() instanceof URI) {
						xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, binding.getName());
						xmlWriter.textElement(TransactionXMLConstants.BINDING_URI, binding.getValue().stringValue());
					}

					if (binding.getValue() instanceof BNode) {
						xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, binding.getName());
						xmlWriter.textElement(TransactionXMLConstants.BINDING_BNODE,
								binding.getValue().stringValue());
					}

					if (binding.getValue() instanceof Literal) {
						xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, binding.getName());

						Literal literal = (Literal)binding.getValue();
						if (literal.getLanguage() != null)
							xmlWriter.setAttribute(TransactionXMLConstants.LANGUAGE_ATT, literal.getLanguage());

						if (literal.getDatatype() != null)
							xmlWriter.setAttribute(TransactionXMLConstants.DATA_TYPE_ATT,
									literal.getDatatype().stringValue());

						xmlWriter.textElement(TransactionXMLConstants.BINDING_LITERAL,
								binding.getValue().stringValue());
					}
				}
			}

			xmlWriter.endTag(TransactionXMLConstants.BINDINGS);
		}

		xmlWriter.endTag(TransactionXMLConstants.SPARQL_UPDATE_TAG);

	}

	protected void serialize(RemoveStatementsOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.REMOVE_STATEMENTS_TAG);
		serialize((StatementOperation)op, xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.REMOVE_STATEMENTS_TAG);
	}

	protected void serialize(StatementOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		serialize(op.getSubject(), xmlWriter);
		serialize(op.getPredicate(), xmlWriter);
		serialize(op.getObject(), xmlWriter);
		serialize(op.getContexts(), xmlWriter);
	}

	protected void serialize(ClearOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.startTag(TransactionXMLConstants.CLEAR_TAG);
		serialize(op.getContexts(), xmlWriter);
		xmlWriter.endTag(TransactionXMLConstants.CLEAR_TAG);
	}

	protected void serialize(SetNamespaceOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.setAttribute(TransactionXMLConstants.PREFIX_ATT, op.getPrefix());
		xmlWriter.setAttribute(TransactionXMLConstants.NAME_ATT, op.getName());
		xmlWriter.emptyElement(TransactionXMLConstants.SET_NAMESPACE_TAG);
	}

	protected void serialize(RemoveNamespaceOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.setAttribute(TransactionXMLConstants.PREFIX_ATT, op.getPrefix());
		xmlWriter.emptyElement(TransactionXMLConstants.REMOVE_NAMESPACE_TAG);
	}

	protected void serialize(ClearNamespacesOperation op, XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.emptyElement(TransactionXMLConstants.CLEAR_NAMESPACES_TAG);
	}

	protected void serialize(Resource[] contexts, XMLWriter xmlWriter)
		throws IOException
	{
		if (contexts.length > 0) {
			xmlWriter.startTag(TransactionXMLConstants.CONTEXTS_TAG);
			for (Resource context : contexts) {
				serialize(context, xmlWriter);
			}
			xmlWriter.endTag(TransactionXMLConstants.CONTEXTS_TAG);
		}
		else {
			xmlWriter.emptyElement(TransactionXMLConstants.CONTEXTS_TAG);
		}
	}

	protected void serialize(Value value, XMLWriter xmlWriter)
		throws IOException
	{
		if (value instanceof Resource) {
			serialize((Resource)value, xmlWriter);
		}
		else if (value instanceof Literal) {
			serialize((Literal)value, xmlWriter);
		}
		else if (value == null) {
			serializeNull(xmlWriter);
		}
		else {
			throw new IllegalArgumentException("Unknown value type: " + value.getClass().toString());
		}
	}

	protected void serialize(Resource resource, XMLWriter xmlWriter)
		throws IOException
	{
		if (resource instanceof URI) {
			serialize((URI)resource, xmlWriter);
		}
		else if (resource instanceof BNode) {
			serialize((BNode)resource, xmlWriter);
		}
		else if (resource == null) {
			serializeNull(xmlWriter);
		}
		else {
			throw new IllegalArgumentException("Unknown resource type: " + resource.getClass().toString());
		}
	}

	protected void serialize(URI uri, XMLWriter xmlWriter)
		throws IOException
	{
		if (uri != null) {
			xmlWriter.textElement(TransactionXMLConstants.URI_TAG, uri.toString());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	protected void serialize(BNode bnode, XMLWriter xmlWriter)
		throws IOException
	{
		if (bnode != null) {
			xmlWriter.textElement(TransactionXMLConstants.BNODE_TAG, bnode.getID());
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	protected void serialize(Literal literal, XMLWriter xmlWriter)
		throws IOException
	{
		if (literal != null) {
			if (literal.getLanguage() != null) {
				xmlWriter.setAttribute(TransactionXMLConstants.LANG_ATT, literal.getLanguage());
			}
			if (literal.getDatatype() != null) {
				xmlWriter.setAttribute(TransactionXMLConstants.DATATYPE_ATT, literal.getDatatype().toString());
			}

			String label = literal.getLabel();

			boolean valid = true;
			int i = 0;
			while (valid && i < label.length()) {
				char c = label.charAt(i++);
				valid = XMLUtil.isValidCharacterDataChar(c);
			}

			if (!valid) {
				xmlWriter.setAttribute(TransactionXMLConstants.ENCODING_ATT, "base64");
				label = javax.xml.bind.DatatypeConverter.printBase64Binary(label.getBytes("UTF-8"));
			}

			xmlWriter.textElement(TransactionXMLConstants.LITERAL_TAG, label);
		}
		else {
			serializeNull(xmlWriter);
		}
	}

	protected void serializeNull(XMLWriter xmlWriter)
		throws IOException
	{
		xmlWriter.emptyElement(TransactionXMLConstants.NULL_TAG);
	}
}
