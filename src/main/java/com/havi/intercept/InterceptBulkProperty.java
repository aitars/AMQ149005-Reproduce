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

package com.havi.intercept;

import java.lang.invoke.MethodHandles;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.Interceptor;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.protocol.core.Packet;
import org.apache.activemq.artemis.core.protocol.core.impl.wireformat.SessionSendMessage;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Interceptor implementation
 */
public class InterceptBulkProperty implements Interceptor {
   private static final Logger logger;
   static {
      logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
      logger.info("InterceptBulkProperty initialized.");
      } 

   @Override
   public boolean intercept(final Packet packet, final RemotingConnection connection) throws ActiveMQException {
      if (packet instanceof SessionSendMessage) {
         SessionSendMessage realPacket = (SessionSendMessage) packet;
         Message msg = realPacket.getMessage();

         long propsSize = 0;
         for (SimpleString propName : msg.getPropertyNames()) {
            String propValue = msg.getStringProperty(propName);
            propsSize += propValue.length();
         }

         if (propsSize >= 190000) {
            logger.warn("Got message with cumulated property size {} for address {} from {}.", propsSize*2, msg.getAddress(), connection.getRemoteAddress());
            logger.warn("Body first 256 characters: {}", StringUtils.left(msg.getStringBody(), 256));
            for (SimpleString propName : msg.getPropertyNames()) {
               String propValue = msg.getStringProperty(propName);
               logger.warn("Property \"{}\" value \"{}\"", propName, StringUtils.left(propValue, 256));
            }
            // return false;
         }
      }

      // We return true which means "call next interceptor" (if there is one) or target.
      // If we returned false, it means "abort call" - no more interceptors would be called and neither would
      // the target
      return true;
   }

}
