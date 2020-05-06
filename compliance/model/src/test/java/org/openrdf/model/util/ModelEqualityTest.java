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
package org.openrdf.model.util;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.junit.Test;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 */
public class ModelEqualityTest {

	public static final String TESTCASES_DIR = "/testcases/model/equality/";

	@Test
	public void testTest001()
		throws Exception
	{
		testFilesEqual("test001a.ttl", "test001b.ttl");
	}

	@Test
	public void testFoafExampleAdvanced()
		throws Exception
	{
		testFilesEqual("foaf-example-advanced.rdf", "foaf-example-advanced.rdf");
	}

	@Test
	public void testSparqlGraph11()
		throws Exception
	{
		testFilesEqual("sparql-graph-11.ttl", "sparql-graph-11.ttl");
	}

	@Test
	public void testBlankNodeGraphs()
		throws Exception
	{
		testFilesEqual("toRdf-0061-out.nq", "toRdf-0061-out.nq");
	}

	// public void testSparqlGraph11Shuffled()
	// throws Exception
	// {
	// testFilesEqual("sparql-graph-11.ttl", "sparql-graph-11-shuffled.ttl");
	// }

	// public void testSparqlGraph11Shuffled2()
	// throws Exception
	// {
	// testFilesEqual("sparql-graph-11-shuffled.ttl", "sparql-graph-11.ttl");
	// }

	// public void testPhotoData()
	// throws Exception
	// {
	// testFilesEqual("photo-data.rdf", "photo-data.rdf");
	// }

	private void testFilesEqual(String file1, String file2)
		throws Exception
	{
		Set<Statement> model1 = loadModel(file1);
		Set<Statement> model2 = loadModel(file2);

		// long startTime = System.currentTimeMillis();
		boolean modelsEqual = ModelUtil.equals(model1, model2);
		// long endTime = System.currentTimeMillis();
		// System.out.println("Model equality checked in " + (endTime - startTime)
		// + "ms (" + file1 + ", " + file2
		// + ")");

		assertTrue(modelsEqual);
	}

	private Model loadModel(String fileName)
		throws Exception
	{
		URL modelURL = this.getClass().getResource(TESTCASES_DIR + fileName);
		assertNotNull("Test file not found: " + fileName, modelURL);

		Model model = new LinkedHashModel();
		
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(fileName);
		assertNotNull("Unable to determine RDF format for file: " + fileName, rdfFormat);

		RDFParser parser = Rio.createParser(rdfFormat);
		parser.setDatatypeHandling(DatatypeHandling.IGNORE);
		parser.setPreserveBNodeIDs(true);
		parser.setRDFHandler(new StatementCollector(model));

		InputStream in = modelURL.openStream();
		try {
			parser.parse(in, modelURL.toString());
			return model;
		}
		finally {
			in.close();
		}
	}
}
