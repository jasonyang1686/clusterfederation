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
public class ClusterFederation0HopQueryC9Test {
	private static final Logger log = Logger.getLogger(ClusterFederation0HopQueryC9Test.class);

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
		//federation of cluster federations repository 1234
				HTTPRepository repo12_34 = new HTTPRepository(SESAME_SERVER_2, repository_cluster_ID_1234);
				//federation repository 1234
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
					String query_1 ="Prefix dbpedia: <http://dbpedia.org/ontology/> "+
							"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"Prefix owl: <http://www.w3.org/2002/07/owl#> "+
							"Prefix drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"SELECT * WHERE "+
							"{ "+
							"?Drug rdf:type dbpedia:Drug . "+
							"?drugbankDrug owl:sameAs ?Drug . "+
							"OPTIONAL "+
							"{ "+
							"?drugbankDrug  drugbank:affectedOrganism 'Humans and other mammals'; "+
							"drugbank:description ?description ; "+
							"drugbank:structure ?structure ; "+
							"drugbank:casRegistryNumber ?casRegistryNumber "+
							"} "+
							"} "+
							"ORDER BY (?drugbankDrug) "+
							"LIMIT 100 ";
					
					TupleQuery tupleQuery1 = con12_34.prepareTupleQuery(QueryLanguage.SPARQL, query_1);
					TupleQueryResult result1= tupleQuery1.evaluate();
					
					BindingSet bindingSet1 = null;	
					List <String> drug_1 = new ArrayList<String>();
					List <String> drugbankDrug_1 = new ArrayList<String>();
					List <String> drgBnkDrg_1 = new ArrayList<String>();
					
					List <String> X_3 = new ArrayList<String>();
					List <String> Y_3 = new ArrayList<String>();

					while(result1.hasNext()){
						bindingSet1 = result1.next();	
						drugbankDrug_1.add(bindingSet1.getValue("drugbankDrug").toString());
				//		System.out.println(bindingSet1.getValue("drugbankDrug").toString());

					}


					final long phase_1 = System.currentTimeMillis();
					
					System.out.println("phase 1 time: "+(phase_1-start));
					System.out.println("phase_1_size: "+(drugbankDrug_1.size()));
					List <String> drugbankdrug_2 = new ArrayList<String>();
					if (!drugbankDrug_1.isEmpty()){
					for (int i=0;i<drugbankDrug_1.size();i++){
							
						String query_2 ="Prefix dbpedia: <http://dbpedia.org/ontology/> "+
								"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
								"Prefix owl: <http://www.w3.org/2002/07/owl#> "+
								"Prefix drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
								"SELECT (COUNT(*) as ?count ) WHERE "+
								"{ "+
								"?InteractionName drugbank:interactionDrug1 <"+drugbankDrug_1.get(i)+"> ."+
								"?InteractionName drugbank:interactionDrug2 ?drugbankDrug2 . "+
								"?InteractionName drugbank:text ?IntEffect "+
								"OPTIONAL "+
								"{ "+
								"?drugbankDrug  drugbank:affectedOrganism 'Humans and other mammals'; "+
								"drugbank:description ?description ; "+
								"drugbank:structure ?structure ; "+
								"drugbank:casRegistryNumber ?casRegistryNumber "+
								"} "+
								"} "+
								"ORDER BY (?drugbankDrug) "+
								"LIMIT 100 ";
					
					TupleQuery tupleQuery2 = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
				   TupleQueryResult result2= tupleQuery2.evaluate();

					BindingSet bindingSet2 = null;
		
	
					while(result2.hasNext()){
						bindingSet2 = result2.next();
						if(!bindingSet2.getValue("count").toString().contains("\"0\"")){
						drugbankdrug_2.add(drugbankDrug_1.get(i));
						}

						}


					}
					}
					
					final long phase_2 = System.currentTimeMillis();
					
					System.out.println("phase 2 time: "+(phase_2-phase_1));
					System.out.println("phase_2_size: "+drugbankdrug_2.size());	
	              repo1234.shutDown();
				     repo12_34.shutDown();
					
				}finally {
					//	out.close();	
					con1234.close();
					con12_34.close();			
						}
	}

				}
	

