### Start Kafka in docker
`docker-compose -f kafka-docker.yml up` 

## Run Ingestion service
This service is responsive for pushing a streaming data from meetup stream API into Kafka.
I have used Akka-Http single websocket and Alpakka-Kafka for ingestion rsvp into Kafka.

#### Execute below command for starting this service:
`sbt "project ingestion" run` 

## Run etl service
This service is responsible for performing business logic on incoming kafka stream. After performing business logic, I am 
storing output into external data source.

#### Execute below command for starting this service:
```
sbt "project" test
sbt "project etl" run
``` 
Note: Remove ing_meetup directory if you get any error.

## Run API service
 This service is responsible for below use cases:
 
 - Coupling the implementation to the stream
 - Calculating the most popular meetup locations in the world
 - Show trending topics by country
 
#### Execute below command for starting this service:
`sbt "project api" run`


## Build web app
This app responsible for display live stream rsvp, most popular meetups in the world and trending topics
of the country.  

##### Execute below command for building app
```
cd app 
npm install
npm start
```

Open http://localhost:8000/ on browser