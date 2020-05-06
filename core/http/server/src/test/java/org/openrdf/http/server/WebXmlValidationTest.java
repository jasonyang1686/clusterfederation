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

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import info.aduna.xml.DocumentUtil;

/**
 * @author Herko ter Horst
 */
public class WebXmlValidationTest extends TestCase {

	public void testValidXml() {
		try {
			File webXml = new File("src/main/webapp/WEB-INF/web.xml");

			DocumentUtil.getDocument(webXml.toURL(), SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
