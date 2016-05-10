/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logging.internal;

import javax.inject.Named;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;

@Named
public class MavenExecutionListener implements EventSpy {

  @Override
  public void init(Context context) throws Exception {}

  @Override
  public void onEvent(Object event) throws Exception {
    if (event instanceof ExecutionEvent) {
      ExecutionEvent executionEven = (ExecutionEvent) event;
      MavenSession session = executionEven.getSession();

      switch (executionEven.getType()) {
        case SessionStarted:
          SLF4J.notifySessionStart(session);
          break;
        case SessionEnded:
          SLF4J.notifySessionFinish(session);
          break;
        case ProjectStarted:
          SLF4J.notifyProjectBuildStart(executionEven.getProject());
          break;
        case ProjectSucceeded:
        case ProjectFailed:
        case ProjectSkipped:
          SLF4J.notifyProjectBuildFinish(executionEven.getProject());
          break;
        case MojoStarted:
          SLF4J.notifyMojoExecutionStart(executionEven.getMojoExecution());
          break;
        case MojoSucceeded:
        case MojoSkipped:
        case MojoFailed:
          SLF4J.notifyMojoExecutionFinish(executionEven.getMojoExecution());
          break;
        default:
          break;
      }
    }
  }

  @Override
  public void close() throws Exception {}

}
