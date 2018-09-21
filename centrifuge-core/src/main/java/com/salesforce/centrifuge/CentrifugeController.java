/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

/**
 * Executes MBean initiated operations on the contained {@link Centrifuge} instance.
 */
public class CentrifugeController implements CentrifugeControllerMBean {

    private final Centrifuge centrifuge;

    @SuppressWarnings("unused")
    private CentrifugeController() {
        throw new UnsupportedOperationException();
    }

    public CentrifugeController(final Centrifuge centrifuge) {
        this.centrifuge = centrifuge;
    }

    /**
     * Start will delegate down to {@link Centrifuge} async call method.
     */
    @Override
    public void start() {
        this.centrifuge.start();
    }

    /**
     * Stop will delegate down to {@link Centrifuge} stop method.
     */
    @Override
    public void stop() {
        this.centrifuge.stop();
    }
}