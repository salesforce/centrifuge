/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import java.lang.reflect.Constructor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Provide test coverage for {@link WarmerContainer}, see test cases for the different features provided.
 */
@Test
public class WarmerContainerTest {
    private static final Logger logger = LoggerFactory.getLogger(WarmerContainerTest.class);

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testWarmerContainerCtor() throws Throwable {
        final Constructor<WarmerContainer> c;
        try {
            c = WarmerContainer.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void testWarmerContainerLifecycle() throws Exception {
        final WarmerConfig config = new WarmerConfig();
        config.setWarmerName("test-sleep-warmer");
        config.setWarmerClass(SleepWarmer.class);
        config.setMaxIterations(2);
        config.setTimeoutMillis(100000);

        final WarmerContainer container = new WarmerContainer(config, Executors.newScheduledThreadPool(5));

        assertFalse(container.isRunning());
        container.start();
        while (!(container.isStopped())) {
            Thread.sleep(100);
            logger.info("waiting for container to stop...");
        }
        assertFalse(container.isRunning());
        assertTrue(((SleepWarmer) container.getWarmer()).isInitCalled);
        assertEquals(((SleepWarmer) container.getWarmer()).nextCount, container.getSuccessfulRounds());
        assertEquals(config.getMaxIterations(), container.getSuccessfulRounds());
    }

    @Test
    public void testDoCallThrowingException() throws Exception {
        final WarmerConfig config = new WarmerConfig();
        config.setWarmerName("test-sleep-warmer")
                .setWarmerClass(SleepWarmer.class)
                .setMaxIterations(2)
                .setTimeoutMillis(10);

        final WarmerContainer container = new WarmerContainer(config, Executors.newScheduledThreadPool(5)) {
            protected void doNext() {
                throw new RuntimeException();
            }
        };

        assertFalse(container.isRunning());
        container.start();
        // container is in running state only during the execution of call
        assertFalse(container.isRunning());
    }
}