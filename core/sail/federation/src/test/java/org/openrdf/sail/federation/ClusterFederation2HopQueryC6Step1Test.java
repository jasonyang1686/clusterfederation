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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class ClusterFederation2HopQueryC6Step1Test {
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//

	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_3="http://192.168.33.30:8080/openrdf-sesame";

	private static final String repositoryID_12="Federation12";

	private static final String repositoryID_34="Federation34";
	
	private static final String repositoryID_1234="Federation12_34";	

	public static void main(String[] args) throws Exception {



		// federation repository 12
				Repository repo12 = new HTTPRepository(SESAME_SERVER_1, repositoryID_12);		
				repo12.initialize();	

						
		//federation repository 34
				Repository repo34 = new HTTPRepository(SESAME_SERVER_3, repositoryID_34);		
				repo34.initialize();	
								
				
		//federation repository 1234
				HTTPRepository repo12_34 = new HTTPRepository(SESAME_SERVER_2, repositoryID_1234);		
				repo12_34.initialize();	
				

				RepositoryConnection con1234=null;

				RepositoryConnection con12=null;
		 	   RepositoryConnection con34=null; 	  

   			
				try {

					log.info("Connecting to SailRepository.");
			      
	   			con1234=repo12_34.getConnection();				
            
	   			con12=repo12.getConnection();
	   			con34=repo34.getConnection();	
		
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
							"?actor owl:sameAs 			?dbpediaURI. "+
							"?nytURI owl:sameAs 			?dbpediaURI . "+
							"?film linkedmdb:actor 			?actor . "+
							"?nytURI nytimes:topicPage 		?news ; "+
							"	nytimes:number_of_variants 	?variants; "+
							"	nytimes:associated_article_count ?articleCount; "+
							"	nytimes:first_use 		?first_use; "+
							"	nytimes:latest_use 		?latest_use . "+
							"?film purl:title 			?filmTitle . "+
							"} "+
							"ORDER BY (?actor) ";
				//	System.out.println(query);
					int count=0;
					TupleQuery tupleQuery12 = con12.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result12= tupleQuery12.evaluate();
					
					System.out.println("phase1_12:");	
					BindingSet bindingSet = null;	
					while(result12.hasNext()){
						bindingSet = result12.next();	
						Value valueOfX = bindingSet.getValue("actor");
          //        System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					
					final long phase_1_12 = System.currentTimeMillis();	
					
					System.out.println("phase 1_12 time: "+(phase_1_12-start));
					
					TupleQuery tupleQuery34 = con34.prepareTupleQuery(QueryLanguage.SPARQL, query);
				   TupleQueryResult result34= tupleQuery34.evaluate();

					
					System.out.println("phase1_34:");
					count =0;
					while(result34.hasNext()){
						bindingSet = result34.next();
						Value valueOfX = bindingSet.getValue("actor");
         //         System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					
					final long phase_1_34 = System.currentTimeMillis();	
					System.out.println("phase 1_34 time: "+(phase_1_34-phase_1_12));
					
					System.out.println("phase2_0hop");
					
					String query0Hop = "PREFIX linkedmdb: <http://data.linkedmdb.org/resource/movie/> "+
							"prefix owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX dcterms: <http://purl.org/dc/terms/> "+
							"PREFIX purl: <http://purl.org/dc/terms/> "+
							"PREFIX nytimes: <http://data.nytimes.com/elements/> "+
							"SELECT ?actor ?nytURI "+
							"WHERE "+ 
							"{ "+
							"?actor owl:sameAs 			?dbpediaURI. "+
							"?nytURI owl:sameAs 			?dbpediaURI . "+
							"} "+
							"ORDER BY (?actor) ";
					
					
				
					TupleQuery tupleQuery0Hop = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query0Hop);
				   TupleQueryResult result0Hop= tupleQuery0Hop.evaluate();
				   
					count =0;
					List<String> actor = new ArrayList<String>();
					List<String> nytURI = new ArrayList<String>();
					
					while(result0Hop.hasNext()){
						bindingSet = result0Hop.next();
						Value valueOfX = bindingSet.getValue("actor");
						Value valueOfY = bindingSet.getValue("nytURI");
						if(valueOfY.toString().contains("http://data.nytimes.com")){
						actor.add(valueOfX.toString());
						nytURI.add(valueOfY.toString());
						
						count++;	
						}
					}
					System.out.println("result size: "+count);
				   				
					final long phase_2_0 = System.currentTimeMillis();					

					System.out.println("phase 2_0Hop time: "+(phase_2_0-phase_1_34));
					
					FileOutputStream fosActor = new FileOutputStream("/home/vagrant/results/actor");
					OutputStreamWriter oswActor = new OutputStreamWriter(fosActor);
					BufferedWriter bwActor = new BufferedWriter(oswActor);
					
					FileOutputStream fosNYT = new FileOutputStream("/home/vagrant/results/NYT");
					OutputStreamWriter oswNYT = new OutputStreamWriter(fosNYT);
					BufferedWriter bwNYT = new BufferedWriter(oswNYT);
					
					for (int i=0;i<actor.size();i++){
						bwActor.write(actor.get(i)+"\n");
						bwNYT.write(nytURI.get(i)+"\n");
					}
				  bwActor.close();
				  bwNYT.close();
              repo12.shutDown();
              repo34.shutDown();
           //   repo56.shutDown();
			     repo12_34.shutDown();
		
				} finally {	
			con1234.close();
			con34.close();
			con12.close();
			
				}
			}

}
