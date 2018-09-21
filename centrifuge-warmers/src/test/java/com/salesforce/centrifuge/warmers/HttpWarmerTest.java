/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.centrifuge.warmers;

import com.google.common.collect.ImmutableMap;
import com.salesforce.centrifuge.Centrifuge;
import com.salesforce.centrifuge.CentrifugeConfig;
import com.salesforce.centrifuge.Warmer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


import org.eclipse.jetty.server.Server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.salesforce.centrifuge.Constants.LOGGER_PREFIX;

public class HttpWarmerTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpWarmerTest.class);
    private HttpWarmerTestServer testServer;

    @BeforeSuite
    public void init() {
        testServer = new HttpWarmerTestServer();
        testServer.start();
    }

    @AfterSuite
    public void teardown() {
        testServer.stop();
    }

    @Test(expectedExceptions = UnknownHostException.class)
    public void testNext() throws Exception {
        final Warmer warmer = new HttpWarmer();
        warmer.init(ImmutableMap.of("urls", Collections.singletonList("http://foo.bar"), "method", "get"));
        warmer.next();
    }

    @Test
    public void testAll() throws Exception {
        testServer.acceptAll(true);

        final CentrifugeConfig centrifugeConfig = new CentrifugeConfig("centrifuge-httpwarmer-test.conf");
        final Centrifuge centrifuge = Centrifuge.newInstance(centrifugeConfig);
        centrifuge.registerMbean();

        final long before = System.currentTimeMillis();
        centrifuge.start();
        while (! centrifuge.isWarm()) {
            Thread.sleep(1000);
            logger.info(LOGGER_PREFIX + "waiting for centrifuge to finish...");
        }
        final long now = System.currentTimeMillis();
        logger.info(LOGGER_PREFIX + "{} millis took to run centrifuge", now - before);

        testServer.acceptAll(false);
    }

    private class HttpWarmerTestServer {
        private final Server server;
        private final int port;

        private String nextExpectedHeaderName;
        private String nextExpectedBody;
        private String nextExpectedHeaderValue;

        private boolean acceptAll;

        private HttpWarmerTestServer() {
            port = 29876;
            server = new Server(port);
            server.setHandler(new HttpWarmerTestHandler());

            nextExpectedHeaderName = "";
            nextExpectedBody = "";
            nextExpectedHeaderValue = "";
        }

        protected void start() {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void stop() {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // needed for when we are testing multiple requests and cant set the expected values between the calls.
        public void acceptAll(boolean acceptAll) {
            this.acceptAll = acceptAll;
        }

        private class HttpWarmerTestHandler extends AbstractHandler {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) throws IOException, ServletException {
                httpServletResponse.setContentType("text/html;charset=utf-8");
                request.setHandled(true);
                switch(HttpWarmer.HttpWarmerMethods.valueOf(httpServletRequest.getMethod().toUpperCase())) {
                    case GET: {
                        if(checkHeader(httpServletRequest)) {
                            httpServletResponse.setStatus(200);
                        } else {
                            httpServletResponse.setStatus(410);
                        }
                        break;
                    }
                    case PUT : {
                        if(checkHeader(httpServletRequest) && checkBody(httpServletRequest)) {
                            httpServletResponse.setStatus(200);
                        } else {
                            httpServletResponse.setStatus(410);
                        }
                        break;
                    }
                    case POST : {
                        if(checkHeader(httpServletRequest) && checkBody(httpServletRequest)) {
                            httpServletResponse.setStatus(200);
                        } else {
                            httpServletResponse.setStatus(410);
                        }
                        break;
                    }
                    case HEAD : {
                        if(checkHeader(httpServletRequest)) {
                            httpServletResponse.setStatus(200);
                        } else {
                            httpServletResponse.setStatus(410);
                        }
                        break;
                    }

                    default: {
                        httpServletResponse.setStatus(404);
                        logger.warn(LOGGER_PREFIX + "didnt get correct method got {}",
                                HttpWarmer.HttpWarmerMethods.valueOf(httpServletRequest.getMethod()));
                    }
                }
            }

            private boolean checkHeader(HttpServletRequest httpServletRequest) {
                return nextExpectedHeaderValue.equals(httpServletRequest.getHeader(nextExpectedHeaderName)) || acceptAll;
            }

            private boolean checkBody(HttpServletRequest httpServletRequest) throws IOException {
                    return nextExpectedBody.equals(httpServletRequest.getReader().lines()
                            .collect(Collectors.joining(System.lineSeparator()))) || acceptAll;
            }
        }
    }
}
