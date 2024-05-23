/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.takari.maven.logging.internal.SLF4JPrintStream;

/**
 * Custom ConsoleAppender that integrates with SLF4JPrintStream System out/err bridge for better
 * console logging performance.
 */
public class ConsoleAppender extends ch.qos.logback.core.ConsoleAppender<ILoggingEvent> {
    @Override
    protected void subAppend(ILoggingEvent event) {
        SLF4JPrintStream.enterPrivileged();
        try {
            super.subAppend(event);
        } finally {
            SLF4JPrintStream.leavePrivileged();
        }
    }
}
