/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CentrifugeConfigTest {

    @Test
    public void testConstructorWithFile() throws IOException {
        final File tempFile = File.createTempFile("centrifuge-config", ".conf");
        tempFile.deleteOnExit();

        Files.copy(getClass().getResourceAsStream("/centrifuge-config-test.conf"), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        final CentrifugeConfig centrifugeConfig = new CentrifugeConfig(tempFile.getAbsolutePath());
        assertEquals(centrifugeConfig.getWarmerConfigs().size(), 1);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getWarmerClass(), EchoWarmer.class);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getMaxIterations(), 7777);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getTimeoutMillis(), 1000);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getYieldMillis(), 77);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getMaxFailure(), 777);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getConcurrency(), 7);
        assertTrue(centrifugeConfig.getWarmerConfigs().get(0).isRequired());
        assertEquals(String.valueOf(centrifugeConfig.getWarmerConfigs().get(0).getParams().get("text")), "this is a sample warmer implementation");
    }

    @Test
    public void testConstructorWithResource() {
        final CentrifugeConfig centrifugeConfig = new CentrifugeConfig("centrifuge-config-test.conf");
        assertEquals(centrifugeConfig.getWarmerConfigs().size(), 1);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getWarmerClass(), EchoWarmer.class);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getMaxIterations(), 7777);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getTimeoutMillis(), 1000);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getYieldMillis(), 77);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getMaxFailure(), 777);
        assertEquals(centrifugeConfig.getWarmerConfigs().get(0).getConcurrency(), 7);
        assertEquals(String.valueOf(centrifugeConfig.getWarmerConfigs().get(0).getParams().get("text")), "this is a sample warmer implementation");
    }
}