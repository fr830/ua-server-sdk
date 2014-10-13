/*
 * Copyright 2014 Inductive Automation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inductiveautomation.opcua.sdk.server.util;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.inductiveautomation.opcua.sdk.server.api.AttributeManager;
import com.inductiveautomation.opcua.sdk.server.api.MonitoredItem;
import com.inductiveautomation.opcua.sdk.server.api.SampledItem;
import com.inductiveautomation.opcua.stack.core.types.builtin.DataValue;
import com.inductiveautomation.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.inductiveautomation.opcua.stack.core.types.structured.ReadValueId;
import com.inductiveautomation.opcua.stack.core.util.ExecutionQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;

public class SubscriptionModel {

    private static final ScheduledExecutorService SharedScheduler = Executors.newSingleThreadScheduledExecutor();

    private final Set<SampledItem> itemSet = Collections.newSetFromMap(Maps.newConcurrentMap());

    private final List<ScheduledUpdate> schedule = Lists.newCopyOnWriteArrayList();

    private final ExecutionQueue<Runnable> executionQueue;

    private final AttributeManager attributeManager;
    private final ExecutorService executorService;

    public SubscriptionModel(AttributeManager attributeManager, ExecutorService executorService) {
        this.attributeManager = attributeManager;
        this.executorService = executorService;

        executionQueue = new ExecutionQueue<>(ExecutionQueue.RUNNABLE_EXECUTOR, executorService);
    }

    public void onSampledItemsCreated(List<SampledItem> items) {
        executionQueue.submit(() -> {
            itemSet.addAll(items);
            reschedule();
        });
    }

    public void onSampledItemsModified(List<SampledItem> items) {
        executionQueue.submit(this::reschedule);
    }

    public void onSampledItemsDeleted(List<SampledItem> items) {
        executionQueue.submit(() -> {
            itemSet.removeAll(items);
            reschedule();
        });
    }

    public void onMonitoringModeChanged(List<MonitoredItem> items) {
        executionQueue.submit(this::reschedule);
    }

    private void reschedule() {
        Map<Double, List<SampledItem>> bySamplingInterval = itemSet.stream()
                .filter(SampledItem::isSamplingEnabled)
                .collect(Collectors.groupingBy(SampledItem::getSamplingInterval));

        List<ScheduledUpdate> updates = bySamplingInterval.keySet().stream().map(samplingInterval -> {
            List<SampledItem> items = bySamplingInterval.get(samplingInterval);

            return new ScheduledUpdate(samplingInterval, items);
        }).collect(Collectors.toList());

        schedule.forEach(ScheduledUpdate::cancel);
        schedule.clear();
        schedule.addAll(updates);
        schedule.forEach(SharedScheduler::execute);
    }

    private class ScheduledUpdate implements Runnable {

        private volatile boolean cancelled = false;

        private final long samplingInterval;
        private final List<SampledItem> items;

        private ScheduledUpdate(double samplingInterval, List<SampledItem> items) {
            this.samplingInterval = DoubleMath.roundToLong(samplingInterval, RoundingMode.UP);
            this.items = items;
        }

        private void cancel() {
            cancelled = true;
        }

        @Override
        public void run() {
            List<PendingRead> pending = items.stream()
                    .map(item -> new PendingRead(item.getReadValueId()))
                    .collect(Collectors.toList());

            List<ReadValueId> ids = pending.stream()
                    .map(PendingRead::getInput)
                    .collect(Collectors.toList());

            CompletableFuture<List<DataValue>> future = Pending.callback(pending);

            future.thenAcceptAsync(values -> {
                Iterator<SampledItem> ii = items.iterator();
                Iterator<DataValue> vi = values.iterator();

                while (ii.hasNext() && vi.hasNext()) {
                    ii.next().setValue(vi.next());
                }

                if (!cancelled) {
                    SharedScheduler.schedule(this, samplingInterval, TimeUnit.MILLISECONDS);
                }
            }, executorService);

            executorService.execute(() -> attributeManager.read(ids, 0d, TimestampsToReturn.Both, future));
        }

    }

}