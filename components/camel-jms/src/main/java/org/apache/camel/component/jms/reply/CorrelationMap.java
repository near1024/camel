/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jms.reply;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.util.DefaultTimeoutMap;

/**
 * @version
 */
public class CorrelationMap extends DefaultTimeoutMap<String, ReplyHandler> {

    private CorrelationListener listener;

    public CorrelationMap(ScheduledExecutorService executor, long requestMapPollTimeMillis) {
        super(executor, requestMapPollTimeMillis);
    }

    public void setListener(CorrelationListener listener) {
        // there is only one listener needed
        this.listener = listener;
    }

    public boolean onEviction(String key, ReplyHandler value) {
        try {
            if (listener != null) {
                listener.onEviction(key);
            }
        } catch (Throwable e) {
            // ignore
        }

        // trigger timeout
        value.onTimeout(key);
        // return true to remove the element
        return true;
    }

    @Override
    public void put(String key, ReplyHandler value, long timeoutMillis) {
        try {
            if (listener != null) {
                listener.onPut(key);
            }
        } catch (Throwable e) {
            // ignore
        }

        if (timeoutMillis <= 0) {
            // no timeout (must use Integer.MAX_VALUE)
            super.put(key, value, Integer.MAX_VALUE);
        } else {
            super.put(key, value, timeoutMillis);
        }
    }

    @Override
    public ReplyHandler remove(String key) {
        try {
            if (listener != null) {
                listener.onRemove(key);
            }
        } catch (Throwable e) {
            // ignore
        }

        ReplyHandler answer = super.remove(key);
        return answer;
    }

}
