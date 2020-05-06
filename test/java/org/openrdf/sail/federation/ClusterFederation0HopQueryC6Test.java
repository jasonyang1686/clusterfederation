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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author vagrant
 */
public class ClusterFederation0HopQueryC6Test {
	private static final Logger log = Logger.getLogger(ClusterFederation0HopQueryC6Test.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";

	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";

	
	private static final String repositoryID_1234="Federation1234";
	
	private static final String repository_cluster_ID_1234="Federation12_34";
	
	public static void main(String[] args) throws Exception {
		//federation of cluster federations repository 123456
				HTTPRepository repo12_34 = new HTTPRepository(SESAME_SERVER_2, repository_cluster_ID_1234);
				//federation repository 123456
				HTTPRepository repo1234 = new HTTPRepository(SESAME_SERVER_2, repositoryID_1234);
				repo12_34.initialize();	
				repo1234.initialize();	
				

				RepositoryConnection con1234=null;

				RepositoryConnection con12_34=null;
				
				try {
					log.info("Connecting to SailRepository.");
		      	
	   			con12_34=repo12_34.getConnection();				
            
	   			con1234=repo1234.getConnection();
	   			
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					String query_1 = "PREFIX linkedmdb: <http://data.linkedmdb.org/resource/movie/> "+
							"prefix owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX dcterms: <http://purl.org/dc/terms/> "+
							"PREFIX purl: <http://purl.org/dc/terms/> "+
							"PREFIX nytimes: <http://data.nytimes.com/elements/> "+
							"SELECT ?actor ?nytURI "+
							"WHERE "+ 
							"{ "+
						//	"?film linkedmdb:actor 			?actor . "+
							"?actor owl:sameAs 			?dbpediaURI. "+
							"?nytURI owl:sameAs 			?dbpediaURI . "+
						//	"?nytURI nytimes:topicPage 		?news ; "+
						//	"	nytimes:number_of_variants 	?variants; "+
						//	"	nytimes:associated_article_count ?articleCount; "+
						//	"	nytimes:first_use 		?first_use; "+
						//	"	nytimes:latest_use 		?latest_use . "+
						//	"?film purl:title 			?filmTitle . "+
							"} "+
							"ORDER BY (?actor) ";
					
					TupleQuery tupleQuery1 = con12_34.prepareTupleQuery(QueryLanguage.SPARQL, query_1);
					TupleQueryResult result1= tupleQuery1.evaluate();
					
					BindingSet bindingSet1 = null;	
					List <String> actor_1 = new ArrayList<String>();
					List <String> nytURI_1 = new ArrayList<String>();
					List <String> nytLocation_1 = new ArrayList<String>();
					
					while(result1.hasNext()){
						bindingSet1 = result1.next();
						if(bindingSet1.getValue("actor").toString().contains("data.linkedmdb")){
							if(bindingSet1.getValue("nytURI").toString().contains("data.nytimes.com")){
						actor_1.add(bindingSet1.getValue("actor").toString());
						System.out.println("actor :"+bindingSet1.getValue("actor").toString());
						
						
						nytURI_1.add(bindingSet1.getValue("nytURI").toString());
						System.out.println("nytURI: "+bindingSet1.getValue("nytURI").toString());
						}
						}
					}
				//	System.out.println(count);
					final long phase_1 = System.currentTimeMillis();
					
					System.out.println("phase_1_time: "+(phase_1-start));
					System.out.println("phase_1_size: "+(nytURI_1.size())+","+(actor_1.size()));
					
					List <String> nytURI_2 = new ArrayList<String>();
					List <String> actor_2 = new ArrayList<String>();
					
					if (!actor_1.isEmpty()){

                for (int i =0;i<actor_1.size();i++){
	
               	String query_2 = "PREFIX linkedmdb: <http://data.linkedmdb.org/resource/movie/> "+
               			 "prefix owl: <http://www.w3.org/2002/07/owl#> "+
               			 "PREFIX dcterms: <http://purl.org/dc/terms/> "+
               			 "PREFIX purl: <http://purl.org/dc/terms/> "+
               			 "PREFIX nytimes: <http://data.nytimes.com/elements/> "+
               			 "SELECT ?filmTitle ?news ?variants ?articleCount ?first_use ?latest_use "+
               			 "WHERE "+ 
               			 "{ "+
               	//		 "?actor owl:sameAs 			?dbpediaURI. "+
               	//		 "?nytURI owl:sameAs 			?dbpediaURI . "+ 
               			 "<"+nytURI_1.get(i)+"> nytimes:topicPage 		?news ; "+
               			 "	nytimes:number_of_variants 	?variants; "+
               			 "	nytimes:associated_article_count ?articleCount; "+
               			 "	nytimes:first_use 		?first_use; "+
               			 "	nytimes:latest_use 		?latest_use . "+
               			 "?film linkedmdb:actor <"+actor_1.get(i)+"> . "+
               			 "?film purl:title 			?filmTitle . "+
               			 "} "+
               			 " ";
						
						TupleQuery tupleQuery2 = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
					   TupleQueryResult result2= tupleQuery2.evaluate();
					  
						
						BindingSet bindingSet2 = null;	
					   
						while(result2.hasNext()){
							bindingSet2 = result2.next();						
								System.out.println(bindingSet2.getValue("filmTitle").toString());
								actor_2.add(actor_1.get(i));
								nytURI_2.add(nytURI_1.get(i));
								
								
						}
                }
						final long phase_2 = System.currentTimeMillis();
						
						System.out.println("phase_2_time: "+(phase_2-phase_1));
					//	System.out.println("phase_2_size: "+actor_2.size());							
					}
					
	              repo1234.shutDown();
				     repo12_34.shutDown();
					
				}finally {
					//	out.close();	
					con1234.close();
					con12_34.close();			
						}
	}
}
