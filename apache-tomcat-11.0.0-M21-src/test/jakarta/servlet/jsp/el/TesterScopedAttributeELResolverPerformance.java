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
package jakarta.servlet.jsp.el;

import jakarta.el.ELContext;
import jakarta.el.ELManager;
import jakarta.el.ELResolver;
import jakarta.el.StandardELContext;
import jakarta.servlet.jsp.JspContext;
import jakarta.servlet.jsp.TesterPageContext;

import org.junit.Test;

/*
 * This is an absolute performance test. There is no benefit it running it as part of a standard test run so it is
 * excluded due to the name starting Tester...
 */
public class TesterScopedAttributeELResolverPerformance {

    /*
     * With the caching of NotFound responses this test takes ~20ms. Without the
     * caching it takes ~6s.
     */
    @Test
    public void testGetValuePerformance() throws Exception {

        ELContext context = new StandardELContext(ELManager.getExpressionFactory());

        context.putContext(JspContext.class, new TesterPageContext());

        ELResolver resolver = new ScopedAttributeELResolver();

        for (int i = 0; i < 100000; i++) {
            resolver.getValue(context, null, "unknown");
        }
    }
}
