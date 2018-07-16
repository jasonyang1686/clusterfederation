
### Introduction
Federated RDF systems allow users to retrieve data from multiple independent sources without needing to have all the data in the same triple store. However, the performance of these systems can be poor for geographically distributed sources where network transfer costs are high. We introduces novel join algorithms that take advantage of network topology to decrease the cost of processing Basic Graph Pattern SPARQL queries in a geographically distributed environment. Federation members are grouped in clusters, based on the network communication cost between the members, and the bulk of the join processing is pushed to the clusters.

### Necessary packages
We implemented cluster federation method based on OpenRDF Sesame 2.7.x, to enable cluster federation feature, you need to download OpenRDF Sesame 2.7.x and Accumulo 1.7.x first, and then replace openrdf-sesame-2.7.x/core/sail/fedeferation/src/main folder with our main folder.

### Structures
Here is the list of java packages we implemented based on openrdf sesame, files names with emphasized are the ones we modified or added:

##### Project: sesame-sail-federation

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
  Also we override the *getConfig* method in line 86 to 89:

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

##### Project: sesame-runtime

package: org.openrdf.runtime

* **RepositoryManagerClusterFederator.java**

  Similar as *RepositoryManagerFederator* class, we create a repository manager to handle cluster federation. We modified *addFed* method in line 105 to 108:

  ```java
  if (members.contains(fedID)) {
  throw new RepositoryConfigException(
      "A cluster federation member may not have the same ID as the federation.");
  }
  ```

  and in line 111:

  ```java
  LOGGER.debug("Cluster Federation repository root node: {}", fedRepoNode);
  ```
  We also modified *addSail* method in line 143 to 146:

  ```java
  addToGraph(graph, sailRoot, SailConfigSchema.SAILTYPE,
  valueFactory.createLiteral(ClusterFederationFactory.SAIL_TYPE));
  addToGraph(graph, sailRoot, ClusterFederationConfig.READ_ONLY, valueFactory.createLiteral(readonly));
  addToGraph(graph, sailRoot, ClusterFederationConfig.DISTINCT, valueFactory.createLiteral(distinct));
  ```
  And line 157 of *addMember* method:

  ```java
  addToGraph(graph, sailRoot, ClusterFederationConfig.MEMBER, memberNode);
  ```

* RepositoryManagerFederator.java

##### Project:sesame-http-workbench

package:org.openrdf.workbench.command

* AddServlet.java
* ClearServlet.java
* ContextsServlet.java
* **CreateServlet.java**

  Modify the original *CreateServlet* class to handle cluster federation in the workbench. We add member variable *RepositoryManagerClusterFederator* at line 59:

  ```java
  private RepositoryManagerClusterFederator rmcf;
  ```

  and line 67 of *init* method:

  ```java
  this.rmcf = new RepositoryManagerClusterFederator(manager);
  ```
  We also modify *service* method to add cluster federation condition from line 98 to 125:

  ```java
  boolean federate;
  boolean clusterFederate;
  if (req.isParameterPresent("type")) {
  final String type = req.getTypeParameter();
  federate = "federate".equals(type);
  clusterFederate="ClusterFederation".equals(type);
  builder.transform(xslPath, "create-" + type + ".xsl");
  }
  else {
  federate = false;
  clusterFederate=false;
  builder.transform(xslPath, "create.xsl");
  }
  if(federate||clusterFederate){
  builder.start(new String[] { "id", "description", "location" });
  }else{
  builder.start(new String[] {});
  }
  builder.link(Arrays.asList(INFO));
  if (federate||clusterFederate) {
  for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
    String identity = info.getId();
    System.out.println(identity);
    if (!SystemRepository.ID.equals(identity)) {
      builder.result(identity, info.getDescription(), info.getLocation());
    }
  }
  }
  ```

  We added the similar condition in *createRepositoryConfig* method from line 140 to 147:

  ```java
  else if ("ClusterFederation".equals(type)){
  newID = req.getParameter("Local repository ID");
  rmcf.addFed(newID, req.getParameter("Repository title"),
      Arrays.asList(req.getParameterValues("memberID")),
      Boolean.parseBoolean(req.getParameter("readonly")),
      Boolean.parseBoolean(req.getParameter("distinct")));
  }
  ```
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
