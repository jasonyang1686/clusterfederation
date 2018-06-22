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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

/**
 *
 * @author vagrant
 */
public class CreateOverlapTest {
	
	static public BatchWriter createWriter(Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		long memBuf = 1000000L; // bytes to store before sending a batch
		long timeout = 1000L; // milliseconds to wait before sending
		int numThreads = 10;
		BatchWriter writer =
		    conn.createBatchWriter(tableName, memBuf, timeout, numThreads);		
		return writer;
	}
	
	static public void addURIs(List<String> URIs, Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		
		  BatchWriter writer = createWriter(conn, tableName);
		  for(int i=0;i<URIs.size();i++){
		  Text rowID = new Text(URIs.get(i));
		  Text colFam = new Text("   ");
		  Text colQual = new Text("   ");
	//	  ColumnVisibility colVis = new ColumnVisibility("public");
		  long timestamp = System.currentTimeMillis();

		  Value temp_value = new Value("2".getBytes());

		  Mutation mutation = new Mutation(rowID);
		  mutation.put(colFam, colQual, timestamp, temp_value);		  
		  writer.addMutation(mutation);      
		  }
		  writer.close();
		
	}
	
	public static void main(String[] args) throws Exception {
		String instanceName = "dev";
	   String tableURI ="URI_index";
		String tableNewURI15 = "new_URI_index_15";
		String tableNewURI35 = "new_URI_index_35";
		String tableOverlap ="rya_overlap";

		

		String zkServer1 = "192.168.33.10:2181";
		String zkServer3 = "192.168.33.30:2181";
		String zkServer5 = "192.168.33.50:2181";

		String userName="root";

		String passWord="root";
		
		final long start = System.currentTimeMillis();
	 	   
		Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
		Connector conn1 = inst1.getConnector(userName, passWord);
		
		Instance inst3 = new ZooKeeperInstance(instanceName, zkServer3);
		Connector conn3 = inst3.getConnector(userName, passWord);
		
		Instance inst5 = new ZooKeeperInstance(instanceName, zkServer5);
		Connector conn5 = inst5.getConnector(userName, passWord);
		
   	List<String>overlap = new ArrayList<String>();
   	
   	Scanner scan5 =  conn5.createScanner(tableURI, new Authorizations());
   	Scanner scan15 =  conn1.createScanner(tableNewURI15, new Authorizations());
   	Scanner scan35 =  conn3.createScanner(tableNewURI35, new Authorizations());
   	

   	Iterator<Map.Entry<Key,Value>> iterator15 = scan15.iterator();
   	Iterator<Map.Entry<Key,Value>> iterator35 = scan35.iterator();
   	
   	  while (iterator15.hasNext()) {
   		   Map.Entry<Key,Value> entry15 = iterator15.next();
   		   String key15 = entry15.getKey().getRow().toString();
   			scan5.setRange(Range.exact(key15));
   	   	Iterator<Map.Entry<Key,Value>> iterator5 = scan5.iterator();
   	   	  if (iterator5.hasNext()) {
   	   		  if(key15.contains("http"))
   	   		 overlap.add(key15); 
   	   	  }
   	  }
   	  
   	  while (iterator35.hasNext()) {
  		   Map.Entry<Key,Value> entry35 = iterator35.next();
  		   String key35 = entry35.getKey().getRow().toString();
  			scan5.setRange(Range.exact(key35));
  	   	Iterator<Map.Entry<Key,Value>> iterator5 = scan5.iterator();
  	   	  if (iterator5.hasNext()) {
  	   		 if(key35.contains("http"))
  	   		 overlap.add(key35); 
  	   	  }
  	  }
     	System.out.println("size: "+overlap.size());  
     	addURIs(overlap, conn5, tableOverlap);  
     	
   	final long end = System.currentTimeMillis();
   	
   	System.out.println((end-start));
		
	}

}
