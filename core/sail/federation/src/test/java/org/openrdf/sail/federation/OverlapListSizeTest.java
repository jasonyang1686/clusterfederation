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

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

/**
 *
 * @author vagrant
 */
public class OverlapListSizeTest {
	
	
	public static void main(String[] args) throws Exception {
		String instanceName = "dev";
	   String tableURI ="URI_index";
		String tableOverlap ="rya_overlap";
		
		String zkServer1 = "192.168.33.10:2181";

		String userName="root";

		String passWord="root";
		
		Set overlap = new HashSet();
		
		final long start = System.currentTimeMillis();
	 	   
		Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
		Connector conn1 = inst1.getConnector(userName, passWord);
		
   	Scanner scanURI =  conn1.createScanner(tableURI, new Authorizations());
   	Scanner scanOverlap =  conn1.createScanner(tableOverlap, new Authorizations());
   	
   	Iterator<Map.Entry<Key,Value>> iteratorURI = scanURI.iterator();
   	Iterator<Map.Entry<Key,Value>> iteratorOverlap = scanOverlap.iterator();
   	
   	String key;
		while(iteratorOverlap.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorOverlap.next();
		   key = entry.getKey().getRow().toString();
  		   overlap.add(key);		  
		}
		
		final long phase1 = System.currentTimeMillis();
		
		System.out.println("phase1 :"+(phase1-start));
		
		int count=0;
		while(iteratorURI.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorURI.next();
		   key = entry.getKey().getRow().toString();
		   if(overlap.contains(key)){
  		    count++;	
		   }
		}
		final long end = System.currentTimeMillis();
		
		System.out.println("phase2 :"+(end-phase1));
	}
}
