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
package org.openrdf.sail.federation.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelNodeBase;
import org.openrdf.query.algebra.QueryModelVisitor;

/**
 * An abstract superclass for operators which have (zero or more) arguments.
 */
public abstract class AbstractNaryOperator<Expr extends QueryModelNode> extends
		QueryModelNodeBase {

	private static final long serialVersionUID = 2645544440976923085L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	private List<Expr> args = new ArrayList<Expr>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AbstractNaryOperator() {
		super();
	}

	/**
	 * Creates a new n-ary operator.
	 */
	public AbstractNaryOperator(final Expr... args) {
		this(Arrays.asList(args));
	}

	/**
	 * Creates a new n-ary operator.
	 */
	public AbstractNaryOperator(final List<? extends Expr> args) {
		this();
		setArgs(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the arguments of this n-ary operator.
	 * 
	 * @return A copy of the current argument list.
	 */
	public List<? extends Expr> getArgs() {
		return new CopyOnWriteArrayList<Expr>(args);
	}

	/**
	 * Gets the number of arguments of this n-ary operator.
	 * 
	 * @return The number of arguments.
	 */
	public int getNumberOfArguments() {
		return args.size();
	}

	/**
	 * Gets the <tt>idx</tt>-th argument of this n-ary operator.
	 * 
	 * @return The operator's arguments.
	 */
	public Expr getArg(final int idx) {
		return (idx < args.size()) ? args.get(idx) : null; // NOPMD
	}

	/**
	 * Sets the arguments of this n-ary tuple operator.
	 */
	private final void addArgs(final List<? extends Expr> args) {
		assert args != null;
		for (Expr arg : args) {
			addArg(arg);
		}
	}

	/**
	 * Sets the arguments of this n-ary operator.
	 */
	public final void addArg(final Expr arg) {
		setArg(this.args.size(), arg);
	}

	/**
	 * Sets the arguments of this n-ary operator.
	 */
	private final void setArgs(final List<? extends Expr> args) {
		this.args.clear();
		addArgs(args);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this n-ary tuple operator.
	 */
	protected final void setArg(final int idx, final Expr arg) {
		if (arg != null) {
			// arg can be null (i.e. Regex)
			arg.setParentNode(this);
		}

		while (args.size() <= idx) {
			args.add(null);
		}

		this.args.set(idx, arg);
	}

	public boolean removeArg(final Expr arg) {
		return args.remove(arg);
	}

	@Override
	public <X extends Exception> void visitChildren(
			final QueryModelVisitor<X> visitor) throws X {
		for (Expr arg : args) {
			if (arg != null) {
				arg.visit(visitor);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void replaceChildNode(final QueryModelNode current,
			final QueryModelNode replacement) {
		final int index = args.indexOf(current);
		if (index >= 0) {
			setArg(index, (Expr) replacement);
		} else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public AbstractNaryOperator<Expr> clone() { // NOPMD
		final AbstractNaryOperator<Expr> clone = (AbstractNaryOperator<Expr>) super.clone();
		clone.args = new ArrayList<Expr>(args.size());
		for (Expr arg : args) {
			final Expr argClone = (arg == null) ? null : (Expr) arg.clone(); // NOPMD
			clone.addArg(argClone);
		}
		return clone;
	}
}
