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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
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
public class ClusterFederation2HopQueryC2Step2Test {
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//

	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	
	private static final String repositoryID_123456="Federation12_34_56";	

	public static void main(String[] args) throws Exception {
		
		List<String> drug = new ArrayList<String>();
		List<String> chebiDrug = new ArrayList<String>();
	//	List<String> locationName = new ArrayList<String>();
		
		BufferedReader brDrug = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/drug")));
      String lineDrug = brDrug.readLine();
      drug.add(lineDrug);
      
		BufferedReader brChebiDrug = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/chebiDrug")));
      String lineChebiDrug = brChebiDrug.readLine(); 
      chebiDrug.add(lineChebiDrug);
      
		while(lineDrug!=null){
			drug.add(lineDrug);
			lineDrug = brDrug.readLine();
		}
		
		while(lineChebiDrug!=null){
			chebiDrug.add(lineChebiDrug);
			lineChebiDrug = brChebiDrug.readLine();
		}
		
		brDrug.close();
		brChebiDrug.close();	
		
		System.out.println(drug.get(0));
		System.out.println(chebiDrug.get(0));		
		//federation repository 1234
				HTTPRepository repo12_34_56 = new HTTPRepository(SESAME_SERVER_2, repositoryID_123456);		
				repo12_34_56.initialize();				

				RepositoryConnection con123456=null;  
   			
				try {

					log.info("Connecting to SailRepository.");
			      
	   			con123456=repo12_34_56.getConnection();				
		
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					int count=0;
					List<String> chebiRef = new ArrayList<String>();
					for (int i=0; i<drug.size();i++){
						
					
						String query ="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
								"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
								"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
								"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
								"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
								"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
								"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
								"SELECT ?chebiRef  "+
								"WHERE "+ 
								"{ "+
								"<"+drug.get(i)+"> rdf:type drugbank:drugs . "+
			//					"?drug drugbank:keggCompoundId ?keggDrug . "+
			//					"?keggDrug bio2RDF:mass ?keggmass . "+
			//					"?drug drugbank:genericName ?drugBankName . "+
			//					"?chebiDrug purl:title ?drugBankName . "+
								"?chebiRef bio2RDF:xRef <"+ chebiDrug.get(i)+">. "+
			//					"?chebiRef chebi:iupacName ?chebiIupacName . "+

								"OPTIONAL { "+
								"<"+drug.get(i)+"> drugbank:inchiIdentifier ?drugbankInchi . "+
								"<"+chebiDrug.get(i)+"> bio2RDF:inchi ?chebiInchi . "+
								"			FILTER (?drugbankInchi = ?chebiInchi)  "+
								"		} "+

								"} ";

					TupleQuery tupleQuery = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result = tupleQuery.evaluate();
					
					BindingSet bindingSet = null;	
					while(result.hasNext()){
						bindingSet = result.next();	
						Value valueOfX = bindingSet.getValue("chebiRef");
						System.out.println(valueOfX.toString());
						chebiRef.add(valueOfX.toString());	
						count++;	
					}
					}
					System.out.println("result size: "+count);
					
					final long phase_2_2 = System.currentTimeMillis();	
					
					System.out.println("phase 2_1Hop time: "+(phase_2_2-start));
					
					FileOutputStream foschebiRef= new FileOutputStream("/home/vagrant/results/chebiRef");
					OutputStreamWriter oswchebiRef = new OutputStreamWriter(foschebiRef);
					BufferedWriter bwchebiRef = new BufferedWriter(oswchebiRef);
					
					for (int i=0;i<chebiRef.size();i++){
						bwchebiRef.write(chebiRef.get(i)+"\n");
		
					}
				  bwchebiRef.close();
									
			     repo12_34_56.shutDown();
					
				} finally {	
			con123456.close();
			
				}
				
			}
}
