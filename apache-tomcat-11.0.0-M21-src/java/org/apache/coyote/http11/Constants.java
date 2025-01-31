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
package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;

/**
 * Constants.
 *
 * @author Remy Maucherat
 */
public final class Constants {

    public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;


    /**
     * CRLF.
     */
    public static final String CRLF = "\r\n";


    /**
     * CR.
     */
    public static final byte CR = (byte) '\r';


    /**
     * LF.
     */
    public static final byte LF = (byte) '\n';


    /**
     * SP.
     */
    public static final byte SP = (byte) ' ';


    /**
     * HT.
     */
    public static final byte HT = (byte) '\t';


    /**
     * COLON.
     */
    public static final byte COLON = (byte) ':';


    /**
     * SEMI_COLON.
     */
    public static final byte SEMI_COLON = (byte) ';';


    /**
     * '?'.
     */
    public static final byte QUESTION = (byte) '?';


    /* Various constant "strings" */
    public static final String CONNECTION = "Connection";
    public static final String CLOSE = "close";
    public static final String KEEP_ALIVE_HEADER_VALUE_TOKEN = "keep-alive";
    public static final String CHUNKED = "chunked";
    public static final byte[] ACK_BYTES = ByteChunk.convertToBytes("HTTP/1.1 100 " + CRLF + CRLF);
    public static final String TRANSFERENCODING = "Transfer-Encoding";
    public static final String KEEP_ALIVE_HEADER_NAME = "Keep-Alive";
    public static final byte[] _200_BYTES = ByteChunk.convertToBytes("200");
    public static final byte[] _400_BYTES = ByteChunk.convertToBytes("400");
    public static final byte[] _404_BYTES = ByteChunk.convertToBytes("404");


    /**
     * Identity filters (input and output).
     */
    public static final int IDENTITY_FILTER = 0;


    /**
     * Chunked filters (input and output).
     */
    public static final int CHUNKED_FILTER = 1;


    /**
     * Void filters (input and output).
     */
    public static final int VOID_FILTER = 2;


    /**
     * GZIP filter (output).
     */
    public static final int GZIP_FILTER = 3;


    /**
     * Buffered filter (input)
     */
    public static final int BUFFERED_FILTER = 3;


    /**
     * HTTP/1.0.
     */
    public static final String HTTP_10 = "HTTP/1.0";


    /**
     * HTTP/1.1.
     */
    public static final String HTTP_11 = "HTTP/1.1";
    public static final byte[] HTTP_11_BYTES = ByteChunk.convertToBytes(HTTP_11);
}
