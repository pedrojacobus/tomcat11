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
package org.apache.tomcat.util.bcel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.apache.tomcat.Jar;
import org.apache.tomcat.util.bcel.classfile.ClassParser;
import org.apache.tomcat.util.scan.JarFactory;

/*
 * This is an absolute performance test. There is no benefit it running it as part of a standard test run so it is
 * excluded due to the name starting Tester...
 *
 * The test also requires that the Jira lib directory has been extracted from a Jira install and copied to /tmp.
 */
public class TesterPerformance {

    private static final String JAR_LOCATION = "/tmp/jira-libs";

    @Test
    public void testClassParserPerformance() throws IOException {
        File libDir = new File(JAR_LOCATION);
        String[] libs = libDir.list();

        Assert.assertNotNull(libs);

        Set<URL> jarURLs = new HashSet<>();

        for (String lib : libs) {
            if (!lib.toLowerCase(Locale.ENGLISH).endsWith(".jar")) {
                continue;
            }
            jarURLs.add(URI.create("jar:" + new File (libDir, lib).toURI().toURL().toExternalForm() + "!/").toURL());
        }

        long duration = 0;

        for (URL jarURL : jarURLs) {
            try (Jar jar = JarFactory.newInstance(jarURL)) {
                jar.nextEntry();
                String jarEntryName = jar.getEntryName();
                while (jarEntryName != null) {
                    if (jarEntryName.endsWith(".class")) {
                        InputStream is = jar.getEntryInputStream();
                        long start = System.nanoTime();
                        ClassParser cp = new ClassParser(is);
                        cp.parse();
                        duration += System.nanoTime() - start;
                    }
                    jar.nextEntry();
                    jarEntryName = jar.getEntryName();
                }
            }
        }

        System.out.println("ClassParser performance test took: " + duration + " ns");
    }
}
