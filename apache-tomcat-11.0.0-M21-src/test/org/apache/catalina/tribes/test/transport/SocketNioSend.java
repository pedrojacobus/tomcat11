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
package org.apache.catalina.tribes.test.transport;

import java.math.BigDecimal;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.transport.nio.NioSender;

public class SocketNioSend {

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        Member mbr = new MemberImpl("localhost", 9999, 0);
        ChannelData data = new ChannelData();
        data.setOptions(Channel.SEND_OPTIONS_BYTE_MESSAGE);
        data.setAddress(mbr);
        byte[] buf = new byte[8192 * 4];
        data.setMessage(new XByteBuffer(buf,false));
        buf = XByteBuffer.createDataPackage(data);
        int len = buf.length;
        BigDecimal total = new BigDecimal((double)0);
        BigDecimal bytes = new BigDecimal((double)len);
        NioSender sender = new NioSender();
        sender.setDestination(mbr);
        sender.setDirectBuffer(true);
        sender.setSelector(selector);
        sender.setTxBufSize(1024*1024);
        sender.connect();
        sender.setMessage(buf);
        System.out.println("Writing to 9999");
        long start = 0;
        double mb = 0;
        boolean first = true;
        int count = 0;
        DecimalFormat df = new DecimalFormat("##.00");
        while (count<100000) {
            if (first) {
                first = false;
                start = System.currentTimeMillis();
            }
            sender.setMessage(buf);
            int selectedKeys = 0;
            try {
                selectedKeys = selector.select(0);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if (selectedKeys == 0) {
                continue;
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey sk = it.next();
                it.remove();
                try {
                    int readyOps = sk.readyOps();
                    sk.interestOps(sk.interestOps() & ~readyOps);
                    if (sender.process(sk, false)) {
                        total = total.add(bytes);
                        sender.reset();
                        sender.setMessage(buf);
                        mb += ( (double) len) / 1024 / 1024;
                        if ( ( (++count) % 10000) == 0) {
                            long time = System.currentTimeMillis();
                            double seconds = ( (double) (time - start)) / 1000;
                            System.out.println("Throughput " + df.format(mb / seconds) + " MiB/s, total "+mb+" MiB total "+total+" bytes.");
                        }
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                    return;
                }
            }
            selector.selectedKeys().clear();
        }
        System.out.println("Complete, sleeping 15 seconds");
        Thread.sleep(15000);
    }
}
