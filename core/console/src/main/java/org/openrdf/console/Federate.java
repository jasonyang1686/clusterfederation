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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.runtime.RepositoryManagerFederator;

/**
 * Implements the 'federate' command for the Sesame Console.
 * 
 * @author Dale Visser
 */
public class Federate implements Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Federate.class);

	private final ConsoleIO cio;

	private final ConsoleState state;

	protected Federate(ConsoleIO cio, ConsoleState state) {
		this.cio = cio;
		this.state = state;
	}

	/**
	 * Executes a 'federate' command for the Sesame Console.
	 * 
	 * @param parameters
	 *        the expectations for the tokens in this array are fully documented
	 *        in {@link PrintHelp#FEDERATE}.
	 */
	@Override
	public void execute(String... parameters)
		throws IOException
	{
		if (parameters.length < 4) {
			cio.writeln(PrintHelp.FEDERATE);
		}
		else {
			LinkedList<String> plist = new LinkedList<String>(Arrays.asList(parameters));
			plist.remove(); // "federate"
			boolean distinct = getOptionalParamValue(plist, "distinct", false);
			boolean readonly = getOptionalParamValue(plist, "readonly", true);
			if (distinctValues(plist)) {
				String fedID = plist.pop();
				federate(distinct, readonly, fedID, plist);
			}
			else {
				cio.writeError("Duplicate repository id's specified.");
			}
		}
	}

	private boolean distinctValues(Deque<String> plist) {
		return plist.size() == new HashSet<String>(plist).size();
	}

	private void federate(boolean distinct, boolean readonly, String fedID, Deque<String> memberIDs) {
		if (LOGGER.isDebugEnabled()) {
			logCallDetails(distinct, readonly, fedID, memberIDs);
		}
		else {
			RepositoryManager manager = state.getManager();
			try {
				if (manager.hasRepositoryConfig(fedID)) {
					cio.writeError(fedID + " already exists.");
				}
				else if (validateMembers(manager, readonly, memberIDs)) {
					String description = cio.readln("Federation Description (optional):");
					RepositoryManagerFederator rmf = new RepositoryManagerFederator(manager);
					rmf.addFed(fedID, description, memberIDs, readonly, distinct);
					cio.writeln("Federation created.");
				}
			}
			catch (RepositoryConfigException rce) {
				cio.writeError(rce.getMessage());
			}
			catch (RepositoryException re) {
				cio.writeError(re.getMessage());
			}
			catch (MalformedURLException mue) {
				cio.writeError(mue.getMessage());
			}
			catch (OpenRDFException ore) {
				cio.writeError(ore.getMessage());
			}
			catch (IOException ioe) {
				cio.writeError(ioe.getMessage());
			}
		}
	}

	private boolean validateMembers(RepositoryManager manager, boolean readonly, Deque<String> memberIDs) {
		boolean result = true;
		try {
			for (String memberID : memberIDs) {
				if (manager.hasRepositoryConfig(memberID)) {
					if (!readonly) {
						if (!manager.getRepository(memberID).isWritable()) {
							result = false;
							cio.writeError(memberID + " is read-only.");
						}
					}
				}
				else {
					result = false;
					cio.writeError(memberID + " does not exist.");
				}
			}
		}
		catch (RepositoryException re) {
			cio.writeError(re.getMessage());
		}
		catch (RepositoryConfigException rce) {
			cio.writeError(rce.getMessage());
		}
		return result;
	}

	private void logCallDetails(boolean distinct, boolean readonly, String fedID, Deque<String> memberIDs) {
		StringBuilder builder = new StringBuilder();
		builder.append("Federate called with federation ID = " + fedID + ", and member ID's = ");
		for (String member : memberIDs) {
			builder.append("[").append(member).append("]");
		}
		builder.append(".\n  Distinct set to ").append(distinct).append(", and readonly set to ").append(
				readonly).append(".\n");
		LOGGER.debug(builder.toString());
	}

	private boolean getOptionalParamValue(Deque<String> parameters, String name, boolean defaultValue) {
		return Boolean.parseBoolean(getOptionalParamValue(parameters, name, Boolean.toString(defaultValue)));
	}

	private String getOptionalParamValue(Deque<String> parameters, String name, String defaultValue) {
		String result = defaultValue;
		for (String parameter : parameters) {
			if (parameter.length() >= name.length()
					&& parameter.substring(0, name.length()).equalsIgnoreCase(name))
			{
				String[] parsed = parameter.split("=");
				if (parsed.length == 2 && parsed[0].equalsIgnoreCase(name)) {
					result = parsed[1].toLowerCase();
					parameters.remove(parameter);
					break;
				}
			}
		}
		return result;
	}
}
