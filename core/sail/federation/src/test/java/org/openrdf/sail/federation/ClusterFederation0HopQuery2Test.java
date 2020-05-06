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
public class ClusterFederation0HopQuery2Test {
	private static final Logger log = Logger.getLogger(FederationTest.class);

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
	  
   	//		FileOutputStream out = new FileOutputStream("/home/vagrant/result.csv");
   	//		SPARQLResultsCSVWriter sparqlWriter = new SPARQLResultsCSVWriter(out);
   			
				try {
					

					log.info("Connecting to SailRepository.");
			      	
	   			con12_34_56=repo12_34_56.getConnection();				
            
	   			con123456=repo123456.getConnection();
					
	   			System.out.println("start");
					final long start = System.currentTimeMillis();
		// execute query
					String query_12 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE { "+
							"?Z ub:subOrganizationOf ?Y ."+
							"?X ub:memberOf ?Z ."+
							 "}";
					
					TupleQuery tupleQuery12 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_12);
					TupleQueryResult result12= tupleQuery12.evaluate();
					
					BindingSet bindingSet12 = null;	
					List <String> Z_12 = new ArrayList<String>();
					List <String> Y_12 = new ArrayList<String>();				
					List <String> X_12 = new ArrayList<String>();
					
					List <String> X_3 = new ArrayList<String>();
					List <String> Y_3 = new ArrayList<String>();
					

					while(result12.hasNext()){
						bindingSet12 = result12.next();	
						X_12.add(bindingSet12.getValue("X").toString());
					//	System.out.println(bindingSet12.getValue("X").toString());
						Y_12.add(bindingSet12.getValue("Y").toString());
					//	System.out.println(bindingSet12.getValue("Y").toString());
						Z_12.add(bindingSet12.getValue("Z").toString());
					//	System.out.println(bindingSet12.getValue("Z").toString());
					}
					
					final long phase_2_12 = System.currentTimeMillis();
					
					System.out.println("phase 2_12 time: "+(phase_2_12-start));
					System.out.println("phase_2_12_size: "+(X_12.size()));
					
					if (!X_12.isEmpty()){
					for (int i=0;i<X_12.size();i++){
							
					String query_3 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT (COUNT(*) as ?count ) "+
							"WHERE { "+
							"<"+X_12.get(i)+">"+" ub:undergraduateDegreeFrom <"+Y_12.get(i)+"> ."+
							"<"+X_12.get(i)+">"+" rdf:type ub:GraduateStudent ."+
							 "}";
					
					TupleQuery tupleQuery3 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_3);
				   TupleQueryResult result3= tupleQuery3.evaluate();

					BindingSet bindingSet3 = null;	
					while(result3.hasNext()){
						bindingSet3 = result3.next();
						if(bindingSet3.getValue("count").toString().contains("\"1\"")){
						X_3.add(X_12.get(i));
						Y_3.add(Y_12.get(i));
						
						}
					}					
					}
					}
					System.out.println("x_3 size: "+X_3.size());
										
					final long phase_2_3 = System.currentTimeMillis();	
					
					System.out.println("phase 2_3 time: "+(phase_2_3-phase_2_12));	
					
					
					
					
					String query_13 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE { "+
							"?Z ub:subOrganizationOf ?Y ."+
							"?X ub:undergraduateDegreeFrom ?Y ."+
							 "}";
					
					TupleQuery tupleQuery13 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_13);
					TupleQueryResult result13= tupleQuery13.evaluate();
					
					BindingSet bindingSet13 = null;	
					List <String> Z_13 = new ArrayList<String>();
					List <String> Y_13 = new ArrayList<String>();				
					List <String> X_13 = new ArrayList<String>();
					
					List <String> X_2 = new ArrayList<String>();
					List <String> Z_2 = new ArrayList<String>();
					

					while(result13.hasNext()){
						bindingSet13 = result13.next();	
						X_13.add(bindingSet13.getValue("X").toString());
						Y_13.add(bindingSet13.getValue("Y").toString());
						Z_13.add(bindingSet13.getValue("Z").toString());
					}
					
					final long phase_2_13 = System.currentTimeMillis();
					
					System.out.println("phase 2_13 time: "+(phase_2_13-phase_2_3));
					System.out.println("phase_2_13_size: "+(X_13.size()));
					
