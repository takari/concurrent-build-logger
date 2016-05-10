/*
 * Copyright (c) 2015-2016 salesforce.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.takari.maven.logback.internal;


import java.util.ListIterator;

import javax.inject.Named;

import org.apache.maven.classrealm.ClassRealmConstituent;
import org.apache.maven.classrealm.ClassRealmManagerDelegate;
import org.apache.maven.classrealm.ClassRealmRequest;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * Replaces plugin logging dependencies with corresponding libraries used by the core.
 */
@Named
public class PluginLoggingDependenciesInjector implements ClassRealmManagerDelegate {

  @Override
  public void setupRealm(ClassRealm classRealm, ClassRealmRequest request) {
    final ListIterator<ClassRealmConstituent> iter = request.getConstituents().listIterator();
    while (iter.hasNext()) {
      final ClassRealmConstituent entry = iter.next();
      // logback
      if ("ch.qos.logback".equals(entry.getGroupId())
          && "logback-core".equals(entry.getArtifactId())) {
        iter.remove();
        request.getForeignImports().put("ch.qos.logback", getClass().getClassLoader());
      } else if ("ch.qos.logback".equals(entry.getGroupId())
          && "logback-classic".equals(entry.getArtifactId())) {
        iter.remove();
        request.getForeignImports().put("ch.qos.logback", getClass().getClassLoader());
      }
      // log4j 1.x
      else if ("log4j".equals(entry.getGroupId()) && "log4j".equals(entry.getArtifactId())) {
        iter.remove();
        request.getForeignImports().put("org.apache.log4j", getClass().getClassLoader());
      }
      // commons-logging
      else if ("commons-logging".equals(entry.getGroupId())
          && "commons-logging".equals(entry.getArtifactId())) {
        iter.remove();
        request.getForeignImports().put("org.apache.commons.logging", getClass().getClassLoader());
      }
    }
  }

}
