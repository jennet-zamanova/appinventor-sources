// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.gwt.user.client.ui.Widget;


public interface IProjectEditor {
  void processProject();
  Widget asWidget();
  // void onShow();
  // void onHide();
  UiStyleFactory getUiFactory();
  // void saveProject();

  void setScreenCheckboxState(String screen, Boolean isChecked);
  Boolean getScreenCheckboxState(String screen);
  String getScreenCheckboxMapString();
  void buildScreenHashMap();

  void insertFileEditor(FileEditor fileEditor, int beforeIndex);
  void selectFileEditor(FileEditor fileEditor);
  FileEditor getFileEditor(String fileId);
  FileEditor getFileEditor(String entityName, String editorType);
  Iterable<FileEditor> getOpenFileEditors();
  // FileEditor getSelectedFileEditor();
  void closeFileEditors(String[] closeFileIds);

  String getProjectSettingsProperty(String category, String name);
  void changeProjectSettingsProperty(String category, String name, String newValue);
  void recordLocationSetting(String componentName, String newValue);
  void clearLocation(String componentName);
  BlocksEditor<?, ?> getBlocksFileEditor(String formName);
  DesignerEditor<?, ?, ?, ?, ?> getFormFileEditor(String formName);

  void onSave(String fileId);
  // void onLoad();
  // void onUnload();
} 