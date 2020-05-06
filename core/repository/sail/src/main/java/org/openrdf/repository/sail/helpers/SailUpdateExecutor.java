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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Add;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.Copy;
import org.openrdf.query.algebra.Create;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.Move;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.sail.SailUpdate;
import org.openrdf.repository.util.RDFLoader;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UpdateContext;

/**
 * Implementation of {@link SailUpdate#execute()} using
 * {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)} and
 * other {@link SailConnection} methods. LOAD is handled at the Repository API
 * level because it requires access to the Rio parser.
 * 
 * @author jeen
 * @author James Leigh
 * @see SailConnection#startUpdate(UpdateContext)
 * @see SailConnection#endUpdate(UpdateContext)
 * @see SailConnection#addStatement(UpdateContext, Resource, URI, Value,
 *      Resource...)
 * @see SailConnection#removeStatement(UpdateContext, Resource, URI, Value,
 *      Resource...)
 * @see SailConnection#clear(Resource...)
 * @see SailConnection#getContextIDs()
 * @see SailConnection#getStatements(Resource, URI, Value, boolean, Resource...)
 * @see SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)
 */
public class SailUpdateExecutor {

	private final Logger logger = LoggerFactory.getLogger(SailUpdateExecutor.class);

	private final SailConnection con;

	private final ValueFactory vf;

	private final RDFLoader loader;

	/**
	 * Implementation of {@link SailUpdate#execute()} using
	 * {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)}
	 * and other {@link SailConnection} methods.
	 * 
	 * @param con
	 *        Used to read data from and write data to.
	 * @param vf
	 *        Used to create {@link BNode}s
	 * @param loadConfig
	 */
	public SailUpdateExecutor(SailConnection con, ValueFactory vf, ParserConfig loadConfig) {
		this.con = con;
		this.vf = vf;
		this.loader = new RDFLoader(loadConfig, vf);
	}

	public void executeUpdate(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred)
		throws SailException, RDFParseException, IOException
	{
		UpdateContext uc = new UpdateContext(updateExpr, dataset, bindings, includeInferred);
		logger.trace("Incoming update expression:\n{}", uc);

		con.startUpdate(uc);
		try {
			if (updateExpr instanceof Load) {
				executeLoad((Load)updateExpr, uc);
			}
			else if (updateExpr instanceof Modify) {
				executeModify((Modify)updateExpr, uc);
			}
			else if (updateExpr instanceof InsertData) {
				executeInsertData((InsertData)updateExpr, uc);
			}
			else if (updateExpr instanceof DeleteData) {
				executeDeleteData((DeleteData)updateExpr, uc);
			}
			else if (updateExpr instanceof Clear) {
				executeClear((Clear)updateExpr, uc);
			}
			else if (updateExpr instanceof Create) {
				executeCreate((Create)updateExpr, uc);
			}
			else if (updateExpr instanceof Copy) {
				executeCopy((Copy)updateExpr, uc);
			}
			else if (updateExpr instanceof Add) {
				executeAdd((Add)updateExpr, uc);
			}
			else if (updateExpr instanceof Move) {
				executeMove((Move)updateExpr, uc);
			}
			else if (updateExpr instanceof Load) {
				throw new SailException("load operations can not be handled directly by the SAIL");
			}
		}
		finally {
			con.endUpdate(uc);
		}
	}

	protected void executeLoad(Load load, UpdateContext uc)
		throws IOException, RDFParseException, SailException
	{
		Value source = load.getSource().getValue();
		Value graph = load.getGraph() != null ? load.getGraph().getValue() : null;

		URL sourceURL = new URL(source.stringValue());

		RDFSailInserter rdfInserter = new RDFSailInserter(con, vf, uc);
		if (graph != null) {
			rdfInserter.enforceContext((Resource)graph);
		}
		try {
			loader.load(sourceURL, source.stringValue(), null, rdfInserter);
		}
		catch (RDFHandlerException e) {
			// RDFSailInserter only throws wrapped SailExceptions
			throw (SailException)e.getCause();
		}
	}

	protected void executeCreate(Create create, UpdateContext uc)
		throws SailException
	{
		// check if named graph exists, if so, we have to return an error.
		// Otherwise, we simply do nothing.
		Value graphValue = create.getGraph().getValue();

		if (graphValue instanceof Resource) {
			Resource namedGraph = (Resource)graphValue;

			CloseableIteration<? extends Resource, SailException> contextIDs = con.getContextIDs();
			try {
				while (contextIDs.hasNext()) {
					Resource contextID = contextIDs.next();

					if (namedGraph.equals(contextID)) {
						throw new SailException("Named graph " + namedGraph + " already exists. ");
					}
				}
			}
			finally {
				contextIDs.close();
			}
		}
	}

