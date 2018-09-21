/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SleepWarmer implements Warmer {
    static final Logger logger = LoggerFactory.getLogger(SleepWarmer.class);
    boolean isInitCalled;
    int nextCount;

    public SleepWarmer() {
        logger.info("constructor called.");
        isInitCalled = false;
        nextCount = 0;
    }

    @Override
    public void init(final Map<String, Object> params) throws Exception {
        logger.info("init called.");
        isInitCalled = true;
    }

    @Override
    public void next() throws Exception {
        logger.info("next called.");
        nextCount++;
        Thread.sleep(100);
    }
}
