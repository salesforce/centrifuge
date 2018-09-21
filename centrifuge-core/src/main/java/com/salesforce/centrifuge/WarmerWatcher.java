/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.salesforce.centrifuge.Constants.LOGGER_PREFIX;

/**
 * Works as a contained callable class to initialize and execute warmers.
 */
public class WarmerWatcher {
    private final Logger logger = LoggerFactory.getLogger(WarmerWatcher.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("centrifuge-watcher").build()
    );
    private final Centrifuge centrifugeInstance;
    private final int cleanupIntervalMillis;
    private final int logIntervalSeconds;
    private String lastLog;

    @SuppressWarnings("unused")
    private WarmerWatcher(CentrifugeImpl centrifugeInstance, int cleanupIntervalMillis) {
        throw new UnsupportedOperationException();
    }

    // warmer container is used internally; constructor is not exposed to users.
    WarmerWatcher(final Centrifuge centrifugeInstance, int cleanupIntervalMillis, int logIntervalSeconds) {
        this.centrifugeInstance = centrifugeInstance;
        this.cleanupIntervalMillis = cleanupIntervalMillis;
        this.logIntervalSeconds = logIntervalSeconds;

        logStats();
    }

    void run() {
        try {
            if (doCleanup()) {
                scheduleNextRound();
            }
        } catch (Exception e) {
            logger.warn(LOGGER_PREFIX + "exception on executing warmer watcher run: ", e);
        }
    }

    // stop warmers that have to be stopped; return true if next run is needed, false otherwise.
    private boolean doCleanup() {
        final List<WarmerContainer> warmers = this.centrifugeInstance.getWarmers();
        for (final WarmerContainer wc : warmers) {
            if (! wc.isStopped() && wc.getDurationNanos()/1_000_000 > wc.getWarmerConfig().getTimeoutMillis()) {
                wc.stop();
            }
        }
        boolean allStopped = true;
        for (final WarmerContainer wc : warmers) {
            if (! wc.isStopped()) {
                allStopped = false;
            }
        }
        if (allStopped) {
            logStats();
            this.centrifugeInstance.stop();
            this.executorService.shutdownNow();
            return false;
        }
        return true;
    }

    private void scheduleNextRound() {
        if (! this.executorService.isShutdown()) {
            this.executorService.schedule(this::run, this.cleanupIntervalMillis, TimeUnit.MILLISECONDS);
        }
    }

    private void logStats() {
        final StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append("centrifuge stats:\n---\n");
        this.centrifugeInstance.getWarmers().forEach(w -> {
            statsBuilder
                    .append(" * warmer=").append(w.getName())
                    .append("  iteration=").append(w.getIteration())
                    .append("  success=").append(w.getSuccessfulRounds())
                    .append("  failure=").append(w.getFailedRounds())
                    .append("  start_millis=").append(w.getStartTimestampMillis())
                    .append("  duration_millis=").append(w.getDurationNanos()/1_000_000)
                    .append("  timeout_millis=").append(w.getWarmerConfig().getTimeoutMillis())
                    .append("  initialized=").append(w.isInitialized())
                    .append("  running=").append(w.isRunning())
                    .append("  stopped=").append(w.isStopped())
                    .append("\n")
            ;
        });
        statsBuilder.append("---\n");

        final String theLog = statsBuilder.toString();
        // only log if something is changed
        if (! theLog.equals(this.lastLog)) {
            logger.info(LOGGER_PREFIX + statsBuilder.toString());
        }
        this.lastLog = theLog;
        this.executorService.schedule(this::logStats, this.logIntervalSeconds, TimeUnit.SECONDS);
    }
}

