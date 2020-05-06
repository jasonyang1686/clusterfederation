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
package org.openrdf.repository.sail.helpers;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UpdateContext;

/**
 * An RDFHandler that adds RDF data to a repository.
 * 
 * @author jeen
 */
public class RDFSailInserter extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The connection to use for the add operations.
	 */
	private final SailConnection con;

	private final ValueFactory vf;

	private final UpdateContext uc;

	/**
	 * The contexts to add the statements to. If this variable is a non-empty
	 * array, statements will be added to the corresponding contexts.
	 */
	private Resource[] contexts = new Resource[0];

	/**
	 * Flag indicating whether blank node IDs should be preserved.
	 */
	private boolean preserveBNodeIDs;

	/**
	 * Map that stores namespaces that are reported during the evaluation of the
	 * query. Key is the namespace prefix, value is the namespace name.
	 */
	private final Map<String, String> namespaceMap;

	/**
	 * Map used to keep track of which blank node IDs have been mapped to which
	 * BNode object in case preserveBNodeIDs is false.
	 */
	private final Map<String, BNode> bNodesMap;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFInserter object that preserves bnode IDs and that does
	 * not enforce any context upon statements that are reported to it.
	 * 
	 * @param con
	 *        The connection to use for the add operations.
	 */
	public RDFSailInserter(SailConnection con, ValueFactory vf, UpdateContext uc) {
		this.con = con;
		this.vf = vf;
		this.uc = uc;
		preserveBNodeIDs = true;
		namespaceMap = new HashMap<String, String>();
		bNodesMap = new HashMap<String, BNode>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets whether this RDFInserter should preserve blank node IDs.
	 * 
	 * @param preserveBNodeIDs
	 *        The new value for this flag.
	 */
	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		this.preserveBNodeIDs = preserveBNodeIDs;
	}

	/**
	 * Checks whether this RDFInserter preserves blank node IDs.
	 */
	public boolean preservesBNodeIDs() {
		return preserveBNodeIDs;
	}

	/**
	 * Enforces the supplied contexts upon all statements that are reported to
	 * this RDFInserter.
	 * 
	 * @param contexts
	 *        the contexts to use. Use an empty array (not null!) to indicate no
	 *        context(s) should be enforced.
	 */
	public void enforceContext(Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);
		this.contexts = contexts;
	}

	/**
	 * Checks whether this RDFInserter enforces its contexts upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its contexts, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return contexts.length != 0;
	}

	/**
	 * Gets the contexts that this RDFInserter enforces upon all statements that
	 * are reported to it (in case <tt>enforcesContext()</tt> returns
	 * <tt>true</tt>).
	 * 
	 * @return A Resource[] identifying the contexts, or <tt>null</tt> if no
	 *         contexts is enforced.
	 */
	public Resource[] getContexts() {
		return contexts;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
			String prefix = entry.getKey();
			String name = entry.getValue();

			try {
				if (con.getNamespace(prefix) == null) {
					con.setNamespace(prefix, name);
				}
			}
			catch (SailException e) {
				throw new RDFHandlerException(e);
			}
		}

		namespaceMap.clear();
		bNodesMap.clear();
	}

	@Override
	public void handleNamespace(String prefix, String name) {
		// FIXME: set namespaces directly when they are properly handled wrt
		// rollback
		// don't replace earlier declarations
		if (prefix != null && !namespaceMap.containsKey(prefix)) {
			namespaceMap.put(prefix, name);
		}
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource ctxt = st.getContext();

		if (!preserveBNodeIDs) {
			if (subj instanceof BNode) {
				subj = mapBNode((BNode)subj);
			}

			if (obj instanceof BNode) {
				obj = mapBNode((BNode)obj);
			}

			if (!enforcesContext() && ctxt instanceof BNode) {
				ctxt = mapBNode((BNode)ctxt);
			}
		}

		try {
			if (enforcesContext()) {
				con.addStatement(uc, subj, pred, obj, contexts);
			}
			else {
				if (ctxt == null) {
					final URI insertGraph = uc.getDataset().getDefaultInsertGraph();
					if (insertGraph != null) {
						con.addStatement(uc, subj, pred, obj, insertGraph);
					}
					else {
						con.addStatement(uc, subj, pred, obj);
					}
				}
				else {
					con.addStatement(uc, subj, pred, obj, ctxt);
				}
			}
		}
		catch (SailException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Maps the supplied BNode, which comes from the data, to a new BNode object.
	 * Consecutive calls with equal BNode objects returns the same object
	 * everytime.
	 * 
	 * @throws RepositoryException
	 */
	private BNode mapBNode(BNode bNode) {
		BNode result = bNodesMap.get(bNode.getID());

		if (result == null) {
			result = vf.createBNode();
			bNodesMap.put(bNode.getID(), result);
		}

		return result;
	}
}
