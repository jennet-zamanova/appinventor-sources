// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.HasAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;


/**
 * EMPTY Project root node for Young Android projects.
 *
 */
public final class YoungAndroidProjectEmptyNode extends ProjectRootNode
    implements HasAssetsFolder<YoungAndroidAssetsFolder>, HasComponentsFolder<YoungAndroidComponentsFolder> {
  /**
   * Project type for Young Android projects.
   */
  public static final String YOUNG_ANDROID_PROJECT_TYPE = "YoungAndroid";

  // For serialization
  private static final long serialVersionUID = -3993102178645391450L;

  /**
   * Default constructor (for serialization only).
   */
  public YoungAndroidProjectEmptyNode() {
    super("$diff$empty$", -1, YOUNG_ANDROID_PROJECT_TYPE);
  }

  /**
   * Returns the asset folder node of the project.
   *
   * @return asset folder node
   */
  @Override
  public YoungAndroidAssetsFolder getAssetsFolder() {
    return new YoungAndroidAssetsFolder("$diff$empty$");
  }

  /**
   * Returns the package node of the project.
   *
   * @return package node
   */
  public YoungAndroidPackageNode getPackageNode() {
    return new YoungAndroidPackageNode("$diff$empty$", "$diff$empty$");
  }

  @Override
  public YoungAndroidComponentsFolder getComponentsFolder() {
    return new YoungAndroidComponentsFolder("$diff$empty$");
  }
  
  @Override
  public boolean hasExtensions() {
    return false;
  }
}
