# lagom-template
Building Reactive Java 8 application with Lagom framework. This is a classic CRUD application which persist events in Cassandra Db using Lagom. It also presents a data model for Folio transactions.Here we are using local Cassandra instance instead of the usual embedded one to persist events.

# Prerequisites
1. Java 1.8
2. Maven 4.0

# Getting the Project
https://github.com/abhinavsinha1991/lagom-cassandra-poc.git

####Create executable jar: 
`mvn package -Dmaven.skip.test=true`

####Command to start the project

`mvn lagom:runAll`

## Json Formats for different Rest services are mentioned below :

#### 1. Create Folio:

Route(Method - POST) : `localhost:9000/api/new-folio`

Rawdata(json): 
    {
	"shipCode": "XX",
	"sailDate": "20170726",
	"bookingId": "12345678",
	"paxId":1,
	"folioTransaction":"{cardno:1234,outstanding:900}"
    }


#### 2. Update Movie:

Route(Method - PUT) : `localhost:9000/api/update-folio/:shipCode?sailDate&bookingId&paxId`
    

#### 3. Delete Movie:

Route(Method - DELETE) : `localhost:9000/api/delete-folio/:shipCode?sailDate&bookingId&paxId`
    

#### 4. Get Movie details:

Route(Method - GET) : `localhost:9000/api/folio/:shipCode?sailDate&bookingId&paxId`

