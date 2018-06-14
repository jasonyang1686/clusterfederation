Cluster Federation
=================
Federated RDF systems allow users to retrieve data from multiple independent sources without needing to have all the data in the same triple store. However, the performance of these systems can be poor for geographically distributed sources where network transfer costs are high. This paper introduces novel join algorithms that take advantage of network topology to decrease the cost of processing Basic Graph Pattern SPARQL queries in a geographically distributed environment. Federation members are grouped in clusters, based on the network communication cost between the members, and the bulk of the join processing is pushed to the clusters. 

How to use
----------
We modify the federation method based on OpenRDF Sesame 2.7.x, to implement cluster federation method, you need download OpenRDF Sesame 2.7.x and Accumulo 1.7.x first, and then replace openrdf-sesame-2.7.x/core/sail/federation/src/main folder with our main folder.

Example
-------
