package com.aws.sample.cmr;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class QueuePurger {

    private static final Logger LOG = LoggerFactory.getLogger(QueuePurger.class);

    ActiveMQSslConnectionFactory connFact;
    ActiveMQConnection conn;
    ActiveMQSession session;
    ActiveMQQueue queue;
    String brokerUrl = System.getenv("BROKER_URL");
    String brokerUser = System.getenv("BROKER_USER");
    String brokerPassword = System.getenv("BROKER_PASSWORD");

    public QueuePurger() throws JMSException {
        connFact = new ActiveMQSslConnectionFactory(brokerUrl);
        connFact.setConnectResponseTimeout(10000);
        conn = (ActiveMQConnection) connFact.createConnection (brokerUser, brokerPassword);
        session = (ActiveMQSession) conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    void purge() throws JMSException {
        for (int queueCount = 0; queueCount < 200; queueCount++) {
            queue = (ActiveMQQueue) session.createQueue("queue.number" + queueCount);
            conn.destroyDestination(queue);
            System.out.println("deleted queue queue.number" + queueCount);
        }

        session.close();
        conn.close();
    }

    public static void main(String[] args) throws JMSException {
        QueuePurger purgeer = new QueuePurger();
        purgeer.purge();
    }
}