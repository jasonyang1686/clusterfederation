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
public class ClusterFederation0HopQueryC2Test {
	private static final Logger log = Logger.getLogger(ClusterFederation0HopQueryC2Test.class);

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
					String query_1 ="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
							"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
							"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
							"SELECT ?drug ?keggmass  "+
							"WHERE "+ 
							"{ "+
		//					"?drug rdf:type drugbank:drugs . "+
							"?drug drugbank:keggCompoundId ?keggDrug . "+
							"?keggDrug bio2RDF:mass ?keggmass . "+
		//					"?drug drugbank:genericName ?drugBankName . "+
		//					"?chebiDrug purl:title ?drugBankName . "+
		//					"?chebiRef bio2RDF:xRef ?chebiDrug . "+
		//					"?chebiRef chebi:iupacName ?chebiIupacName . "+

							"OPTIONAL { "+
							 "           ?drug drugbank:inchiIdentifier ?drugbankInchi . "+
							"			?chebiDrug bio2RDF:inchi ?chebiInchi . "+
							"			FILTER (?drugbankInchi = ?chebiInchi)  "+
							"		} "+

							"} ";
					
					TupleQuery tupleQuery1 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_1);
					TupleQueryResult result1= tupleQuery1.evaluate();
					
					BindingSet bindingSet1 = null;	
					List <String> drug_1 = new ArrayList<String>();
		
					while(result1.hasNext()){
						bindingSet1 = result1.next();	
						drug_1.add(bindingSet1.getValue("drug").toString());
			//			drugbcr_1.add(bindingSet1.getValue("drugbcr").toString());
			//			System.out.println(bindingSet1.getValue("drugbcr").toString());
			//			drgBnkDrg_1.add(bindingSet1.getValue("drgBnkDrg").toString());
			//			System.out.println(bindingSet1.getValue("drgBnkDrg").toString());
					}
				//	System.out.println(count);
					final long phase_1 = System.currentTimeMillis();
					
					System.out.println("phase_1_time: "+(phase_1-start));
					System.out.println("phase_1_size: "+(drug_1.size()));
					List <String> drug_2 = new ArrayList<String>();
					/*
					if (!drug_1.isEmpty()){
						String filter ="";
						for (int i=0;i<drug_1.size()-1;i++){
							filter+="<"+drug_1.get(i)+"> ,";
						}
						filter+="<"+drug_1.get(drug_1.size()-1)+">";
					
					
					String query_2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
							"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
							"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
							"SELECT (COUNT(*) as ?count ) "+
							"WHERE "+ 
							"{ filter (?drug in ("+filter+"))"+
							"?drug rdf:type drugbank:drugs . "+
					//		"?drug drugbank:keggCompoundId ?keggDrug . "+
					//		"?keggDrug bio2RDF:mass ?keggmass . "+
					      "?drug drugbank:genericName ?drugBankName . "+
							"?chebiDrug purl:title ?drugBankName . "+
							"?chebiDrug chebi:iupacName ?chebiIupacName . "+

							"} ";
					
					TupleQuery tupleQuery2 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
				   TupleQueryResult result2= tupleQuery2.evaluate();
					BindingSet bindingSet2 = null;	
					while(result2.hasNext()){
						bindingSet2 = result2.next();

							System.out.println(bindingSet2.getValue("count").toString());

						
						}
					}
					*/
					
					if (!drug_1.isEmpty()){
											
						for (int i=0;i<drug_1.size();i++){
	
							String query_2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
									"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
									"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
									"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
									"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
									"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
									"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
									"SELECT (COUNT(*) as ?count ) "+
									"WHERE "+ 
									"{ "+
							//		"<"+drug_1.get(i)+"> rdf:type drugbank:drugs . "+
							//		"?drug drugbank:keggCompoundId ?keggDrug . "+
							//		"?keggDrug bio2RDF:mass ?keggmass . "+
							      "<"+drug_1.get(i)+"> drugbank:genericName ?drugBankName . "+
									"?chebiDrug purl:title ?drugBankName . "+
									"?chebiDrug chebi:iupacName ?chebiIupacName . "+

									"OPTIONAL { "+
									 "           ?drug drugbank:inchiIdentifier ?drugbankInchi . "+
									"			?chebiDrug bio2RDF:inchi ?chebiInchi . "+
									"			FILTER (?drugbankInchi = ?chebiInchi)  "+
									"		} "+

									"} ";
						
						TupleQuery tupleQuery2 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
					   TupleQueryResult result2= tupleQuery2.evaluate();
					   
						List <String> Y_3 = new ArrayList<String>();
						
						BindingSet bindingSet2 = null;	
					   
						while(result2.hasNext()){
							bindingSet2 = result2.next();
							if(!bindingSet2.getValue("count").toString().contains("\"0\"")){
								System.out.println(drug_1.get(i));
								drug_2.add(drug_1.get(i));
							}
							}	

						}
							
					}
					
					final long phase_2 = System.currentTimeMillis();
					
					System.out.println("phase_2_time: "+(phase_2-phase_1));
					System.out.println("phase_2_size: "+drug_2.size());
					
					
					String query_3 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
							"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
							"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
							"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
							"SELECT ?drug ?chebiDrug "+
							"WHERE "+ 
							"{ "+
						//	"?drug rdf:type drugbank:drugs . "+
						//	"?drug drugbank:keggCompoundId ?keggDrug . "+
						//	"?keggDrug bio2RDF:mass ?keggmass . "+
							"?drug drugbank:genericName ?drugBankName . "+
							"?chebiDrug purl:title ?drugBankName . "+
						//	"?chebiDrug chebi:iupacName ?chebiIupacName . "+

							"OPTIONAL { "+
							 "           ?drug drugbank:inchiIdentifier ?drugbankInchi . "+
							"			?chebiDrug bio2RDF:inchi ?chebiInchi . "+
							"			FILTER (?drugbankInchi = ?chebiInchi)  "+
							"		} "+

							"} ";
					
					TupleQuery tupleQuery3 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_3);
					TupleQueryResult result3= tupleQuery3.evaluate();
					
					BindingSet bindingSet3 = null;	
					List <String> drug_3 = new ArrayList<String>();
					List <String> chebiDrug_3 = new ArrayList<String>();
					
					while(result3.hasNext()){
						bindingSet3 = result3.next();	
						drug_3.add(bindingSet3.getValue("drug").toString());
			//			System.out.println(bindingSet3.getValue("location").toString());
						chebiDrug_3.add(bindingSet3.getValue("chebiDrug").toString());
					//	System.out.println(bindingSet1.getValue("artist").toString());

					}
				//	System.out.println(count);
					final long phase_3 = System.currentTimeMillis();
					
					System.out.println("phase_3_time: "+(phase_3-phase_2));
					System.out.println("phase_3_size: "+(drug_3.size()));
					System.out.println("phase_3_size: "+(chebiDrug_3.size()));
					
					List <String> drug_4 = new ArrayList<String>();
					List <String> chebiDrug_4 = new ArrayList<String>();

					
					if (!drug_3.isEmpty()){

                for (int i =0;i<drug_3.size();i++){
	
    					String query_4 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
   							"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
   							"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/> "+
   							"PREFIX kegg: <http://bio2rdf.org/ns/kegg#> "+
   							"PREFIX chebi: <http://bio2rdf.org/ns/chebi#> "+
   							"PREFIX purl: <http://purl.org/dc/elements/1.1/> "+
   							"PREFIX bio2RDF: <http://bio2rdf.org/ns/bio2rdf#> "+
   							"SELECT (COUNT(*) as ?count ) "+
   							"WHERE "+ 
   							"{ "+
   							"<"+drug_3.get(i)+"> rdf:type drugbank:drugs . "+
   							"<"+drug_3.get(i)+"> drugbank:keggCompoundId ?keggDrug . "+
   						//	"?keggDrug bio2RDF:mass ?keggmass . "+
   						//	"?drug drugbank:genericName ?drugBankName . "+
   						//	"?chebiDrug purl:title ?drugBankName . "+
   						   "<"+chebiDrug_3.get(i)+"> chebi:iupacName ?chebiIupacName . "+

   							"OPTIONAL { "+
   							 "           ?drug drugbank:inchiIdentifier ?drugbankInchi . "+
   							"			?chebiDrug bio2RDF:inchi ?chebiInchi . "+
   							"			FILTER (?drugbankInchi = ?chebiInchi)  "+
   							"		} "+

   							"} ";
	
						TupleQuery tupleQuery4 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_4);
					   TupleQueryResult result4= tupleQuery4.evaluate();
					  						
						BindingSet bindingSet4 = null;	
					   
						while(result4.hasNext()){
							bindingSet4 = result4.next();
						//	drug_2.add(bindingSet2.getValue("drug").toString());
							if(!bindingSet4.getValue("count").toString().contains("\"0\"")){
								drug_4.add(drug_3.get(i));
								
								}
						}
                }
						
					}	
               final long phase_4 = System.currentTimeMillis();
					
						System.out.println("phase_4_time: "+(phase_4-phase_3));
						System.out.println("phase_4_size: "+drug_4.size());	
					
	              repo123456.shutDown();
				     repo12_34_56.shutDown();
					
				}finally {
					//	out.close();	
					con123456.close();
					con12_34_56.close();			
						}
	}
}
