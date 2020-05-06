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
public class ClusterFederation2HopQueryC6Step3Test {
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
		
		List<String> film = new ArrayList<String>();
		
		BufferedReader brFilm = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/film")));
      String lineFilm = brFilm.readLine();
      film.add(lineFilm);
		
		while(lineFilm!=null){
			film.add(lineFilm);
			lineFilm = brFilm.readLine();
		}
		
		brFilm.close();			
		
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
				
					for (int i=0; i<film.size();i++){
						
					
					String query = "PREFIX linkedmdb: <http://data.linkedmdb.org/resource/movie/> "+
							"prefix owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX dcterms: <http://purl.org/dc/terms/> "+
							"PREFIX purl: <http://purl.org/dc/terms/> "+
							"PREFIX nytimes: <http://data.nytimes.com/elements/> "+
							"SELECT ?filmTitle "+
							"WHERE "+ 
							"{ "+
							" <"+film.get(i)+">  purl:title 	?filmTitle . "+
							"} ";
				//	System.out.println(query);

					TupleQuery tupleQuery = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result = tupleQuery.evaluate();
					
					BindingSet bindingSet = null;	
					while(result.hasNext()){
						bindingSet = result.next();	
						Value valueOfX = bindingSet.getValue("filmTitle");
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
