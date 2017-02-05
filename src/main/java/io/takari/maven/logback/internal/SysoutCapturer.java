/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logback.internal;

import javax.inject.Named;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;

import io.takari.maven.logging.internal.SLF4JPrintStream;

@Named
public class SysoutCapturer extends AbstractMavenLifecycleParticipant {
  @Override
  public void afterSessionStart(MavenSession session) throws MavenExecutionException {
    // this is the earliest callback able to tell if the build is running in batch mode
    // TODO extend Maven Slf4jConfiguration API to support this usecase

    // "interactive mode" means plugins can communicate with the user using console
    // release plugin asks the user to provide release information, for example
    // no need to redirect such interactions to slf4j.

    if (!session.getRequest().isInteractiveMode()) {
      // funnel System out/err message to slf4j when in batch mode
      System.setOut(new SLF4JPrintStream(System.out, false));
      System.setErr(new SLF4JPrintStream(System.err, true));
    }
  }
}
