/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.salesforce.centrifuge.Constants.LOGGER_PREFIX;

/**
 * Works as a contained thread to initialize and execute a single warmer.
 */
public class WarmerContainer {
    private final Logger logger = LoggerFactory.getLogger(WarmerContainer.class);

    // instance counter used to keep track of total number of warmers
    private static final AtomicInteger instanceCounter = new AtomicInteger();

    // instance number of this warmer container
    private final int instanceIndex = instanceCounter.incrementAndGet();

    // warmer config passed by user
    private final WarmerConfig config;

    // warmer class
    private final Class warmerClass;

    // warmer instance
    private final Warmer warmer;

    // the thread executing this warmer
    private volatile Thread warmerThread;

    // executor service to watch and interrupt thread on timeout
    private final ScheduledExecutorService executorService;

    // set to true when stop() is called
    private volatile boolean isStopped = false;

    // set to true when all iterations are completed successfully
    private volatile boolean isCompleted = false;

    // set to true the first time next() is called; back to false when it is finished
    private volatile boolean isRunning = false;

    // set to true when init() ran successfully
    private volatile boolean isInitialized = false;

    // number of successful rounds completed
    private volatile int successfulRounds = 0;

    // number of rounds throwing an exception
    private volatile int failedRounds = 0;

    // current iteration
    private volatile int iteration = 0;

    // millis timestamp for when start() is called
    private volatile long startTimestampMillis = 0L;

    // accumulative duration of all executions
    private volatile long durationNanos = 0L;

    // thread name before executing this task
    private String oldThreadName;

    @SuppressWarnings("unused")
    private WarmerContainer() {
        throw new UnsupportedOperationException();
    }

    // warmer container is used internally; constructor is not exposed to users.
    WarmerContainer(final WarmerConfig config, final ScheduledExecutorService executorService)
            throws Exception {
        this.config = config;
        this.executorService = executorService;
        this.warmerClass = config.getWarmerClass();
        this.warmer = (Warmer) this.warmerClass.newInstance();
    }

    public String getName() {
        return this.getWarmerConfig().getWarmerName() + "-" + this.instanceIndex;
    }

    public Warmer getWarmer() {
        return this.warmer;
    }

    public WarmerConfig getWarmerConfig() {
        return this.config;
    }

    public int getIteration() {
        return this.iteration;
    }

    public long getStartTimestampMillis() {
        return this.startTimestampMillis;
    }

    public long getDurationNanos() {
        return this.durationNanos;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public int getSuccessfulRounds() {
        return this.successfulRounds;
    }

    public int getFailedRounds() {
        return this.failedRounds;
    }

    void start() {
        this.startTimestampMillis = System.currentTimeMillis();
        schedule(this::run);
    }

    synchronized void stop() {
        logger.info(LOGGER_PREFIX + "stop called on warmer {} after {}ms",
                getName(), System.currentTimeMillis() - getStartTimestampMillis());

        this.isStopped = true;
        if (this.warmerThread != null) {
            this.warmerThread.interrupt();
        }
    }

    private long prepareForExecution() {
        // keep a reference to current thread
        this.warmerThread = Thread.currentThread();

        // flag as running
        this.isRunning = true;

        // keep track of the thread name
        this.oldThreadName = this.warmerThread.getName();

        // set thread name
        this.warmerThread.setName("centrifuge-warmer-" + getName());

        return System.nanoTime();
    }

    private long postExecution() {
        // flag as not running anymore
        this.isRunning = false;

        // set thread name
        this.warmerThread.setName(this.oldThreadName);

        // unset current thread
        this.warmerThread = null;

        return System.nanoTime();
    }

    private synchronized void run() {
        final long startNanos = prepareForExecution();
        try {
            if (!shouldRun()) {
                this.isStopped = true;
                logger.info(LOGGER_PREFIX + "warmer {} stopped.", getName());
                return;
            }

            if (!isInitialized()) {
                // call init() if not initialized yet
                logger.info(LOGGER_PREFIX + "trying to initialize warmer {}...", getName());
                synchronized (this.warmerClass) {
                    getWarmer().init(getWarmerConfig().getParams());
                }
                logger.info(LOGGER_PREFIX + "warmer {} initialized successfully", getName());
                this.isInitialized = true;
            } else {
                // call next if already initialized
                this.iteration++;
                synchronized (this.warmerClass) {
                    getWarmer().next();
                }
                this.successfulRounds++;
            }

            // schedule to run again
            schedule(this::run);
        } catch (InterruptedException e) {
            logger.warn(LOGGER_PREFIX + "warmer {} execution interrupted.", getName());
            this.failedRounds++;
            this.isStopped = true;
        } catch (Exception e) {
            logger.warn(LOGGER_PREFIX + "warmer {} execution failed with error '{}'.",
                    getName(), e.getMessage());
            this.failedRounds++;
            // schedule to run again
            schedule(this::run);
        } finally {
            this.durationNanos += postExecution() - startNanos;
        }
    }

    private boolean shouldRun() {
        // stop if flagged to stop
        if (this.isStopped) {
            logger.info(LOGGER_PREFIX + "warmer {} flagged to stop.", getName());
            return false;
        }
        // stop if too many failures
        if (getFailedRounds() == getWarmerConfig().getMaxFailure()) {
            logger.warn(LOGGER_PREFIX + "warmer failure count higher than allowed {}; " +
                    "stopping execution of warmer {}", getFailedRounds(), getName());
            return false;
        }
        // stop if max iterations reached
        if (getIteration() == getWarmerConfig().getMaxIterations()) {
            logger.info(LOGGER_PREFIX + "warmer {} reached max iterations {}.",
                    getName(), getWarmerConfig().getMaxIterations());
            this.isCompleted = true;
            return false;
        }
        // stop if interrupted
        if (Thread.currentThread().isInterrupted()) {
            logger.warn(LOGGER_PREFIX + "warmers execution thread is interrupted after {} iterations.",
                    getSuccessfulRounds());
            return false;
        }
        return true;
    }

    private void schedule(final Runnable runnable) {
        // schedule and yield
        this.executorService.schedule(runnable, getWarmerConfig().getYieldMillis(), TimeUnit.MILLISECONDS);
    }
}

