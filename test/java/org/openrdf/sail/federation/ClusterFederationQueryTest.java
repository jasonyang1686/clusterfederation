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
package org.openrdf.sail.federation;

import java.util.Iterator;

import org.apache.log4j.Logger;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection; 

/**
 *
 * @author vagrant
 */
public class ClusterFederationQueryTest {
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_3="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_5="http://192.168.33.50:8080/openrdf-sesame";
	
	private static final String repositoryID_12="Federation12";

	private static final String repositoryID_34="Federation34";
	
	private static final String repositoryID_56="Federation56";
	
	private static final String repositoryID_123456="Federation12_34_56";
	
//	private static final String repositoryID_cluster12="ClusterFederation12_sec";
//	private static final String repositoryID_cluster34="ClusterFederation34_sec";

	

	public static void main(String[] args) throws Exception {



		// federation repository 12
				Repository repo12 = new HTTPRepository(SESAME_SERVER_1, repositoryID_12);		
				repo12.initialize();	

						
		//federation repository 34
				Repository repo34 = new HTTPRepository(SESAME_SERVER_3, repositoryID_34);		
				repo34.initialize();	
				
		//federation repository 56
				Repository repo56 = new HTTPRepository(SESAME_SERVER_5, repositoryID_56);		
				repo56.initialize();					
				
		//federation repository 123456
				Repository repo12_34_56 = new HTTPRepository(SESAME_SERVER_2, repositoryID_123456);		
				repo12_34_56.initialize();	
				

				RepositoryConnection con1234=null;

				RepositoryConnection con12=null;
		 	   RepositoryConnection con34=null;
		 	   RepositoryConnection con56=null;	 	  

			   
				try {

					log.info("Connecting to SailRepository.");
		//overlap list info
					String instanceName="dev";
					String tableName = "rya_overlap";
					String zkServer = "localhost:2181";
			      String userName="root";
			      String passWord="root";
			    
			      
		// federation of 12,34
			      /*
			        Federation federation12 = new Federation();
			        federation12.addMember(repo1);
			        federation12.addMember(repo2);     
			        
			        Federation federation34 = new Federation();
			        federation34.addMember(repo3);
			        federation34.addMember(repo4);    
			      */

					   /*
			        sailRepo12 = new SailRepository(federation12);

					  sailRepo12.initialize();
					   sailRepo34 = new SailRepository(federation34);

					   sailRepo34.initialize();
*/
					


		
	   			con1234=repo12_34_56.getConnection();				
            
	   			con12=repo12.getConnection();
	   			con34=repo34.getConnection();
	   			con56=repo56.getConnection();
			
			/*		
		// create a new repository and id			
					RemoteRepositoryManager manager = new RemoteRepositoryManager(SESAME_SERVER_3);
					manager.initialize();
									
					String repositoryId = "ClusterFederation34";
					RepositoryImplConfig implConfig= new HTTPRepositoryConfig(SESAME_SERVER_3);
					RepositoryConfig repConfig = new RepositoryConfig(repositoryId, implConfig);
					manager.addRepositoryConfig(repConfig);
					
		// create a repository variable from a given id			
					Repository Repo34 =manager.getRepository(repositoryId);
					Repo34.initialize();
					con34=Repo34.getConnection();
				

					
					
	   // remove a repository
					manager.removeRepository(repositoryId);

                    
				*/	
					

					final long start = System.currentTimeMillis();
		// execute query
					String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE"+
							"{ "+
							 "?Y ub:teacherOf ?Z . "+							 
							 "?X ub:advisor ?Y . "+
							 "?X ub:takesCourse ?Z. "+

							 " }";
												
					TupleQuery tupleQuery12 = con12.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result12= tupleQuery12.evaluate();
					final long phase_1_12 = System.currentTimeMillis();			
					
					TupleQuery tupleQuery34 = con34.prepareTupleQuery(QueryLanguage.SPARQL, query);
				   TupleQueryResult result34= tupleQuery34.evaluate();
					final long phase_1_34 = System.currentTimeMillis();	
					
					TupleQuery tupleQuery56 = con56.prepareTupleQuery(QueryLanguage.SPARQL, query);
				   TupleQueryResult result56= tupleQuery56.evaluate();					
					final long phase_1_56 = System.currentTimeMillis();					
					
				TupleQuery tupleQuery1234 = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query);

					
					TupleQueryResult result1234= tupleQuery1234.evaluate();
					
					final long end = System.currentTimeMillis();
					
					System.out.println("phase 1_12 time: "+(phase_1_12-start));
					System.out.println("phase 1_34 time: "+(phase_1_34-phase_1_12));	
					System.out.println("phase 1_56 time: "+(phase_1_56-phase_1_34));					
					System.out.println("phase 2 time: "+(end-phase_1_56));

					
					BindingSet bindingSet = null;	
					int count=0;
					System.out.println("phase1_12:");								
					while(result12.hasNext()){
						bindingSet = result12.next();	
						Value valueOfX = bindingSet.getValue("X");
          //        System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					System.out.println("phase1_34:");
				
					while(result34.hasNext()){
						bindingSet = result34.next();
						Value valueOfX = bindingSet.getValue("X");
         //         System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					
					System.out.println("phase1_56:");
					
					while(result56.hasNext()){
						bindingSet = result56.next();
						Value valueOfX = bindingSet.getValue("X");
           //       System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
										
					System.out.println("phase2:");
					
					while(result1234.hasNext()){
						bindingSet = result1234.next();
						Value valueOfX = bindingSet.getValue("X");
            //      System.out.println("X: "+valueOfX);	
						count++;

		
					}
				
					System.out.println("result size: "+count);
					
              repo12.shutDown();
              repo34.shutDown();
              repo56.shutDown();
			     repo12_34_56.shutDown();
		
				} finally {
			con1234.close();
			con56.close();
			con34.close();
			con12.close();
			
				}
			}

}
