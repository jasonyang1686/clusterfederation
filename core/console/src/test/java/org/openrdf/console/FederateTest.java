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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.http.config.HTTPRepositoryFactory;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.config.ProxyRepositoryConfig;
import org.openrdf.repository.sail.config.ProxyRepositoryFactory;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.sparql.config.SPARQLRepositoryConfig;
import org.openrdf.repository.sparql.config.SPARQLRepositoryFactory;
import org.openrdf.sail.federation.config.FederationConfig;

/**
 * Unit tests for {@link Federate}.
 * 
 * @author Dale Visser
 */
public class FederateTest extends AbstractCommandTest {

	private static final String FED_ID = "fedID";

	private static final String MEMORY_MEMBER_ID1 = "alien";

	private static final String MEMORY_MEMBER_ID2 = "scary";

	private static final String HTTP_MEMBER_ID = "http";

	private static final String HTTP2_MEMBER_ID = "http2";

	private static final String SPARQL_MEMBER_ID = "sparql";

	private static final String SPARQL2_MEMBER_ID = "sparql2";

	private static final String FED_DESCRIPTION = "Test Federation Title";

	private Federate federate;

	@Rule
	public final TemporaryFolder LOCATION = new TemporaryFolder();

	@Before
	public void prepareManager()
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		manager = new LocalRepositoryManager(LOCATION.getRoot());
		manager.initialize();
		addRepositories(MEMORY_MEMBER_ID1, MEMORY_MEMBER_ID2, HTTP_MEMBER_ID, HTTP2_MEMBER_ID,
				SPARQL_MEMBER_ID, SPARQL2_MEMBER_ID);
		ConsoleState state = mock(ConsoleState.class);
		when(state.getManager()).thenReturn(manager);
		when(streams.readln("Federation Description (optional):")).thenReturn(FED_DESCRIPTION);
		federate = new Federate(streams, state);
	}


	@After
	public void tearDown()
		throws OpenRDFException
	{
		manager.shutDown();
	}

	private void execute(String... args)
		throws IOException
	{
		List<String> execArgs = new ArrayList<String>(args.length + 1);
		execArgs.add("federate");
		Collections.addAll(execArgs, args);
		federate.execute(execArgs.toArray(new String[execArgs.size()]));
	}

	@Test
	public void noArgumentsPrintsHelp()
		throws IOException, OpenRDFException
	{
		execute();
		verify(streams).writeln(PrintHelp.FEDERATE);
	}

	@Test
	public void oneArgumentPrintsHelp()
		throws IOException, OpenRDFException
	{
		execute(FED_ID);
		verify(streams).writeln(PrintHelp.FEDERATE);
	}

	@Test
	public void twoArgumentsPrintsHelp()
		throws IOException, OpenRDFException
	{
		execute(FED_ID, MEMORY_MEMBER_ID1);
		verify(streams).writeln(PrintHelp.FEDERATE);
	}

	@Test
	public void invalidArgumentPrintsError()
		throws IOException, OpenRDFException
	{
		execute("type=memory", FED_ID, MEMORY_MEMBER_ID1, MEMORY_MEMBER_ID2);
		verifyFailure();
	}

	@Test
	public void duplicateMembersPrintsError()
		throws IOException, OpenRDFException
	{
		execute(FED_ID, MEMORY_MEMBER_ID1, MEMORY_MEMBER_ID1);
		verifyFailure();
	}

	@Test
	public void fedSameAsMemberPrintsError()
		throws IOException, OpenRDFException
	{
		execute(FED_ID, MEMORY_MEMBER_ID1, FED_ID, MEMORY_MEMBER_ID1);
		verifyFailure();
	}

	@Test
	public void sparqlAndNotReadOnlyPrintsError()
		throws IOException, OpenRDFException
	{
		execute("readonly=false", FED_ID, SPARQL_MEMBER_ID, SPARQL2_MEMBER_ID);
		verifyFailure(SPARQL_MEMBER_ID + " is read-only.");
		verifyFailure(SPARQL2_MEMBER_ID + " is read-only.");
	}

	@Test
	public void fedAlreadyExistsPrintsSpecificError()
		throws IOException, OpenRDFException
	{
		execute(MEMORY_MEMBER_ID1, FED_ID, MEMORY_MEMBER_ID2);
		verifyFailure(MEMORY_MEMBER_ID1 + " already exists.");
	}

	@Test
	public void nonexistentMemberPrintsSpecificError()
		throws IOException, OpenRDFException
	{
		execute(FED_ID, MEMORY_MEMBER_ID1, "FreeLunch");
		verifyFailure("FreeLunch does not exist.");
	}

	@Test
	public void federateMemoryMembersSuccess()
		throws UnsupportedEncodingException, IOException, RepositoryException, RepositoryConfigException
	{
		execute(FED_ID, MEMORY_MEMBER_ID1, MEMORY_MEMBER_ID2);
		verifySuccess(ProxyRepositoryFactory.REPOSITORY_TYPE, ProxyRepositoryFactory.REPOSITORY_TYPE);
		long expectedSize = getSize(MEMORY_MEMBER_ID1) + getSize(MEMORY_MEMBER_ID2);
		assertThat(getSize(FED_ID), is(equalTo(expectedSize)));
	}

	private long getSize(String memberID)
		throws RepositoryException, RepositoryConfigException
	{
		RepositoryConnection connection = manager.getRepository(memberID).getConnection();
		try {
			return connection.size();
		}
		finally {
			connection.close();
		}
	}

	@Test
	public void federateSucceedsWithHTTPandSPARQLmembers()
		throws UnsupportedEncodingException, IOException, OpenRDFException
	{
		execute(FED_ID, HTTP_MEMBER_ID, SPARQL_MEMBER_ID);
		verifySuccess(HTTPRepositoryFactory.REPOSITORY_TYPE, SPARQLRepositoryFactory.REPOSITORY_TYPE);
	}

	@Test
	public void federateHTTPtypeSucceeds()
		throws IOException, OpenRDFException
	{
		execute(FED_ID, HTTP_MEMBER_ID, HTTP2_MEMBER_ID);
		verifySuccess(HTTPRepositoryFactory.REPOSITORY_TYPE, HTTPRepositoryFactory.REPOSITORY_TYPE);
	}

	@Test
	public void federateSPARQLtypeSucceeds()
		throws IOException, OpenRDFException
	{
		execute(FED_ID, SPARQL_MEMBER_ID, SPARQL2_MEMBER_ID);
		verifySuccess(SPARQLRepositoryFactory.REPOSITORY_TYPE, SPARQLRepositoryFactory.REPOSITORY_TYPE);
	}

	@Test
	public void successWithNonDefaultReadonlyAndDistinct()
		throws IOException, RepositoryException, RepositoryConfigException
	{
		execute(FED_ID, "distinct=true", "readonly=false", MEMORY_MEMBER_ID1, MEMORY_MEMBER_ID2);
		verifySuccess(false, true, ProxyRepositoryFactory.REPOSITORY_TYPE,
				ProxyRepositoryFactory.REPOSITORY_TYPE);
		long expectedSize = getSize(MEMORY_MEMBER_ID1) + getSize(MEMORY_MEMBER_ID2);
		assertThat(getSize(FED_ID), is(equalTo(expectedSize)));
	}

	@Test
	public void fullyHeterogeneousSuccess()
		throws IOException, RepositoryException, RepositoryConfigException
	{
		execute(FED_ID, SPARQL_MEMBER_ID, MEMORY_MEMBER_ID1, HTTP_MEMBER_ID);
		verifySuccess(SPARQLRepositoryFactory.REPOSITORY_TYPE, ProxyRepositoryFactory.REPOSITORY_TYPE,
				HTTPRepositoryFactory.REPOSITORY_TYPE);
	}

	private void verifySuccess(String... memberTypes)
		throws RepositoryException, RepositoryConfigException, IOException
	{
		verifySuccess(true, false, memberTypes);
	}

	private void verifySuccess(boolean readonly, boolean distinct, String... memberTypes)
		throws RepositoryException, RepositoryConfigException, IOException
	{
		assertThat(manager.hasRepositoryConfig(FED_ID), is(equalTo(true)));
		verify(streams, times(1)).writeln("Federation created.");
		verify(streams, never()).writeError(anyString());
		verify(streams, times(1)).readln("Federation Description (optional):");
		assertThat(manager.getRepositoryInfo(FED_ID).getDescription(), is(equalTo(FED_DESCRIPTION)));
		SailRepositoryConfig sailRepoConfig = (SailRepositoryConfig)manager.getRepositoryConfig(FED_ID).getRepositoryImplConfig();
		FederationConfig fedSailConfig = (FederationConfig)sailRepoConfig.getSailImplConfig();
		assertThat(fedSailConfig.isReadOnly(), is(equalTo(readonly)));
		assertThat(fedSailConfig.isDistinct(), is(equalTo(distinct)));
		List<RepositoryImplConfig> members = fedSailConfig.getMembers();
		assertThat(members.size(), is(equalTo(memberTypes.length)));
		int i = 0;
		for (RepositoryImplConfig ric : members) {
			String memberType = memberTypes[i];
			i++;
			assertThat(ric.getType(), is(equalTo(memberType)));
			Class<? extends RepositoryImplConfig> implType;
			if (HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberType)) {
				implType = HTTPRepositoryConfig.class;
			}
			else if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberType)) {
				implType = SPARQLRepositoryConfig.class;
			}
			else {
				implType = ProxyRepositoryConfig.class;
			}
			assertThat(ric, is(instanceOf(implType)));
		}
	}

	private void verifyFailure(String... error)
		throws RepositoryException, RepositoryConfigException
	{
		if (error.length > 0) {
			verify(streams).writeError(error[0]);
		}
		else {
			verify(streams).writeError(anyString());
		}
		assertThat(manager.hasRepositoryConfig(FED_ID), is(equalTo(false)));
	}
}