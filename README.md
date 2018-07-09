
### Introduction
Federated RDF systems allow users to retrieve data from multiple independent sources without needing to have all the data in the same triple store. However, the performance of these systems can be poor for geographically distributed sources where network transfer costs are high. We introduces novel join algorithms that take advantage of network topology to decrease the cost of processing Basic Graph Pattern SPARQL queries in a geographically distributed environment. Federation members are grouped in clusters, based on the network communication cost between the members, and the bulk of the join processing is pushed to the clusters.

### Necessary packages
We implemented cluster federation method based on OpenRDF Sesame 2.7.x, to enable cluster federation feature, you need to download OpenRDF Sesame 2.7.x and Accumulo 1.7.x first, and then replace openrdf-sesame-2.7.x/core/sail/fedeferation/src/main folder with our main folder.

### Structures
Here is the list of java packages we implemented based on openrdf sesame, files names with emphasized are the ones we modified or added:

Project: sesame-sail-federation

package: org.openrdf.sail.federation
* **AbstractClusterEchoWriteConnection.java**

  Similar as *AbstractEchoWriteConnection.java*, Extended from *AbstractClusterFederationConnection.java*,we modified construction method in line 34:
	
  ```java
	public AbstractClusterEchoWriteConnection(ClusterFederation federation,
	List<RepositoryConnection> members) {
  ```
* **AbstractClusterFederationConnection.java**


* AbstractEchoWriteConnection.java
* AbstractFederationConnection.java
* **ClusterFederation.java**

  Extended from Federation.java, we modified *getConnection* method in line 146 and 147:
 
  ```java
  return readOnly ? new ReadOnlyClusterConnection(this, connections)
  					: new WritableClusterConnection(this, connections);
  ```

* Federation.java
* **IntersectOverlapList.java**
* **OverlapList.java**
* PrefixHashSet.java
* **ReadOnlyClusterConnection.java**

  Similar as *ReadOnlyConnection.java*, Extended from *AbstractClusterFederationConnection.java*,we modified construction method in line 15:
  
  ```java
  public ReadOnlyClusterConnection(ClusterFederation federation,
  List<RepositoryConnection> members) {
  ```
* ReadOnlyConnection.java
* **WritableClusterConnection.java**

  Similar as *WritableConnection.java*, Extended from *AbstractClusterEchoWriteConnection.java*,we modified construction method in line 39:
  
  ```java
  public WritableClusterConnection(ClusterFederation federation,
  List<RepositoryConnection> members) throws SailException {
  ```
* WritableConnection.java

package: org.openrdf.sail.federation.config
* **ClusterFederationConfig.java**

  Similar as *FederationConfig.java*, extended from *SailImplConfigBase*, we modified *NAMESPACE* in line 74:
  
  ```java
  public static final String NAMESPACE =
 "http://www.openrdf.org/config/sail/clusterfederation#";
  ```
  and SailConfigException message in line 189:
  ```java
  throw new SailConfigException("No cluster federation members
  specified");
  ```
* **ClusterFederationFactory.java**

  Similar as *FederationFactory.java*, implemented from *SailFactory*, we added two member variables in line 38 and 39:
  
  ```java
  public static final String SAIL_TYPE = "openrdf:ClusterFederation";
  private String zkServer = "localhost:2181";
  ```
  We modified *getSail* method from line 48 to 50:
  
  ```java
  ClusterFederationConfig cfg = (ClusterFederationConfig) config;
  ClusterFederation sail = new ClusterFederation(zkServer);
  ```
  Also we override the *getConfig* method:
  
  ```java
  @Override
  public SailImplConfig getConfig() {
  return new ClusterFederationConfig();
	}
  ```
* FederationConfig.java
* FederationFactory.java

package: src/main/resources/META-INF/services/
* **org.openrdf.sail.config.SailFactory**

  Add one line at the bottom of the file:
  
  ```java  
  org.openrdf.sail.federation.config.ClusterFederationFactory
  ```

Project: sesame-runtime

package: org.openrdf.runtime

* **RepositoryManagerClusterFederator.java**
* RepositoryManagerFederator.java

Project:sesame-http-workbench

package:org.openrdf.workbench.command

* AddServlet.java
* ClearServlet.java
* ContextsServlet.java
* **CreateServlet.java**
* DeleteServlet.java
* ExploreServlet.java
* ExportServlet.java
* InformationServlet.java
* InfoServlet.java
* NamespacesServlet.java
* QueryServlet.java
* RemoveServlet.java
* RepositoriesServlet.java
* SavedQueriesServlet.java
* SummaryServlet.java
* TypesServlet.java
* UpdateServlet.java
