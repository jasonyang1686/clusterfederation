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
public class ClusterFederation0HopQueryC10Test {
	
	private static final Logger log = Logger.getLogger(ClusterFederation0HopQueryC10Test.class);

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
					String query_1 ="PREFIX tcga: <http://tcga.deri.ie/schema/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX dbpedia: <http://dbpedia.org/ontology/> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX purl: <http://purl.org/dc/terms/> "+
							"SELECT  DISTINCT ?patient ?country ?popDensity "+ 
							"WHERE "+
							"{ "+
					//	   "?uri tcga:bcr_patient_barcode 			?patient . "+
					//		"?patient tcga:gender ?gender."+
							"?patient dbpedia:country 				?country. "+
							"?country dbpedia:populationDensity 		?popDensity. "+
					//		"?patient tcga:bcr_drug_barcode 			?drugbcr. "+
					//		"?drugbcr tcga:drug_name 				?drugName. "+
					//		"?drgBnkDrg  drugbank:genericName 		?drugName. "+
							"} ";
					
					TupleQuery tupleQuery1 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_1);
					TupleQueryResult result1= tupleQuery1.evaluate();
					
					BindingSet bindingSet1 = null;	
					List <String> patient_1 = new ArrayList<String>();
					List <String> drugbcr_1 = new ArrayList<String>();

					
					List <String> X_3 = new ArrayList<String>();
					List <String> Y_3 = new ArrayList<String>();

					while(result1.hasNext()){
						bindingSet1 = result1.next();
						if(bindingSet1.getValue("patient").toString().contains("http://tcga.deri.ie/")){
						patient_1.add(bindingSet1.getValue("patient").toString());
						System.out.println(bindingSet1.getValue("patient").toString());
						}
					}
				//	System.out.println(count);
					final long phase_1 = System.currentTimeMillis();
					
					System.out.println("phase_1_time: "+(phase_1-start));
					System.out.println("phase_1_size: "+(patient_1.size()));
					
					List <String> patient_2 = new ArrayList<String>();
					
					if (!patient_1.isEmpty()){
						for (int i=0;i<patient_1.size();i++){
								
							String query_2 ="PREFIX tcga: <http://tcga.deri.ie/schema/> "+
									"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
									"PREFIX dbpedia: <http://dbpedia.org/ontology/> "+
									"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
									"PREFIX purl: <http://purl.org/dc/terms/> "+
									"SELECT (COUNT(*) as ?count ) "+
									"WHERE "+
									"{ "+
								   "?uri tcga:bcr_patient_barcode <"+patient_1.get(i)+"> ."+
									"<"+patient_1.get(i)+"> tcga:gender ?gender."+
									"<"+patient_1.get(i)+"> tcga:bcr_drug_barcode ?drugbcr. "+
									"?drugbcr tcga:drug_name 				?drugName. "+
									"?drgBnkDrg  drugbank:genericName 		?drugName. "+
									"?drgBnkDrg  drugbank:indication 		?indication. "+
									"?drgBnkDrg  drugbank:chemicalFormula 	?formula. "+
									"?drgBnkDrg 	drugbank:keggCompoundId 	?compound . "+									
									"} ";
						
						TupleQuery tupleQuery2 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
					   TupleQueryResult result2= tupleQuery2.evaluate();
					   
						BindingSet bindingSet2 = null;	
					   
						while(result2.hasNext()){
							bindingSet2 = result2.next();
					//		System.out.println(bindingSet2.getValue("count").toString());
							if(!bindingSet2.getValue("count").toString().contains("\"0\"")){
								patient_2.add(patient_1.get(i));							
							}
						}
						
						}
						
					}
					final long phase_2 = System.currentTimeMillis();
					
					System.out.println("phase 2 time: "+(phase_2-phase_1));
					System.out.println("phase_2_size: "+patient_2.size());
					
					String query_3 ="PREFIX tcga: <http://tcga.deri.ie/schema/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX dbpedia: <http://dbpedia.org/ontology/> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX purl: <http://purl.org/dc/terms/> "+
							"SELECT  DISTINCT  ?drugbcr ?drgBnkDrg  "+ 
							"WHERE "+
							"{ "+
			//				"?uri tcga:bcr_patient_barcode 			?patient . "+
			//				"?patient tcga:gender 					?gender. "+
		//					"?patient dbpedia:country 				?country. "+
			//				"?country dbpedia:populationDensity 		?popDensity. "+
		//					"?patient tcga:bcr_drug_barcode 			?drugbcr. "+
							"?drugbcr tcga:drug_name 				?drugName. "+
							"?drgBnkDrg  drugbank:genericName 		?drugName. "+
		//					"?drgBnkDrg  drugbank:indication 		?indication. "+
		//					"?drgBnkDrg  drugbank:chemicalFormula 	?formula. "+
		//					"?drgBnkDrg 	drugbank:keggCompoundId 	?compound . "+
							"} ";
					
					TupleQuery tupleQuery3 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_3);
					TupleQueryResult result3= tupleQuery3.evaluate();
					
					BindingSet bindingSet3 = null;	
					List <String> drugbcr_3 = new ArrayList<String>();
					List <String> drgBnkDrg_3 = new ArrayList<String>();
					
					while(result3.hasNext()){
						bindingSet3 = result3.next();	
						drugbcr_3.add(bindingSet3.getValue("drugbcr").toString());
			//			System.out.println(bindingSet3.getValue("location").toString());
						drgBnkDrg_3.add(bindingSet3.getValue("drgBnkDrg").toString());
					//	System.out.println(bindingSet1.getValue("artist").toString());

					}
				//	System.out.println(count);
					final long phase_3 = System.currentTimeMillis();
					
					System.out.println("phase_3_time: "+(phase_3-phase_2));
					System.out.println("phase_3_size: "+(drugbcr_3.size()));
					System.out.println("phase_3_size: "+(drgBnkDrg_3.size()));
					
					List <String> drgBnkDrg_4 = new ArrayList<String>();
					List <String> drugbcr_4 = new ArrayList<String>();

					
					if (!drgBnkDrg_3.isEmpty()){

                for (int i =0;i<drgBnkDrg_3.size();i++){
	
    					String query_4 ="PREFIX tcga: <http://tcga.deri.ie/schema/> "+
   							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
   							"PREFIX dbpedia: <http://dbpedia.org/ontology/> "+
   							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
   							"PREFIX purl: <http://purl.org/dc/terms/> "+
   							"SELECT  (COUNT(*) as ?count )  "+ 
   							"WHERE "+
   							"{ "+
   							"?patient tcga:bcr_drug_barcode 	<"+drugbcr_3.get(i)+">. "+
   							"?uri tcga:bcr_patient_barcode 			?patient . "+
   							"?patient tcga:gender 					?gender. "+
   							"?patient dbpedia:country 				?country. "+
   							"?country dbpedia:populationDensity 		?popDensity. "+
   		//					"?drugbcr tcga:drug_name 				?drugName. "+
   		//					"?drgBnkDrg  drugbank:genericName 		?drugName. "+
   							"<"+drgBnkDrg_3.get(i)+">  drugbank:indication 		?indication. "+
   							"<"+drgBnkDrg_3.get(i)+">  drugbank:chemicalFormula 	?formula. "+
   							"<"+drgBnkDrg_3.get(i)+"> 	drugbank:keggCompoundId 	?compound . "+
   							"} ";
	
						TupleQuery tupleQuery4 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_4);
					   TupleQueryResult result4= tupleQuery4.evaluate();
					  
						
						BindingSet bindingSet4 = null;	
					   
						while(result4.hasNext()){
							bindingSet4 = result4.next();
						//	drug_2.add(bindingSet2.getValue("drug").toString());
							if(!bindingSet4.getValue("count").toString().contains("\"0\"")){
								drgBnkDrg_4.add(drgBnkDrg_3.get(i));
								drugbcr_4.add(drugbcr_3.get(i));
								
								}
						}
                }
						
					}	
               final long phase_4 = System.currentTimeMillis();
					
						System.out.println("phase_4_time: "+(phase_4-phase_3));
						System.out.println("phase_4_size: "+drugbcr_4.size());	
					
					
	              repo123456.shutDown();
				     repo12_34_56.shutDown();
					
				}finally {
					//	out.close();	
					con123456.close();
					con12_34_56.close();			
						}
	}

}
