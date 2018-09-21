/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.testng.annotations.Test;

@Test
public class CentrifugeControllerTest {
    
    public void testLifecycle() throws Exception {
        CentrifugeMock centrifuge = new CentrifugeMock();
        CentrifugeController controller = new CentrifugeController(centrifuge);
        assertFalse(centrifuge.isStarted());
        controller.start();
        assertTrue(centrifuge.isStarted());
        controller.stop();
        assertFalse(centrifuge.isStarted());
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testCentrifugeControllerCtor() throws Throwable {
        Constructor<CentrifugeController> c;
        try {
            c = CentrifugeController.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
           throw e.getCause();
        }
    }

    private static class CentrifugeMock implements Centrifuge {
        private boolean isStarted = false;
        
        public boolean isStarted() {
            return isStarted;
        }

        @Override
        public void registerWarmer(WarmerConfig warmerConfig) {
        }

        @Override
        public void start() {
            isStarted = true;
        }

        @Override
        public void stop() {
            isStarted = false;            
        }

        @Override
        public boolean isWarm() {
            return false;
        }

        @Override
        public void registerMbean() {
        }

        @Override
        public List<WarmerContainer> getWarmers() {
            return null;
        }
    }
}
