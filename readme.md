# Better Maven multithreaded build logging support


* Routes all (most) log messages to SLF4J.
* Uses SLF4J [Mapped Diagnostic Context](http://www.slf4j.org/manual.html#mdc) (or "MDC" for short) to identify per-project log messages.
* Provides a number of Maven-specific Logback extensions:
  * `ProjectBuildLogAppender` appender to write per-project log messages to `${project.build.directory}/build.log` log files.
  * `ProjectConsoleAppender` appender to display per-project log messages as continuous block at the end of individual project build.
  * `ProjectBuildLogFilter` filter to allow/deny per-project build log messages.
  * `BuildRollingPolicy` rolloing policy to rotate log files at the begenning of a build.
  * `ConsoleAppender` console appender replacement for better console logging performance.

## Build and installation

To build logging extensions and maven distribution

    mvn clean integration-test -Pmaven-distro

Maven distribution with concurrent logging is created under target/ folder as `maven-distro-${mavenVersion}-${project.version}` archive.

## Usage

Default Logback logging configuration included with the Maven distribution matches standard Maven (as of version 3.3.9).

A number of additional example Logback configuration files is included with the distribution to demostrate more advanced logging features.

`-Dmaven.logging=<classifier>` command line parameter allows switch between available configuration. For example, the following command selects `M2_HOME/conf/logging/logback-dev.xml` Logback configuration file:

    mvn package -Dmaven.logging=dev

### Provided example Logback configuration files

[**ci**](src/main/distro/conf/logging/logback-ci.xml) example logs all build log messages to the console. All build log messages are timestamped and include project artifactId and thread name. Use `ci` logging configuration classifier to enable this configuration, for example `mvn package -Dmaven.logging=ci`.

[**dev**](src/main/distro/conf/logging/logback-dev.xml) example provides more elaborate logging scheme useful when running and/or troubleshooting large multi-module builds on a developer workstation. Build console log only includes overall build log messages with INFO or higher severity, which provides build progress and particularly useful with [Takari Smart Builder](https://github.com/takari/takari-smart-builder) multi-threaded build scheduler. To facilitate build troublshooting, all build log messages are written to `build.log` file located in the current directory and project-specific build log messages are written to `build.log` files located in `${project.build.directory}`. Messages written to `build.log` files honour `-X`/`--debug` Maven command line parameter. All messages are timestamped and can be correlated between console, `build.log` files and external log files. Additionally, messages written to overall `build.log` file are stamped with project artiactId and thread of the message. Note messages written to project-specific `build.log` files are buffered during execution of `clean` lifecycle and are lost of jvm crashes or killed before `default` lifecycle execution starts for the project. Use `dev` logging configuration classifier to enable this configuration, for example `mvn package -Dmaven.logging=dev`.

[**byproject**](src/main/distro/conf/logging/logback-byproject.xml) logging configuration example groups together log messages from individual reactor build projects and prints them to the console after projects builds finish. Project messages never interleave with messages from other projects or overall build log messages. This is useful when multiple projects are built concurrently and default Maven logging makes it difficult to correlated log messages to individual projects. The downside of this configuration, build may appear "stuck" due to message buffering. Buffered messages will be lost in the build jvm crashes or killed by the user. Use `byproject` logging configuration classifier to enable this configuration, for example `mvn package -Dmaven.logging=byproject`.

## Under the hood

### System out/err

Uses custom PrintStream to capture System out/err messages and route them to SLF4J logger. Same idea as [sysout-over-slf4j](http://projects.lidalia.org.uk/sysout-over-slf4j/) but independent Maven-compatible implementation.

System out/err capture is enabled during batch builds only (`-B`/`--batch` mvn invocation parameter) because it interferes with user console interaction, like, for example, `release` plugin asking for release version.

Implementation uses slow caller stack walkback to determine if System out/err messages should be captured or passed through to the console. This can result in poor console logging performance. To avoid the problem, use either `io.takari.maven.logback.ConsoleAppender` console appender (fully compatible with standard Logback console appender) or `io.takari.maven.logback.ProjectConsoleAppender` (groups console messages by project).

### `java.util.logging` (or "JUL" for short)

Uses [jul-to-slf4j](http://www.slf4j.org/legacy.html#jul-to-slf4j) to capture  JUL log messages and route them to SLF4J.

### log4j 1.x and commons-logging

Uses [log4j-over-slf4j](https://www.slf4j.org/legacy.html#log4j-over-slf4j) and [jcl-over-slf4j](https://www.slf4j.org/legacy.html#jclOverSLF4J) to capture and route log4j and commons logging log messages to SLF4j. Replaces plugin dependencies with corresponding bridge libraries as part of plugin classrealm setup. 

## Known issues

* none at this point
