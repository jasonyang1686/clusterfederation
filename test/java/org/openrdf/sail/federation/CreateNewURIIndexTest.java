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

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.skjegstad.utils.BloomFilter;

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
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

import jcifs.smb.SmbFile;

/**
 *
 * @author vagrant
 */
public class CreateNewURIIndexTest {
	
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
		
		String url31 = "smb://192.168.33.10/share/1";
		SmbFile  file31 = new SmbFile(url31);
      ObjectInputStream in31 = new ObjectInputStream(file31.getInputStream());
      BloomFilter<String> instance31 = (BloomFilter)in31.readObject();
       
      in31.close();
  //    file3.close();
      
		String url35 = "smb://192.168.33.50/share/5";
		SmbFile  file35 = new SmbFile(url35);
  //    FileInputStream file5 = new FileInputStream("/home/vagrant/share/5");
      ObjectInputStream in35 = new ObjectInputStream(file35.getInputStream());
       
      // Method for deserialization of object
      BloomFilter<String> instance35 = (BloomFilter)in35.readObject();
       
      in35.close();
     
   //   file5.close();
      
		final long start = System.currentTimeMillis();
    
     
     final long phase2 = System.currentTimeMillis();
    	System.out.println((phase2-start));
     
     List<String>overlap31 = new ArrayList<String>();
     List<String>overlap35 = new ArrayList<String>();
  	String instanceName = "dev";
   String tableURI ="URI_index";
	String tableNewURI31 = "new_URI_index_31";
	String tableNewURI35 = "new_URI_index_35";


	

	String zkServer3 = "192.168.33.30:2181";


	String userName="root";

	String passWord="root";
	

 	   
	Instance inst3 = new ZooKeeperInstance(instanceName, zkServer3);
	Connector conn3 = inst3.getConnector(userName, passWord);
	
	Scanner scan3 =  conn3.createScanner(tableURI, new Authorizations());
	
	Iterator<Map.Entry<Key,Value>> iterator3 = scan3.iterator();
	  while (iterator3.hasNext()) {
		   Map.Entry<Key,Value> entry3 = iterator3.next();
		   String key3 = entry3.getKey().getRow().toString();
		   if(instance31.contains(key3)){
		   	overlap31.add(key3);
		   }
		   if(instance35.contains(key3)){
		   	overlap35.add(key3);
		   }
	  }
	  
	  addURIs(overlap31,conn3,tableNewURI31);
	  addURIs(overlap35,conn3,tableNewURI35);
	  
	  	final long end = System.currentTimeMillis();
		
	  	System.out.println((end-phase2));
				
	}
	

}
