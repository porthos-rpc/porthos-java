package com.github.porthos;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.github.porthos.client.Client;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ClientTest {

    private Connection connection;

    @Before
    public void setUp() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("broker");
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setPort(5672);
        factory.setVirtualHost("porthos");

        connection = factory.newConnection();
    }

    @Test(expected = TimeoutException.class)
    public void testCallAMQPUrl() throws Exception {
        new Client("amqp://guest:guest@broker/porthos", "SomeService").call("sampleMethod").sync(2, TimeUnit.SECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void testCallSync() throws Exception {
        new Client(connection, "SomeService").call("sampleMethod").sync(2, TimeUnit.SECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void testCallWithJSONSync() throws Exception {
        new Client(connection, "SomeService").call("sampleMethod").withJSON("[1,2,3]").sync(2, TimeUnit.SECONDS);
    }
}
