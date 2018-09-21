/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

/**
 * Holder class for constants used throughout centrifuge.
 */
public class Constants {
    public static final String LOGGER_PREFIX = "[CENTRIFUGE] ";

    public static final String PROPERTY_KEY_DEFAULT_TIMEOUT_MILLIS = "centrifuge.default.timeout_millis";
    public static final String PROPERTY_KEY_DEFAULT_YIELD_MILLIS = "centrifuge.default.yield_millis";
    public static final String PROPERTY_KEY_DEFAULT_CONCURRENCY = "centrifuge.default.concurrency";
    public static final String PROPERTY_KEY_DEFAULT_MAX_ITERATIONS = "centrifuge.default.max_iterations";
    public static final String PROPERTY_KEY_DEFAULT_MAX_FAILURE = "centrifuge.default.max_failure";

    public static final int PROPERTY_VALUE_DEFAULT_TIMEOUT_MILLIS = Integer.MAX_VALUE;
    public static final int PROPERTY_VALUE_DEFAULT_CLEANUP_INTERVAL_MILLIS = 300;
    public static final int PROPERTY_VALUE_DEFAULT_YIELD_MILLIS = 10;
    public static final int PROPERTY_VALUE_DEFAULT_LOG_INTERVAL_SECONDS = 30;
    public static final int PROPERTY_VALUE_DEFAULT_CONCURRENCY = 1;
    public static final long PROPERTY_VALUE_DEFAULT_MAX_ITERATIONS = Long.MAX_VALUE;
    public static final long PROPERTY_VALUE_DEFAULT_MAX_FAILURE = Long.MAX_VALUE;

    public static final String CONFIG_KEY_WARMERS = "centrifuge.warmers";
    public static final String CONFIG_KEY_PARALLELISM = "centrifuge.parallelism";
    public static final String CONFIG_KEY_LOG_INTERVAL_SECONDS = "centrifuge.log_interval_seconds";
    public static final String CONFIG_KEY_CLEANUP_INTERVAL_MILLIS = "centrifuge.cleanup_interval_millis";

    public static final String CONFIG_KEY_CLASS = "class";
    public static final String CONFIG_KEY_NAME = "name";
    public static final String CONFIG_KEY_CONCURRENCY = "concurrency";
    public static final String CONFIG_KEY_TIMEOUT_MILLIS = "timeout_millis";
    public static final String CONFIG_KEY_YIELD_MILLIS = "yield_millis";
    public static final String CONFIG_KEY_REQUIRED = "required";
    public static final String CONFIG_KEY_MAX_ITERATIONS = "max_iterations";
    public static final String CONFIG_KEY_MAX_FAILURE = "max_failure";
    public static final String CONFIG_KEY_PARAMS = "params";
}
