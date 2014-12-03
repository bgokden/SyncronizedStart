# SyncronizedStart

SyncronizedStart is an Proof of Concept App that it is how easy to start syncronized applications with Hazelcast. 
It is an application that will be running on 10 nodes. The application coordinates among the nodes and makes sure that one and only one of them does a System.out.println("We are started!"). You will also observe that if one of the server fails others will warn you.

### Installation
```
$ git clone https://github.com/bgokden/SyncronizedStart.git
```

### Usage
```
$ cd SyncronizedStart
$ mvn clean compile
$ mvn exec:java -Dexec.mainClass="com.berkgokden.App"
```
### Usage as Jar
If you want to run as a package
```
$ mvn clean package
$ java -jar target/SyncronizedStart-1.0-SNAPSHOT-jar-with-dependencies.jar
```
###Don't forget to run unit tests
```
$ mvn test 
```

Have Fun :)
