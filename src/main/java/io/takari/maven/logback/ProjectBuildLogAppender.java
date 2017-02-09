/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logback;


import java.io.File;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.sift.AppenderFactory;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.util.Duration;
import io.takari.maven.logging.internal.SLF4J;

/**
 * This Maven-specific appender outputs project build log messages to per-project build.log files
 * <code>${project.build.directory}/build.log</code>.
 * <p>
 * <strong>WARNING</strong> this appender does not work with default maven-clean-plugin
 * configuration, which deletes log files or *nix and osx and fails the build on windows. Use the
 * pom.xml snippet bellow to configure maven-clean-plugin to keep per-project build log files.
 * 
 * <pre>
 * {@literal
   ...
      <build>
        <pluginManagement>
          <plugins>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-clean-plugin</artifactId>
              <version>2.6.1</version>
              <executions>
                <execution>
                  <id>default-clean</id>
                  <configuration>
                    <excludeDefaultDirectories>true</excludeDefaultDirectories>
                    <filesets>
                      <fileset>
                        <directory>${project.build.directory}</directory>
                        <excludes>
                          <exclude>build.log</exclude>
                        </excludes>
                        <useDefaultExcludes>false</useDefaultExcludes>
                      </fileset>
                      <fileset>
                        <directory>${project.build.outputDirectory}</directory>
                        <useDefaultExcludes>false</useDefaultExcludes>
                      </fileset>
                      <fileset>
                        <directory>${project.build.testOutputDirectory}</directory>
                        <useDefaultExcludes>false</useDefaultExcludes>
                      </fileset>
                      <fileset>
                        <directory>${project.reporting.outputDirectory}</directory>
                        <useDefaultExcludes>false</useDefaultExcludes>
                      </fileset>
                    </filesets>
                  </configuration>
                </execution>
              </executions>
            </plugin>
   ...
 * }
 * </pre>
 * <p>
 * Typical logback.xml configuration file
 * 
 * <pre>
 * {@code
      <configuration>
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
          <encoder>
            <pattern>[%level] %msg%n</pattern>
          </encoder>
        </appender>

        <appender name="PROJECT" class="io.takari.maven.logback.ProjectBuildLogAppender">
          <pattern>%date %level %msg%n</pattern>
        </appender>

        <root level="info">
          <appender-ref ref="STDOUT" />
          <appender-ref ref="PROJECT" />
        </root>
      </configuration>
 * }
 * </pre>
 */
public class ProjectBuildLogAppender extends AppenderBase<ILoggingEvent>
    implements SLF4J.LifecycleListener {

  private String pattern;

  private AppenderTracker<ILoggingEvent> appenderTracker;

  private Duration timeout = new Duration(AppenderTracker.DEFAULT_TIMEOUT);

  private int maxAppenderCount = AppenderTracker.DEFAULT_MAX_COMPONENTS;

  @Override
  public void start() {
    appenderTracker =
        new AppenderTracker<ILoggingEvent>(context, new AppenderFactory<ILoggingEvent>() {
          @Override
          public Appender<ILoggingEvent> buildAppender(Context context, String discriminatingValue)
              throws JoranException {
            return ProjectBuildLogAppender.this.buildAppender(context, discriminatingValue);
          }
        });
    appenderTracker.setMaxComponents(maxAppenderCount);
    appenderTracker.setTimeout(timeout.getMilliseconds());

    super.start();

    SLF4J.addListener(this);
  }

  protected Appender<ILoggingEvent> buildAppender(Context context, String discriminatingValue) {
    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern(pattern);
    encoder.start();

    FileAppender<ILoggingEvent> appender = new FileAppender<>();
    appender.setContext(context);
    appender.setName(discriminatingValue);
    appender.setAppend(false);
    appender.setEncoder(encoder);
    return appender;
  }

  @Override
  protected void append(ILoggingEvent event) {
    Map<String, String> mdc = event.getMDCPropertyMap();
    if (mdc == null) {
      return;
    }

    String projectId = mdc.get(SLF4J.KEY_PROJECT_ID);
    String projectLogdir = mdc.get(SLF4J.KEY_PROJECT_LOGDIR);
    if (projectId == null || projectLogdir == null) {
      return;
    }

    long timestamp = event.getTimeStamp();
    Appender<ILoggingEvent> appender = appenderTracker.getOrCreate(projectId, timestamp);
    if (!appender.isStarted()) {
      ((FileAppender<ILoggingEvent>) appender).setFile(getLogfile(projectLogdir).getAbsolutePath());
      appender.start();
    }
    appenderTracker.removeStaleComponents(timestamp);
    appender.doAppend(event);
  }

  private void cleanOldLogFiles(MavenSession session) {
    for (MavenProject project : session.getAllProjects()) {
      File logfile = getLogfile(SLF4J.getLogdir(project));
      logfile.delete();
    }
  }

  private File getLogfile(String logdir) {
    return new File(logdir, "build.log");
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public void stop() {
    SLF4J.removeListener(this);
    for (Appender<ILoggingEvent> appender : appenderTracker.allComponents()) {
      appender.stop();
    }
    super.stop();
  }

  @Override
  public void onSessionStart(MavenSession session) {
    cleanOldLogFiles(session);
  }

  @Override
  public void onProjectBuildFinish(MavenProject project) {
    appenderTracker.endOfLife(project.getId());
    // TODO actually stop and remove the appender here
  }

}
