/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logback.internal;

import org.apache.maven.cli.logging.BaseSlf4jConfiguration;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogbackConfiguration extends BaseSlf4jConfiguration {
  /*
   * Maven Slf4jConfiguration API does not provide access to required execution request parameters
   * (interactive/batch mode, system anduser properties, log file, etc). All logging configuration
   * is done in SysoutCapturer.
   * 
   * TODO rename LogbackConfiguration DummyLogbackConfiguration and SysoutCapturer to
   * LogbackConfiguration
   * 
   * TODO extend LogbackConfiguration API to support our usecase
   */

  public LogbackConfiguration() {
    // funnel all java.util.logging messages to slf4j
    // see http://www.slf4j.org/legacy.html#jul-to-slf4j
    SLF4JBridgeHandler.removeHandlersForRootLogger(); // suppress annoying stderr messages
    SLF4JBridgeHandler.install();
  }

  @Override
  public void setRootLoggerLevel(Level level) {}

  @Override
  public void activate() {}
}
