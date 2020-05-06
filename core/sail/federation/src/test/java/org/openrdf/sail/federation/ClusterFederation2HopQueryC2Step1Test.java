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
public class ClusterFederation2HopQueryC2Step1Test {
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
	private static final String SESAME_SERVER_5="http://192.168.33.50:8080/openrdf-sesame";
	
	private static final String repositoryID_12="Federation12";

	private static final String repositoryID_34="Federation34";
	
	private static final String repositoryID_56="Federation56";
	
	private static final String repositoryID_123456="Federation12_34_56";	

	public static void main(String[] args) throws Exception {



		// federation repository 12
				Repository repo12 = new HTTPRepository(SESAME_SERVER_1, repositoryID_12);		
				repo12.initialize();	

						
		//federation repository 34
				Repository repo34 = new HTTPRepository(SESAME_SERVER_3, repositoryID_34);		
				repo34.initialize();	
				
		//federation repository 56
				Repository repo56 = new HTTPRepository(SESAME_SERVER_5, repositoryID_56);		
				repo34.initialize();	
								
				
		//federation repository 123456
				HTTPRepository repo12_34_56 = new HTTPRepository(SESAME_SERVER_2, repositoryID_123456);		
				repo12_34_56.initialize();	
				

				RepositoryConnection con123456=null;

				RepositoryConnection con12=null;
		 	   RepositoryConnection con34=null; 
		 	   RepositoryConnection con56=null;

   			
				try {

					log.info("Connecting to SailRepository.");
			      
	   			con123456=repo12_34_56.getConnection();				
            
	   			con12=repo12.getConnection();
	   			con34=repo34.getConnection();
	   			con56=repo56.getConnection();	
		
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					
					String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
							"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
							"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
							"SELECT ?drug ?keggmass ?chebiIupacName "+
							"WHERE "+ 
							"{ "+
							"?chebiDrug purl:title ?drugBankName . "+
							"?drug rdf:type drugbank:drugs . "+
							"?keggDrug bio2RDF:mass ?keggmass . "+
							"?drug drugbank:genericName ?drugBankName . "+
							"?drug drugbank:keggCompoundId ?keggDrug . "+
							"?chebiDrug chebi:iupacName ?chebiIupacName . "+

							"OPTIONAL { "+
							 "           ?drug drugbank:inchiIdentifier ?drugbankInchi . "+
							"			?chebiDrug bio2RDF:inchi ?chebiInchi . "+
							"			FILTER (?drugbankInchi = ?chebiInchi)  "+
							"		} "+

							"} ";
				//	System.out.println(query);
					int count=0;
					TupleQuery tupleQuery12 = con12.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result12= tupleQuery12.evaluate();
					
					System.out.println("phase1_12:");	
					BindingSet bindingSet = null;	
					while(result12.hasNext()){
						bindingSet = result12.next();	
						Value valueOfX = bindingSet.getValue("drug");
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
						Value valueOfX = bindingSet.getValue("drug");
         //         System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					
					final long phase_1_34 = System.currentTimeMillis();	
					System.out.println("phase 1_34 time: "+(phase_1_34-phase_1_12));
					
					TupleQuery tupleQuery56 = con56.prepareTupleQuery(QueryLanguage.SPARQL, query);
				   TupleQueryResult result56= tupleQuery56.evaluate();
					
					System.out.println("phase1_56:");
					count =0;
					while(result56.hasNext()){
						bindingSet = result56.next();
						Value valueOfX = bindingSet.getValue("drug");
         //         System.out.println("X: "+valueOfX);	
						count++;	
					}
					System.out.println("result size: "+count);
					
					final long phase_1_56 = System.currentTimeMillis();	
					System.out.println("phase 1_56 time: "+(phase_1_56-phase_1_34));
					
					System.out.println("phase2_0hop");
					
					String query0Hop ="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
							"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
							"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
							"SELECT ?drug ?chebiDrug  "+
							"WHERE "+ 
							"{ "+
		//					"?drug rdf:type drugbank:drugs . "+
							"?drug drugbank:keggCompoundId ?keggDrug . "+
							"?keggDrug bio2RDF:mass ?keggmass . "+
							"?drug drugbank:genericName ?drugBankName . "+
							"?chebiDrug purl:title ?drugBankName . "+
		//					"?chebiRef bio2RDF:xRef ?chebiDrug . "+
		//					"?chebiRef chebi:iupacName ?chebiIupacName . "+

		//					"OPTIONAL { "+
		//					 "           ?drug drugbank:inchiIdentifier ?drugbankInchi . "+
		//					"			?chebiDrug bio2RDF:inchi ?chebiInchi . "+
		//					"			FILTER (?drugbankInchi = ?chebiInchi)  "+
		//					"		} "+

							"} ";
					
					
				
					TupleQuery tupleQuery0Hop = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query0Hop);
				   TupleQueryResult result0Hop= tupleQuery0Hop.evaluate();
				   
					count =0;
					List<String> drug = new ArrayList<String>();
					List<String> chebiDrug = new ArrayList<String>();
					List<String> locationName = new ArrayList<String>();
					
					while(result0Hop.hasNext()){
						bindingSet = result0Hop.next();
						Value valueOfX = bindingSet.getValue("drug");
						Value valueOfY = bindingSet.getValue("chebiDrug");
						if(valueOfY.toString().contains("http://bio2rdf.org/cpd")){
						drug.add(valueOfX.toString());
						chebiDrug.add(valueOfY.toString());					
						count++;	
						}
					}
					System.out.println("result size: "+count);
				   				
					final long phase_2_0 = System.currentTimeMillis();					

					System.out.println("phase 2_0Hop time: "+(phase_2_0-phase_1_56));
					
					FileOutputStream fosDrug = new FileOutputStream("/home/vagrant/results/drug");
					OutputStreamWriter oswDrug = new OutputStreamWriter(fosDrug);
					BufferedWriter bwDrug = new BufferedWriter(oswDrug);
					
					FileOutputStream fosChebiDrug = new FileOutputStream("/home/vagrant/results/chebiDrug");
					OutputStreamWriter oswChebiDrug = new OutputStreamWriter(fosChebiDrug);
					BufferedWriter bwChebiDrug = new BufferedWriter(oswChebiDrug);

					for (int i=0;i<drug.size();i++){
						bwDrug.write(drug.get(i)+"\n");
						bwChebiDrug.write(chebiDrug.get(i)+"\n");
					}
				  bwDrug.close();
				  bwChebiDrug.close();
				  				  
              repo12.shutDown();
              repo34.shutDown();
              repo56.shutDown();
			     repo12_34_56.shutDown();
		
				} finally {	
			con123456.close();
			con56.close();
			con34.close();
			con12.close();
			
				}
			}

}