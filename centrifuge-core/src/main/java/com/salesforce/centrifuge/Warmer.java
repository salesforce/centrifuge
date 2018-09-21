/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import java.util.Map;

public interface Warmer {

    /**
     * This method is called at the beginning of execution; if an exception
     * is thrown, centrifuge pauses (thread sleeps) for a configurable
     * duration, and then retries calling init.
     *
     * @param params parameters passed from config file
     */
    void init(final Map<String, Object> params) throws Exception;

    /**
     * This method is called as often as the number of iterations (config);
     * or until the max failure (exception thrown) is reached (config);
     * or until the max timeout for this warmer is reached (config);
     * or until the max timeout for engine is reached (config).
     */
    void next() throws Exception;
}
