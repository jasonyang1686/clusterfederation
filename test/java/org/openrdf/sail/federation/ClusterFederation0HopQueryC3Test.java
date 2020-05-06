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
public class ClusterFederation0HopQueryC3Test {
	private static final Logger log = Logger.getLogger(ClusterFederation0HopQueryC3Test.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";

	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";

	
	private static final String repositoryID_123456="Federation123456";
	
	private static final String repository_cluster_ID_123456="Federation12_34_56";
	
	public static void main(String[] args) throws Exception {
		//federation of cluster federations repository 123456
				HTTPRepository repo12_34_56 = new HTTPRepository(SESAME_SERVER_2, repository_cluster_ID_123456);
				//federation repository 123456
				HTTPRepository repo123456 = new HTTPRepository(SESAME_SERVER_2, repositoryID_123456);
				repo12_34_56.initialize();	
				repo123456.initialize();	
				

				RepositoryConnection con123456=null;

				RepositoryConnection con12_34_56=null;
				
				try {
					log.info("Connecting to SailRepository.");
		      	
	   			con12_34_56=repo12_34_56.getConnection();				
            
	   			con123456=repo123456.getConnection();
	   			
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					String query_1 = "PREFIX foaf: 		<http://xmlns.com/foaf/0.1/> "+
							"PREFIX geonames: 	<http://www.geonames.org/ontology#> "+
							"PREFIX mo:   		<http://purl.org/ontology/mo/> "+
							"PREFIX nytimes:         <http://data.nytimes.com/elements/> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"SELECT DISTINCT ?artist ?locationName ?nytLocation "+
							"WHERE { "+
						//	"	     ?artist a mo:MusicArtist ; "+
						//	"         foaf:name ?name ; "+
							"	       ?artist	 foaf:based_near ?location . "+
							"         ?nytLocation owl:sameAs ?location. "+
							"         ?location geonames:parentFeature ?locationName . "+
						//	"         ?locationName geonames:name ?anylocation . "+

						//	"         ?nytLocation nytimes:topicPage ?news "+
							"OPTIONAL "+
							"{ "+
							"         ?locationName geonames:name 'Islamic Republic of Afghanistan' . "+
							"} "+
							"} ";
					
					TupleQuery tupleQuery1 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_1);
					TupleQueryResult result1= tupleQuery1.evaluate();
					
					BindingSet bindingSet1 = null;	
					List <String> locationName_1 = new ArrayList<String>();
					List <String> artist_1 = new ArrayList<String>();
					List <String> nytLocation_1 = new ArrayList<String>();
					
					while(result1.hasNext()){
						bindingSet1 = result1.next();	
						locationName_1.add(bindingSet1.getValue("locationName").toString());
					//	System.out.println(bindingSet1.getValue("locationName").toString());
						artist_1.add(bindingSet1.getValue("artist").toString());
					//	System.out.println(bindingSet1.getValue("artist").toString());
						nytLocation_1.add(bindingSet1.getValue("nytLocation").toString());
					//	System.out.println(bindingSet1.getValue("nytLocation").toString());

					}
				//	System.out.println(count);
					final long phase_1 = System.currentTimeMillis();
					
					System.out.println("phase_1_time: "+(phase_1-start));
					System.out.println("phase_1_size: "+(locationName_1.size()));
					
					List <String> locationName_2 = new ArrayList<String>();
					List <String> artist_2 = new ArrayList<String>();

					if (!locationName_1.isEmpty()){

                for (int i =0;i<locationName_1.size();i++){
	
							String query_2 ="PREFIX foaf: 		<http://xmlns.com/foaf/0.1/> "+
									"PREFIX geonames: 	<http://www.geonames.org/ontology#> "+
									"PREFIX mo:   		<http://purl.org/ontology/mo/> "+
									"PREFIX nytimes:         <http://data.nytimes.com/elements/> "+
									"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
									"SELECT (COUNT(*) as ?count ) "+
									"WHERE { "+
									"	     <"+artist_1.get(i)+"> a mo:MusicArtist ; "+
									"         foaf:name ?name . "+
						//			"		 foaf:based_near ?location . "+
						//			"         ?location geonames:parentFeature ?locationName . "+
									"         <"+locationName_1.get(i)+"> geonames:name ?anylocation . "+
						//			"         ?nytLocation owl:sameAs ?location. "+
									"         <"+nytLocation_1.get(i)+"> nytimes:topicPage ?news "+
									"OPTIONAL "+
									"{ "+
									"         ?locationName geonames:name 'Islamic Republic of Afghanistan' . "+
									"} "+
									"} ";
						
						TupleQuery tupleQuery2 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
					   TupleQueryResult result2= tupleQuery2.evaluate();
					  
						
						BindingSet bindingSet2 = null;	
					   
						while(result2.hasNext()){
							bindingSet2 = result2.next();
						//	drug_2.add(bindingSet2.getValue("drug").toString());
							if(!bindingSet2.getValue("count").toString().contains("\"0\"")){
								artist_2.add(artist_1.get(i));
								locationName_2.add(locationName_1.get(i));
								
								}
						}
                }
						
					}
				   final   long phase_2 = System.currentTimeMillis();
						
					System.out.println("phase_2_time: "+(phase_2-phase_1));
					System.out.println("phase_2_size: "+locationName_2.size());	
					
					String query_3 = "PREFIX foaf: 		<http://xmlns.com/foaf/0.1/> "+
							"PREFIX geonames: 	<http://www.geonames.org/ontology#> "+
							"PREFIX mo:   		<http://purl.org/ontology/mo/> "+
							"PREFIX nytimes:         <http://data.nytimes.com/elements/> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"SELECT DISTINCT ?location ?anylocation "+
							"WHERE { "+
						//	"	     ?artist a mo:MusicArtist ; "+
						//	"         foaf:name ?name ; "+
						//	"	       ?artist	 foaf:based_near ?location . "+
						//	"         ?nytLocation owl:sameAs ?location. "+
							"         ?location geonames:parentFeature ?locationName . "+
							"         ?locationName geonames:name ?anylocation . "+

						//	"         ?nytLocation nytimes:topicPage ?news "+
							"OPTIONAL "+
							"{ "+
							"         ?locationName geonames:name 'Islamic Republic of Afghanistan' . "+
							"} "+
							"} ";
					
					TupleQuery tupleQuery3 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_3);
					TupleQueryResult result3= tupleQuery3.evaluate();
					
					BindingSet bindingSet3 = null;	
					List <String> location_3 = new ArrayList<String>();
					List <String> anylocation_3 = new ArrayList<String>();
					
					while(result3.hasNext()){
						bindingSet3 = result3.next();	
						location_3.add(bindingSet3.getValue("location").toString());
			//			System.out.println(bindingSet3.getValue("location").toString());
						anylocation_3.add(bindingSet3.getValue("anylocation").toString());
					//	System.out.println(bindingSet1.getValue("artist").toString());

					}
				//	System.out.println(count);
					final long phase_3 = System.currentTimeMillis();
					
					System.out.println("phase_3_time: "+(phase_3-phase_2));
					System.out.println("phase_3_size: "+(location_3.size()));
					System.out.println("phase_3_size: "+(anylocation_3.size()));
					
					List <String> location_4 = new ArrayList<String>();
					List <String> anylocation_4 = new ArrayList<String>();

					
					if (!location_3.isEmpty()){

                for (int i =0;i<location_3.size();i++){
	
							String query_4 ="PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
									"PREFIX geonames: <http://www.geonames.org/ontology#> "+
									"PREFIX mo:   <http://purl.org/ontology/mo/> "+
									"PREFIX nytimes:  <http://data.nytimes.com/elements/> "+
									"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
									"SELECT (COUNT(*) as ?count ) "+
									"WHERE { "+
									"		  ?artist foaf:based_near <"+location_3.get(i)+"> . "+
									"	     ?artist a mo:MusicArtist . "+
									"       ?artist  foaf:name ?name . "+		
						//			" <"+location_3.get(i)+"> geonames:parentFeature ?locationName . "+
						//			"        ?locationName  geonames:name "+anylocation_3.get(i)+" . "+
									"         ?nytLocation owl:sameAs <"+location_3.get(i)+"> . "+
									"         ?nytLocation nytimes:topicPage ?news ."+
									"OPTIONAL "+ 
									"{ "+
									"         ?locationName geonames:name 'Islamic Republic of Afghanistan' . "+
									"} "+
									"} ";
	
						TupleQuery tupleQuery4 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_4);
					   TupleQueryResult result4= tupleQuery4.evaluate();
					  
						
						BindingSet bindingSet4 = null;	
					   
						while(result4.hasNext()){
							bindingSet4 = result4.next();
						//	drug_2.add(bindingSet2.getValue("drug").toString());
							if(!bindingSet4.getValue("count").toString().contains("\"0\"")){
								anylocation_4.add(anylocation_3.get(i));
								location_4.add(location_3.get(i));
								
								}
						}
                }
						
					}	
               final long phase_4 = System.currentTimeMillis();
					
						System.out.println("phase_4_time: "+(phase_4-phase_3));
						System.out.println("phase_4_size: "+location_4.size());	
					
					
	              repo123456.shutDown();
				     repo12_34_56.shutDown();
					
				}finally {
					//	out.close();	
					con123456.close();
					con12_34_56.close();			
						}
	}
}
