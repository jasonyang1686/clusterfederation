package org.openrdf.sail.federation;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;

public class ClusterFederationTest{
	
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_5="http://192.168.33.50:8080/openrdf-sesame";
	private static final String SESAME_SERVER_6="http://192.168.33.60:8080/openrdf-sesame";
	private static final String SESAME_SERVER_3="http://192.168.33.30:8080/openrdf-sesame";
	private static final String SESAME_SERVER_4="http://192.168.33.40:8080/openrdf-sesame";
	private static final String repositoryID_12="ClusterFederation12";
	private static final String repositoryID_34="ClusterFederation34";
	private static final String repositoryID_56="ClusterFederation56";
	private static final String repositoryID_1="RyaAccumulo_1";
	private static final String repositoryID_2="RyaAccumulo_2";
	private static final String repositoryID_3="RyaAccumulo_3";
	private static final String repositoryID_4="RyaAccumulo_4";
	private static final String repositoryID_test1="test1";
	private static final String repositoryID_test2="test2";
	

	public static void main(String[] args) throws Exception {

		
	    //repository 1
				Repository repo1 = new HTTPRepository(SESAME_SERVER_1, repositoryID_1);
				repo1.initialize();				
		//repository 2
				Repository repo2 = new HTTPRepository(SESAME_SERVER_2, repositoryID_2);		
				repo2.initialize();
			
		// repository 3
				Repository repo3 = new HTTPRepository(SESAME_SERVER_3, repositoryID_3);		
				repo3.initialize();	
				
		//repository 4
				Repository repo4 = new HTTPRepository(SESAME_SERVER_4, repositoryID_4);		
				repo4.initialize();		
		/*		
		// repository 5
				Repository repo5 = new HTTPRepository(SESAME_SERVER_5, repositoryID_5);		
				repo5.initialize();	
				
		//repository 6
				Repository repo6 = new HTTPRepository(SESAME_SERVER_6, repositoryID_6);		
				repo6.initialize();	
*/

			   SailRepository sailRepo12 =null;
		//	   SailRepository sailRepo34 =null;
		//	   SailRepository sailRepo56 =null;
			   
				//repository 12
				Repository repo12 = new HTTPRepository(SESAME_SERVER_1, repositoryID_12);		
				repo12.initialize();	
				
				//repository 34
				Repository repo34 = new HTTPRepository(SESAME_SERVER_3, repositoryID_34);		
				repo34.initialize();	
				
				//repository 56
				Repository repo56 = new HTTPRepository(SESAME_SERVER_5, repositoryID_56);		
				repo56.initialize();	
							   
			   
				RepositoryConnection con12_34_56=null;


				Repository sailRepo123456 =null;
				
				try {

					log.info("Connecting to SailRepository.");
		//overlap list info

					String zkServer1 = "192.168.33.10:2181";
					String zkServer2 = "192.168.33.20:2181";
					String zkServer3 = "192.168.33.30:2181";
					String zkServer5 = "192.168.33.50:2181";

			        
		// federation of 1,2
			//        Federation federation12 = new Federation();
			//        federation12.addMember(repo1);
			//        federation12.addMember(repo2);
				
		       
		// Cluster federation of 1,2
				//	Federation clusterFederation12=new Federation();
				   ClusterFederation clusterFederation12=new ClusterFederation(zkServer1);
					clusterFederation12.addMember(repo1);
					clusterFederation12.addMember(repo2);

				
	    //Cluster federation of 3, 4    
					Federation clusterFederation34 = new Federation();	
					clusterFederation34.addMember(repo3);
					clusterFederation34.addMember(repo4);
		/*			
		 //Cluster federation of 3, 4    
				   ClusterFederation clusterFederation56 = new ClusterFederation(zkServer5);	
					clusterFederation56.addMember(repo5);
					clusterFederation56.addMember(repo6);
					
		*/			
		   sailRepo12 = new SailRepository(clusterFederation12);
		   
	//		sailRepo34 = new SailRepository(clusterFederation34);
	//		sailRepo56 = new SailRepository(clusterFederation56);
			
			sailRepo12.initialize();
	//		sailRepo34.initialize();
	//		sailRepo56.initialize();
			      
	
               con12_34_56=sailRepo12.getConnection();	

					final long start = System.currentTimeMillis();	
		// execute query
					/*
					String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE{"+
							"?Z ub:subOrganizationOf ?Y ."+
							"?X ub:memberOf ?Z ."+
							"?X ub:undergraduateDegreeFrom ?Y ."+ 
							 "}";
					*/
					String query = "PREFIX linkedmdb: <http://data.linkedmdb.org/resource/movie/> "+
							"prefix owl: <http://www.w3.org/2002/07/owl#> "+
							"PREFIX dcterms: <http://purl.org/dc/terms/> "+
							"PREFIX purl: <http://purl.org/dc/terms/> "+
							"PREFIX nytimes: <http://data.nytimes.com/elements/> "+
							"SELECT ?actor "+
							"WHERE "+ 
							"{ "+
							"?actor owl:sameAs 			?dbpediaURI. "+
						//	"?nytURI owl:sameAs 			?dbpediaURI . "+

							"} ";
						
					
					TupleQuery tupleQuery12_34_56 = con12_34_56.prepareTupleQuery(QueryLanguage.SPARQL, query);		
					TupleQueryResult result12_34_56 = tupleQuery12_34_56.evaluate();

					BindingSet bindingSet = null;
					int count=0;
					
					while(result12_34_56.hasNext()){
					bindingSet = result12_34_56.next();
			//		System.out.println(bindingSet.getValue("country"));
					System.out.println(bindingSet.getValue("actor"));
					count++;		
					}
					
									
				System.out.println("result size: "+count);
				final long end = System.currentTimeMillis();	
				
				System.out.println(end-start);

				 	
				} finally {
		      sailRepo12.shutDown();
				con12_34_56.close();
					
				}
			}
}
