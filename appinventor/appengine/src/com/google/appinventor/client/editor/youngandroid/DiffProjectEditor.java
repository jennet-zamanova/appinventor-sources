// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.shared.settings.SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PROJECT_COLORS;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.IProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.components.MockFusionTablesControl;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.SourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.Command;
import com.google.appinventor.shared.properties.json.JSONObject;

import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.appinventor.client.jzip.TextDecoder;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.gwt.core.client.Scheduler;

/**
 * Abstract superclass for all project editors.
 * Each ProjectEditor is associated with a single project and may have multiple
 * FileEditors open in a DeckPanel.
 *
 * TODO(sharon): consider merging this into YaProjectEditor, since we now
 * only have one type of project editor.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class DiffProjectEditor extends Composite implements IProjectEditor {
  private static final Logger LOG = Logger.getLogger(DiffProjectEditor.class.getName());

  protected final UiStyleFactory uiFactory;

  private final Map<String, FileEditor> openFileEditors;
  private static class EditorSet {
    DesignerEditor<?, ?, ?, ?, ?> formEditor = null;
    BlocksEditor<?, ?> blocksEditor = null;
  }

  // Maps form name -> editors for this form
  private final Map<String, EditorSet> editorMap = new HashMap<>();
  private final Map<String, Map<String, FileEditor>> editorsByType;
  protected final List<String> fileIds;
  // private final HashMap<String,String> locationHashMap = new HashMap<String,String>();
  private final DeckPanel deckPanel;
  private FileEditor selectedFileEditor;
  private final TreeMap<String, Boolean> screenHashMap = new TreeMap<String, Boolean>();

  private boolean screen1BlocksLoaded = false;
  private boolean screen1FormLoaded = false;
  private boolean screen1Added = false;

  private final Set<String> loadedBlocksEditors = new HashSet<>();

  protected static class FileContentHolder {
    private String content;

    FileContentHolder(String content) {
      this.content = content;
    }

    public void setFileContent(String content) {
      this.content = content;
    }

    public String getFileContent() {
      return content;
    }
  }

  /**
   * Creates a {@code ProjectEditor} instance.
   *
   * @param projectRootNode  the project root node
   */
  public DiffProjectEditor(UiStyleFactory uiFactory) {
    this.uiFactory = uiFactory;

    openFileEditors = Maps.newHashMap();
    fileIds = new ArrayList<String>();
    editorsByType = Maps.newHashMap();

    deckPanel = new DeckPanel();
    deckPanel.addStyleName("diff-deck-panel");

    deckPanel.setSize("100%", "100%");
    initWidget(deckPanel);
    // Note: I'm not sure that the setSize call below does anything useful.
    setSize("100%", "100%");
  }

  /**
   * Processes the project before loading into the project editor.
   * To do any any pre-processing of the Project
   * Calls the loadProject() after prepareProject() is fully executed.
   * Currently, prepareProject loads all external components associated with project.
   */
  public void processProject() {
    LOG.info("processing uploaded project");
    // resetExternalComponents();
    resetProjectWarnings();
    // loadExternalComponents()
    //     .then(this::loadProject);
    loadProject();
    LOG.info("finished loading");
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  private void loadProject() {
    // add form editors first, then blocks editors because the blocks editors
    // need access to their corresponding form editors to set up properly
    LOG.info("loading project" + Ode.getInstance().getDiffRoot());
    for (ProjectNode source : Ode.getInstance().getDiffRoot().getAllSourceNodes()) {
      if (source instanceof YoungAndroidFormNode) {
        LOG.info("entitynames in diff: " + ((YoungAndroidFormNode) source).getFormName());
        addDesigner(((YoungAndroidFormNode) source).getFormName(),
            new YaFormEditor(this, (YoungAndroidFormNode) source));
      }
    }
    for (ProjectNode source: Ode.getInstance().getDiffRoot().getAllSourceNodes()) {
      if (source instanceof YoungAndroidBlocksNode) {
        addBlocksEditor(((YoungAndroidBlocksNode) source).getFormName(),
            new YaBlocksEditor(this, (YoungAndroidBlocksNode) source));
      }
    }

    // Add the screens to the design toolbar, along with their associated editors
    // DesignToolbar designToolbar = Ode.getInstance().getDesignToolbar();
    // for (String formName : editorMap.keySet()) {
    //   EditorSet editors = editorMap.get(formName);
    //   if (editors.formEditor != null && editors.blocksEditor != null) {
    //     designToolbar.addScreen(projectRootNode.getProjectId(), formName, editors.formEditor,
    //         editors.blocksEditor);

    //     if (isLastOpened(formName)) {
    //       screen1Added = true;
    //       if (readyToShowScreen1()) {  // probably not yet but who knows?
    //         LOG.info("YaProjectEditor.loadProject: switching to screen " + formName
    //             + " for project " + projectRootNode.getProjectId());
    //         switchToForm(formName, projectRootNode.getProjectId());
    //       }
    //     }
    //   } else if (editors.formEditor == null) {
    //     LOG.warning("Missing form editor for " + formName);
    //   } else {
    //     LOG.warning("Missing blocks editor for " + formName);
    //   }
    // }

    // New project loading logic
    // 1. Create all editors
    // 2. Load all files
    // 3. Upgrade Screen1
    // 4. Upgrade all other screens
    // 5. Open Screen1
    return ;
  }

  private void addDesigner(final String entityName, final DesignerEditor<?, ?, ?, ?, ?> newDesigner) {
    if (editorMap.containsKey(entityName)) {
      // This happens if the blocks editor was already added.
      editorMap.get(entityName).formEditor = newDesigner;

    } else {
      EditorSet editors = new EditorSet();
      editors.formEditor = newDesigner;
      editorMap.put(entityName, editors);
    }
    addFileEditorByType(newDesigner);
    // if (!isLastOpened(entityName) && !screen1FormLoaded) {
    //   // Defer loading other screens until Screen1 is loaded. Otherwise we can end up in an
    //   // inconsistent state during project upgrades with Screen1-only properties.
    //   Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
    //     @Override
    //     public boolean execute() {
    //       if (screen1FormLoaded) {
    //         newDesigner.loadFile(afterLoadCommand);
    //         return false;
    //       } else {
    //         return true;
    //       }
    //     }
    //   }, 100);
    // } else {
      // final long projectId = "$diff".hashCode();
      final String fileId = newDesigner.getFileId();
      LOG.info("designer fileid: " + fileId);
      // where does the setting actually happen??
      String contents = load2(fileId);
      // upgrade
      JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(
        contents, new ClientJsonParser());
      newDesigner.setPreUpgradeJsonString(propertiesObject.toJson());
      // final FileContentHolder fileContentHolder = new FileContentHolder(contents);
      int pos = Collections.binarySearch(fileIds, newDesigner.getFileId(),
            getFileIdComparator());
        if (pos < 0) {
          pos = -pos - 1;
        }
        insertFileEditor(newDesigner, pos);
        if (isLastOpened(entityName)) {
          screen1FormLoaded = true;
          if (readyToShowScreen1()) {
            LOG.info("YaProjectEditor.addFormEditor.loadFile.execute: switching to screen "
                + entityName + " for project " + newDesigner.getProjectId());
            // switchToForm(entityName, newDesigner.getProjectId());
          }
        }
        newDesigner.onFileLoaded(contents);
        LOG.info("designer fileid: " +newDesigner.getFileId());
        // loadBlocksEditor(entityName);
    // }
  }

  private String load2(String fileId) {
    ArrayBuffer buffer = Ode.getInstance().getDiffContents().get("$diff:" + fileId);
    if (buffer != null) {
      TextDecoder decoder = new TextDecoder("utf-8");
      String content = decoder.decode(buffer);
      return content;
    } else {
      new Exception("File not found");
      return "";
    }
  }

  private void addBlocksEditor(String entityName, final BlocksEditor<?, ?> newBlocksEditor) {
    if (editorMap.containsKey(entityName)) {
      // This happens if the form editor was already added.
      EditorSet pair = editorMap.get(entityName);
      pair.blocksEditor = newBlocksEditor;
    } else {
      EditorSet editors = new EditorSet();
      editors.blocksEditor = newBlocksEditor;
      editorMap.put(entityName, editors);
    }
    addFileEditorByType(newBlocksEditor);
    loadBlocksEditor(entityName);
  }

  public void addBlocksEditor(BlocksEditor<?, ?> editor) {
    String formName = editor.getEntityName();
    int pos = Collections.binarySearch(fileIds, editor.getFileId(),
        getFileIdComparator());
    if (pos < 0) {
      pos = -pos - 1;
    }
    insertFileEditor(editor, pos);
    if (isLastOpened(formName)) {
      screen1BlocksLoaded = true;
      if (readyToShowScreen1()) {
        LOG.info("YaProjectEditor.addBlocksEditor.loadFile.execute: switching to screen "
            + formName + " for project " + editor.getProjectId());
        Ode.getInstance().getDesignToolbar().switchToScreen(editor.getProjectId(),
            formName, DesignToolbar.View.DESIGNER);
      }
    }
  }

  private void loadBlocksEditor(String formName) {
    if (loadedBlocksEditors.contains(formName)) {
      return;
    }
    loadedBlocksEditors.add(formName);

    final BlocksEditor<?, DesignerEditor<?, ?, ?, ?, ?>> newBlocksEditor =
        (BlocksEditor) editorMap.get(formName).blocksEditor;

    final String fileId = newBlocksEditor.getFileId();
    String blkFileContent = load2(fileId);
    String designerJson = editorMap.get(formName).formEditor.getJson();
    newBlocksEditor.loadDiffBlocks(designerJson, blkFileContent);
  }

  private boolean isLastOpened(String formName) {
    String lastOpened = this.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED);
    if (lastOpened.isEmpty()) { // This happens sometimes when a screen is deleted
      lastOpened = "Screen1";   // Haven't found the cause, so this is a workaround
    }
    return lastOpened.equals(formName);
  }

  private boolean readyToShowScreen1() {
    return screen1FormLoaded && screen1BlocksLoaded && screen1Added;
  }

  private static Comparator<String> getFileIdComparator() {
    // File editors (YaFormEditors and YaBlocksEditors) are sorted so that Screen1 always comes
    // first and others are in alphabetical order. Within each pair, the YaFormEditor is
    // immediately before the YaBlocksEditor.
    return new Comparator<String>() {
      @Override
      public int compare(String fileId1, String fileId2) {
        boolean isForm1 = fileId1.endsWith(FORM_PROPERTIES_EXTENSION);
        boolean isForm2 = fileId2.endsWith(FORM_PROPERTIES_EXTENSION);

        // Give priority to screen1.
        if (YoungAndroidSourceNode.isScreen1(fileId1)) {
          if (YoungAndroidSourceNode.isScreen1(fileId2)) {
            // They are both named screen1. The form editor should come before the blocks editor.
            if (isForm1) {
              return isForm2 ? 0 : -1;
            } else {
              return isForm2 ? 1 : 0;
            }
          } else {
            // Only fileId1 is named screen1.
            return -1;
          }
        } else if (YoungAndroidSourceNode.isScreen1(fileId2)) {
          // Only fileId2 is named screen1.
          return 1;
        }

        String fileId1WithoutExtension = StorageUtil.trimOffExtension(fileId1);
        String fileId2WithoutExtension = StorageUtil.trimOffExtension(fileId2);
        int compare = fileId1WithoutExtension.compareTo(fileId2WithoutExtension);
        if (compare != 0) {
          return compare;
        }
        // They are both the same name without extension. The form editor should come before the
        // blocks editor.
        if (isForm1) {
          return isForm2 ? 0 : -1;
        } else {
          return isForm2 ? 1 : 0;
        }
      }
    };
  }

  // HOW???
  // private void resetExternalComponents() {
  //   COMPONENT_DATABASE.addComponentDatabaseListener(this);
  //   try {
  //     COMPONENT_DATABASE.resetDatabase();
  //   } catch (JSONException e) {
  //     // thrown if any of the component/extension descriptions are not valid JSON
  //     // ErrorReporter.reportError(Ode.MESSAGES.componentDatabaseCorrupt(project.getProjectName()));
  //   }
  //   externalComponents.clear();
  //   extensionsInNode.clear();
  //   extensionToNodeName.clear();
  // }

  // private Promise<Object> loadExternalComponents() {
  //   //Get the list of all ComponentNodes to be Added
  //   List<ProjectNode> componentNodes = new ArrayList<>();
  //   YoungAndroidComponentsFolder componentsFolder =
  //       ((YoungAndroidProjectNode) project.getRootNode()).getComponentsFolder();
  //   for (ProjectNode node : componentsFolder.getChildren()) {
  //     // Find all components that are json files.
  //     final String nodeName = node.getName();
  //     if (nodeName.endsWith(".json") && StringUtils.countMatches(node.getFileId(), "/") == 3) {
  //       componentNodes.add(node);
  //     }
  //   }

  //   // Create a promise that resolves once all components have been added
  //   return Promise.allOf(componentNodes
  //       .stream()
  //       .map(this::importExtension)
  //       .toArray(Promise[]::new));
  // }

  private void resetProjectWarnings() {
    MockFusionTablesControl.resetWarning();
  }

  /**
   * Called when the ProjectEditor widget is loaded after having been hidden. 
   * Subclasses must implement this method, taking responsibility for causing 
   * the onShow method of the selected file editor to be called and for updating 
   * any other UI elements related to showing the project editor.
   */
  protected void onShow() {
    // AssetListBox.getAssetListBox().getAssetList().refreshAssetList(projectId);
  }

  /**
   * Called when the ProjectEditor widget is about to be unloaded. Subclasses
   * must implement this method, taking responsibility for causing the onHide 
   * method of the selected file editor to be called and for updating any 
   * other UI elements related to hiding the project editor.
   */
  protected void onHide() {
    AssetListBox.getAssetListBox().getAssetList().refreshAssetList(0);

    FileEditor selectedFileEditor = getSelectedFileEditor();
    if (selectedFileEditor != null) {
      selectedFileEditor.onHide();
    }
  }

  public UiStyleFactory getUiFactory() {
    return uiFactory;
  }

    // TODO: how to manage chekboxes!??
  public final void setScreenCheckboxState(String screen, Boolean isChecked) {
    screenHashMap.put(screen, isChecked);
  }

  public final Boolean getScreenCheckboxState(String screen) {
    if (screenHashMap.size() == 0) {
      buildScreenHashMap();
    }
    return screenHashMap.get(screen);
  }

  public final String getScreenCheckboxMapString() {
    String screenCheckboxMap = "";
    int count = 0;
    Set<String> screens = screenHashMap.keySet();
    int size = screens.size();
    for (String screen : screens) {
      Boolean isChecked = screenHashMap.get(screen);
      if (isChecked == null) {
        continue;
      }
      String isCheckedString = (isChecked) ? "True" : "False";
      String separator = (count == size) ? "" : " ";
      screenCheckboxMap += screen + ":" + isCheckedString + separator;
    }
    return screenCheckboxMap;
  }

  public final void buildScreenHashMap() {
    String screenCheckboxMap = getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP
    );
    String[] pairs = screenCheckboxMap.split(" ");
    for (String pair : pairs) {
      String[] mapping = pair.split(":");
      String screen = mapping[0];
      Boolean isChecked = Boolean.parseBoolean(mapping[1]);
      screenHashMap.put(screen, isChecked);
    }
  }

  /**
   * Inserts a file editor in this editor at the specified index.
   *
   * @param fileEditor  file editor to insert
   * @param beforeIndex  the index before which fileEditor will be inserted
   */
  public final void insertFileEditor(FileEditor fileEditor, int beforeIndex) {
    String fileId = fileEditor.getFileId();
    openFileEditors.put(fileId, fileEditor);
    fileIds.add(beforeIndex, fileId);
    deckPanel.insert(fileEditor, beforeIndex);
    LOG.info("Inserted file editor for " + fileEditor.getFileId() + " at pos " + beforeIndex);
  }

  protected final void addFileEditorByType(FileEditor fileEditor) {
    String entityName = SourceNode.getEntityName(fileEditor.getFileId());
    if (!editorsByType.containsKey(entityName)) {
      Map<String, FileEditor> editorMap = new HashMap<>();
      editorMap.put(fileEditor.getEditorType(), fileEditor);
      editorsByType.put(entityName, editorMap);
    } else {
      editorsByType.get(entityName).put(fileEditor.getEditorType(), fileEditor);
    }
  }

  /**
   * Selects the given file editor in the deck panel and calls its onShow()
   * method. Calls onHide() for a previously selected file editor if there was
   * one (and it wasn't the same one).
   *
   * Note: all actions that cause the selected file editor to change should
   * be going through DesignToolbar.SwitchScreenAction.execute(), which calls
   * this method. If you're thinking about calling this method directly from
   * somewhere else, please reconsider!
   *
   * @param fileEditor  file editor to select
   */
  public final void selectFileEditor(FileEditor fileEditor) {
    LOG.info("select file edtor");
    int index = deckPanel.getWidgetIndex(fileEditor);
    if (index == -1) {
      if (fileEditor != null) {
        LOG.warning("Can't find widget for fileEditor " + fileEditor.getFileId());
      } else {
        LOG.warning("Not expecting selectFileEditor(null)");
      }
    }
    LOG.info("ProjectEditor: got selectFileEditor for "
        + ((fileEditor == null) ? null : fileEditor.getFileId())
        +  " selectedFileEditor is "
        + ((selectedFileEditor == null) ? null : selectedFileEditor.getFileId()));
    if (selectedFileEditor != null && selectedFileEditor != fileEditor) {
      selectedFileEditor.onHide();
    }
    // Note that we still want to do the following statements even if
    // selectedFileEditor == fileEditor already. This handles the case of switching back
    // to a previously opened project from another project.
    selectedFileEditor = fileEditor;
    deckPanel.showWidget(index);
    // in file editor add getcolumns function
    // call here and readd to workcolumns
    // then call onshow
    selectedFileEditor.onShow();
  }

  /*
  * Returns the BlocksEditor for the given form name in this project
  */
  public BlocksEditor<?, ?> getBlocksFileEditor(String formName) {
    if (editorMap.containsKey(formName)) {
      return editorMap.get(formName).blocksEditor;
    }
    return null;
  }

  /*
  * Returns the YaFormEditor for the given form name in this project
  */
  public DesignerEditor<?, ?, ?, ?, ?> getFormFileEditor(String formName) {
    if (editorMap.containsKey(formName)) {
      return editorMap.get(formName).formEditor;
    }
    return null;
  }

  /**
   * Returns the file editor for the given file ID.
   *
   * @param fileId  file ID of the file
   */
  public final FileEditor getFileEditor(String fileId) {
    return openFileEditors.get(fileId);
  }

  /**
   * Get the file editor of the given <code>editorType</code>, if any, for <code>entityName</code>.
   *
   * @param entityName
   * @param editorType
   * @return
   */
  public final FileEditor getFileEditor(String entityName, String editorType) {
    Map<String, FileEditor> entityEditors = editorsByType.get(entityName);
    if (entityEditors != null) {
      return entityEditors.get(editorType);
    } else {
      return null;
    }
  }

  /**
   * Returns the set of open file editors
   */
  public final Iterable<FileEditor> getOpenFileEditors() {
    return Collections.unmodifiableCollection(openFileEditors.values());
  }

  /**
   * Returns the currently selected file editor
   */
  protected final FileEditor getSelectedFileEditor() {
    return selectedFileEditor;
  }

  /**
   * Closes the file editors for the given file IDs, without saving.
   * This is used when the files are about to be deleted. If
   * selectedFileEditor is closed, sets selectedFileEditor to null.
   *
   * @param closeFileIds  file IDs of the files to be closed
   */
  public final void closeFileEditors(String[] closeFileIds) {
    for (String fileId : closeFileIds) {
      FileEditor fileEditor = openFileEditors.remove(fileId);
      if (fileEditor == null) {
        LOG.severe("File editor is unexpectedly null for " + fileId);
        continue;
      }
      int index = deckPanel.getWidgetIndex(fileEditor);
      fileIds.remove(index);
      deckPanel.remove(fileEditor);
      if (selectedFileEditor == fileEditor) {
        selectedFileEditor.onHide();
        selectedFileEditor = null;
      }
      fileEditor.onClose();
    }
  }

  /**
   * Returns the value of a project settings property.
   *
   * @param category  property category
   * @param name  property name
   * @return the property value
   */
  public final String getProjectSettingsProperty(String category, String name) {
    return "";
  }

  /**
   * Changes the value of a project settings property.
   *
   * @param category  property category
   * @param name  property name
   * @param newValue  new property value
   */
  public final void changeProjectSettingsProperty(String category, String name, String newValue) {
    return;
  }

  /**
   *
   * @param componentName The name of the component registering location permission
   * @param newValue either "True" or "False" indicating whether permission is need.
   */
  public final void recordLocationSetting(String componentName, String newValue) {
    return;
  }

  public void clearLocation(String componentName) {
    return;
  }

  /**
   * Notification that the file with the given file ID has been saved.
   *
   * @param fileId  file ID of the file that was saved
   */
  public final void onSave(String fileId) {
    // FileEditor fileEditor = openFileEditors.get(fileId);
    // if (fileEditor != null) {
    //   fileEditor.onSave();
    // }
  }

  // GWT Widget methods

  @Override
  protected void onLoad() {
    // onLoad is called immediately after a widget becomes attached to the browser's document.
    // onLoad will be called both when a project is opened the first time and when an
    // already-opened project is re-opened.
    // This is different from the ProjectEditor method loadProject, which is called to load the
    // project just after the editor is created.
    // LOG.info("ProjectEditor: got onLoad for project " + projectId);
    super.onLoad();
    // String tutorialURL = getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
    //                                                 SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
    // if (!tutorialURL.isEmpty()) {
    //   Ode ode = Ode.getInstance();
    //   ode.setTutorialURL(tutorialURL);
    // }

    onShow();
  }

  @Override
  protected void onUnload() {
    // onUnload is called immediately before a widget becomes detached from the browser's document.
    // Ode ode = Ode.getInstance();
    // ode.setTutorialVisible(false, true);
    // ode.getDesignToolbar().setTutorialToggleVisible(false);
    // LOG.info("ProjectEditor: got onUnload for project " + projectId);
    super.onUnload();
    onHide();
  }
}
