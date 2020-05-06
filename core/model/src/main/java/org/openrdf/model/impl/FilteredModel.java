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
package org.openrdf.model.impl;

import java.util.Iterator;
import java.util.Set;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Applies a basic graph pattern filter to what triples can be see.
 * 
 * @since 2.7.0
 */
public abstract class FilteredModel extends AbstractModel {

	private final Model model;

	private static final long serialVersionUID = -2353344619836326934L;

	private Resource subj;

	private URI pred;

	private Value obj;

	private Resource[] contexts;

	public FilteredModel(AbstractModel model, Resource subj, URI pred, Value obj, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		this.model = model;
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.contexts = contexts;
	}

	@Override
	public Namespace getNamespace(String prefix) {
		return model.getNamespace(prefix);
	}

	@Override
	public Set<Namespace> getNamespaces() {
		return model.getNamespaces();
	}

	@Override
	public Namespace setNamespace(String prefix, String name) {
		return model.setNamespace(prefix, name);
	}

	@Override
	public void setNamespace(Namespace namespace) {
		this.model.setNamespace(namespace);
	}

	@Override
	public Namespace removeNamespace(String prefix) {
		return model.removeNamespace(prefix);
	}

	@Override
	public int size() {
		Iterator<Statement> iter = iterator();
		try {
			int size = 0;
			while (iter.hasNext()) {
				size++;
				iter.next();
			}
			return size;
		}
		finally {
			closeIterator(iter);
		}
	}

	@Override
	public boolean add(Resource s, URI p, Value o, Resource... c) {
		if (s == null) {
			s = (Resource)subj;
		}
		if (p == null) {
			p = (URI)pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			throw new IllegalArgumentException("Statement is filtered out of view");
		}
		return model.add(s, p, o, c);
	}

	@Override
	public boolean remove(Resource s, URI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			return false;
		}
		return model.remove(s, p, o, c);
	}

	@Override
	public boolean contains(Resource s, URI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			return false;
		}
		return model.contains(s, p, o, c);
	}

	@Override
	public Model filter(Resource s, URI p, Value o, Resource... c) {
		if (s == null) {
			s = subj;
		}
		if (p == null) {
			p = pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			return new EmptyModel(model);
		}
		return model.filter(s, p, o, c);
	}

	@Override
	public final void removeTermIteration(Iterator<Statement> iter, Resource s, URI p, Value o, Resource... c)
	{
		if (s == null) {
			s = (Resource)subj;
		}
		if (p == null) {
			p = (URI)pred;
		}
		if (o == null) {
			o = obj;
		}
		if (c != null && c.length == 0) {
			c = contexts;
		}
		if (!accept(s, p, o, c)) {
			throw new IllegalStateException();
		}
		removeFilteredTermIteration(iter, s, p, o, c);
	}

	/**
	 * Called by aggregate sets when a term has been removed from a term
	 * iterator. At least one of the last four terms will be non-empty.
	 * 
	 * @param iter
	 *        The iterator used to navigate the live set (never null)
	 * @param subj
	 *        the subject term to be removed or null
	 * @param pred
	 *        the predicate term to be removed or null
	 * @param obj
	 *        the object term to be removed or null
	 * @param contexts
	 *        an array of one context term to be removed or an empty array
	 */
	protected abstract void removeFilteredTermIteration(Iterator<Statement> iter, Resource subj, URI pred,
			Value obj, Resource... contexts);

	private boolean accept(Resource s, URI p, Value o, Resource... c) {
		if (subj != null && !subj.equals(s)) {
			return false;
		}
		if (pred != null && !pred.equals(p)) {
			return false;
		}
		if (obj != null && !obj.equals(o)) {
			return false;
		}
		if (!matches(c, contexts)) {
			return false;
		}
		return (s == null || s instanceof Resource) && (p == null || p instanceof URI);
	}

	private boolean matches(Resource[] stContext, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(stContext);
		if (stContext != null && stContext.length > 0) {
			for (Resource c : stContext) {
				if (!matches(c, contexts)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean matches(Resource stContext, Resource... contexts) {
		if (contexts != null && contexts.length == 0) {
			// Any context matches
			return stContext == null || stContext instanceof Resource;
		}
		else {
			OpenRDFUtil.verifyContextNotNull(contexts);
			// Accept if one of the contexts from the pattern matches
			for (Resource context : contexts) {
				if (context == null && stContext == null) {
					return true;
				}
				if (context != null && context.equals(stContext)) {
					return true;
				}
			}

			return false;
		}
	}
}
