// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.gwt.user.client.ui.DockPanel;

import java.util.logging.Logger;
/**
 * Box implementation for source structure explorer.
 */
public class DiffSourceStructureBox extends Box implements ISourceStructureBox {
  private static final Logger LOG = Logger.getLogger(SourceStructureBox.class.getName());
  // Singleton source structure explorer box instance
  private static final DiffSourceStructureBox INSTANCE = new DiffSourceStructureBox();
  // Singleton source structure explorer child instance
  private static ISourceStructureBox SUBINSTANCE;

  /**
   * Return the singleton source structure explorer box.
   *
   * @return  source structure explorer box
   */
  public static DiffSourceStructureBox getSourceStructureBox() {
    return INSTANCE;
  }

  /**
   * Creates new source structure explorer box.
   */
  protected DiffSourceStructureBox() {
    super(MESSAGES.sourceStructureBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    // Creates the child instance according to the enabled features.
    SUBINSTANCE = new DiffSourceStructureBoxFilter(this);

    setContent(SUBINSTANCE.getSourceStructureExplorer());
  }

  /**
   * Returns source structure explorer associated with box.
   *
   * @return source structure explorer
   */
  public SourceStructureExplorer getSourceStructureExplorer() {
    return SUBINSTANCE.getSourceStructureExplorer();
  }

  /**
   * Calls the child box and renders it according to its behaviour.
   * @param root current form
   */
  public void show(DesignerRootComponent root) {
    LOG.warning("called show on diff ssb");
    getSourceStructureExplorer().updateTree(root.buildComponentsTree(),
        root.getLastSelectedComponent().getSourceStructureExplorerItem());
    getSourceStructureBox().setVisible(true);
    this.setVisible(true);
    setContent(SUBINSTANCE.getSourceStructureExplorer());
  }

  /**
   * Returns the header container for the source structure box (used by childs).
   * @return DockPanel header container
   */
  public DockPanel getHeaderContainer() {
    return super.getHeaderContainer();
  }
}
