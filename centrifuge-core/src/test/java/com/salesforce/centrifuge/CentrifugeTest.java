/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class CentrifugeTest {
    private static final Logger logger = LoggerFactory.getLogger(CentrifugeTest.class);

    @Test
    public void testNewInstance() throws Exception {
        assertEquals(Centrifuge.newInstance(null), Centrifuge.NOOP_INSTANCE);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testDefaultConstructor() {
        new CentrifugeImpl();
    }

    @Test
    public void testCounterWarmer() throws Exception {
        final WarmerConfig counterConfig = new WarmerConfig()
                .setWarmerName("test-counter-warmer")
                .setWarmerClass(CounterWarmer.class)
                .setMaxIterations(-1)
                .setConcurrency(10)
                .setTimeoutMillis(3000);

        final Centrifuge centrifuge = Centrifuge.newInstance(new CentrifugeConfig()
                .addWarmerConfig(counterConfig)
                .setLogIntervalSeconds(3)
        );

        assertFalse(CounterWarmer.isInitCalled);
        centrifuge.start();
        boolean allStopped = false;
        while (! allStopped) {
            allStopped = true;
            for (final WarmerContainer wc : centrifuge.getWarmers()) {
                if (! wc.isStopped()) {
                    allStopped = false;
                }
            }
            Thread.sleep(100);
            logger.info("waiting for centrifuge to finish...");
        }
        assertTrue(CounterWarmer.isInitCalled);
    }

    @Test
    public void test() throws Exception {
        final WarmerConfig warmerConfig = new WarmerConfig();
        warmerConfig.setWarmerClass(EchoWarmer.class);
        warmerConfig.setMaxIterations(10);
        warmerConfig.setMaxFailure(3);
        warmerConfig.setTimeoutMillis(1000);
        warmerConfig.setRequired(true);
        final Map<String, Object> params = new HashMap<>();
        params.put("text", "echo is a sample warmer...");
        warmerConfig.setParams(params);

        final CentrifugeConfig centrifugeConfig = new CentrifugeConfig();
        centrifugeConfig.addWarmerConfig(warmerConfig);

        final Centrifuge centrifuge = Centrifuge.newInstance(centrifugeConfig);
        centrifuge.start();

        while (!centrifuge.isWarm()) {
            Thread.sleep(10);
        }

        logger.info("finished.");
    }
}