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
package org.openrdf.repository;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class RDFSchemaRepositoryConnectionTest extends RepositoryConnectionTest {

	private URI person;

	private URI woman;

	private URI man;

	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();

		person = vf.createURI(FOAF_NS + "Person");
		woman = vf.createURI("http://example.org/Woman");
		man = vf.createURI("http://example.org/Man");
	}

	@Test
	public void testDomainInference()
		throws Exception
	{
		testCon.begin();
		testCon.add(name, RDFS.DOMAIN, person);
		testCon.add(bob, name, nameBob);
		testCon.commit();

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, person, true));
	}

	@Test
	public void testSubClassInference()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(man, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

	@Test
	public void testMakeExplicit()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));

		testCon.begin();
		testCon.add(alice, RDF.TYPE, person);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

	@Test
	public void testExplicitFlag()
		throws Exception
	{
		RepositoryResult<Statement> result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, true);
		try {
			assertTrue("result should not be empty", result.hasNext());
		}
		finally {
			result.close();
		}

		result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, false);
		try {
			assertFalse("result should be empty", result.hasNext());
		}
		finally {
			result.close();
		}
	}

	@Test
	public void testInferencerUpdates()
		throws Exception
	{
		testCon.begin();

		testCon.add(bob, name, nameBob);
		testCon.remove(bob, name, nameBob);

		testCon.commit();

		assertFalse(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

	@Test
	public void testInferencerQueryDuringTransaction()
		throws Exception
	{
		testCon.begin();

		testCon.add(bob, name, nameBob);
		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));

		testCon.commit();
	}

	@Test
	public void testInferencerTransactionIsolation()
		throws Exception
	{
		testCon.begin();
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
		assertFalse(testCon2.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));

		testCon.commit();

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
		assertTrue(testCon2.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

	@Ignore
	@Test
	@Override
	public void testDefaultContext()
		throws Exception
	{
		// ignore
	}

	@Ignore
	@Test
	@Override
	public void testDefaultInsertContext()
		throws Exception
	{
		// ignore
	}

	@Ignore
	@Test
	@Override
	public void testExclusiveNullContext()
		throws Exception
	{
		// ignore
	}
}
