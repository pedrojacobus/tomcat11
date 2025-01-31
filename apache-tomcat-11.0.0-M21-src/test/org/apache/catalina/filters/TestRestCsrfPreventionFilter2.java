/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;

import org.apache.catalina.Context;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

public class TestRestCsrfPreventionFilter2 extends TomcatBaseTest {
    private static final boolean USE_COOKIES = true;
    private static final boolean NO_COOKIES = !USE_COOKIES;

    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";

    private static final String HTTP_PREFIX = "http://localhost:";
    private static final String CONTEXT_PATH_LOGIN = "";
    private static final String URI_PROTECTED = "/services/*";
    private static final String URI_CSRF_PROTECTED = "/services/customers/*";
    private static final String LIST_CUSTOMERS = "/services/customers/";
    private static final String REMOVE_CUSTOMER = "/services/customers/removeCustomer";
    private static final String ADD_CUSTOMER = "/services/customers/addCustomer";
    private static final String REMOVE_ALL_CUSTOMERS = "/services/customers/removeAllCustomers";
    private static final String FILTER_INIT_PARAM = "pathsAcceptingParams";
    private static final String SERVLET_NAME = "TesterServlet";
    private static final String FILTER_NAME = "Csrf";

    private static final String CUSTOMERS_LIST_RESPONSE = "Customers list";
    private static final String CUSTOMER_REMOVED_RESPONSE = "Customer removed";
    private static final String CUSTOMER_ADDED_RESPONSE = "Customer added";

    private static final String INVALID_NONCE_1 = "invalid_nonce";
    private static final String INVALID_NONCE_2 = "";

    private static final String USER = "user";
    private static final String PWD = "pwd";
    private static final String ROLE = "role";
    private static final String METHOD = "BASIC";
    private static final BasicCredentials CREDENTIALS = new BasicCredentials(METHOD, USER, PWD);

    private static final String CLIENT_AUTH_HEADER = "authorization";
    private static final String SERVER_COOKIE_HEADER = "Set-Cookie";
    private static final String CLIENT_COOKIE_HEADER = "Cookie";

    private static final int SHORT_SESSION_TIMEOUT_MINS = 1;

    private Tomcat tomcat;
    private Context context;
    private List<String> cookies = new ArrayList<>();
    private String validNonce;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        tomcat = getTomcatInstance();

        tomcat.addUser(USER, PWD);
        tomcat.addRole(USER, ROLE);

        setUpApplication();

