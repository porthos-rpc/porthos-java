package com.github.porthos.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Call represents an intention of a RPC call.
 * It holds the state of the call, such as the content, content type and method name.
 * @author Germano Fronza.
 *
 */
public class Call {

    private Client client;
    private String method;
    private byte[] body;
    private String contentType = "application/octet-stream";

    /**
     * Constructor takes the client and the name of the method to be invoked.
     * @param client
     * @param method
     */
    protected Call(Client client, String method) {
        this.client = client;
        this.method = method;
    }

    /**
     * Sets the body (payload) of the method to be invoked.
     * @param bodyBuffer
     * @return this same Call object.
     */
    public Call withBody(byte[] bodyBuffer) {
        this.body = bodyBuffer;
        this.contentType = "application/octet-stream";
        return this;
    }

    /**
     * Sets the json body (payload) of the method to be invoked.
     * @param json
     * @return this same Call object.
     */
    public Call withJSON(String json) {
        this.body = json.getBytes();
        this.contentType = "application/json";
        return this;
    }

    /**
     * Makes the RPC call and returns a Future of the Response.
     * This is the final method of this DSL.
     * @return ResponseFuture.
     * @throws IOException
     */
    public ResponseFuture async() throws IOException {
        Slot slot = this.client.getNewSlot();

        this.client.sendRequest(this.method, this.body, this.contentType, slot.getCorrelationId());

        return slot.getFuture();
    }

    /**
     * Makes the RPC call and returns the Response.
     * This method hold the execution until a response is available.
     * This is the final method fo this DSL.
     * @return Response
     * @throws InterruptedException
     * @throws IOException
     */
    public Response sync() throws InterruptedException, IOException {
        return this.async().get();
    }

    /**
     * Makes the RPC call and returns the Response.
     * This method hold the execution until a response is available or the given timeout is reached.
     * This is the final method fo this DSL.
     * @param timeout
     * @param unit
     * @return Response
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws IOException
     */
    public Response sync(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, IOException {
        return this.async().get(timeout, unit);
    }

    /**
     * Makes the RPC call, but does not care about any response.
     * @throws IOException
     */
    public void noResponse() throws IOException {
        this.client.sendRequest(this.method, this.body, this.contentType);
    }
}
