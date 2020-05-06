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
package org.openrdf.http.server.repository.namespaces;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.io.IOUtil;
import info.aduna.webapp.views.EmptySuccessView;
import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Handles requests for manipulating a specific namespace definition in a
 * repository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class NamespaceController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public NamespaceController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_HEAD, "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		String pathInfoStr = request.getPathInfo();
		String[] pathInfo = pathInfoStr.substring(1).split("/");
		String prefix = pathInfo[pathInfo.length - 1];

		String reqMethod = request.getMethod();

		if (METHOD_HEAD.equals(reqMethod)) {
			logger.info("HEAD namespace for prefix {}", prefix);

			Map<String, Object> model = new HashMap<String, Object>();
			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}

		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET namespace for prefix {}", prefix);
			return getExportNamespaceResult(request, prefix);
		}

		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT prefix {}", prefix);
			return getUpdateNamespaceResult(request, prefix);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE prefix {}", prefix);
			return getRemoveNamespaceResult(request, prefix);
		}
		else {
			throw new ServerHTTPException("Unexpected request method: " + reqMethod);
		}
	}

	private ModelAndView getExportNamespaceResult(HttpServletRequest request, String prefix)
		throws ServerHTTPException, ClientHTTPException
	{
		try {
			String namespace = null;

			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				namespace = repositoryCon.getNamespace(prefix);
			}

			if (namespace == null) {
				throw new ClientHTTPException(SC_NOT_FOUND, "Undefined prefix: " + prefix);
			}

			Map<String, Object> model = new HashMap<String, Object>();
			model.put(SimpleResponseView.CONTENT_KEY, namespace);

			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}
	}

	private ModelAndView getUpdateNamespaceResult(HttpServletRequest request, String prefix)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		String namespace = IOUtil.readString(request.getReader());
		namespace = namespace.trim();

		if (namespace.length() == 0) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "No namespace name found in request body");
		}
		// FIXME: perform some sanity checks on the namespace string

		try {
			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				repositoryCon.setNamespace(prefix, namespace);
			}
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}

	private ModelAndView getRemoveNamespaceResult(HttpServletRequest request, String prefix)
		throws ServerHTTPException
	{
		try {
			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				repositoryCon.removeNamespace(prefix);
			}
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}
}