	/**
	 * @param copy
	 * @param uc
	 * @throws SailException
	 */
	protected void executeCopy(Copy copy, UpdateContext uc)
		throws SailException
	{
		ValueConstant sourceGraph = copy.getSourceGraph();
		ValueConstant destinationGraph = copy.getDestinationGraph();

		Resource source = sourceGraph != null ? (Resource)sourceGraph.getValue() : null;
		Resource destination = destinationGraph != null ? (Resource)destinationGraph.getValue() : null;

		if (source == null && destination == null || (source != null && source.equals(destination))) {
			// source and destination are the same, copy is a null-operation.
			return;
		}

		// clear destination
		con.clear((Resource)destination);

		// get all statements from source and add them to destination
		CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null, null,
				uc.isIncludeInferred(), (Resource)source);
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				con.addStatement(uc, st.getSubject(), st.getPredicate(), st.getObject(), (Resource)destination);
			}
		}
		finally {
			statements.close();
		}
	}

	/**
	 * @param add
	 * @param uc
	 * @throws SailException
	 */
	protected void executeAdd(Add add, UpdateContext uc)
		throws SailException
	{
		ValueConstant sourceGraph = add.getSourceGraph();
		ValueConstant destinationGraph = add.getDestinationGraph();

		Resource source = sourceGraph != null ? (Resource)sourceGraph.getValue() : null;
		Resource destination = destinationGraph != null ? (Resource)destinationGraph.getValue() : null;

		if (source == null && destination == null || (source != null && source.equals(destination))) {
			// source and destination are the same, copy is a null-operation.
			return;
		}

		// get all statements from source and add them to destination
		CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null, null,
				uc.isIncludeInferred(), (Resource)source);
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				con.addStatement(uc, st.getSubject(), st.getPredicate(), st.getObject(), (Resource)destination);
			}
		}
		finally {
			statements.close();
		}
	}

	/**
	 * @param move
	 * @param uc
	 * @throws SailException
	 */
	protected void executeMove(Move move, UpdateContext uc)
		throws SailException
	{
		ValueConstant sourceGraph = move.getSourceGraph();
		ValueConstant destinationGraph = move.getDestinationGraph();

		Resource source = sourceGraph != null ? (Resource)sourceGraph.getValue() : null;
		Resource destination = destinationGraph != null ? (Resource)destinationGraph.getValue() : null;

		if (source == null && destination == null || (source != null && source.equals(destination))) {
			// source and destination are the same, move is a null-operation.
			return;
		}

		// clear destination
		con.clear((Resource)destination);

		// remove all statements from source and add them to destination
		CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null, null,
				uc.isIncludeInferred(), (Resource)source);
		try {
			while (statements.hasNext()) {
				Statement st = statements.next();
				con.addStatement(uc, st.getSubject(), st.getPredicate(), st.getObject(), (Resource)destination);
				con.removeStatement(uc, st.getSubject(), st.getPredicate(), st.getObject(), (Resource)source);
			}
		}
		finally {
			statements.close();
		}
	}

	/**
	 * @param clearExpr
	 * @param uc
	 * @throws SailException
	 */
	protected void executeClear(Clear clearExpr, UpdateContext uc)
		throws SailException
	{
		try {
			ValueConstant graph = clearExpr.getGraph();

			if (graph != null) {
				Resource context = (Resource)graph.getValue();
				con.clear(context);
			}
			else {
				Scope scope = clearExpr.getScope();
				if (Scope.NAMED_CONTEXTS.equals(scope)) {
					CloseableIteration<? extends Resource, SailException> contextIDs = con.getContextIDs();
					try {
						while (contextIDs.hasNext()) {
							con.clear(contextIDs.next());
						}
					}
					finally {
						contextIDs.close();
					}
				}
				else if (Scope.DEFAULT_CONTEXTS.equals(scope)) {
					con.clear((Resource)null);
				}
				else {
					con.clear();
				}
			}
		}
		catch (SailException e) {
			if (!clearExpr.isSilent()) {
				throw e;
			}
		}
	}

	/**
	 * @param insertDataExpr
	 * @param uc
	 * @throws SailException
	 */
	protected void executeInsertData(InsertData insertDataExpr, UpdateContext uc)
		throws SailException
	{

		SPARQLUpdateDataBlockParser parser = new SPARQLUpdateDataBlockParser(vf);
		parser.setRDFHandler(new RDFSailInserter(con, vf, uc));
		parser.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		parser.getParserConfig().addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
		try {
			// TODO process update context somehow? dataset, base URI, etc.
			parser.parse(new ByteArrayInputStream(insertDataExpr.getDataBlock().getBytes("UTF-8")), "");
		}
		catch (RDFParseException e) {
			throw new SailException(e);
		}
		catch (RDFHandlerException e) {
			throw new SailException(e);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	/**
	 * @param deleteDataExpr
	 * @param uc
	 * @throws SailException
	 */
	protected void executeDeleteData(DeleteData deleteDataExpr, UpdateContext uc)
		throws SailException
	{
		SPARQLUpdateDataBlockParser parser = new SPARQLUpdateDataBlockParser(vf);
		parser.setAllowBlankNodes(false); // no blank nodes allowed in DELETE DATA.
		parser.setRDFHandler(new RDFSailRemover(con, vf, uc));

		try {
			// TODO process update context somehow? dataset, base URI, etc.
			parser.parse(new ByteArrayInputStream(deleteDataExpr.getDataBlock().getBytes()), "");
		}
		catch (RDFParseException e) {
			throw new SailException(e);
		}
		catch (RDFHandlerException e) {
			throw new SailException(e);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	protected void executeModify(Modify modify, UpdateContext uc)
		throws SailException
	{
		try {
			TupleExpr whereClause = modify.getWhereExpr();

			if (!(whereClause instanceof QueryRoot)) {
				whereClause = new QueryRoot(whereClause);
			}

			CloseableIteration<? extends BindingSet, QueryEvaluationException> sourceBindings;
			sourceBindings = evaluateWhereClause(whereClause, uc);
			try {
				while (sourceBindings.hasNext()) {
					BindingSet sourceBinding = sourceBindings.next();
					deleteBoundTriples(sourceBinding, modify.getDeleteExpr(), uc);

					insertBoundTriples(sourceBinding, modify.getInsertExpr(), uc);
				}
			}
			finally {
				sourceBindings.close();
			}
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	private URI[] getDefaultRemoveGraphs(Dataset dataset) {
		if (dataset == null)
			return new URI[0];
		Set<URI> set = dataset.getDefaultRemoveGraphs();
		if (set == null || set.isEmpty())
			return new URI[0];
		return set.toArray(new URI[set.size()]);
	}

	private CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateWhereClause(
			final TupleExpr whereClause, final UpdateContext uc)
		throws SailException, QueryEvaluationException
	{
		CloseableIteration<? extends BindingSet, QueryEvaluationException> sourceBindings;
		sourceBindings = con.evaluate(whereClause, uc.getDataset(), uc.getBindingSet(), uc.isIncludeInferred());

		return new ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException>(sourceBindings) {

			protected BindingSet convert(BindingSet sourceBinding)
				throws QueryEvaluationException
			{
				if (whereClause instanceof SingletonSet && sourceBinding instanceof EmptyBindingSet
						&& uc.getBindingSet() != null)
				{
					// in the case of an empty WHERE clause, we use the supplied
					// bindings to produce triples to DELETE/INSERT
					return uc.getBindingSet();
				}
				else {
					// check if any supplied bindings do not occur in the bindingset
					// produced by the WHERE clause. If so, merge.
					Set<String> uniqueBindings = new HashSet<String>(uc.getBindingSet().getBindingNames());
					uniqueBindings.removeAll(sourceBinding.getBindingNames());
					if (uniqueBindings.size() > 0) {
						MapBindingSet mergedSet = new MapBindingSet();
						for (String bindingName : sourceBinding.getBindingNames()) {
							mergedSet.addBinding(sourceBinding.getBinding(bindingName));
						}
						for (String bindingName : uniqueBindings) {
							mergedSet.addBinding(uc.getBindingSet().getBinding(bindingName));
						}
						return mergedSet;
					}
					return sourceBinding;
				}
			}
		};
	}

	/**
	 * @param whereBinding
	 * @param deleteClause
	 * @throws SailException
	 */
	private void deleteBoundTriples(BindingSet whereBinding, TupleExpr deleteClause, UpdateContext uc)
		throws SailException
	{
		if (deleteClause != null) {
			List<StatementPattern> deletePatterns = StatementPatternCollector.process(deleteClause);

			for (StatementPattern deletePattern : deletePatterns) {

				Resource subject = (Resource)getValueForVar(deletePattern.getSubjectVar(), whereBinding);
				URI predicate = (URI)getValueForVar(deletePattern.getPredicateVar(), whereBinding);
				Value object = getValueForVar(deletePattern.getObjectVar(), whereBinding);

				Resource context = null;
				if (deletePattern.getContextVar() != null) {
					context = (Resource)getValueForVar(deletePattern.getContextVar(), whereBinding);
				}

				if (subject == null || predicate == null || object == null) {
					// skip removal of triple if any variable is unbound (may happen
					// with optional patterns)
					// See SES-1047.
					continue;
				}

				if (context != null) {
					con.removeStatement(uc, subject, predicate, object, context);
				}
				else {
					URI[] remove = getDefaultRemoveGraphs(uc.getDataset());
					con.removeStatement(uc, subject, predicate, object, remove);
				}
			}
		}
	}

	/**
	 * @param whereBinding
	 * @param insertClause
	 * @throws SailException
	 */
	private void insertBoundTriples(BindingSet whereBinding, TupleExpr insertClause, UpdateContext uc)
		throws SailException
	{
		if (insertClause != null) {
			List<StatementPattern> insertPatterns = StatementPatternCollector.process(insertClause);

			// bnodes in the insert pattern are locally scoped for each
			// individual source binding.
			MapBindingSet bnodeMapping = new MapBindingSet();
			for (StatementPattern insertPattern : insertPatterns) {
				Statement toBeInserted = createStatementFromPattern(insertPattern, whereBinding, bnodeMapping);

				if (toBeInserted != null) {
					URI with = uc.getDataset().getDefaultInsertGraph();
					if (with == null && toBeInserted.getContext() == null) {
						con.addStatement(uc, toBeInserted.getSubject(), toBeInserted.getPredicate(),
								toBeInserted.getObject());
					}
					else if (toBeInserted.getContext() == null) {
						con.addStatement(uc, toBeInserted.getSubject(), toBeInserted.getPredicate(),
								toBeInserted.getObject(), with);
					}
					else {
						con.addStatement(uc, toBeInserted.getSubject(), toBeInserted.getPredicate(),
								toBeInserted.getObject(), toBeInserted.getContext());
					}
				}
			}
		}
	}

	private Statement createStatementFromPattern(StatementPattern pattern, BindingSet sourceBinding,
			MapBindingSet bnodeMapping)
		throws SailException
	{

		Resource subject = null;
		URI predicate = null;
		Value object = null;
		Resource context = null;

		if (pattern.getSubjectVar().hasValue()) {
			subject = (Resource)pattern.getSubjectVar().getValue();
		}
		else {
			subject = (Resource)sourceBinding.getValue(pattern.getSubjectVar().getName());

			if (subject == null && pattern.getSubjectVar().isAnonymous()) {
				Binding mappedSubject = bnodeMapping.getBinding(pattern.getSubjectVar().getName());

				if (mappedSubject != null) {
					subject = (Resource)mappedSubject.getValue();
				}
				else {
					subject = vf.createBNode();
					bnodeMapping.addBinding(pattern.getSubjectVar().getName(), subject);
				}
			}
		}

		if (pattern.getPredicateVar().hasValue()) {
			predicate = (URI)pattern.getPredicateVar().getValue();
		}
		else {
			predicate = (URI)sourceBinding.getValue(pattern.getPredicateVar().getName());
		}

		if (pattern.getObjectVar().hasValue()) {
			object = pattern.getObjectVar().getValue();
		}
		else {
			object = sourceBinding.getValue(pattern.getObjectVar().getName());

			if (object == null && pattern.getObjectVar().isAnonymous()) {
				Binding mappedObject = bnodeMapping.getBinding(pattern.getObjectVar().getName());

				if (mappedObject != null) {
					object = (Resource)mappedObject.getValue();
				}
				else {
					object = vf.createBNode();
					bnodeMapping.addBinding(pattern.getObjectVar().getName(), object);
				}
			}
		}

		if (pattern.getContextVar() != null) {
			if (pattern.getContextVar().hasValue()) {
				context = (Resource)pattern.getContextVar().getValue();
			}
			else {
				context = (Resource)sourceBinding.getValue(pattern.getContextVar().getName());
			}
		}

		Statement st = null;
		if (subject != null && predicate != null && object != null) {
			if (context != null) {
				st = vf.createStatement(subject, predicate, object, context);
			}
			else {
				st = vf.createStatement(subject, predicate, object);
			}
		}
		return st;
	}

	private Value getValueForVar(Var var, BindingSet bindings)
		throws SailException
	{
		Value value = null;
		if (var.hasValue()) {
			value = var.getValue();
		}
		else {
			value = bindings.getValue(var.getName());
		}
		return value;
	}
}
