// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.IProjectEditor;
import com.google.appinventor.client.editor.youngandroid.DiffProjectEditor;
import com.google.appinventor.client.widgets.boxes.Box;

import java.util.logging.Logger;

/**
 * Box implementation for properties panels.
 *
 */
public final class BoxSupplier {

  // Singleton properties box instance
  private static final Logger LOG = Logger.getLogger(BoxSupplier.class.getName());

  private static final PropertiesBox PROPERTIES_BOX = PropertiesBox.getPropertiesBox();
  private static final DiffPropertiesBox DIFF_PROPERTIES_BOX = DiffPropertiesBox.getPropertiesBox();
  private static final SourceStructureBox STRUCTURE_BOX = SourceStructureBox.getSourceStructureBox();
  private static final DiffSourceStructureBox DIFF_SOURCE_STRUCTURE_BOX = DiffSourceStructureBox.getSourceStructureBox();

  private BoxSupplier() {
    // utility class; no instances
  }

  public static Box getPropertiesBox(IProjectEditor projectEditor) {
    if (projectEditor instanceof DiffProjectEditor) {
      LOG.warning("BoxSupplier: returning DiffPropertiesBox for diff view");
      return DIFF_PROPERTIES_BOX;
    }
    LOG.warning("BoxSupplier: returning PropertiesBox for regular view");
    return PROPERTIES_BOX;
  }

  public static Box getSourceStructureBox(IProjectEditor projectEditor) {
    if (projectEditor instanceof DiffProjectEditor) {
      LOG.warning("BoxSupplier: returning DiffSourceStructureBox for diff view");
      return DIFF_SOURCE_STRUCTURE_BOX;
    }
    LOG.warning("BoxSupplier: returning SourceStructureBox for regular view");
    return STRUCTURE_BOX;
  }

  public static SourceStructureBox getRegularSourceStructureBox() {
    return STRUCTURE_BOX;
  }

  public static DiffSourceStructureBox getDiffSourceStructureBox() {
    return DIFF_SOURCE_STRUCTURE_BOX;
  }

  public static PropertiesBox getRegularPropertiesBox() {
    return PROPERTIES_BOX;
  }

  public static DiffPropertiesBox getDiffPropertiesBox() {
    return DIFF_PROPERTIES_BOX;
  }

}
