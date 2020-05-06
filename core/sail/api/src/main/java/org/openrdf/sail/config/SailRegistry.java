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
package org.openrdf.sail.config;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available {@link SailFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class SailRegistry extends ServiceRegistry<String, SailFactory> {

	private static SailRegistry defaultRegistry;

	/**
	 * Gets the default SailRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized SailRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new SailRegistry();
		}

		return defaultRegistry;
	}

	public SailRegistry() {
		super(SailFactory.class);
	}

	@Override
	protected String getKey(SailFactory factory) {
		return factory.getSailType();
	}
}
