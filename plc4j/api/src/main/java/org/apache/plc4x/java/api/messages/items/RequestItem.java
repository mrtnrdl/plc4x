/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.api.messages.items;

import org.apache.plc4x.java.api.model.Address;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class RequestItem<DATA_TYPE, RESPONSE_ITEM> {

    private final Class<DATA_TYPE> datatype;

    private final Address address;

    private volatile RESPONSE_ITEM responseItem;

    private final Lock lock = new ReentrantLock();

    private final Condition responseSet = lock.newCondition();

    public RequestItem(Class<DATA_TYPE> datatype, Address address) {
        this.datatype = datatype;
        this.address = address;
    }

    public Class<DATA_TYPE> getDatatype() {
        return datatype;
    }

    public Address getAddress() {
        return address;
    }

    public CompletableFuture<RESPONSE_ITEM> getResponseItem() {
        return CompletableFuture.supplyAsync(() -> {
            if (responseItem == null) {
                try {
                    lock.lock();
                    responseSet.await();
                } catch (InterruptedException e) {
                    throw new CompletionException(e);
                } finally {
                    lock.unlock();
                }
            }
            return responseItem;
        });
    }

    protected void setResponseItem(RESPONSE_ITEM responseItem) {
        Objects.requireNonNull(responseItem);
        try {
            lock.lock();
            responseSet.signalAll();
        } finally {
            lock.unlock();
        }
        this.responseItem = responseItem;
    }
}