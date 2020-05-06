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
package org.openrdf.repository.manager;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.event.base.NotifyingRepositoryWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * FIXME: do not extend NotifyingRepositoryWrapper, because SystemRepository
 * shouldn't expose RepositoryWrapper behaviour, just implement
 * NotifyingRepository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class SystemRepository extends NotifyingRepositoryWrapper {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * The repository identifier for the system repository that contains the
	 * configuration data.
	 */
	public static final String ID = "SYSTEM";

	public static final String TITLE = "System configuration repository";

	public static final String REPOSITORY_TYPE = "openrdf:SystemRepository";

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SystemRepository(File systemDir)
		throws RepositoryException
	{
		super();
		super.setDelegate(new SailRepository(new MemoryStore(systemDir)));
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void initialize()
		throws RepositoryException
	{
		super.initialize();

		RepositoryConnection con = getConnection();
		try {
			if (con.isEmpty()) {
				logger.debug("Initializing empty {} repository", ID);

				con.begin();
				con.setNamespace("rdf", RDF.NAMESPACE);
				con.setNamespace("sys", RepositoryConfigSchema.NAMESPACE);
				con.commit();

				RepositoryConfig repConfig = new RepositoryConfig(ID, TITLE, new SystemRepositoryConfig());
				RepositoryConfigUtil.updateRepositoryConfigs(con, repConfig);

			}
		}
		catch (RepositoryConfigException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
		finally {
			con.close();
		}
	}

	@Override
	public void setDelegate(Repository delegate)
	{
		throw new UnsupportedOperationException("Setting delegate on system repository not allowed");
	}
}
