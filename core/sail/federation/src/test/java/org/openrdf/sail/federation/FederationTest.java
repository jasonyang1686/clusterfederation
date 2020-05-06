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

import java.io.FileOutputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
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
public class FederationTest {
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_3="http://192.168.33.30:8080/openrdf-sesame";
	private static final String SESAME_SERVER_4="http://192.168.33.40:8080/openrdf-sesame";
	
	private static final String repositoryID_1="RyaAccumulo_1";
	
	private static final String repositoryID_2="RyaAccumulo_2";

	private static final String repositoryID_3="RyaAccumulo_3";
	
	private static final String repositoryID_4="RyaAccumulo_4";
	
	private static final String repositoryID_123456="Federation12_34_56";
	


	

	public static void main(String[] args) throws Exception {



		// federation repository 12
				Repository repo1 = new HTTPRepository(SESAME_SERVER_1, repositoryID_1);		
				repo1.initialize();	
				
				Repository repo2 = new HTTPRepository(SESAME_SERVER_2, repositoryID_2);		
				repo2.initialize();	

						
		//federation repository 34
				Repository repo3 = new HTTPRepository(SESAME_SERVER_3, repositoryID_3);		
				repo3.initialize();	
				
				Repository repo4 = new HTTPRepository(SESAME_SERVER_4, repositoryID_4);		
				repo4.initialize();
				
		//federation repository 56
			//	Repository repo56 = new HTTPRepository(SESAME_SERVER_5, repositoryID_56);		
			//	repo56.initialize();					
				
		//federation repository 123456
			//	HTTPRepository repo12_34_56 = new HTTPRepository(SESAME_SERVER_2, repositoryID_123456);		
			//	repo12_34_56.initialize();	
				

				RepositoryConnection con123456=null;

  

   	//		FileOutputStream out = new FileOutputStream("/home/vagrant/result.csv");
   	//		SPARQLResultsCSVWriter sparqlWriter = new SPARQLResultsCSVWriter(out);
   			
				try {

					log.info("Connecting to SailRepository.");
			      
		// federation of 12,34,56
			      
			        Federation federation = new Federation();
			        federation.addMember(repo1);
			        federation.addMember(repo2);  
			        federation.addMember(repo3);   
			        federation.addMember(repo4); 
			        
			       
					   
			        SailRepository sailRepo = new SailRepository(federation);

					  sailRepo.initialize();

		
	   			con123456=sailRepo.getConnection();				
            
		
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					
					String query = "PREFIX linkedmdb: <http://data.linkedmdb.org/resource/movie/> "+
"prefix owl: <http://www.w3.org/2002/07/owl#> "+
"PREFIX dcterms: <http://purl.org/dc/terms/> "+
"PREFIX purl: <http://purl.org/dc/terms/> "+
"PREFIX nytimes: <http://data.nytimes.com/elements/> "+
"SELECT ?actor ?filmTitle ?news ?variants ?articleCount ?first_use ?latest_use "+
"WHERE "+ 
"{ "+
"?film linkedmdb:actor 			?actor . "+
"?actor owl:sameAs 			?dbpediaURI. "+
"?nytURI owl:sameAs 			?dbpediaURI . "+
"?nytURI nytimes:topicPage 		?news ; "+
"	nytimes:number_of_variants 	?variants; "+
"	nytimes:associated_article_count ?articleCount; "+
"	nytimes:first_use 		?first_use; "+
"	nytimes:latest_use 		?latest_use . "+
"?film purl:title 			?filmTitle . "+
"} "+
"ORDER BY (?actor) ";
					

					
				//	System.out.println(query);
					TupleQuery tupleQuery123456 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query);
					
			   TupleQueryResult result123456= tupleQuery123456.evaluate();
			   
				BindingSet bindingSet = null;	
				int count=0;
			
				
				while(result123456.hasNext()){
					bindingSet = result123456.next();
					Value valueOfX = bindingSet.getValue("actor");
               System.out.println("Drug: "+valueOfX);	
					count++;
	
				}
			
				System.out.println("result size: "+count);
					
				final long end = System.currentTimeMillis();
					
			System.out.println("time: "+(end-start));
					
              repo1.shutDown();
              repo2.shutDown();
              repo3.shutDown();
              repo4.shutDown();
         //     repo2.shutDown();
			     sailRepo.shutDown();
		
				} finally {
		//	out.close();	
			con123456.close();

			
				}
			}

}
