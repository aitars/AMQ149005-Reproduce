/*
 * Reproduce AMQ149005 in backup broker
 */
package com.havi.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.apache.activemq.artemis.util.ServerUtil;

/**
 * Example of live and replicating backup pair.
 * <p>
 * After both servers are started, the live server is killed and the backup becomes active ("fails-over").
 * <p>
 * Later the live server is restarted and takes back its position by asking the backup to stop ("fail-back").
 */
public class ReproduceAMQ149005Backup {

   private static Process server0;

   private static Process server1;

   public static void main(final String[] args) throws Exception {
      final int numMessages = 30;

      Connection connection = null;

      InitialContext initialContext = null;

      try {
         server0 = ServerUtil.startServer(args[0], ReproduceAMQ149005Backup.class.getSimpleName() + "0", 0, 30000);
         server1 = ServerUtil.startServer(args[1], ReproduceAMQ149005Backup.class.getSimpleName() + "1", 1, 10000);

         // Step 1. Get an initial context for looking up JNDI from the server #1
         initialContext = new InitialContext();

         // Step 2. Look up the JMS resources from JNDI
         Queue queue = (Queue) initialContext.lookup("queue/exampleQueue");
         ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

         // Step 3. Create a JMS Connection
         connection = connectionFactory.createConnection();

         // Step 4. Create a *transacted* JMS Session with client acknowledgement
         Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

         // Step 5. Start the connection to ensure delivery occurs
         // connection.start();

         // Step 6. Create a JMS MessageProducer and a MessageConsumer
         MessageProducer producer = session.createProducer(queue);
         //MessageConsumer consumer = session.createConsumer(queue);

         // Step 7. Send some messages to server #1, the live server
         TextMessage message = session.createTextMessage("This is a text message.");

         StringBuilder strBuilder = new StringBuilder();
         for (int i = 0; i < 15671; ++i) { 
            strBuilder.append("xxxxxxxxxxxxxxxx");
         } 
         message.setStringProperty("bulk", strBuilder.toString());

         System.out.println("Sending message with bulk header length " + 2*strBuilder.length());
         producer.send(message);
         System.out.println("Message sent");
        
         

 
      } finally {
         // Step 13. Be sure to close our resources!

         if (connection != null) {
            connection.close();
         }

         if (initialContext != null) {
            initialContext.close();
         }

         ServerUtil.killServer(server0);
         ServerUtil.killServer(server1);
      }
   }
}
