/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge.warmers;

import com.google.common.base.Strings;

import com.salesforce.centrifuge.Warmer;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static com.salesforce.centrifuge.Constants.LOGGER_PREFIX;

public class HttpWarmer implements Warmer {

    private static final Logger logger = LoggerFactory.getLogger(HttpWarmer.class);

    private final HttpClient client;
    private List<String> urls;
    private String method = "get";  // default is GET
    private String body;
    private Map<String, String> headers = Collections.emptyMap();

    public HttpWarmer() {
        this.client = HttpClients.createDefault();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(final Map<String, Object> params) throws Exception {
        if (!params.containsKey("urls")) {
            throw new IllegalArgumentException("missing 'urls' parameter");
        }
        this.urls = (List<String>) params.get("urls");
        if (params.containsKey("method")) {
            this.method = String.valueOf(params.get("method"));
        }
        if (params.containsKey("body")) {
            this.body = String.valueOf(params.get("body"));
        }
        if (params.containsKey("headers")) {
            this.headers = (Map<String, String>) params.get("headers");
        }
    }

    @Override
    public void next() throws Exception {
        doNext();
    }

    /**
     * Executes an {@link HttpRequest} while swallowing all exceptions.
     */
    protected void doNext() throws Exception {
        final HttpWarmerMethods warmerMethods = HttpWarmerMethods.valueOf(this.method.toUpperCase());
        for (final String url : urls) {
            try {
                logger.debug(LOGGER_PREFIX + "calling {} method on url {}", this.method, this.urls);
                final HttpUriRequest httpRequest = warmerMethods.run(url);

                // add body if present
                if (!Strings.isNullOrEmpty(this.body)) {
                    final ByteArrayEntity body = new ByteArrayEntity(this.body.getBytes());
                    ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(body);
                }

                // add headers
                this.headers.forEach(httpRequest::addHeader);

                // make the call and follow redirects
                final HttpResponse response = this.client.execute(httpRequest);
                logger.debug(LOGGER_PREFIX + "got response code {} from url {}", response.getStatusLine(), url);
            } catch(Exception e) {
                logger.warn(LOGGER_PREFIX + "failed to call url: {} with error message: {}", url, e.getMessage());
                throw e;
            }
        }
    }

    protected enum HttpWarmerMethods {
        GET(HttpGet::new),
        POST(HttpPost::new),
        PUT(HttpPut::new),
        HEAD(HttpHead::new);

        private final Function<String,HttpUriRequest> httpMethodFunction;

        HttpWarmerMethods(final Function<String,HttpUriRequest> httpMethodFunction) {
            this.httpMethodFunction = httpMethodFunction;
        }

        HttpUriRequest run(final String uri) {
            return this.httpMethodFunction.apply(uri);
        }
    }
}
