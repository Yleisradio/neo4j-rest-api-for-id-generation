Neo4j REST API for id generation

This Neo4j plugin that provides rest api to create unique ids. Ids are incrementally generated starting from 1.

Please note!!!
This plugin is intended for single neo4j server setup. Synchronization over multiple server nodes requires generator id node locking.

Install to server
-----------------
mvn clean compile package install
copy jar to servers plugin folder
restart server

Usage example
----------------
curl --data "amount=10" http://127.0.0.1:7474/db/data/ext/IdGenerator/graphdb/generateIds

