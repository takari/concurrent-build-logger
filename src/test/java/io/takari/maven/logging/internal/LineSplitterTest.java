/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logging.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

public class LineSplitterTest {
    private final String STRING = "\u0442\u0435\u0441\u0442"; // "test" in Russian
    private final Charset UTF8;
    private final byte[] BYTES;

    public LineSplitterTest() throws Exception {
        this.UTF8 = StandardCharsets.UTF_8;
        this.BYTES = STRING.getBytes(UTF8);

        // sanity check
        assertEquals(4, STRING.length());
        assertEquals(8, BYTES.length);
    }

    @Test
    public void testUTF8MultibyteReassembly() throws Exception {
        LineSplitter adaptor = new LineSplitter(UTF8);

        assertTrue(adaptor.split(BYTES, 0, 1).isEmpty());
        assertTrue(adaptor.split(BYTES, 1, 6).isEmpty());
        assertTrue(adaptor.split(BYTES, 7, 1).isEmpty());

        assertEquals(STRING, adaptor.flush());
    }

    @Test
    public void testBytesBufferOverflow() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LineSplitter.BYTES_SIZE * 2 + 10; i++) {
            sb.append('a');
        }

        LineSplitter adaptor = new LineSplitter(UTF8);
        byte[] bytes = sb.toString().getBytes(UTF8);
        adaptor.split(bytes, 0, bytes.length);

        assertEquals(sb.toString(), adaptor.flush());
    }

    @Test
    public void testLineSplit() throws Exception {
        LineSplitter adaptor = new LineSplitter(UTF8);

        assertEquals(Arrays.asList(""), adaptor.split("\n"));
        assertEquals(Arrays.asList(""), adaptor.split("\r"));
        assertEquals(Arrays.asList(""), adaptor.split("\r\n"));
        assertEquals(Arrays.asList("", ""), adaptor.split("\r\r"));

        adaptor.split("a");
        assertEquals("a", adaptor.flush());

        Collection<String> strings = adaptor.split("a\nb\rc\rd");
        assertEquals(Arrays.asList("a", "b", "c"), strings);
        assertEquals("d", adaptor.flush());
    }
}
