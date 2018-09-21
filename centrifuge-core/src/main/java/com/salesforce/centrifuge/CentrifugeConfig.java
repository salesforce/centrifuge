/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.salesforce.centrifuge.Constants.*;

public class CentrifugeConfig {

    private static final Logger logger = LoggerFactory.getLogger(CentrifugeConfig.class);
    private int parallelism = Runtime.getRuntime().availableProcessors();
    private int logIntervalSeconds = Constants.PROPERTY_VALUE_DEFAULT_LOG_INTERVAL_SECONDS;
    private int cleanupIntervalMillis = Constants.PROPERTY_VALUE_DEFAULT_CLEANUP_INTERVAL_MILLIS;

    private final List<WarmerConfig> warmerConfigs = new CopyOnWriteArrayList<>();

    public CentrifugeConfig() {
    }

    /**
     * Load config from the given path, or use environment variable/default if null/empty is passed.
     *
     * @param pathToConfig path to config file
     */
    public CentrifugeConfig(final String pathToConfig) {
        final Config config;
        if (getClass().getClassLoader().getResource(pathToConfig) != null) {
            config = ConfigFactory.load(pathToConfig);
        } else {
            final File configFile = new File(pathToConfig);
            if (configFile.exists() && !configFile.isDirectory()) {
                config = ConfigFactory.parseFile(configFile);
            } else {
                logger.warn(LOGGER_PREFIX + "cannot find configuration file {}; returning empty config.", pathToConfig);
                return;
            }
        }

        if (config.hasPath(CONFIG_KEY_PARALLELISM)) {
            this.parallelism = config.getInt(CONFIG_KEY_PARALLELISM);
        }
        if (config.hasPath(CONFIG_KEY_CLEANUP_INTERVAL_MILLIS)) {
            this.cleanupIntervalMillis = config.getInt(CONFIG_KEY_CLEANUP_INTERVAL_MILLIS);
        }

        if (config.hasPath(CONFIG_KEY_LOG_INTERVAL_SECONDS)) {
            this.logIntervalSeconds = config.getInt(CONFIG_KEY_LOG_INTERVAL_SECONDS);
        }

        config.getConfigList(CONFIG_KEY_WARMERS).forEach(c -> {
            try {
                if (!c.hasPath(CONFIG_KEY_CLASS)) {
                    logger.warn(LOGGER_PREFIX + "the '" + CONFIG_KEY_CLASS + "' property is missing from config; ignoring warmer.");
                    return;
                }

                // init warmer config
                final WarmerConfig warmerConfig = new WarmerConfig();
                warmerConfig.setWarmerClass(c.getString(CONFIG_KEY_CLASS));
                if (c.hasPath(CONFIG_KEY_NAME)) {
                    warmerConfig.setWarmerName(c.getString(CONFIG_KEY_NAME));
                }
                if (c.hasPath(CONFIG_KEY_TIMEOUT_MILLIS)) {
                    warmerConfig.setTimeoutMillis(c.getInt(CONFIG_KEY_TIMEOUT_MILLIS));
                }
                if (c.hasPath(CONFIG_KEY_MAX_ITERATIONS)) {
                    warmerConfig.setMaxIterations(c.getLong(CONFIG_KEY_MAX_ITERATIONS));
                }
                if (c.hasPath(CONFIG_KEY_CONCURRENCY)) {
                    warmerConfig.setConcurrency(c.getInt(CONFIG_KEY_CONCURRENCY));
                }
                if (c.hasPath(CONFIG_KEY_MAX_FAILURE)) {
                    warmerConfig.setMaxFailure(c.getLong(CONFIG_KEY_MAX_FAILURE));
                }
                if (c.hasPath(CONFIG_KEY_YIELD_MILLIS)) {
                    warmerConfig.setYieldMillis(c.getInt(CONFIG_KEY_YIELD_MILLIS));
                }
                if (c.hasPath(CONFIG_KEY_REQUIRED)) {
                    warmerConfig.setRequired(c.getBoolean(CONFIG_KEY_REQUIRED));
                }
                if (c.hasPath(CONFIG_KEY_PARAMS)) {
                    warmerConfig.setParams(c.getObject(CONFIG_KEY_PARAMS).unwrapped());
                } else {
                    warmerConfig.setParams(Collections.emptyMap());
                }

                // add to list of warmers
                this.warmerConfigs.add(warmerConfig);
            } catch (Exception e) {
                logger.warn(LOGGER_PREFIX + "failed to register warmer", e);
            }
        });
    }

    public CentrifugeConfig addWarmerConfig(final WarmerConfig config) {
        this.warmerConfigs.add(config);
        return this;
    }

    public List<WarmerConfig> getWarmerConfigs() {
        return ImmutableList.copyOf(this.warmerConfigs);
    }

    public int getLogIntervalSeconds() {
        return this.logIntervalSeconds;
    }

    public CentrifugeConfig setLogIntervalSeconds(int logIntervalSeconds) {
        this.logIntervalSeconds = logIntervalSeconds;
        return this;
    }

    public int getCleanupIntervalMillis() {
        return cleanupIntervalMillis;
    }

    public CentrifugeConfig setCleanupIntervalMillis(int cleanupIntervalMillis) {
        this.cleanupIntervalMillis = cleanupIntervalMillis;
        return this;
    }

    public int getParallelism() {
        return parallelism;
    }

    public CentrifugeConfig setParallelism(int parallelism) {
        this.parallelism = parallelism;
        return this;
    }
}
