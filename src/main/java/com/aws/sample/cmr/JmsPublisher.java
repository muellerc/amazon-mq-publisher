package com.aws.sample.cmr;

import javax.jms.*;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class JmsPublisher implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(JmsPublisher.class);
    private static final int MESSAGES_PER_QUEUE = 100;

    static final String brokerUrl = System.getenv("BROKER_URL");
    static final String brokerUser = System.getenv("BROKER_USER");
    static final String brokerPassword = System.getenv("BROKER_PASSWORD");
    private static final String[] MESSAGES = new String[MESSAGES_PER_QUEUE];
    private static final int MESSAGE_SIZE = 1024 * 1;

    static {
        for (int i = 0; i < MESSAGES_PER_QUEUE; i++) {
            MESSAGES[i] = RandomStringUtils.randomAlphabetic(MESSAGE_SIZE);
        }
    }

    long offset;
    final ActiveMQSslConnectionFactory connFact;
    final Connection conn;
    final Session session;
    Queue queue;
    MessageProducer producer;

    public JmsPublisher(int offset) throws JMSException {
        this.offset = offset;
        connFact = new ActiveMQSslConnectionFactory(brokerUrl);
        connFact.setConnectResponseTimeout(10000);
        conn = connFact.createConnection (brokerUser, brokerPassword);
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public Void call() {
        try {
            for (int queueCount = (int) (offset * 5); queueCount < (int) (5 + (5 * offset)); queueCount++) {

                LOG.info(String.format("start publishing message to queue: 'queue.number%s'", queueCount));

                queue = session.createQueue("queue.number" + queueCount);
                producer = session.createProducer(queue);

                for (int messageCountPerQueue = 0; messageCountPerQueue < MESSAGES_PER_QUEUE; messageCountPerQueue++) {
                    producer.send(
                            session.createTextMessage(MESSAGES[messageCountPerQueue]),
                            DeliveryMode.PERSISTENT,
                            4,
                            -1);
//                    LOG.info(String.format("sent message '%s' to queue: 'queue.number%s'", messageCountPerQueue, queueCount));
                }

                producer.close();
            }

            session.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) throws JMSException, ExecutionException, InterruptedException {
        Collection<JmsPublisher> publishers = new ArrayList<>();

        for (int publisherCount = 0; publisherCount < 40; publisherCount++) {
            publishers.add(new JmsPublisher(publisherCount));
        }

        ExecutorService executor = Executors.newFixedThreadPool(40);
        List<Future<Void>> futures = executor.invokeAll(publishers);

        for (Future future : futures) {
            future.get();
        }

        executor.shutdown();
    }
}