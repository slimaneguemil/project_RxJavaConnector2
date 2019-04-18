== What is this app?
This application is based on a publish/subscribe reactive service to consume messages from broker.
It run on all broker systems compatible with spring coud stream framework. 

=== Running the app:

1-Go to the root of the repository and do:

`docker-compose up -d`
(This starts both Kafka and Rabbitmq in docker containers)



Go to testConnector Module :
2-check applications.properties.

choose your broker :
spring.cloud.stream.default-binder= rabbit or kafka
choose your topic prefic name :
mks.broker.topic.prefix=

3- run 'com.mks.RunMe class'