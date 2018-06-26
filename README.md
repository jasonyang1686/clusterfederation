Cluster Federation
=================
Federated RDF systems allow users to retrieve data from multiple independent sources without needing to have all the data in the same triple store. However, the performance of these systems can be poor for geographically distributed sources where network transfer costs are high. This paper introduces novel join algorithms that take advantage of network topology to decrease the cost of processing Basic Graph Pattern SPARQL queries in a geographically distributed environment. Federation members are grouped in clusters, based on the network communication cost between the members, and the bulk of the join processing is pushed to the clusters. 

How to use
----------
We modify the federation method based on OpenRDF Sesame 2.7.x, to implement cluster federation method, you need download OpenRDF Sesame 2.7.x and Accumulo 1.7.x first, and then replace openrdf-sesame-2.7.x/core/sail/federation/src/main folder with our main folder.

Example
-------

The following steps are example how to create overlap list after uploading data via workbench.

At first we create 6 RyaAccumulo repositories on each VM via workbench and insert data. Then we create 3 regular federations and cluster federations on each cluster center, in our experiments that means box 1, 3 and 5. Each federation contains two repositories which are from its cluster members. On the cluster coordinator (box 2), we create 2 repositories: one is the regular federation of 6 repositories. This is created for regular federation test. Second one is the federation of each cluster federations, this is created for the second step of cluster federation test.

Then we run the following code:

* Run CreateURITableTest.java on each cluster center
* Create IRI index table in accumulo based rya_spo and rya_osp tables.

* Run CreateBloomFilterTest.java on each cluster center
* Create bloom filters based on IRI index tables and save them in serialized files.

* Run CreatNewURIIndexTest.java on each cluster center
* Create two new URI index tables on each cluster center based on the two remote bloom filter files.

* Run CreateOverlapTest.java on each cluster center
* Create overlap list on each cluster center based on new URI index tables.

