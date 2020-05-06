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
import java.util.Map.Entry;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;

/**
 *
 * @author vagrant
 */
public class CreateURITableTest {
	public static void main(String[] args) throws Exception {
	String instanceName = "dev";
   String tableURI ="URI_index";
	String tableSPO = "rya_spo";
	String tablePO ="rya_po";
	String tableOSP ="rya_osp";
	

	String zkServer1 = "192.168.33.10:2181";
	String zkServer2 = "192.168.33.20:2181";

	String userName="root";

	String passWord="root";
	
   
   Iterator<Entry<Key, Value>> iterator1;
   Iterator<Entry<Key, Value>> iterator2;

	OverlapList ol1 = new OverlapList(zkServer1,instanceName);
	OverlapList ol2 = new OverlapList(zkServer2,instanceName);
	OverlapList olURI = new OverlapList(zkServer1,instanceName); 
	
	final long start = System.currentTimeMillis();
	try {
	   ol1.getConnection(userName, passWord);
      ol1.selectTable(tableSPO);
	   ol2.getConnection(userName, passWord);
      ol2.selectTable(tableSPO);
	   olURI.getConnection(userName, passWord);
      olURI.selectTable(tableURI);
      
      Scanner sc1;
		sc1 = ol1.createScan();
      iterator1 = sc1.iterator();
      
      Scanner sc2;
		sc2 = ol2.createScan();
      iterator2 = sc2.iterator();
      
      
 	  while (iterator1.hasNext()) {
		   Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator1.next();
		   String [] pattern = entry.getKey().getRow().toString().split("\\x00");
         olURI.addData(pattern[0].replaceAll("\\x01\\x03",""), "1");
         olURI.addData(pattern[2].replaceAll("\\x01\\x03",""), "1");
		  }
 	  
 	  while (iterator2.hasNext()) {
		   Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator2.next();
		   String [] pattern = entry.getKey().getRow().toString().split("\\x00");
        olURI.addData(pattern[0].replaceAll("\\x01\\x03",""), "1");
        olURI.addData(pattern[2].replaceAll("\\x01\\x03",""), "1");
		  }
	}
	catch (TableNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (AccumuloException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (AccumuloSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	catch (TableExistsException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	final long end = System.currentTimeMillis();
	
	System.out.println((end-start));

	}
		 
}
