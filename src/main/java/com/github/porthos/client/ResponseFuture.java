package com.github.porthos.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents a future response of a RPC call.
 * @author Germano Fronza
 *
 */
public final class ResponseFuture implements Future<Response> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private Response value;
        private boolean cancelled;
        private Slot slot;

        public ResponseFuture(Slot slot) {
            this.slot = slot;
        }

        /**
         * Cancel this future.
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            this.cancelled = true;

            if (this.latch.getCount() > 0) {
                this.latch.countDown();
            }

            return this.cancelled;
        }

        /**
         * Returns true if this future is canceled.
         */
        @Override
        public boolean isCancelled() {
            return this.cancelled && this.latch.getCount() == 0;
        }

        /**
         * Returns true if this future is done (and not canceled).
         */
        @Override
        public boolean isDone() {
            return !this.cancelled && this.latch.getCount() == 0;
        }

        /**
         * Attempts to get the Response from this future.
         * Awaits for as long as needed.
         */
        @Override
        public Response get() throws InterruptedException {
            this.latch.await();
            return this.value;
        }

        /**
         * Attempts to get the Response from this future.
         * Awaits for the given timeout.
         */
        @Override
        public Response get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            if (this.latch.await(timeout, unit)) {
                return this.value;
            } else {
                this.slot.free();
                throw new TimeoutException();
            }
        }

        protected void put(Response result) {
            if (this.latch.getCount() > 0 && this.value == null) {
                this.value = result;
                this.latch.countDown();
            }
        }
    }