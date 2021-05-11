# spring-data-neo4j-aura-test

To be run inside a TeamCity environment to test against Spring Data Neo4j against Neo4j Aura or any other cluster distribution.
The tests do run outside TeamCity, too:

```
SDN_NEO4J_URL=neo4j+s://your.neo4j.cluster.io SDN_NEO4J_PASSWORD=yourPassword ./runClusterTests.sh
```
