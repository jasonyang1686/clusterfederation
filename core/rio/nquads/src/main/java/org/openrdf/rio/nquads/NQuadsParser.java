/*
 * Licensed to Aduna under one or more contributor license agreements. See the NOTICE.txt file
 * distributed with this work for additional information regarding copyright ownership.
 * 
 * Aduna licenses this file to you under the terms of the Aduna BSD License (the "License"); you may
 * not use this file except in compliance with the License. See the LICENSE.txt file distributed
 * with this work for the full License.
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openrdf.rio.nquads;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.ntriples.NTriplesParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.input.BOMInputStream;

/**
 * RDF parser implementation for the {@link RDFFormat#NQUADS N-Quads} RDF
 * format, extending the Rio N-Triples parser. A specification of N-Quads can be
 * found <a href="http://sw.deri.org/2008/07/n-quads/">here</a>. This parser is
 * not thread-safe, therefore its public methods are synchronized.
 * 
 * @since 2.7.0
 * @author Joshua Shinavier
 */
public class NQuadsParser extends NTriplesParser {

	protected Resource context;

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.NQUADS;
	}

	@Override
	public synchronized void parse(final InputStream inputStream, final String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (inputStream == null) {
			throw new IllegalArgumentException("Input stream can not be 'null'");
		}
		// Note: baseURI will be checked in parse(Reader, String)

		try {
			parse(new InputStreamReader(new BOMInputStream(inputStream, false), "US-ASCII"), baseURI);
		}
		catch (UnsupportedEncodingException e) {
			// Every platform should support the US-ASCII encoding...
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void parse(final Reader reader, final String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (reader == null) {
			throw new IllegalArgumentException("Reader can not be 'null'");
		}
		if (baseURI == null) {
			throw new IllegalArgumentException("base URI can not be 'null'");
		}

		if(rdfHandler != null) {
			rdfHandler.startRDF();
		}

		this.reader = reader;
		lineNo = 1;

		reportLocation(lineNo, 1);

		try {
			int c = readCodePoint();
			c = skipWhitespace(c);

			while (c != -1) {
				if (c == '#') {
					// Comment, ignore
					c = skipLine(c);
				}
				else if (c == '\r' || c == '\n') {
					// Empty line, ignore
					c = skipLine(c);
				}
				else {
					c = parseQuad(c);
				}

				c = skipWhitespace(c);
			}
		}
		finally {
			clear();
		}

		if(rdfHandler != null) {
			rdfHandler.endRDF();
		}
	}

	private int parseQuad(int c)
		throws IOException, RDFParseException, RDFHandlerException
	{

		boolean ignoredAnError = false;
		try {
			c = parseSubject(c);

			c = skipWhitespace(c);

			c = parsePredicate(c);

			c = skipWhitespace(c);

			c = parseObject(c);

			c = skipWhitespace(c);

			// Context is not required
			if (c != '.') {
				c = parseContext(c);
				c = skipWhitespace(c);
			}
			if (c == -1) {
				throwEOFException();
			}
			else if (c != '.') {
				reportFatalError("Expected '.', found: " + new String(Character.toChars(c)));
			}

			c = assertLineTerminates(c);
		}
		catch (RDFParseException rdfpe) {
			if (getParserConfig().isNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES)) {
				reportError(rdfpe, NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
				ignoredAnError = true;
			}
			else {
				throw rdfpe;
			}
		}

		c = skipLine(c);

		if (!ignoredAnError) {
			Statement st = createStatement(subject, predicate, object, context);
			if(rdfHandler != null) {
				rdfHandler.handleStatement(st);
			}
		}

		subject = null;
		predicate = null;
		object = null;
		context = null;

		return c;
	}

	protected int parseContext(int c)
		throws IOException, RDFParseException
	{
		// FIXME: context (in N-Quads) can be a literal
		StringBuilder sb = new StringBuilder(100);

		// subject is either an uriref (<foo://bar>) or a nodeID (_:node1)
		if (c == '<') {
			// subject is an uriref
			c = parseUriRef(c, sb);
			context = createURI(sb.toString());
		}
		else if (c == '_') {
			// subject is a bNode
			c = parseNodeID(c, sb);
			context = createBNode(sb.toString());
		}
		else if (c == -1) {
			throwEOFException();
		}
		else {
			reportFatalError("Expected '<' or '_', found: " + new String(Character.toChars(c)));
		}

		return c;
	}

}