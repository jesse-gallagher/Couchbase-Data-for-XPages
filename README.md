Couchbase Data Sources for XPages
=================================

TODO
----
* Switch to use CAS for document data sources, perhaps optionally
* Add a data source for a CAS-based key-value entry
* Package up as OSGi plugin

Requirements
------------
* Couchbase: [http://www.couchbase.com/download](http://www.couchbase.com/download)
* Couchbase SDK for Java: [http://www.couchbase.com/develop/java/current](http://www.couchbase.com/develop/java/current)
	* Note: the SDK jars should be placed at the server level (e.g. jvm/lib/ext), not in the NSF - their internal multithreading causes trouble when inside an application NSF