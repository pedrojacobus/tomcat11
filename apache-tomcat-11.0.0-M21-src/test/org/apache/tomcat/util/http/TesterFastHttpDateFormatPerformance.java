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
package org.apache.tomcat.util.http;

import org.junit.Test;

/*
 * This is an absolute performance test. There is no benefit it running it as part of a standard test run so it is
 * excluded due to the name starting Tester...
 */
public class TesterFastHttpDateFormatPerformance {

    @Test
    public void testGetCurrentDateConcurrent() throws InterruptedException {
        int threadCount = 8;
        int callCount = 100000000;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new GetCurrentDateThread(callCount);
        }

        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }

        long endTime = System.nanoTime();

        System.out.println("Duration: " + (endTime - startTime));
    }


    private static class GetCurrentDateThread extends Thread {

        private final int callCount;

        GetCurrentDateThread(int callCount) {
            this.callCount = callCount;
        }

        @Override
        public void run() {
            for (int i = 0; i < callCount; i++) {
                FastHttpDateFormat.getCurrentDate();
            }
        }
    }
}
