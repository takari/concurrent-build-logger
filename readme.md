# Better Maven multithreaded build logging support


* Route all (most) log messages to SLF4J.
* Uses SLF4J [Mapped Diagnostic Context](http://www.slf4j.org/manual.html#mdc) (or "MDC" for short) to identify per-project log messages.
* `ProjectBuildLogAppender` appender to write per-project log messages to `${project.build.directory}/build.log` log files.
* `ProjectConsoleAppender` appender to display per-project log messages as continuous block at the end of individual project build.
* `ProjectBuildLogFilter` filter to allow/deny per-project build log messages.

## Build, installation and usage

To build logging extensions and maven distribution

    mvn clean package -Pmaven-distro

Maven distribution with concurrent logging is created under target/ folder as `maven-distro-${mavenVersion}-${project.version}` archive.

See `src/examples` for example logback configuration files.


## Under the hood

### System out/err

Uses custom PrintStream to capture System out/err messages and route them to SLF4J logger. Same idea as [sysout-over-slf4j](http://projects.lidalia.org.uk/sysout-over-slf4j/) but independent Maven-compatible implementation.

### `java.util.logging` (or "JUL" for short)

Uses [jul-to-slf4j](http://www.slf4j.org/legacy.html#jul-to-slf4j) to capture  JUL log messages and route them to SLF4J.

### log4j 1.x and commons-logging

Uses log4j-over-slf4j and jcl-over-slf4j to capture and route log4j and commons logging log messages to SLF4j. Replaces plugin dependencies with corresponding bridge libraries as part of plugin classrealm setup. 

## Known issues

* no automated tests
