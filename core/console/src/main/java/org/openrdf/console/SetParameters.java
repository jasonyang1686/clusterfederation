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

/**
 * @author dale
 */
public class SetParameters implements Command {

	private final ConsoleIO consoleIO;

	private final ConsoleParameters parameters;

	SetParameters(ConsoleIO consoleIO, ConsoleParameters parameters) {
		this.consoleIO = consoleIO;
		this.parameters = parameters;
	}

	public void execute(String... tokens) {
		if (tokens.length == 1) {
			showParameters();
		}
		else if (tokens.length == 2) {
			final String param = tokens[1];
			String key, value;
			final int eqIdx = param.indexOf('=');
			if (eqIdx == -1) {
				key = param;
				value = null; // NOPMD
			}
			else {
				key = param.substring(0, eqIdx);
				value = param.substring(eqIdx + 1);
			}
			setParameter(key, value);
		}
		else {
			consoleIO.writeln(PrintHelp.SET);
		}
	}

	private void showParameters() {
		setWidth(null);
		setShowPrefix(null);
		setQueryPrefix(null);
	}

	private void setParameter(final String key, final String value) {
		if ("width".equalsIgnoreCase(key)) {
			setWidth(value);
		}
		else if ("showprefix".equalsIgnoreCase(key)) {
			setShowPrefix(value);
		}
		else if ("queryprefix".equalsIgnoreCase(key)) {
			setQueryPrefix(value);
		}
		else {
			consoleIO.writeError("unknown parameter: " + key);
		}
	}

	private void setWidth(final String value) {
		if (value == null) {
			consoleIO.writeln("width: " + parameters.getWidth());
		}
		else {
			try {
				final int width = Integer.parseInt(value);
				if (width > 0) {
					parameters.setWidth(width);
				}
				else {
					consoleIO.writeError("Width must be larger than 0");
				}
			}
			catch (NumberFormatException e) {
				consoleIO.writeError("Width must be a positive number");
			}
		}
	}

	private void setShowPrefix(final String value) {
		if (value == null) {
			consoleIO.writeln("showPrefix: " + parameters.isShowPrefix());
		}
		else {
			parameters.setShowPrefix(Boolean.parseBoolean(value));
		}
	}

	private void setQueryPrefix(final String value) {
		if (value == null) {
			consoleIO.writeln("queryPrefix: " + parameters.isQueryPrefix());
		}
		else {
			parameters.setQueryPrefix(Boolean.parseBoolean(value));
		}
	}
}
