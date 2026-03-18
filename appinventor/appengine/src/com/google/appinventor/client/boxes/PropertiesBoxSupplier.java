// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.boxes.Box;
import java.util.logging.Logger;

/**
 * Box implementation for properties panels.
 *
 */
public final class PropertiesBoxSupplier {

  // Singleton properties box instance
  private static final Logger LOG = Logger.getLogger(PropertiesBoxSupplier.class.getName());

  private static final PropertiesBox PROPERTIES_BOX = PropertiesBox.getPropertiesBox();
  private static final DiffPropertiesBox DIFF_PROPERTIES_BOX = DiffPropertiesBox.getPropertiesBox();

  private PropertiesBoxSupplier() {
    // utility class; no instances
  }

  public static Box getPropertiesBox() {
    if (Ode.getInstance().isInDiffView()) {
      LOG.fine("PropertiesBoxSupplier: returning DiffPropertiesBox for diff view");
      return DIFF_PROPERTIES_BOX;
    }
    LOG.fine("PropertiesBoxSupplier: returning PropertiesBox for regular view");
    return PROPERTIES_BOX;
  }

  public static PropertiesBox getRegularPropertiesBox() {
    return PROPERTIES_BOX;
  }

  public static DiffPropertiesBox getDiffPropertiesBox() {
    return DIFF_PROPERTIES_BOX;
  }

}
