/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 // Modified by Arno Schuerhoff 

package com.havi.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.apache.activemq.artemis.util.ServerUtil;

public class ReproduceAMQ149005Backup {

   private static Process server0;

   private static Process server1;

   public static void main(final String[] args) throws Exception {

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

         // Step 4. Create JMS Session with client acknowledgement
         Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);

          // Step 5. Create a JMS MessageProducer and a MessageConsumer
         MessageProducer producer = session.createProducer(queue);
  
         // Step 6. Send  message
         TextMessage message = session.createTextMessage("This is a text message.");

         System.out.println("Sending message");
         producer.send(message);
         System.out.println("Message sent, commiting...");
         session.commit();
         System.out.println("Message committed.");

         // Step 7. Send  message with properties size near to 490K to server #1, the live server
         message = session.createTextMessage("This is a text message with bulk header.");

         StringBuilder strBuilder = new StringBuilder();
         for (int i = 0; i < 250736; ++i) { 
            strBuilder.append('x');
         } 
         message.setStringProperty("bulk", strBuilder.toString());

         System.out.println("Sending message with bulk header length " + 2*strBuilder.length());
         producer.send(message);
         System.out.println("Message sent, commiting...");
         session.commit();
         System.out.println("Message committed.");
 
      } finally {
         // Step 8. Be sure to close our resources!

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
