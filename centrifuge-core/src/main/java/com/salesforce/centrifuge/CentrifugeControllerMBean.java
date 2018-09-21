/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

/**
 * MBean interface for controlling {@link Centrifuge}.
 */
public interface CentrifugeControllerMBean {

    /**
     * Start running Centrifuge engine.
     */
    void start();

    /**
     * Stop Centrifuge engine if it is running.
     */
    void stop();
}