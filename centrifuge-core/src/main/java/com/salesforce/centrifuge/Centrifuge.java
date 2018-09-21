/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import java.util.List;

public interface Centrifuge {

    // the no-op instance
    Centrifuge NOOP_INSTANCE = new CentrifugeImpl(new CentrifugeConfig());

    /**
     * Creates new {@link Centrifuge} instance.
     *
     * @param config  corresponding configuration
     * @return {@link Centrifuge} instance
     */
    static Centrifuge newInstance(final CentrifugeConfig config) {
        if (config == null) {
            return NOOP_INSTANCE;
        }
        return new CentrifugeImpl(config);
    }

    /**
     * Allows registering a warmer.
     *
     * @param warmerConfig corresponding warmer config
     */
    void registerWarmer(WarmerConfig warmerConfig);

    /**
     * Starts {@link Centrifuge} which will schedule all registered {@link Warmer}s to run
     * and returns immediately.
     */
    void start();

    /**
     * Sends stop/cancel signal to all running/waiting threads and returns immediately.
     * no-op if it is not started yet.
     */
    void stop();

    /**
     * Check for whether all required warmers have finished executing.
     *
     * @return true if all required warmers are stopped; false otherwise.
     */
    boolean isWarm();

    /**
     * Register controller MBean that can be used to call/stop the engine.
     */
    void registerMbean();

    /**
     * Returns all registered warmers.
     *
     * @return list of registered warmers.
     */
    List<WarmerContainer> getWarmers();
}