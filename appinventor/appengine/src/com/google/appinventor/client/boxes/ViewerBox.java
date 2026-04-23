// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.DiffProjectEditor;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import java.util.logging.Logger;

/**
 * Implementation for a box that can hold multiple viewers (including editors).
 *
 */
public class ViewerBox extends Box {
  private static final Logger LOG = Logger.getLogger(ViewerBox.class.getName());

  // Singleton viewer box instance
  private static final ViewerBox INSTANCE = new ViewerBox();

  private boolean isCleared = false;

  private ProjectEditor projectEditor;

  /**
   * Return the singleton viewer box.
   *
   * @return  viewer box
   */
  public static ViewerBox getViewerBox() {
    return INSTANCE;
  }

  /**
   * Creates new empty viewer box.
   */
  private ViewerBox() {
    super(MESSAGES.viewerBoxCaption(),
        600,    // height
        false,  // minimizable
        false); // removable
    addStyleName("ViewerBox");
  }

  /**
   * Shows the content associated with the given project in the viewer.
   *
   * @param projectRootNode  the root node of the project to show in the viewer
   */
  public ProjectEditor show(ProjectRootNode projectRootNode) {
    projectEditor = Ode.getInstance().getEditorManager().openProject(projectRootNode);
    setContent(projectEditor);
    Ode.getInstance().switchToProjectEditor();
    isCleared = false;
    return projectEditor;
  }

  public ProjectEditor show() {
    if (!isCleared) {
      LOG.warning("not cleared!");
    }
    setContent(projectEditor);
    isCleared = false;
    return projectEditor;
  }

  public void clear() {
    setContent((new DiffProjectEditor(Ode.getInstance().getUiStyleFactory())).asWidget());
    isCleared = true;
  }

  public boolean isCleared() {
    return isCleared;
  }
}

