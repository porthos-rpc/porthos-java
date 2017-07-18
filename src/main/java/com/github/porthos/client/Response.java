package com.github.porthos.client;

import java.util.Map;

import com.rabbitmq.tools.json.JSONReader;

/**
 * Represents a RPC Response.
 * @author Germano Fronza
 *
 */
public class Response {

    private byte[] content;
    private String contentType;
    private Integer statusCode;
    private Map<String, Object> headers;

    protected Response(byte[] content, String contentType, Integer statusCode, Map<String, Object> headers) {
        this.content = content;
        this.contentType = contentType;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public byte[] getContent() {
        return this.content;
    }

    public Object getContentAsJSON() {
        if (this.contentType.equals("application/json")) {
            return new JSONReader().read(new String(this.content));
        } else {
           throw new IllegalStateException(String.format("The correct contentType of this response is: %s", this.content));
        }
    }

    public String getContentType() {
        return this.contentType;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

    public Map<String, Object> getHeaders() {
        return this.headers;
    }
}
