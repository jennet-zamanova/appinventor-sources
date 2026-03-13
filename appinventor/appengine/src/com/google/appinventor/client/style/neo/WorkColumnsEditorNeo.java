// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.neo;

import com.google.appinventor.client.editor.WorkColumnsEditor;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.youngandroid.ConsolePanel;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;


public class WorkColumnsEditorNeo extends WorkColumnsEditor {
  interface WorkColumnsEditorUiBinderNeo extends UiBinder<FlowPanel, WorkColumnsEditorNeo> {}

  @UiField
  protected FlowPanel workColumns; 
  @UiField
  protected FlowPanel structureAndAssets;
  @UiField 
  protected ConsolePanel consolePanel;

  @UiField
  protected DesignToolbar designToolbar;
  @UiField(provided = true)
  protected PaletteBox paletteBox = PaletteBox.getPaletteBox();
  @UiField(provided = true)
  protected ViewerBox viewerBox = ViewerBox.getViewerBox();
  @UiField(provided = true)
  protected AssetListBox assetListBox = AssetListBox.getAssetListBox();
  @UiField(provided = true)
  protected SourceStructureBox sourceStructureBox;
  @UiField(provided = true)
  protected BlockSelectorBox blockSelectorBox;
  @UiField(provided = true)
  protected PropertiesBox propertiesBox = PropertiesBox.getPropertiesBox();

  // private boolean consoleVisible = false;

  @Override
  public void bindUI() {
    WorkColumnsEditorUiBinderNeo uibinder = GWT.create(WorkColumnsEditorUiBinderNeo.class);
    initWidget(uibinder.createAndBindUi(this));
    super.workColumns = workColumns;
    super.structureAndAssets = structureAndAssets;
    super.designToolbar = designToolbar;
    super.paletteBox = paletteBox;
    super.viewerBox = viewerBox;
    super.assetListBox = assetListBox;
    sourceStructureBox = SourceStructureBox.getSourceStructureBox();
    blockSelectorBox = BlockSelectorBox.getBlockSelectorBox();
    super.sourceStructureBox = sourceStructureBox;
    super.blockSelectorBox = blockSelectorBox;
    super.propertiesBox = propertiesBox;
    super.consolePanel = consolePanel;
  }
}
