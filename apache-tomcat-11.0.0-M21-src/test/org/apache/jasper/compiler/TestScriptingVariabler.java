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
package org.apache.jasper.compiler;

import java.io.IOException;

import jakarta.servlet.jsp.tagext.TagData;
import jakarta.servlet.jsp.tagext.TagExtraInfo;
import jakarta.servlet.jsp.tagext.TagSupport;
import jakarta.servlet.jsp.tagext.VariableInfo;

import org.junit.Assert;
import org.junit.Test;

import org.apache.catalina.startup.TomcatBaseTest;

public class TestScriptingVariabler extends TomcatBaseTest {

    @Test
    public void testBug42390() throws Exception {
        getTomcatInstanceTestWebapp(false, true);

        Exception e = null;
        try {
            getUrl("http://localhost:" + getPort() + "/test/bug42390.jsp");
        } catch (IOException ioe) {
            e = ioe;
        }

        // Should not fail
        Assert.assertNull(e);
    }

    public static class Bug48616aTag extends TagSupport {
        private static final long serialVersionUID = 1L;
    }

    public static class Bug48616bTag extends TagSupport {
        private static final long serialVersionUID = 1L;
    }

    public static class Bug48616bTei extends TagExtraInfo {
        /**
         * Return information about the scripting variables to be created.
         */
        @Override
        public VariableInfo[] getVariableInfo(TagData data) {
            return new VariableInfo[] { new VariableInfo("Test", "java.lang.String", true, VariableInfo.AT_END) };
        }
    }

    @Test
    public void testBug48616() throws Exception {
        getTomcatInstanceTestWebapp(false, true);

        Exception e = null;
        try {
            getUrl("http://localhost:" + getPort() + "/test/bug48nnn/bug48616.jsp");
        } catch (IOException ioe) {
            e = ioe;
        }

        // Should not fail
        Assert.assertNull(e);
    }

    @Test
    public void testBug48616b() throws Exception {
        getTomcatInstanceTestWebapp(false, true);

        Exception e = null;
        try {
            getUrl("http://localhost:" + getPort() + "/test/bug48nnn/bug48616b.jsp");
        } catch (IOException ioe) {
            e = ioe;
        }

        // Should not fail
        Assert.assertNull(e);
    }
}
