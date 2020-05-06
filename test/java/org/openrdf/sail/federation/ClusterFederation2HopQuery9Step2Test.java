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
public class ClusterFederation2HopQuery9Step2Test {
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
		
		List<String> y = new ArrayList<String>();
		List<String> x = new ArrayList<String>();
	//	List<String> locationName = new ArrayList<String>();
		
		BufferedReader brx = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/x")));
      String linex = brx.readLine();
      x.add(linex);
      
		BufferedReader bry = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/y")));
      String liney = bry.readLine(); 
      y.add(liney);
      
		while(linex!=null){
			x.add(linex);
			linex = brx.readLine();
		}
		
		while(liney!=null){
			y.add(liney);
			liney = bry.readLine();
		}
		
		bry.close();
		brx.close();	
	
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
					for (int i=0; i<x.size();i++){
						
					
						String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
								"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
								"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
								"prefix ub: <https://rya.apache.org#>"+
								"SELECT (COUNT(*) as ?count ) "+
								"WHERE{"+
								"<"+x.get(i)+">"+" ub:advisor <"+y.get(i)+"> ."+
								 "}";

					TupleQuery tupleQuery = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result = tupleQuery.evaluate();
					
					BindingSet bindingSet = null;	
					while(result.hasNext()){
						bindingSet = result.next();	
						if(bindingSet.getValue("count").toString().contains("\"1\""))
							System.out.println(x.get(i));
							count++;	
						count++;	
					}
					}
					System.out.println("result size: "+count);
					
					final long phase_2_2 = System.currentTimeMillis();	
					
					System.out.println("phase 2_1Hop time: "+(phase_2_2-start));
									
			     repo12_34_56.shutDown();
					
				} finally {	
			con123456.close();
			
				}
				
			}
}
