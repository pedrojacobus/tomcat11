/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.unittest;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class TesterLeakingServlet1 extends HttpServlet {

    private static final Log log = LogFactory.getLog(TesterLeakingServlet1.class);

    private static final long serialVersionUID = 1L;

    private static ThreadLocal<TesterCounter> myThreadLocal = new ThreadLocal<>();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {

        TesterCounter counter = myThreadLocal.get();
        if (counter == null) {
            log.info("Adding thread local to thread " + Thread.currentThread().getName());
            counter = new TesterCounter();
            myThreadLocal.set(counter);
        }

        counter.increment();
        response.getWriter().println(
                "The current thread served this servlet "
                        + counter.getCount() + " times");
    }
}