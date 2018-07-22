
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

  We create this class based on *AbstractFederationConnection* class. We added a hashset named *includeSet* to store overlap list from a Accumulo table when the class is initialized. We also modified *getStatementsInternal* method, to replace *DistinctIteration* with our *IntersectOverlapList* in line 310 to 314:

  ```java
  if (!federation.isDistinct() && !isLocal(pred)) {
  // Filter overlap list items
  cursor = new IntersectOverlapList<Statement, SailException>(cursor, includeSet); }
  ```
  For the two parameters, cursor is an iteration which contains statement patterns and includeSet is the hashset we mentioned above which is created to filter out the statement which is not included in the overlap list.

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

  We create this class based on *DistinctIteration* class in the purpose of intersecting overlap list with statement patterns. We store the list into a hashset at constructor:

  ```java
  private Set<String>includeSet;

  public IntersectOverlapList(Iteration<? extends E, ? extends X> iter, Set<String> includeSet) throws X  {
  super(iter);
    this.includeSet=includeSet;
  }
  ```
  And we created a *inIncludeSet* method, for each statement in the iteration (parameter *iter*), this boolean methodto will check whether subject or object from statement pattern is contained from overlap list:

  ```java
  private boolean inIncludeSet(E object) {
		Resource sub =((ContextStatementImpl)(object)).getSubject();
		Value obj = ((ContextStatementImpl)(object)).getObject();
    if( includeSet.contains(sub.stringValue()) || includeSet.contains(obj.stringValue())){
   	 return true; }else{ return false;}}
  ```

* **OverlapList.java**

  We create overlap list and insert data into Accumulo table. This class allows users to create Accumulo tables and execute basic operations like insert or delete for certain tables based on classes from *org.apache.accumulo.core.client*. To create a new OverlapList object, you need to indicate a zoo keeper server address and Accumulo instance name as:

  ```java
  OverlapList at = new OverlapList(zkServer,instanceName);
  ```
  Before creating a scanner to iterate or delete data, you need to get Accumulo server conncted by giving a user name and password and table selected by giving a table name. The sample code is shown as:

  ```java
  at.getConnection(userName, passWord);
  at.selectTable(tableName);
  Scanner sc = at.createScan();
  ```

  You can then add items into the overlap list. Because the list is in an Accumulo table, the members of overlap list are in String type as RowIDs in the Accumulo table and also you need to give a certain RowValue for each ID.

  ```java
  at.addData(rowID, rowValue);
  ```
  Also we have the similar method in *deleteData* when you need to delete items from overlap list:

  ```java
  at.deleteData(rowID, rowValue);
  ```

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

### How to use

There are two ways to enable cluster federation feature on openrdf. One is to replace the necessary libraries from *lib* folder to */var/lib/tomcat7/webapps/ *. Copy create.xsl and create-ClusterFederation.xsl to */var/lib/tomcat7/webapps/openrdf-workbench/transformations/ * and copy the rest three java libraries to both  */var/lib/tomcat7/webapps/openrdf-workbench/WEB-INF/lib/ * and */var/lib/tomcat7/webapps/openrdf-sesame/WEB-INF/lib/ *. Then restart your tomcat service, you can see a new cluster federation option appears on your workbench webpage.

Another way is to implement cluster federation in java code. You can download openrdf-sesame source code and add our code listed above (*main* and *test* folders) to original code. To test cluster federation we created some experiments to compare the execution time of regular federation method and our cluster federation. We choose [Lehigh University Benchmark](http://swat.cse.lehigh.edu/projects/lubm/) as our benchmark dataset and test queries provided from [here](http://swat.cse.lehigh.edu/projects/lubm/queries-sparql.txt). You can reproduce our experiments by using our test codes from *test* folder.

For example, after uploading dataset via workbench on each cluster center, you can run CreateURITableTest, CreateBloomFilterTest, CreateNewURIIndexTest and CreateOverlapTest sequently to generate 0-hop overlap list, then you can run NHopOverlapTest to generate N-hop overlap list in Accumulo table.

After creating overlap lists on each cluster center, you can run ComparisonFederationQueryTest to calculate execution time of regular federation on the cluster coordinator. Or ClusterFederationQueryTest to calculate execution time of cluster federation. You can create your own test code and datasets to run your experiments.

