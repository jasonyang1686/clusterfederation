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
package org.openrdf.query.algebra.evaluation.util;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * 
 * @author james
 * 
 */
public class OrderComparatorTest extends TestCase {
	class EvaluationStrategyStub implements EvaluationStrategy {
		public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
				TupleExpr expr, BindingSet bindings)
				throws QueryEvaluationException {
			throw new UnsupportedOperationException();
		}

		public Value evaluate(ValueExpr expr, BindingSet bindings)
				throws ValueExprEvaluationException, QueryEvaluationException {
			return null;
		}

		public boolean isTrue(ValueExpr expr, BindingSet bindings)
				throws ValueExprEvaluationException, QueryEvaluationException {
			throw new UnsupportedOperationException();
		}
	}

	class ComparatorStub extends ValueComparator {
		Iterator<Integer> iter;

		public void setIterator(Iterator<Integer> iter) {
			this.iter = iter;
		}

		@Override
		public int compare(Value o1, Value o2) {
			return iter.next();
		}
	}

	private EvaluationStrategyStub strategy = new EvaluationStrategyStub();

	private Order order = new Order();

	private OrderElem asc = new OrderElem();

	private OrderElem desc = new OrderElem();

	private ComparatorStub cmp = new ComparatorStub();

	private int ZERO = 0;

	private int POS = 378;

	private int NEG = -7349;

	public void testEquals() throws Exception {
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(ZERO).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) == 0);
	}

	public void testZero() throws Exception {
		order.addElement(asc);
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(ZERO, POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testTerm() throws Exception {
		order.addElement(asc);
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(POS, NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testAscLessThan() throws Exception {
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) < 0);
	}

	public void testAscGreaterThan() throws Exception {
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testDescLessThan() throws Exception {
		order.addElement(desc);
		cmp.setIterator(Arrays.asList(NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testDescGreaterThan() throws Exception {
		order.addElement(desc);
		cmp.setIterator(Arrays.asList(POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) < 0);
	}

	@Override
	protected void setUp() throws Exception {
		asc.setAscending(true);
		desc.setAscending(false);
	}
}
