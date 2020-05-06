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
public class ClusterFederation2HopQueryC3Step2Test {
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
		
		List<String> artist = new ArrayList<String>();
		List<String> nytLocation = new ArrayList<String>();
		List<String> locationName = new ArrayList<String>();
		
		BufferedReader brArtist = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/artist")));
      String lineArtist = brArtist.readLine();
      artist.add(lineArtist);
      
		BufferedReader brNYT = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/NYT")));
      String lineNYT = brNYT.readLine(); 
      nytLocation.add(lineNYT);
      
		BufferedReader brGeo = new BufferedReader(
				new FileReader(new File("/home/vagrant/results/Geo")));
      String lineGeo = brGeo.readLine(); 
      locationName.add(lineGeo);
      
		while(lineArtist!=null){
			artist.add(lineArtist);
			lineArtist = brArtist.readLine();
		}
		
		while(lineNYT!=null){
			nytLocation.add(lineNYT);
			lineNYT = brNYT.readLine();
		}
		
		while(lineGeo!=null){
			locationName.add(lineGeo);
			lineGeo = brGeo.readLine();
		}
		
		brArtist.close();
		brNYT.close();	
		brGeo.close();
		
		
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
					List<String> film = new ArrayList<String>();
					for (int i=0; i<artist.size();i++){
						
					
						String query = "PREFIX foaf: 		<http://xmlns.com/foaf/0.1/> "+
								"PREFIX geonames: 	<http://www.geonames.org/ontology#> "+
								"PREFIX mo:   		<http://purl.org/ontology/mo/> "+
								"PREFIX nytimes:         <http://data.nytimes.com/elements/> "+
								"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
								"SELECT DISTINCT ?name ?anylocation ?news"+
								"WHERE { "+
								"	     <"+artist.get(i)+"> a mo:MusicArtist ; "+
								"         foaf:name ?name ; "+
								"		 foaf:based_near ?location . "+
								"         <"+locationName.get(i)+"> geonames:name ?anylocation . "+
								"         <"+nytLocation.get(i)+"> nytimes:topicPage ?news "+
								"} ";
				//	System.out.println(query);

					TupleQuery tupleQuery = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result = tupleQuery.evaluate();
					
					BindingSet bindingSet = null;	
					while(result.hasNext()){
						bindingSet = result.next();	
						Value valueOfX = bindingSet.getValue("name");
						System.out.println(valueOfX.toString());
                  film.add(valueOfX.toString());	
						count++;	
					}
					}
					System.out.println("result size: "+count);
					
					final long phase_2_2 = System.currentTimeMillis();	
					
					System.out.println("phase 2_1Hop time: "+(phase_2_2-start));
					/*
					FileOutputStream fosFilm= new FileOutputStream("/home/vagrant/results/film");
					OutputStreamWriter oswFilm = new OutputStreamWriter(fosFilm);
					BufferedWriter bwFilm = new BufferedWriter(oswFilm);
					
					for (int i=0;i<film.size();i++){
						bwFilm.write(film.get(i)+"\n");
		
					}
				  bwFilm.close();
					*/					
			     repo12_34_56.shutDown();
					
				} finally {	
			con123456.close();
			
				}
				
			}
}
