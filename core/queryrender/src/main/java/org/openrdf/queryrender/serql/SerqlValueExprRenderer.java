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
package org.openrdf.queryrender.serql;

import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Label;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.queryrender.BaseTupleExprRenderer;
import org.openrdf.queryrender.RenderUtils;

/**
 * <p>
 * Renders a Sesame {@link ValueExpr} into SeRQL syntax.
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
class SerqlValueExprRenderer extends QueryModelVisitorBase<Exception> {

	/**
	 * The current rendered value
	 */
	private StringBuffer mBuffer = new StringBuffer();

	/**
	 * Reset the state of this renderer
	 */
	public void reset() {
		mBuffer = new StringBuffer();
	}

	/**
	 * Return the rendering of the ValueExpr object
	 * 
	 * @param theExpr
	 *        the expression to render
	 * @return the rendering
	 * @throws Exception
	 *         if there is an error while rendering
	 */
	public String render(ValueExpr theExpr)
		throws Exception
	{
		reset();

		theExpr.visit(this);

		return mBuffer.toString();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Bound theOp)
		throws Exception
	{
		mBuffer.append(" bound(");
		theOp.getArg().visit(this);
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Var theVar)
		throws Exception
	{
		if (theVar.isAnonymous() && !theVar.hasValue()) {
			mBuffer.append(BaseTupleExprRenderer.scrubVarName(theVar.getName().substring(1)));
		}
		else if (theVar.hasValue()) {
			mBuffer.append(RenderUtils.getSerqlQueryString(theVar.getValue()));
		}
		else {
			mBuffer.append(theVar.getName());
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(BNodeGenerator theGen)
		throws Exception
	{
		mBuffer.append(theGen.getSignature());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(MathExpr theOp)
		throws Exception
	{
		mBuffer.append("(");
		theOp.getLeftArg().visit(this);
		mBuffer.append(" ").append(theOp.getOperator().getSymbol()).append(" ");
		theOp.getRightArg().visit(this);
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Compare theOp)
		throws Exception
	{
		mBuffer.append("(");
		theOp.getLeftArg().visit(this);
		mBuffer.append(" ").append(theOp.getOperator().getSymbol()).append(" ");
		theOp.getRightArg().visit(this);
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Exists theOp)
		throws Exception
	{
		mBuffer.append(" exists(");
		// TODO: query render this
		theOp.getSubQuery().visit(this);
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(In theOp)
		throws Exception
	{
		theOp.getArg().visit(this);
		mBuffer.append(" in ");
		mBuffer.append("(");
		// TODO: query render this
		theOp.getSubQuery().visit(this);
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(CompareAll theOp)
		throws Exception
	{
		mBuffer.append("(");
		theOp.getArg().visit(this);
		mBuffer.append(" ").append(theOp.getOperator().getSymbol()).append(" all ");
		mBuffer.append("(");
		// TODO: query render this
		theOp.getSubQuery().visit(this);
		mBuffer.append(")");
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(ValueConstant theVal)
		throws Exception
	{
		mBuffer.append(RenderUtils.getSerqlQueryString(theVal.getValue()));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(FunctionCall theOp)
		throws Exception
	{
		mBuffer.append(theOp.getURI()).append("(");
		boolean aFirst = true;
		for (ValueExpr aArg : theOp.getArgs()) {
			if (!aFirst) {
				mBuffer.append(", ");
			}
			else {
				aFirst = false;
			}

			aArg.visit(this);
		}
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(CompareAny theOp)
		throws Exception
	{
		mBuffer.append("(");
		theOp.getArg().visit(this);
		mBuffer.append(" ").append(theOp.getOperator().getSymbol()).append(" any ");
		mBuffer.append("(");
		// TODO: query render this
		theOp.getSubQuery().visit(this);
		mBuffer.append(")");
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Regex theOp)
		throws Exception
	{
		mBuffer.append(" regex(");
		theOp.getArg().visit(this);
		mBuffer.append(", ");
		theOp.getPatternArg().visit(this);
		if (theOp.getFlagsArg() != null) {
			mBuffer.append(",");
			theOp.getFlagsArg().visit(this);
		}
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(LangMatches theOp)
		throws Exception
	{
		mBuffer.append(" langMatches(");
		theOp.getLeftArg().visit(this);
		mBuffer.append(", ");
		theOp.getRightArg().visit(this);
		mBuffer.append(")");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(SameTerm theOp)
		throws Exception
	{
		if (SeRQLQueryRenderer.SERQL_ONE_X_COMPATIBILITY_MODE) {
			mBuffer.append("(");
			theOp.getLeftArg().visit(this);
			mBuffer.append(" = ");
			theOp.getRightArg().visit(this);
			mBuffer.append(")");
		}
		else {
			mBuffer.append(" sameTerm(");
			theOp.getLeftArg().visit(this);
			mBuffer.append(", ");
			theOp.getRightArg().visit(this);
			mBuffer.append(")");
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(And theAnd)
		throws Exception
	{
		binaryMeet("and", theAnd);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Or theOr)
		throws Exception
	{
		binaryMeet("or", theOr);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Not theNot)
		throws Exception
	{
		unaryMeet("not", theNot);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Count theOp)
		throws Exception
	{
		unaryMeet("count", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Datatype theOp)
		throws Exception
	{
		unaryMeet("datatype", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(IsBNode theOp)
		throws Exception
	{
		unaryMeet("isBNode", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(IsLiteral theOp)
		throws Exception
	{
		unaryMeet("isLiteral", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(IsResource theOp)
		throws Exception
	{
		unaryMeet("isResource", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(IsURI theOp)
		throws Exception
	{
		unaryMeet("isURI", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Label theOp)
		throws Exception
	{
		unaryMeet("label", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Lang theOp)
		throws Exception
	{
		unaryMeet("lang", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Like theOp)
		throws Exception
	{
		theOp.getArg().visit(this);
		mBuffer.append(" like \"").append(theOp.getPattern()).append("\"");
		if (!theOp.isCaseSensitive()) {
			mBuffer.append(" ignore case");
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(LocalName theOp)
		throws Exception
	{
		unaryMeet("localName", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Min theOp)
		throws Exception
	{
		unaryMeet("min", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Max theOp)
		throws Exception
	{
		unaryMeet("max", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Namespace theOp)
		throws Exception
	{
		unaryMeet("namespace", theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Str theOp)
		throws Exception
	{
		unaryMeet("str", theOp);
	}

	private void binaryMeet(String theOpStr, BinaryValueOperator theOp)
		throws Exception
	{
		mBuffer.append(" (");
		theOp.getLeftArg().visit(this);
		mBuffer.append(" ").append(theOpStr).append(" ");
		theOp.getRightArg().visit(this);
		mBuffer.append(")");
	}

	private void unaryMeet(String theOpStr, UnaryValueOperator theOp)
		throws Exception
	{
		mBuffer.append(" ").append(theOpStr).append("(");
		theOp.getArg().visit(this);
		mBuffer.append(")");
	}
}
