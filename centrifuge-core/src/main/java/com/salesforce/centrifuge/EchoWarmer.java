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

import static com.salesforce.centrifuge.Constants.LOGGER_PREFIX;

/**
 * Sample implementation of Warmer interface.
 */
public class EchoWarmer implements Warmer {

    private static final Logger logger = LoggerFactory.getLogger(EchoWarmer.class);
    private String text;

    public EchoWarmer() {
        logger.info(LOGGER_PREFIX + getClass().getSimpleName() + " default constructor called.");
    }

    @Override
    public void init(final Map<String, Object> params) throws Exception {
        logger.info(LOGGER_PREFIX + getClass().getSimpleName() + " init() called.");
        this.text = String.valueOf(params.get("text"));
    }

    @Override
    public void next() throws Exception {
        logger.info(LOGGER_PREFIX + getClass().getSimpleName() + " next() called; text is {}", this.text);
    }
}