        tomcat.start();
    }

    @Test
    public void testRestCsrfProtectionWithHeader() throws Exception {
        testClearGet();
        testClearPost();
        testGetFirstFetch();
        testValidPost();
        testInvalidPost();
        testGetSecondFetch();
    }

    @Test
    public void testRestCsrfProtectionWithRequestParams() throws Exception {
        testGetFirstFetch();
        testValidPostWithRequestParams();
        testInvalidPostWithRequestParams();
    }

    private void testClearGet() throws Exception {
        doTest(METHOD_GET, LIST_CUSTOMERS, CREDENTIALS, null, NO_COOKIES, HttpServletResponse.SC_OK,
                CUSTOMERS_LIST_RESPONSE, null, false, null);
    }

    private void testClearPost() throws Exception {
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, null, NO_COOKIES, HttpServletResponse.SC_FORBIDDEN, null,
                null, true, Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
    }

    private void testGetFirstFetch() throws Exception {
        doTest(METHOD_GET, LIST_CUSTOMERS, CREDENTIALS, null, NO_COOKIES, HttpServletResponse.SC_OK,
                CUSTOMERS_LIST_RESPONSE, Constants.CSRF_REST_NONCE_HEADER_FETCH_VALUE, true, null);
    }

    private void testValidPost() throws Exception {
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, null, USE_COOKIES, HttpServletResponse.SC_OK,
                CUSTOMER_REMOVED_RESPONSE, validNonce, false, null);
    }

    private void testInvalidPost() throws Exception {
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, null, USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null,
                Constants.CSRF_REST_NONCE_HEADER_FETCH_VALUE, true, Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, null, USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null,
                INVALID_NONCE_1, true, Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, null, USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null,
                INVALID_NONCE_2, true, Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, null, USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null,
                null, true, Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
    }

    private void testGetSecondFetch() throws Exception {
        doTest(METHOD_GET, LIST_CUSTOMERS, CREDENTIALS, null, USE_COOKIES, HttpServletResponse.SC_OK,
                CUSTOMERS_LIST_RESPONSE, Constants.CSRF_REST_NONCE_HEADER_FETCH_VALUE, true, validNonce);
    }

    private void testValidPostWithRequestParams() throws Exception {
        String validBody = Constants.CSRF_REST_NONCE_HEADER_NAME + "=" + validNonce;
        String invalidbody = Constants.CSRF_REST_NONCE_HEADER_NAME + "=" + INVALID_NONCE_1;
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, validBody.getBytes(StandardCharsets.ISO_8859_1), USE_COOKIES,
                HttpServletResponse.SC_OK, CUSTOMER_REMOVED_RESPONSE, null, false, null);
        doTest(METHOD_POST, ADD_CUSTOMER, CREDENTIALS, validBody.getBytes(StandardCharsets.ISO_8859_1), USE_COOKIES,
                HttpServletResponse.SC_OK, CUSTOMER_ADDED_RESPONSE, null, false, null);
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, invalidbody.getBytes(StandardCharsets.ISO_8859_1),
                USE_COOKIES, HttpServletResponse.SC_OK, CUSTOMER_REMOVED_RESPONSE, validNonce, false, null);
    }

    private void testInvalidPostWithRequestParams() throws Exception {
        String validBody = Constants.CSRF_REST_NONCE_HEADER_NAME + "=" + validNonce;
        String invalidbody1 = Constants.CSRF_REST_NONCE_HEADER_NAME + "=" + INVALID_NONCE_1;
        String invalidbody2 = Constants.CSRF_REST_NONCE_HEADER_NAME + "=" +
                Constants.CSRF_REST_NONCE_HEADER_FETCH_VALUE;
        doTest(METHOD_POST, REMOVE_ALL_CUSTOMERS, CREDENTIALS, validBody.getBytes(StandardCharsets.ISO_8859_1),
                USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null, null, true,
                Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, invalidbody1.getBytes(StandardCharsets.ISO_8859_1),
                USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null, null, true,
                Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
        doTest(METHOD_POST, REMOVE_CUSTOMER, CREDENTIALS, invalidbody2.getBytes(StandardCharsets.ISO_8859_1),
                USE_COOKIES, HttpServletResponse.SC_FORBIDDEN, null, null, true,
                Constants.CSRF_REST_NONCE_HEADER_REQUIRED_VALUE);
    }

    private void doTest(String method, String uri, BasicCredentials credentials, byte[] body, boolean useCookie,
            int expectedRC, String expectedResponse, String nonce, boolean expectCsrfRH, String expectedCsrfRHV)
            throws Exception {
        Map<String, List<String>> reqHeaders = new HashMap<>();
        Map<String, List<String>> respHeaders = new HashMap<>();

        addNonce(reqHeaders, nonce, n -> Objects.nonNull(n));

        if (useCookie) {
            addCookies(reqHeaders, l -> Objects.nonNull(l) && l.size() > 0);
        }

        addCredentials(reqHeaders, credentials, c -> Objects.nonNull(c));

        ByteChunk bc = new ByteChunk();
        int rc;
        if (METHOD_GET.equals(method)) {
            rc = getUrl(HTTP_PREFIX + getPort() + uri, bc, reqHeaders, respHeaders);
        } else {
            rc = postUrl(body, HTTP_PREFIX + getPort() + uri, bc, reqHeaders, respHeaders);
        }

        Assert.assertEquals(expectedRC, rc);

        if (expectedRC == HttpServletResponse.SC_OK) {
            Assert.assertEquals(expectedResponse, bc.toString());
            List<String> newCookies = respHeaders.get(SERVER_COOKIE_HEADER);
            saveCookies(newCookies, l -> Objects.nonNull(l) && l.size() > 0);
        }

        if (!expectCsrfRH) {
            Assert.assertNull(respHeaders.get(Constants.CSRF_REST_NONCE_HEADER_NAME));
        } else {
            List<String> respHeaderValue = respHeaders.get(Constants.CSRF_REST_NONCE_HEADER_NAME);
            Assert.assertNotNull(respHeaderValue);
            if (Objects.nonNull(expectedCsrfRHV)) {
                Assert.assertTrue(respHeaderValue.contains(expectedCsrfRHV));
            } else {
                validNonce = respHeaderValue.get(0);
            }
        }
    }

    private void saveCookies(List<String> newCookies, Predicate<List<String>> tester) {
        if (tester.test(newCookies)) {
            newCookies.forEach(h -> cookies.add(h.substring(0, h.indexOf(';'))));
        }
    }

    private void addCookies(Map<String, List<String>> reqHeaders, Predicate<List<String>> tester) {
        if (tester.test(cookies)) {
            StringBuilder cookieHeader = new StringBuilder();
            boolean first = true;
            for (String cookie : cookies) {
                if (!first) {
                    cookieHeader.append(';');
                } else {
                    first = false;
                }
                cookieHeader.append(cookie);
            }
            addRequestHeader(reqHeaders, CLIENT_COOKIE_HEADER, cookieHeader.toString());
        }
    }

    private void addNonce(Map<String, List<String>> reqHeaders, String nonce, Predicate<String> tester) {
        if (tester.test(nonce)) {
            addRequestHeader(reqHeaders, Constants.CSRF_REST_NONCE_HEADER_NAME, nonce);
        }
    }

    private void addCredentials(Map<String, List<String>> reqHeaders, BasicCredentials credentials,
            Predicate<BasicCredentials> tester) {
        if (tester.test(credentials)) {
            addRequestHeader(reqHeaders, CLIENT_AUTH_HEADER, credentials.getCredentials());
        }
    }

    private void addRequestHeader(Map<String, List<String>> reqHeaders, String key, String value) {
        List<String> valueList = new ArrayList<>(1);
        valueList.add(value);
        reqHeaders.put(key, valueList);
    }

    private void setUpApplication() throws Exception {
        context = tomcat.addContext(CONTEXT_PATH_LOGIN, System.getProperty("java.io.tmpdir"));
        context.setSessionTimeout(SHORT_SESSION_TIMEOUT_MINS);

        Tomcat.addServlet(context, SERVLET_NAME, new TesterServlet());
        context.addServletMappingDecoded(URI_PROTECTED, SERVLET_NAME);

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(FILTER_NAME);
        filterDef.setFilterClass(RestCsrfPreventionFilter.class.getCanonicalName());
        filterDef.addInitParameter(FILTER_INIT_PARAM, REMOVE_CUSTOMER + "," + ADD_CUSTOMER);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(FILTER_NAME);
        filterMap.addURLPatternDecoded(URI_CSRF_PROTECTED);
        context.addFilterMap(filterMap);

        SecurityCollection collection = new SecurityCollection();
        collection.addPatternDecoded(URI_PROTECTED);

        SecurityConstraint sc = new SecurityConstraint();
        sc.addAuthRole(ROLE);
        sc.addCollection(collection);
        context.addConstraint(sc);

        LoginConfig lc = new LoginConfig();
        lc.setAuthMethod(METHOD);
        context.setLoginConfig(lc);

        AuthenticatorBase basicAuthenticator = new BasicAuthenticator();
        context.getPipeline().addValve(basicAuthenticator);
    }

    private static final class BasicCredentials {
        private final String method;
        private final String username;
        private final String password;
        private final String credentials;

        private BasicCredentials(String aMethod, String aUsername, String aPassword) {
            method = aMethod;
            username = aUsername;
            password = aPassword;
            String userCredentials = username + ":" + password;
            byte[] credentialsBytes = userCredentials.getBytes(StandardCharsets.ISO_8859_1);
            String base64auth = Base64.getEncoder().encodeToString(credentialsBytes);
            credentials = method + " " + base64auth;
        }

        private String getCredentials() {
            return credentials;
        }
    }

    private static class TesterServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (Objects.equals(LIST_CUSTOMERS, getRequestedPath(req))) {
                resp.getWriter().print(CUSTOMERS_LIST_RESPONSE);
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (Objects.equals(REMOVE_CUSTOMER, getRequestedPath(req))) {
                resp.getWriter().print(CUSTOMER_REMOVED_RESPONSE);
            } else if (Objects.equals(ADD_CUSTOMER, getRequestedPath(req))) {
                resp.getWriter().print(CUSTOMER_ADDED_RESPONSE);
            }
        }

        private String getRequestedPath(HttpServletRequest request) {
            String path = request.getServletPath();
            if (Objects.nonNull(request.getPathInfo())) {
                path = path + request.getPathInfo();
            }
            return path;
        }
    }
}
