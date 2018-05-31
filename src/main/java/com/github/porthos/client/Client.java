package com.github.porthos.client;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.porthos.BrokerException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Porthos RPC Client.
 * 
 * @author Germano Fronza
 *
 */
public class Client implements Closeable {

	private static final long DEFAULT_REQUEST_MESSAGE_TTL = Duration.ofMinutes(2).toMillis();

	private Logger logger = LoggerFactory.getLogger(Client.class);

	private boolean externalAMQPConn;
	private Connection amqpConn;
	private Channel channel;
	private String serviceName;
	private String responseQueue;
	private String consumerTag;
	private long requestMessageTTL = DEFAULT_REQUEST_MESSAGE_TTL;

	private long requestNumber;
	private final Map<String, Slot> slots = new HashMap<String, Slot>();

	/**
	 * Creates a new Porthos Client given a AQMP URL and the Service Name.
	 * 
	 * @param amqpURL
	 *            like amqp://userName:password@hostName:portNumber/virtualHost
	 * @param serviceName
	 *            the name of the service that this client will invoke methods from.
	 * @throws BrokerException
	 */
	public Client(String amqpURL, String serviceName) throws BrokerException {
		this(createDefaultConnection(amqpURL), serviceName);
	}

	/**
	 * Creates a new Porthos Client given a AQMP Connection object and the Service
	 * Name.
	 * 
	 * @param amqpURL
	 *            AMQP Connection object.
	 * @param serviceName
	 *            the name of the service that this client will invoke methods from.
	 * @throws BrokerException
	 */
	public Client(Connection amqpConn, String serviceName) throws BrokerException {
		this.serviceName = serviceName;
		this.amqpConn = amqpConn;
		this.externalAMQPConn = true;

		this.setupTopology();
	}

	/**
	 * Prepares a call to the given method name.
	 * 
	 * @param method
	 *            name to be invoked.
	 * @return Call
	 */
	public Call call(String method) {
		return new Call(this, method);
	}

	/**
	 * Close AMQP resources.
	 */
	@Override
	public void close() {
		try {
			this.channel.basicCancel(this.consumerTag);

			this.channel.close();

			if (!this.externalAMQPConn) {
				this.amqpConn.close();
			}
		} catch (TimeoutException | IOException e) {
			logger.error("Exception closing the client", e);
		}
	}

	private void setupTopology() throws BrokerException {
		try {
			this.responseQueue = String.format("%s@%d-porthos-java", this.serviceName, System.currentTimeMillis());
			this.channel = this.amqpConn.createChannel();

			this.channel.queueDeclare(this.responseQueue, false, true, false, null);

			this.consumerTag = this.channel.basicConsume(this.responseQueue, new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					long deliveryTag = envelope.getDeliveryTag();
					String correlationId = properties.getCorrelationId();

					logger.debug("Got a response [correlationId: %s]", correlationId);

					channel.basicAck(deliveryTag, false);

					Slot slot = slots.get(correlationId);

					if (slot != null) {
						try {
							String contentType = properties.getContentType();
							Map<String, Object> headers = properties.getHeaders();
							Integer statusCode = (int) headers.get("statusCode");

							logger.debug("Publishing the response to the future [correlationId: %s]", correlationId);
							slot.getFuture().put(new Response(body, contentType, statusCode, headers));
						} finally {
							slot.free();
						}
					} else {
						logger.error("Got a response of an unknow correlation id");
					}
				}
			});
		} catch (IOException e) {
			throw new BrokerException(e);
		}
	}

	private synchronized String getNewCorrelationId() {
		long requestNumber = ++this.requestNumber;

		// prevent overflow.
		if (requestNumber == Long.MAX_VALUE) {
			this.requestNumber = 1;
		}

		return String.valueOf(this.requestNumber);
	}

	protected synchronized Slot getNewSlot() {
		return new Slot(this.slots, getNewCorrelationId());
	}

	protected void sendRequest(String method, byte[] body, String contentType, String correlationId)
			throws IOException {
		this.logger.debug("Sending request [service: %s] [method: %s] [correlationId: %s]", serviceName, method,
				correlationId);

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("X-Method", method);

		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder().contentType(contentType)
				.headers(headers).expiration(String.valueOf(this.requestMessageTTL));

		if (correlationId != null) {
			builder.correlationId(correlationId).replyTo(this.responseQueue);
		}

		BasicProperties props = builder.build();

		this.channel.basicPublish("", this.serviceName, false, props, body);
	}

	protected void sendRequest(String method, byte[] body, String contentType) throws IOException {
		sendRequest(method, body, contentType, null);
	}

	private static Connection createDefaultConnection(String amqpURL) throws BrokerException {
		ConnectionFactory factory = new ConnectionFactory();

		try {
			factory.setUri(amqpURL);
			factory.setRequestedHeartbeat(5);
			return factory.newConnection();
		} catch (Exception e) {
			throw new BrokerException(e);
		}
	}

}
