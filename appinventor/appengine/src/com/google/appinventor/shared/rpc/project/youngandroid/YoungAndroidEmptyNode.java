// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;

import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.storage.StorageUtil;


/**
 * Young Android form source file node in the project tree.
 *
 */
public final class YoungAndroidEmptyNode extends YoungAndroidFormNode {

  // For serialization
  private static final long serialVersionUID = -933267987704020540L;
  private long projectID;

  /**
   * Creates a new Young Android form source file project node.
   *
   * @param fileId  file id
   */
  public YoungAndroidEmptyNode() {
    super("$diff$empty$");
    projectID = -1;
  }

  public YoungAndroidEmptyNode(long projectId) {
    super("$diff$empty$");
    projectID = projectId;
  }
  
  /**
   * Returns the ID of the project associated with this node. This method can be
   * called on any project node within the project hierarchy.
   *
   * @return  ID of the associated project
   */
  public long getProjectId() {
    return projectID;
  }

  public ProjectRootNode getProjectRoot() {
    return new YoungAndroidProjectEmptyNode();
  }

  public static String getFormFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + FORM_PROPERTIES_EXTENSION;
  }
}
