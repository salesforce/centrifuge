/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import org.testng.annotations.Test;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.testng.Assert.*;

@Test(singleThreaded = true)
public class CentrifugeImplTest {
    @Test
    public void testRegisterMbean() throws Exception {
        final CentrifugeImpl centrifuge = new CentrifugeImpl(new CentrifugeConfig());
        final String mbeanName = centrifuge.getMbeanName();
        try {
            ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName(mbeanName));
            fail();
        } catch (InstanceNotFoundException ignored) {}
        centrifuge.registerMbean();
        assertNotNull(ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName(mbeanName)));
    }
}