					if(!X_13.isEmpty()){
					for (int i=0;i<X_13.size();i++){
			//		System.out.println(X_13.get(i));
			//		System.out.println(Z_13.get(i));				
					String query_2 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT (COUNT(*) as ?count ) "+
							"WHERE { "+
							"<"+X_13.get(i)+">"+" ub:memberOf <"+Z_13.get(i)+"> ."+
					//		"<"+X_13.get(i)+">"+" rdf:type ub:GraduateStudent ."+
							 "}";
					
					TupleQuery tupleQuery2 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_2);
				   TupleQueryResult result2= tupleQuery2.evaluate();

					BindingSet bindingSet2 = null;	
					while(result2.hasNext()){
						bindingSet2 = result2.next();
						if(bindingSet2.getValue("count").toString().contains("\"1\"")){
						X_2.add(X_13.get(i));
						Z_2.add(Z_13.get(i));
						
						}
					}	
					}
					}
					System.out.println("x_2 size: "+X_2.size());
										
					final long phase_2_2 = System.currentTimeMillis();	
					
					System.out.println("phase 2_2 time: "+(phase_2_2-phase_2_13));	
					
				
					String query_23 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE { "+
							"?X ub:memberOf ?Z ."+
							"?X ub:undergraduateDegreeFrom ?Y ."+
							 "}";
					
					TupleQuery tupleQuery23 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query_23);
				   TupleQueryResult result23= tupleQuery23.evaluate();
				   
				   List <String> Z_23 = new ArrayList<String>();
					List <String> Y_23 = new ArrayList<String>();
					List <String> X_23 = new ArrayList<String>();
					
					BindingSet bindingSet23 = null;	

					while(result23.hasNext()){
						bindingSet23 = result23.next();	
						X_23.add(bindingSet23.getValue("X").toString());
						Y_23.add(bindingSet23.getValue("Y").toString());
						Z_23.add(bindingSet23.getValue("Z").toString());
						
					}
					
					final long phase_2_23 = System.currentTimeMillis();
					
					System.out.println("phase 2_23 time: "+(phase_2_23-phase_2_2));	
					System.out.println("phase_2_23_size: "+(Y_23.size()));
					
					List <String> Y_1 = new ArrayList<String>();
					List <String> Z_1 = new ArrayList<String>();
					if(!Y_23.isEmpty()){
					for (int i=0;i<Y_23.size();i++){
				//	System.out.println(Z_23.get(i));
						
				//	System.out.println(Y_23.get(i));		
					String query_1 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT (COUNT(*) as ?count ) "+
							"WHERE { "+
							"<"+Z_23.get(i)+"> ub:subOrganizationOf <"+Y_23.get(i)+"> ."+
							 "}";
					
					TupleQuery tupleQuery1 = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query_1);
               TupleQueryResult result1= tupleQuery1.evaluate();
               
					BindingSet bindingSet1 = null;	
					while(result1.hasNext()){
						bindingSet1 = result1.next();
						if(bindingSet1.getValue("count").toString().contains("\"1\"")){
						Y_1.add(Y_23.get(i));
						Z_1.add(Z_23.get(i));
						}
					}		
		
					}
					}
					System.out.println("y_1 size: "+Y_1.size());
					final long phase_2_1 = System.currentTimeMillis();					
					
					System.out.println("phase 2_1 time: "+(phase_2_1-phase_2_23));
				
					System.out.println("part 1: ");
		//			final long end = System.currentTimeMillis();
               List <String> results = new ArrayList<String>();	
               if(!X_3.isEmpty()){
               	for(int i=0;i<X_12.size();i++){
               		for(int j=0;j<X_3.size();j++){
               			if( (X_12.get(i).equals(X_3.get(j))) && (Y_12.get(i).equals(Y_3.get(j))) ){
            					
               				String result = X_12.get(i)+" , "+Y_12.get(i)+" , "+Z_12.get(i);
               				if(!results.contains(result)){
               					results.add(result);
               				}
               			}
               		}
               	}
               }
               System.out.println(results.size());
               for (int i=0;i<results.size();i++){
               //	 System.out.println(results.get(i));	
               }
					System.out.println("part 2: ");
               if(!X_2.isEmpty()){
               	for(int i=0;i<X_13.size();i++){
               		for(int j=0;j<X_2.size();j++){
               			if( (X_13.get(i).equals(X_2.get(j))) && (Z_13.get(i).equals(Z_2.get(j))) ){
            					
               				String result = X_13.get(i)+" , "+Y_13.get(i)+" , "+Z_13.get(i);
               				if(!results.contains(result)){
               					results.add(result);
               				}
               			}
               		}
               	}
               }
               System.out.println(results.size());
               for (int i=0;i<results.size();i++){
            //   	 System.out.println(results.get(i));	
               }

					System.out.println("PART 3:");	
               if(!Y_1.isEmpty()){
               	for(int i=0;i<Y_23.size();i++){
               		for(int j=0;j<Y_1.size();j++){
               			if((Y_23.get(i).equals(Y_1.get(j))) && (Z_23.get(i).equals(Z_1.get(j)))){
               			
            				String result = X_23.get(i)+" , "+Y_23.get(i)+" , "+Z_23.get(i);
            				if(!results.contains(result)){
            					results.add(result);
            				}
               			}
               		}
               	}
               }
               
               System.out.println(results.size());
              for (int i=0;i<results.size();i++){
               	 System.out.println(results.get(i));	
               }
              repo123456.shutDown();
			     repo12_34_56.shutDown();
		
				} finally {
		//	out.close();	
			con123456.close();
			con12_34_56.close();			
				}
			}

}
