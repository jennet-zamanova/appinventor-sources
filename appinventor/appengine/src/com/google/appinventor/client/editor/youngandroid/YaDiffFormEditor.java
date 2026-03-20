// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.DiffPropertiesBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.ComponentNotFoundException;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.palette.AbstractPalettePanel;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.properties.json.ClientJsonString;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.client.youngandroid.YoungAndroidFormUpgrader;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Editor for Young Android Form (.scm) files.
 *
 * <p>This editor shows a designer that provides support for visual design of
 * forms.</p>
 *
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class YaDiffFormEditor extends DesignerEditor<YoungAndroidFormNode, MockForm,
    YoungAndroidPalettePanel, SimpleComponentDatabase, YaVisibleComponentsPanel> {
  private static final Logger LOG = Logger.getLogger(YaFormEditor.class.getName());

  // JSON parser
  private static final JSONParser JSON_PARSER = new ClientJsonParser();

  // [lyn, 2014/10/13] Need to remember JSON initially loaded from .scm file *before* it is upgraded
  // by YoungAndroidFormUpgrader within upgradeFile. This JSON contains pre-upgrade component
  // version info that is needed by Blockly.SaveFile.load to perform upgrades in the Blocks Editor.
  // This was unnecessary in AI Classic because the .blk file contained component version info
  // as well as the .scm file. But in AI2, the .bky file contains no component version info,
  // and we rely on the pre-upgraded .scm file for this info.
  private String preUpgradeJsonString;

  private JSONArray authURL;    // List of App Inventor versions we have been edited on.

  public AbstractPalettePanel.Filter paletteFilter = null;

  /**
   * Creates a new YaFormEditor.
   *
   * @param projectEditor the project editor that contains this file editor
   * @param formNode the YoungAndroidFormNode associated with this YaFormEditor
   */
  YaDiffFormEditor(ProjectEditor projectEditor, YoungAndroidFormNode formNode) {
    super(projectEditor, formNode, SimpleComponentDatabase.getInstance(formNode.getProjectId()),
        projectEditor.getUiFactory().createSimpleVisibleComponentsPanel(projectEditor, new YaNonVisibleComponentsPanel()));
  }

  public boolean shouldDisplayHiddenComponents() {
    return projectEditor.getScreenCheckboxState(root.getTitle()) != null
               && projectEditor.getScreenCheckboxState(root.getTitle());
  }

  // FileEditor methods
  public DropTargetProvider getDropTargetProvider() {
    return new DropTargetProvider() {
      @Override
      public DropTarget[] getDropTargets() {
        // TODO(markf): Figure out a good way to memorize the targets or refactor things so that
        // getDropTargets() doesn't get called for each component.
        // NOTE: These targets must be specified in depth-first order.
        List<DropTarget> dropTargets = root.getDropTargetsWithin();
        dropTargets.add(visibleComponentsPanel);
        dropTargets.add(nonVisibleComponentsPanel);
        return dropTargets.toArray(new DropTarget[dropTargets.size()]);
      }
    };
  }

  @Override
  public void onHide() {
    LOG.info("YaFormEditor: got onHide() for " + getFileId());
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null and clean up the UI.
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      super.onHide();
      unloadDesigner();
    } else {
      LOG.warning("YaFormEditor.onHide: Not doing anything since we're not the "
          + "current file editor!");
    }
  }

  @Override
  public void onClose() {
    root.removeDesignerChangeListener(this);
    // Note: our partner YaBlocksEditor will remove itself as a DesignerChangeListener, even
    // though we added it.
  }

  @Override
  public String getRawFileContent() {
    String encodedProperties = encodeFormAsJsonString(false);
    JSONObject propertiesObject = JSON_PARSER.parse(encodedProperties).asObject();
    return YoungAndroidSourceAnalyzer.generateSourceFile(propertiesObject);
  }

  // SimpleEditor methods
  @Override
  public boolean isScreen1() {
    return sourceNode.isScreen1();
  }

  /**
   * Returns the form associated with this YaFormEditor.
   *
   * @return a MockForm
   */
  public MockForm getForm() {
    return root;
  }

  public String getComponentInstanceTypeName(String instanceName) {
    return getComponents().get(instanceName).getType();
  }

  @Override
  public void onFileLoaded(String content) {
    JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        content, JSON_PARSER);
    try {
      root = createMockForm(propertiesObject.getProperties().get("Properties").asObject());
    } catch(ComponentNotFoundException e) {
      Ode.getInstance().recordCorruptProject(getProjectId(), getProjectRootNode().getName(),
          e.getMessage());
      ErrorReporter.reportError(MESSAGES.noComponentFound(e.getComponentName(),
          getProjectRootNode().getName()));
      throw e;
    }

    // Initialize the nonVisibleComponentsPanel and visibleComponentsPanel.
    nonVisibleComponentsPanel.setRoot(root);
    visibleComponentsPanel.setRoot(root);
    root.select(null);

    // String subsetjson = root.getPropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET);
    // reloadComponentPalette(subsetjson);
    super.onFileLoaded(content);

    // Originally this was done in loadDesigner. However, this resulted in
    // the form and blocks editor not being registered for events until after
    // they were opened. This became problematic if the user deleted an extension
    // prior to opening the screen as they would never trigger a save, resulting
    // in a corrupt project.

    // Listen to changes on the form.
    root.addDesignerChangeListener(this);
    // Also have the blocks editor listen to changes. Do this here instead
    // of in the blocks editor so that we don't risk it missing any updates.
    root.addDesignerChangeListener(projectEditor.getBlocksFileEditor(root.getName()));
  }

  @Override
  public AbstractPalettePanel.Filter getPaletteFilter() {
    return paletteFilter;
  }

  public void setPreUpgradeJsonString (String json) {
    preUpgradeJsonString = json;
  }

  protected void upgradeFile(FileContentHolder fileContentHolder,
    final Command afterUpgradeComplete) {
    }

  /*
   * Parses the JSON properties and creates the form and its component structure.
   */
  private MockForm createMockForm(JSONObject propertiesObject) {
    return (MockForm) createMockComponent(propertiesObject, null, MockForm.TYPE);
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
    getBlocksEditor().getBlocksImage(callback);
  }

  /*
   * Updates the the whole designer: form, palette, source structure explorer,
   * assets list, and properties panel.
   */
  protected void loadDesigner() {
    root.refresh();
    // MockComponent selectedComponent = root.getLastSelectedComponent();

    // Set the palette box's content.
    // palettePanel.setActiveEditor(this);
    // PaletteBox paletteBox = PaletteBox.getPaletteBox();
    // paletteBox.setContent(palettePanel);

    super.loadDesigner();
  }

  public void refreshCurrentPropertiesPanel() {
    updatePropertiesPanel(root.getSelectedComponents(), true);
  }

  public void refreshCurrentDiffPropertiesPanel() {
    updateDiffPropertiesPanel(root.getSelectedComponents(), true);
  }

  /*
   * Encodes the form's properties as a JSON encoded string. Used by YaBlocksEditor as well,
   * to send the form info to the blockly world during code generation.
   */
  protected String encodeFormAsJsonString(boolean forYail) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    // Include authURL in output if it is non-null
    if (authURL != null) {
      sb.append("\"authURL\":").append(authURL.toJson()).append(",");
    }
    sb.append("\"YaVersion\":\"").append(YaVersion.YOUNG_ANDROID_VERSION).append("\",");
    sb.append("\"Source\":\"Form\",");
    sb.append("\"Properties\":");
    encodeComponentProperties(root, sb, forYail);
    sb.append("}");
    return sb.toString();
  }

  // [lyn, 2014/10/13] returns the *pre-upgraded* JSON for this form.
  // needed to allow associated blocks editor to get this info.
  protected String preUpgradeJsonString() {
    return preUpgradeJsonString;
  }

  @Override
  public String getJson() {
    return preUpgradeJsonString;
  }

  @Override
  protected MockForm newRootObject() {
    return new MockForm(this);
  }

  @Override
  public void onKeyDown(KeyDownEvent event) {
    if (!isActiveEditor()) {
      return;  // Not the active editor
    }
    if (event.getNativeKeyCode() == KeyCodes.KEY_V && !palettePanel.isTextboxFocused()
        && !(event.isControlKeyDown() || event.isMetaKeyDown())) {
      getVisibleComponentsPanel().focusCheckbox();
    } else {
      super.onKeyDown(event);
    }
  }

}
