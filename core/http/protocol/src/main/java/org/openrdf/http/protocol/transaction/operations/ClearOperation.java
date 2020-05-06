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
package org.openrdf.http.protocol.transaction.operations;

import java.io.Serializable;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Operation that clears the whole repository.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public class ClearOperation extends ContextOperation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1570893715836564121L;

	public ClearOperation(Resource... contexts) {
		super(contexts);
	}

	public void execute(RepositoryConnection con)
		throws RepositoryException
	{
		con.clear(getContexts());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof ClearOperation) {
			return super.equals(other);
		}

		return false;
	}
}
