/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

import static com.salesforce.centrifuge.Constants.*;

public class WarmerConfig {

    static final int DEFAULT_TIMEOUT_MILLIS = Integer.getInteger(PROPERTY_KEY_DEFAULT_TIMEOUT_MILLIS,
            PROPERTY_VALUE_DEFAULT_TIMEOUT_MILLIS);

    static final int DEFAULT_YIELD_MILLIS = Integer.getInteger(PROPERTY_KEY_DEFAULT_YIELD_MILLIS,
            PROPERTY_VALUE_DEFAULT_YIELD_MILLIS);

    static final int DEFAULT_CONCURRENCY = Integer.getInteger(PROPERTY_KEY_DEFAULT_CONCURRENCY,
            PROPERTY_VALUE_DEFAULT_CONCURRENCY);

    static final long DEFAULT_MAX_ITERATIONS = Long.getLong(PROPERTY_KEY_DEFAULT_MAX_ITERATIONS,
            PROPERTY_VALUE_DEFAULT_MAX_ITERATIONS);

    static final long DEFAULT_MAX_FAILURE = Long.getLong(PROPERTY_KEY_DEFAULT_MAX_FAILURE,
            PROPERTY_VALUE_DEFAULT_MAX_FAILURE);

    private String warmerClass = "";
    private String name = "";
    private int timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
    private int yieldMillis = DEFAULT_YIELD_MILLIS;
    private long maxIterations = DEFAULT_MAX_ITERATIONS;
    private long maxFailure = DEFAULT_MAX_FAILURE;
    private int concurrency = DEFAULT_CONCURRENCY;
    private boolean required = false;
    private Map<String, Object> params = Collections.emptyMap();

    public long getMaxIterations() {
        return this.maxIterations;
    }

    public WarmerConfig setMaxIterations(long maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }

    public long getTimeoutMillis() {
        return this.timeoutMillis;
    }


    public WarmerConfig setTimeoutMillis(int timeoutMillis) {
        // ignore invalida values
        if (timeoutMillis > 0) {
            this.timeoutMillis = timeoutMillis;
        }
        return this;
    }

    public long getMaxFailure() {
        return this.maxFailure;
    }

    public WarmerConfig setMaxFailure(long maxFailure) {
        this.maxFailure = maxFailure;
        return this;
    }

    public int getYieldMillis() {
        return this.yieldMillis;
    }

    public WarmerConfig setYieldMillis(int yieldMillis) {
        if (yieldMillis > 0) {
            this.yieldMillis = yieldMillis;
        }
        return this;
    }

    public String getWarmerName() {
        return !Strings.isNullOrEmpty(this.name) ? this.name : this.warmerClass;
    }

    public WarmerConfig setWarmerName(final String name) {
        this.name = name;
        return this;
    }

    public String getWarmerClass() {
        return this.warmerClass;
    }

    public WarmerConfig setWarmerClass(final String warmerClass) {
        this.warmerClass = warmerClass;
        return this;
    }

    public WarmerConfig setParams(final Map<String, Object> params) {
        if (params != null) {
            this.params = ImmutableMap.copyOf(params);
        }
        return this;
    }

    public Map<String, Object> getParams() {
        return this.params;
    }

    public WarmerConfig setConcurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    public int getConcurrency() {
        return this.concurrency;
    }

    public boolean isRequired() {
        return this.required;
    }

    public WarmerConfig setRequired(boolean required) {
        this.required = required;
        return this;
    }
}

