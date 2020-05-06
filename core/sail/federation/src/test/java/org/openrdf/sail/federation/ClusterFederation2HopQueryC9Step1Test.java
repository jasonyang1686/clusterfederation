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
import java.util.List;

import org.apache.log4j.Logger;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

/**
 *
 * @author vagrant
 */
public class ClusterFederation2HopQueryC9Step1Test {
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
					
					String query ="Prefix dbpedia: <http://dbpedia.org/ontology/> "+
							"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"Prefix owl: <http://www.w3.org/2002/07/owl#> "+
							"Prefix drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"SELECT * WHERE "+
							"{ "+
							"?Drug rdf:type dbpedia:Drug . "+
							"?drugbankDrug owl:sameAs ?Drug . "+
							"?InteractionName drugbank:interactionDrug1 ?drugbankDrug . "+
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
				//	System.out.println(query);
					int count=0;
					TupleQuery tupleQuery12 = con12.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result12= tupleQuery12.evaluate();
					
					System.out.println("phase1_12:");	
					BindingSet bindingSet = null;	
					while(result12.hasNext()){
						bindingSet = result12.next();	
						Value valueOfX = bindingSet.getValue("Drug");
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
						Value valueOfX = bindingSet.getValue("Drug");
         //         System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					
					final long phase_1_34 = System.currentTimeMillis();	
					System.out.println("phase 1_34 time: "+(phase_1_34-phase_1_12));
					
					System.out.println("phase2_0hop");
					
					String query0Hop ="Prefix dbpedia: <http://dbpedia.org/ontology/> "+
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
					
					
				
					TupleQuery tupleQuery0Hop = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query0Hop);
				   TupleQueryResult result0Hop= tupleQuery0Hop.evaluate();
				   
					count =0;
					List<String> drugbankDrug = new ArrayList<String>();
					
					while(result0Hop.hasNext()){
						bindingSet = result0Hop.next();
						Value valueOfX = bindingSet.getValue("drugbankDrug");
						drugbankDrug.add(valueOfX.toString());
						
						count++;	
						
					}
					System.out.println("result size: "+count);
				   				
					final long phase_2_0 = System.currentTimeMillis();					

					System.out.println("phase 2_0Hop time: "+(phase_2_0-phase_1_34));
					
					FileOutputStream fosdrugbankDrug = new FileOutputStream("/home/vagrant/results/drugbankDrug");
					OutputStreamWriter oswdrugbankDrug = new OutputStreamWriter(fosdrugbankDrug);
					BufferedWriter bwdrugbankDrug = new BufferedWriter(oswdrugbankDrug);
					
					
					for (int i=0;i<drugbankDrug.size();i++){
						bwdrugbankDrug.write(drugbankDrug.get(i)+"\n");
					}
				  bwdrugbankDrug.close();
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
