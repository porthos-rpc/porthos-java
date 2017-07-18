package com.github.porthos.client;

import java.util.Map;


public class Slot {

    private final Map<String, Slot> slotsContainer;
    private String correlationId;
    private ResponseFuture future = new ResponseFuture(this);

    protected Slot(Map<String, Slot> slotsContainer, String correlationId) {
        this.slotsContainer = slotsContainer;
        this.correlationId = correlationId;

        this.slotsContainer.put(correlationId, this);
    }

    protected String getCorrelationId() {
        return this.correlationId;
    }

    protected ResponseFuture getFuture() {
        return this.future;
    }

    protected void free() {
        this.slotsContainer.remove(this);
    }
}
