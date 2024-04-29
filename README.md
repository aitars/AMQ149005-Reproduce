# Reproduce AMQ149005 in backup broker

Reproduce behaviour of Artemis MQ 2.31.2 if sending a message with properties size near 490K. 

Run with **mvn verify** from this directory. 

Result should be 
`[org.apache.activemq.artemis.core.replication.ReplicationEndpoint] AMQ149005: Message of 501772 bytes is bigger than the max record size of 501760 bytes. You should try to move large application properties to the message body.`
in backup broker log file **target/server1/log/artemis.log**. 

Client gets a 
`[WARNING] AMQ212037: Connection failure to localhost/127.0.0.1:61616 has been detected: AMQ219014: Timed out after waiting 30000 ms for response when sending packet 71 [code=CONNECTION_TIMEDOUT]`.

This example is derivated from [JMS Replicated Failback Static Example](https://github.com/apache/activemq-artemis-examples).
