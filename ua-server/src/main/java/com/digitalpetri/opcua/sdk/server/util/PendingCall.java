/*
 * digitalpetri OPC-UA SDK
 *
 * Copyright (C) 2015 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.opcua.sdk.server.util;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.stack.core.types.structured.CallMethodRequest;
import com.digitalpetri.opcua.stack.core.types.structured.CallMethodResult;

public class PendingCall implements Pending<CallMethodRequest, CallMethodResult> {

    private final CompletableFuture<CallMethodResult> future = new CompletableFuture<>();

    private final CallMethodRequest request;

    public PendingCall(CallMethodRequest request) {
        this.request = request;
    }

    @Override
    public CompletableFuture<CallMethodResult> getFuture() {
        return future;
    }

    @Override
    public CallMethodRequest getInput() {
        return request;
    }

}
