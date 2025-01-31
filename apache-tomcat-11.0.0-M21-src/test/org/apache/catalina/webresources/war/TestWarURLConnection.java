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
package org.apache.catalina.webresources.war;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;

public class TestWarURLConnection {

    @Before
    public void register() {
        TomcatURLStreamHandlerFactory.register();
    }


    @Test
    public void testContentLength() throws Exception {
        File f = new File("test/webresources/war-url-connection.war");
        String fileUrl = f.toURI().toURL().toString();

        URL indexHtmlUrl =
                URI.create("jar:war:" + fileUrl + "*/WEB-INF/lib/test.jar!/META-INF/resources/index.html").toURL();

        URLConnection urlConn = indexHtmlUrl.openConnection();
        urlConn.connect();

        int size = urlConn.getContentLength();

        Assert.assertEquals(137, size);
    }
}
