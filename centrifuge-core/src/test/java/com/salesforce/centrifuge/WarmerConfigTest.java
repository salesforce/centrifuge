/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class WarmerConfigTest {

    @Test
    public void testProperties() throws Exception {
        final WarmerConfig warmerConfig = new WarmerConfig();
        assertEquals(warmerConfig.getMaxIterations(), WarmerConfig.DEFAULT_MAX_ITERATIONS);
        assertEquals(warmerConfig.getYieldMillis(), WarmerConfig.DEFAULT_YIELD_MILLIS);
        assertEquals(warmerConfig.getTimeoutMillis(), WarmerConfig.DEFAULT_TIMEOUT_MILLIS);
        assertEquals(warmerConfig.getConcurrency(), WarmerConfig.DEFAULT_CONCURRENCY);
        assertEquals(warmerConfig.getMaxFailure(), WarmerConfig.DEFAULT_MAX_FAILURE);
        assertEquals(warmerConfig.getParams(), Collections.emptyMap());

        final int randomNegativeNumber = -1 * Math.abs(new Random().nextInt());
        assertEquals(warmerConfig.setMaxIterations(randomNegativeNumber).getMaxIterations(), randomNegativeNumber);
        assertEquals(warmerConfig.setTimeoutMillis(randomNegativeNumber).getTimeoutMillis(), WarmerConfig.DEFAULT_TIMEOUT_MILLIS);

        final int randomPositiveNumber = Math.abs(new Random().nextInt());
        assertEquals(warmerConfig.setMaxIterations(randomPositiveNumber).getMaxIterations(), randomPositiveNumber);
        assertEquals(warmerConfig.setTimeoutMillis(randomPositiveNumber).getTimeoutMillis(), randomPositiveNumber);
        assertEquals(warmerConfig.setMaxFailure(randomPositiveNumber).getMaxFailure(), randomPositiveNumber);

        assertEquals(warmerConfig.setYieldMillis(randomNegativeNumber).getYieldMillis(), Constants.PROPERTY_VALUE_DEFAULT_YIELD_MILLIS);

        // null params is no-op
        assertEquals(warmerConfig.getParams(), warmerConfig.setParams(null).getParams());
    }
}