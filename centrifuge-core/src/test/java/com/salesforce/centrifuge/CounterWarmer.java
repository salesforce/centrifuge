/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import java.util.Map;

public class CounterWarmer implements Warmer {
    static boolean isInitCalled;
    static int nextCount;

    public CounterWarmer() {
        isInitCalled = false;
        nextCount = 0;
    }

    @Override
    public void init(final Map<String, Object> params) throws Exception {
        isInitCalled = true;
    }

    @Override
    public void next() throws Exception {
        nextCount++;
        Thread.sleep(1);
    }
}
