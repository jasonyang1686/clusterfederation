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
package org.openrdf.http.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import info.aduna.io.IOUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.rio.RDFFormat;

public class ProtocolTest {

	private static TestServer server;

	@BeforeClass
	public static void startServer()
		throws Exception
	{
		server = new TestServer();
		try {
			server.start();
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}
	}

	@AfterClass
	public static void stopServer()
		throws Exception
	{
		server.stop();
	}

	/**
	 * Tests the server's methods for updating all data in a repository.
	 */
	@Test
	public void testRepository_PUT()
		throws Exception
	{
		putFile(Protocol.getStatementsLocation(TestServer.REPOSITORY_URL), "/testcases/default-graph-1.ttl");
	}

	/**
	 * Tests the server's methods for deleting all data in a repository.
	 */
	@Test
	public void testRepository_DELETE()
		throws Exception
	{
		delete(Protocol.getStatementsLocation(TestServer.REPOSITORY_URL));
	}

	/**
	 * Tests the server's methods for updating the data in the default context of
	 * a repository.
	 */
	@Test
	public void testNullContext_PUT()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		location += "?" + Protocol.CONTEXT_PARAM_NAME + "=" + Protocol.NULL_PARAM_VALUE;
		putFile(location, "/testcases/default-graph-1.ttl");
	}

	/**
	 * Tests the server's methods for deleting the data from the default context
	 * of a repository.
	 */
	@Test
	public void testNullContext_DELETE()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		location += "?" + Protocol.CONTEXT_PARAM_NAME + "=" + Protocol.NULL_PARAM_VALUE;
		delete(location);
	}

	/**
	 * Tests the server's methods for updating the data in a named context of a
	 * repository.
	 */
	@Test
	public void testNamedContext_PUT()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		String encContext = Protocol.encodeValue(new URIImpl("urn:x-local:graph1"));
		location += "?" + Protocol.CONTEXT_PARAM_NAME + "=" + encContext;
		putFile(location, "/testcases/named-graph-1.ttl");
	}

	/**
	 * Tests the server's methods for deleting the data from a named context of a
	 * repository.
	 */
	@Test
	public void testNamedContext_DELETE()
		throws Exception
	{
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		String encContext = Protocol.encodeValue(new URIImpl("urn:x-local:graph1"));
		location += "?" + Protocol.CONTEXT_PARAM_NAME + "=" + encContext;
		delete(location);
	}

	/**
	 * Tests the server's methods for quering a repository using GET requests to
	 * send SeRQL-select queries.
	 */
	@Test
	public void testSeRQLselect()
		throws Exception
	{
		TupleQueryResult queryResult = evaluateTupleQuery(TestServer.REPOSITORY_URL, "select * from {X} P {Y}",
				QueryLanguage.SERQL);
		QueryResultIO.write(queryResult, TupleQueryResultFormat.SPARQL, System.out);
	}

	/**
	 * Checks that the requested content type is returned when accept header
	 * explicitly set.
	 */
	@Test
	public void testContentTypeForGraphQuery1_GET()
		throws Exception
	{
		String query = "DESCRIBE <foo:bar>";
		String location = TestServer.REPOSITORY_URL;
		location += "?query=" + URLEncoder.encode(query, "UTF-8");

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		// Request RDF/XML formatted results:
		conn.setRequestProperty("Accept", RDFFormat.RDFXML.getDefaultMIMEType());

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String contentType = conn.getHeaderField("Content-Type");
				assertNotNull(contentType);

				// snip off optional charset declaration
				int charPos = contentType.indexOf(";");
				if (charPos > -1) {
					contentType = contentType.substring(0, charPos);
				}

				assertEquals(RDFFormat.RDFXML.getDefaultMIMEType(), contentType);
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
				throw new RuntimeException(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}

	/**
	 * Checks that a proper error (HTTP 406) is returned when accept header is
	 * set incorrectly on graph query.
	 */
	@Test
	public void testContentTypeForGraphQuery2_GET()
		throws Exception
	{
		String query = "DESCRIBE <foo:bar>";
		String location = TestServer.REPOSITORY_URL;
		location += "?query=" + URLEncoder.encode(query, "UTF-8");

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		// incorrect mime-type for graph query results
		conn.setRequestProperty("Accept", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
				// do nothing, expected
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}

	/**
	 * Checks that a suitable RDF content type is returned when accept header not
	 * explicitly set.
	 */
	@Test
	public void testContentTypeForGraphQuery3_GET()
		throws Exception
	{
		String query = "DESCRIBE <foo:bar>";
		String location = TestServer.REPOSITORY_URL;
		location += "?query=" + URLEncoder.encode(query, "UTF-8");

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String contentType = conn.getHeaderField("Content-Type");
				assertNotNull(contentType);

				// snip off optional charset declaration
				int charPos = contentType.indexOf(";");
				if (charPos > -1) {
					contentType = contentType.substring(0, charPos);
				}

				RDFFormat format = RDFFormat.forMIMEType(contentType);
				assertNotNull(format);
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
				throw new RuntimeException(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}

	@Test
	public void testQueryResponse_HEAD()
		throws Exception
	{
		String query = "DESCRIBE <foo:bar>";
		String location = TestServer.REPOSITORY_URL;
		location += "?query=" + URLEncoder.encode(query, "UTF-8");

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("HEAD");

		// Request RDF/XML formatted results:
		conn.setRequestProperty("Accept", RDFFormat.RDFXML.getDefaultMIMEType());

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String contentType = conn.getHeaderField("Content-Type");
				assertNotNull(contentType);

				// snip off optional charset declaration
				int charPos = contentType.indexOf(";");
				if (charPos > -1) {
					contentType = contentType.substring(0, charPos);
				}

				assertEquals(RDFFormat.RDFXML.getDefaultMIMEType(), contentType);
				assertEquals(0, conn.getContentLength());
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
				throw new RuntimeException(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}

	@Test
	public void testUpdateResponse_HEAD()
		throws Exception
	{
		String query = "INSERT DATA { <foo:foo> <foo:bar> \"foo\". } ";
		String location = Protocol.getStatementsLocation(TestServer.REPOSITORY_URL);
		location += "?update=" + URLEncoder.encode(query, "UTF-8");

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("HEAD");

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String contentType = conn.getHeaderField("Content-Type");
				assertNotNull(contentType);

				// snip off optional charset declaration
				int charPos = contentType.indexOf(";");
				if (charPos > -1) {
					contentType = contentType.substring(0, charPos);
				}

				assertEquals(0, conn.getContentLength());
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
				throw new RuntimeException(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}

	/**
	 * Test for SES-1861
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSequentialNamespaceUpdates()
		throws Exception
	{
		int limitCount = 1000;
		int limitPrefix = 50;

		Random prng = new Random();
		// String repositoryLocation =
		// Protocol.getRepositoryLocation("http://localhost:8080/openrdf-sesame",
		// "Test-NativeStore");
		String repositoryLocation = TestServer.REPOSITORY_URL;

		for (int count = 0; count < limitCount; count++) {
			int i = prng.nextInt(limitPrefix);
			String prefix = "prefix" + i;
			String ns = "http://example.org/namespace" + i;

			String location = Protocol.getNamespacePrefixLocation(repositoryLocation, prefix);

			if (count % 2 == 0) {
				putNamespace(location, ns);
			}
			else {
				deleteNamespace(location);
			}
		}
	}

	/**
	 * Test for SES-1861
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConcurrentNamespaceUpdates()
		throws Exception
	{
		int limitCount = 1000;
		int limitPrefix = 50;

		Random prng = new Random();

		// String repositoryLocation =
		// Protocol.getRepositoryLocation("http://localhost:8080/openrdf-sesame",
		// "Test-NativeStore");
		String repositoryLocation = TestServer.REPOSITORY_URL;

		ExecutorService threadPool = Executors.newFixedThreadPool(20);

		for (int count = 0; count < limitCount; count++) {
			final int number = count;
			final int i = prng.nextInt(limitPrefix);
			final String prefix = "prefix" + i;
			final String ns = "http://example.org/namespace" + i;

			final String location = Protocol.getNamespacePrefixLocation(repositoryLocation, prefix);

			Runnable runner = new Runnable() {

				public void run() {
					try {
						if (number % 2 == 0) {
							putNamespace(location, ns);
						}
						else {
							deleteNamespace(location);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						fail("Failed in test: " + number);
					}
				}
			};
			threadPool.execute(runner);
		}
		threadPool.shutdown();
		threadPool.awaitTermination(30000, TimeUnit.MILLISECONDS);
		threadPool.shutdownNow();
	}

	private void putNamespace(String location, String namespace)
		throws Exception
	{
		// System.out.println("Put namespace to " + location);

		URL url = new URL(location);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);

		InputStream dataStream = new ByteArrayInputStream(namespace.getBytes("UTF-8"));
		try {
			OutputStream connOut = conn.getOutputStream();

			try {
				IOUtil.transfer(dataStream, connOut);
			}
			finally {
				connOut.close();
			}
		}
		finally {
			dataStream.close();
		}

		conn.connect();

		int responseCode = conn.getResponseCode();

		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
					+ responseCode + ")";
			fail(response);
		}
	}

	private void deleteNamespace(String location)
		throws Exception
	{
		// System.out.println("Delete namespace at " + location);

		URL url = new URL(location);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("DELETE");
		conn.setDoOutput(true);

		conn.connect();

		int responseCode = conn.getResponseCode();

		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
					+ responseCode + ")";
			fail(response);
		}
	}

	private void putFile(String location, String file)
		throws Exception
	{
		System.out.println("Put file to " + location);

		URL url = new URL(location);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);

		RDFFormat dataFormat = RDFFormat.forFileName(file, RDFFormat.RDFXML);
		conn.setRequestProperty("Content-Type", dataFormat.getDefaultMIMEType());

		InputStream dataStream = ProtocolTest.class.getResourceAsStream(file);
		try {
			OutputStream connOut = conn.getOutputStream();

			try {
				IOUtil.transfer(dataStream, connOut);
			}
			finally {
				connOut.close();
			}
		}
		finally {
			dataStream.close();
		}

		conn.connect();

		int responseCode = conn.getResponseCode();

		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
					+ responseCode + ")";
			fail(response);
		}
	}

	private void delete(String location)
		throws Exception
	{
		URL url = new URL(location);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("DELETE");

		conn.connect();

		int responseCode = conn.getResponseCode();

		if (responseCode != HttpURLConnection.HTTP_OK && // 200 OK
				responseCode != HttpURLConnection.HTTP_NO_CONTENT) // 204 NO CONTENT
		{
			String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
					+ responseCode + ")";
			fail(response);
		}
	}

	private TupleQueryResult evaluateTupleQuery(String location, String query, QueryLanguage queryLn)
		throws Exception
	{
		location += "?query=" + URLEncoder.encode(query, "UTF-8") + "&queryLn=" + queryLn.getName();

		URL url = new URL(location);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();

		// Request SPARQL-XML formatted results:
		conn.setRequestProperty("Accept", TupleQueryResultFormat.SPARQL.getDefaultMIMEType());

		conn.connect();

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// Process query results
				return QueryResultIO.parse(conn.getInputStream(), TupleQueryResultFormat.SPARQL);
			}
			else {
				String response = "location " + location + " responded: " + conn.getResponseMessage() + " ("
						+ responseCode + ")";
				fail(response);
				throw new RuntimeException(response);
			}
		}
		finally {
			conn.disconnect();
		}
	}

}
