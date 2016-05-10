/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logback.internal;

import java.net.URL;

import org.apache.maven.cli.logging.BaseSlf4jConfiguration;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import io.takari.maven.logging.internal.SLF4JPrintStream;

public class LogbackConfiguration extends BaseSlf4jConfiguration {

  private ch.qos.logback.classic.Level consoleLevel = ch.qos.logback.classic.Level.INFO;

  public LogbackConfiguration() {
    // funnel all java.util.logging messages to slf4j
    // see http://www.slf4j.org/legacy.html#jul-to-slf4j
    SLF4JBridgeHandler.removeHandlersForRootLogger(); // suppress annoying stderr messages
    SLF4JBridgeHandler.install();

    // funnel System out/err message to slf4j
    System.setOut(new SLF4JPrintStream(System.out, false));
    System.setErr(new SLF4JPrintStream(System.err, true));
  }

  @Override
  public void setRootLoggerLevel(Level level) {
    switch (level) {
      case DEBUG:
        this.consoleLevel = ch.qos.logback.classic.Level.DEBUG;
        break;

      case INFO:
        this.consoleLevel = ch.qos.logback.classic.Level.INFO;
        break;

      default:
        this.consoleLevel = ch.qos.logback.classic.Level.ERROR;
        break;
    }
  }

  @Override
  public void activate() {
    // this is called too late to install System out/err bridge

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset();
    lc.putProperty("consoleLevel", consoleLevel.levelStr);

    String resourceName = "logback.xml";
    if (ch.qos.logback.classic.Level.DEBUG.isGreaterOrEqual(consoleLevel)) {
      resourceName = "logback-debug.xml";
    }
    URL url = getClass().getClassLoader().getResource(resourceName);
    if (url == null) {
      ContextInitializer ci = new ContextInitializer(lc);
      url = ci.findURLOfDefaultConfigurationFile(true);
    }

    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    try {
      configurator.doConfigure(url);
    } catch (JoranException e) {
      // StatusPrinter will handle this, see logback documentation for details
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
  }
}
