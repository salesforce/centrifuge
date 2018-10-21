/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.List;

import static com.salesforce.centrifuge.Constants.LOGGER_PREFIX;
import static com.salesforce.centrifuge.Constants.PROPERTY_VALUE_DEFAULT_YIELD_MILLIS;

public final class CentrifugeImpl implements Centrifuge {
    private static final Logger logger = LoggerFactory.getLogger(CentrifugeImpl.class);

    private final CentrifugeConfig config;
    private final List<WarmerContainer> warmerContainers = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService executorService = null;
    private WarmerWatcher watcher;

    private long startTimestampMillis = 0;
    private long startTimestampNanos = 0;

    @SuppressWarnings("unused")
    CentrifugeImpl() {
        throw new UnsupportedOperationException();
    }

    public CentrifugeImpl(final CentrifugeConfig config) {
        this.config = config;
    }

    @Override
    public synchronized void start() {
        if (this.startTimestampMillis != 0) {
            logger.warn(LOGGER_PREFIX + "centrifuge is already running; ignoring call request");
            return;
        }

        this.executorService = Executors.newScheduledThreadPool(this.config.getParallelism());
        config.getWarmerConfigs().forEach(this::registerWarmer);

        // set up watcher (cleanup) thread
        this.watcher = new WarmerWatcher(
                this,
                this.config.getCleanupIntervalMillis(),
                this.config.getLogIntervalSeconds()
        );

        this.startTimestampNanos = System.nanoTime();
        this.startTimestampMillis = System.currentTimeMillis();

        this.watcher.run();

        logger.info(LOGGER_PREFIX + "trying to call centrifuge...");
        doStart();
    }

    @Override
    public synchronized void stop() {
        logger.info(LOGGER_PREFIX + "calling shutdown now on executor service");
        this.executorService.shutdownNow();

        logger.info(LOGGER_PREFIX + "calling stop on all warmers");
        this.warmerContainers.forEach(WarmerContainer::stop);

        // set to null for GC
        this.executorService = null;

        logger.info(LOGGER_PREFIX + "centrifuge execution started at {} and finished at {} - took {}millis.",
                Instant.ofEpochMilli(this.startTimestampMillis),
                Instant.now(),
                (System.nanoTime() - this.startTimestampNanos) / 1_000_000);
    }

    @Override
    public boolean isWarm() {
        boolean isWarm = true;
        for (final WarmerContainer wc : getWarmers()) {
            if (wc.getWarmerConfig().isRequired() && ! wc.isStopped()) {
                isWarm = false;
            }
        }
        return isWarm;
    }

    @Override
    public void registerMbean() {
        try {
            final String mbeanName = getMbeanName();
            logger.info(LOGGER_PREFIX + "registering mbean: {}", mbeanName);
            ManagementFactory.getPlatformMBeanServer().registerMBean(
                    new CentrifugeController(this), new ObjectName(mbeanName)
            );
        } catch (Throwable e) {
            logger.warn(LOGGER_PREFIX + "failed to register centrifuge mbean");
        }
    }

    @Override
    public List<WarmerContainer> getWarmers() {
        return ImmutableList.copyOf(this.warmerContainers);
    }

    @Override
    public void registerWarmer(final WarmerConfig warmerConfig) {
        logger.info(LOGGER_PREFIX + "registering warmer {}", warmerConfig.getWarmerClass());
        try {
            for (int i = 0; i < warmerConfig.getConcurrency(); ++i) {
                this.warmerContainers.add(new WarmerContainer(warmerConfig, this.executorService));
            }
        } catch (Exception e) {
            logger.warn(LOGGER_PREFIX + "failed to register warmer", e);
        }
    }

    String getMbeanName() {
        return CentrifugeControllerMBean.class.getPackage().getName()
                + ":type=CentrifugeController";
    }

    private void doStart() {
        if (this.warmerContainers.isEmpty()) {
            logger.warn(LOGGER_PREFIX + "null/empty warmers; ignoring schedule");
            return;
        }

        logger.info(LOGGER_PREFIX + "scheduling threads to start...");
        this.warmerContainers.forEach(wc -> this.executorService.schedule(wc::start,
                ThreadLocalRandom.current().nextInt(0, 2 * PROPERTY_VALUE_DEFAULT_YIELD_MILLIS),
                TimeUnit.MILLISECONDS));
    }
}

