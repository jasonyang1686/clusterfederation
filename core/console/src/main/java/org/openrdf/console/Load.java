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
package org.openrdf.console;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryReadOnlyException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * @author Dale Visser
 */
class Load implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Load.class);

	private final ConsoleIO consoleIO;

	private final ConsoleState state;

	private final LockRemover lockRemover;

	Load(ConsoleIO consoleIO, ConsoleState state, LockRemover lockRemover) {
		this.consoleIO = consoleIO;
		this.state = state;
		this.lockRemover = lockRemover;
	}

	public void execute(final String... tokens) {
		Repository repository = state.getRepository();
		if (repository == null) {
			consoleIO.writeUnopenedError();
		}
		else {
			if (tokens.length < 2) {
				consoleIO.writeln(PrintHelp.LOAD);
			}
			else {
				String baseURI = null;
				String context = null;
				int index = 2;
				if (tokens.length >= index + 2 && tokens[index].equalsIgnoreCase("from")) {
					baseURI = tokens[index + 1];
					index += 2;
				}
				if (tokens.length >= index + 2 && tokens[index].equalsIgnoreCase("into")) {
					context = tokens[tokens.length - 1];
					index += 2;
				}
				if (index < tokens.length) {
					consoleIO.writeln(PrintHelp.LOAD);
				}
				else {
					load(repository, baseURI, context, tokens);
				}
			}
		}
	}

	private void load(Repository repository, String baseURI, String context, final String... tokens) {
		final String dataPath = tokens[1];
		URL dataURL = null;
		File dataFile = null;
		try {
			dataURL = new URL(dataPath);
			// dataPath is a URI
		}
		catch (MalformedURLException e) {
			// dataPath is a file
			dataFile = new File(dataPath);
		}
		try {
			addData(repository, baseURI, context, dataURL, dataFile);
		}
		catch (RepositoryReadOnlyException e) {
			handleReadOnlyException(repository, e, tokens);
		}
		catch (MalformedURLException e) {
			consoleIO.writeError("Malformed URL: " + dataPath);
		}
		catch (IllegalArgumentException e) {
			// Thrown when context URI is invalid
			consoleIO.writeError(e.getMessage());
		}
		catch (IOException e) {
			consoleIO.writeError("Failed to load data: " + e.getMessage());
		}
		catch (UnsupportedRDFormatException e) {
			consoleIO.writeError("No parser available for this RDF format");
		}
		catch (RDFParseException e) {
			consoleIO.writeError("Malformed document: " + e.getMessage());
		}
		catch (RepositoryException e) {
			consoleIO.writeError("Unable to add data to repository: " + e.getMessage());
			LOGGER.error("Failed to add data to repository", e);
		}
	}

	private void handleReadOnlyException(Repository repository, RepositoryReadOnlyException caught,
			final String... tokens)
	{
		try {
			if (lockRemover.tryToRemoveLock(repository)) {
				execute(tokens);
			}
			else {
				consoleIO.writeError("Failed to load data");
				LOGGER.error("Failed to load data", caught);
			}
		}
		catch (RepositoryException e1) {
			consoleIO.writeError("Unable to restart repository: " + e1.getMessage());
			LOGGER.error("Unable to restart repository", e1);
		}
		catch (IOException e1) {
			consoleIO.writeError("Unable to remove lock: " + e1.getMessage());
		}
	}

	private void addData(Repository repository, String baseURI, String context, URL dataURL, File dataFile)
		throws RepositoryException, IOException, RDFParseException
	{
		Resource[] contexts = getContexts(repository, context);
		consoleIO.writeln("Loading data...");
		final long startTime = System.nanoTime();
		final RepositoryConnection con = repository.getConnection();
		try {
			if (dataURL == null) {
				con.add(dataFile, baseURI, null, contexts);
			}
			else {
				con.add(dataURL, baseURI, null, contexts);
			}
		}
		finally {
			con.close();
		}
		final long endTime = System.nanoTime();
		consoleIO.writeln("Data has been added to the repository (" + (endTime - startTime)
				/ 1000000 + " ms)");
	}

	private Resource[] getContexts(Repository repository, String context) {
		Resource[] contexts = new Resource[0];
		if (context != null) {
			Resource contextURI;
			if (context.startsWith("_:")) {
				contextURI = repository.getValueFactory().createBNode(context.substring(2));
			}
			else {
				contextURI = repository.getValueFactory().createURI(context);
			}
			contexts = new Resource[] { contextURI };
		}
		return contexts;
	}

}
