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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.openrdf.model.Value;
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
public class ClusterFederation2HopQueryC9Step3Test {
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//

	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	
	private static final String repositoryID_1234="Federation12_34";	

	public static void main(String[] args) throws Exception {
		
		List<String> InteractionName = new ArrayList<String>();
		
		BufferedReader brInteractionName = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/InteractionName")));
      String lineInteractionName = brInteractionName.readLine();
      InteractionName.add(lineInteractionName);
		
		while(lineInteractionName!=null){
			InteractionName.add(lineInteractionName);
			lineInteractionName = brInteractionName.readLine();
		}
		
		brInteractionName.close();			
		
		//federation repository 1234
				HTTPRepository repo12_34 = new HTTPRepository(SESAME_SERVER_2, repositoryID_1234);		
				repo12_34.initialize();				

				RepositoryConnection con1234=null;  
   			
				try {

					log.info("Connecting to SailRepository.");
			      
	   			con1234=repo12_34.getConnection();				
		
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					int count=0;
				
					for (int i=0; i<InteractionName.size();i++){
						
					
						String query ="Prefix dbpedia: <http://dbpedia.org/ontology/> "+
								"Prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
								"Prefix owl: <http://www.w3.org/2002/07/owl#> "+
								"Prefix drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
								"SELECT ?drugbankDrug2 ?IntEffect WHERE "+
								"{ "+

								" <"+InteractionName.get(i)+"> drugbank:interactionDrug2 ?drugbankDrug2 . "+
								" <"+InteractionName.get(i)+"> drugbank:text ?IntEffect "+
						//		"OPTIONAL "+
						//		"{ "+
						//		"?drugbankDrug  drugbank:affectedOrganism 'Humans and other mammals'; "+
						//		"drugbank:description ?description ; "+
						//		"drugbank:structure ?structure ; "+
						//		"drugbank:casRegistryNumber ?casRegistryNumber "+
						//		"} "+
								"} "+
						//		"ORDER BY (?drugbankDrug) "+
								"LIMIT 100 ";
				//	System.out.println(query);

					TupleQuery tupleQuery = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result = tupleQuery.evaluate();
					
					BindingSet bindingSet = null;	
					while(result.hasNext()){
						bindingSet = result.next();	
						Value valueOfX = bindingSet.getValue("drugbankDrug2");
                  System.out.println(valueOfX.toString());	
						count++;	
					}
					}
					System.out.println("result size: "+count);
					
					final long phase_2_3 = System.currentTimeMillis();	
					
					System.out.println("phase 2_2Hop time: "+(phase_2_3-start));
										
			     repo12_34.shutDown();
					
				} finally {	
			con1234.close();
			
				}
				
			}
}
