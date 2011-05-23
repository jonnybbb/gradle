/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.messaging.remote.internal;

import org.gradle.messaging.remote.internal.protocol.ChannelAvailable;
import org.gradle.messaging.remote.internal.protocol.ChannelUnavailable;
import org.gradle.messaging.remote.internal.protocol.DiscoveryMessage;
import org.gradle.messaging.remote.internal.protocol.LookupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChannelLookupProtocol implements Protocol<DiscoveryMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelLookupProtocol.class);
    private final Map<String, RequestDetails> pending = new HashMap<String, RequestDetails>();
    private ProtocolContext<DiscoveryMessage> context;

    public void start(ProtocolContext<DiscoveryMessage> context) {
        this.context = context;
    }

    public void handleOutgoing(DiscoveryMessage message) {
        if (message instanceof LookupRequest) {
            LookupRequest lookupRequest = (LookupRequest) message;
            LOGGER.debug("Broadcasting lookup request {}.", lookupRequest);
            RequestDetails request = new RequestDetails(lookupRequest);
            pending.put(lookupRequest.getChannel(), request);
            request.run();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void handleIncoming(DiscoveryMessage message) {
        if (message instanceof ChannelAvailable) {
            ChannelAvailable channelAvailable = (ChannelAvailable) message;
            RequestDetails request = pending.remove(channelAvailable.getChannel());
            if (request != null) {
                LOGGER.debug("Channel discovered: {}.", channelAvailable);
                request.handleResponse(channelAvailable);
            } else {
                LOGGER.debug("Channel discovered: {}. Ignoring", channelAvailable);
            }
        } else if (!(message instanceof LookupRequest) && !(message instanceof ChannelUnavailable)) {
            // Discard
            LOGGER.debug("Received unknown discovery message {}. Discarding.", message);
        }
        // Else ignore
    }

    public void stopRequested() {
        context.stopped();
    }

    private class RequestDetails implements Runnable {
        private final LookupRequest lookupRequest;
        ProtocolContext.Callback timeout;
        int attempts;

        public RequestDetails(LookupRequest lookupRequest) {
            this.lookupRequest = lookupRequest;
        }

        public void handleResponse(ChannelAvailable channelAvailable) {
            timeout.cancel();
            context.dispatchIncoming(channelAvailable);
        }

        public void run() {
            attempts++;
            timeout = context.callbackLater(getTimeoutSeconds(), TimeUnit.SECONDS, this);
            context.dispatchOutgoing(lookupRequest);
        }

        private int getTimeoutSeconds() {
            if (attempts > 10) {
                return 30;
            }
            if (attempts > 5) {
                return 10;
            }
            return 1;
        }

    }
}