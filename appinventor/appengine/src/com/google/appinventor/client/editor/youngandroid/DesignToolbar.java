// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.IProjectEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.youngandroid.actions.SwitchScreenAction;
import com.google.appinventor.client.editor.youngandroid.actions.SwitchToBlocksEditorAction;
import com.google.appinventor.client.editor.youngandroid.actions.SwitchToFormEditorAction;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.ToolbarItem;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The design toolbar houses command buttons in the Young Android Design
 * tab (for the UI designer (a.k.a, Form Editor) and Blocks Editor).
 *
 */
public class DesignToolbar extends Toolbar {
  private static final Logger LOG = Logger.getLogger(DesignToolbar.class.getName());

  /*
   * A EditorPair groups together the designer and blocks editor for an
   * application screen. Name is the name of the screen (form) displayed
   * in the screens pull-down.
   */
  public static class EditorPair {
    public final String screenName;
    public final FileEditor designerEditor;
    public final FileEditor blocksEditor;

    public EditorPair(String name, FileEditor designerEditor, FileEditor blocksEditor) {
      this.screenName = name;
      this.designerEditor = designerEditor;
      this.blocksEditor = blocksEditor;
    }
  }

  public static class Screen extends EditorPair {
    public Screen(String name, FileEditor formEditor, FileEditor blocksEditor) {
      super(name, formEditor, blocksEditor);
    }
  }

  /*
   * A project as represented in the DesignToolbar. Each project has a name
   * (as displayed in the DesignToolbar on the left), a set of named screens,
   * and an indication of which screen is currently being edited.
   */
  public static class DesignProject {
    public final String name;
    public final Map<String, Screen> screens; // screen name -> Screen
    public String currentScreen; // name of currently displayed screen
    private final long projectId;

    public DesignProject(String name, long projectId) {
      this.name = name;
      this.projectId = projectId;
      screens = Maps.newHashMap();
      // Screen1 is initial screen by default
      currentScreen = YoungAndroidSourceNode.SCREEN1_FORM_NAME;
      // Let BlocklyPanel know which screen to send Yail for
      BlocklyPanel.setCurrentForm(projectId + "_" + currentScreen);
    }

    // Returns true if we added the screen (it didn't previously exist), false otherwise.
    public boolean addScreen(String name, FileEditor formEditor, FileEditor blocksEditor) {
      if (screens.containsKey(name)) {
        return false;
      }
      screens.put(name, new Screen(name, formEditor, blocksEditor));
      return true;
    }

    public void removeScreen(String name) {
      screens.remove(name);
    }

    public void setCurrentScreen(String name) {
      currentScreen = name;
    }

    public long getProjectId() {
      return projectId;
    }

  }

  private static final String WIDGET_NAME_TUTORIAL_TOGGLE = "TutorialToggle";
  private static final String WIDGET_NAME_REMOVEFORM = "RemoveForm";
  private static final String WIDGET_NAME_SCREENS_DROPDOWN = "ScreensDropdown";
  private static final String WIDGET_NAME_SWITCH_TO_BLOCKS_EDITOR = "SwitchToBlocksEditor";
  private static final String WIDGET_NAME_SWITCH_TO_FORM_EDITOR = "SwitchToFormEditor";
  private static final String WIDGET_NAME_SENDTOGALLERY = "SendToGallery";
  private static final String WIDGET_NAME_PROJECT_PROPERTIES_DIALOG = "ProjectPropertiesDialog";

  // Enum for type of view showing in the design tab
  public enum View {
    DESIGNER,   // Designer editor view
    BLOCKS  // Blocks editor view
  }
  public View currentView = View.DESIGNER;

  @UiField public Label projectNameLabel;

  // Project currently displayed in designer
  private DesignProject currentProject;

  // Map of project id to project info for all projects we've ever shown
  // in the Designer in this session.
  public Map<Long, DesignProject> projectMap = Maps.newHashMap();

  // Stack of screens switched to from the Companion
  // We implement screen switching in the Companion by having it tell us
  // to switch screens. We then load into the companion the new Screen
  // We save where we were because the companion can have us return from
  // a screen. If we switch projects in the browser UI, we clear this
  // list of screens as we are effectively running a different application
  // on the device.
  public static LinkedList<String> pushedScreens = Lists.newLinkedList();

  private List<String> missingScreens = new ArrayList<String>();
  private List<String> deletedScreens = new ArrayList<String>();
  private List<String> modifiedScreens = new ArrayList<String>();

  private int totalScreensChanged = 0;

  interface DesignToolbarUiBinder extends UiBinder<Toolbar, DesignToolbar> {}

  @UiField protected DropDownButton pickFormItem;
  @UiField protected ToolbarItem addFormItem;
  @UiField protected ToolbarItem removeFormItem;
  @UiField protected ToolbarItem projectPropertiesDialog;
  @UiField protected ToolbarItem switchToDesign;
  @UiField protected ToolbarItem switchToBlocks;
  @UiField protected ToolbarItem switchToDiff;
  @UiField protected ToolbarItem switchFromDiff;
  @UiField protected ToolbarItem sendToGalleryItem;
  @UiField protected ToolbarItem changeIconItem;

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public DesignToolbar() {
    super();
    bindUI();

    if (Ode.getInstance().isReadOnly() || !AppInventorFeatures.allowMultiScreenApplications()) {
      setVisibleItem(addFormItem, false);
      setVisibleItem(removeFormItem, false);
    }
    // Is the Gallery Enabled (new gallery)?
    setVisibleItem(sendToGalleryItem, Ode.getSystemConfig().getGalleryEnabled()
        && !Ode.getInstance().getGalleryReadOnly());

    setButtonEnabled(switchToDiff.getName(), Ode.getInstance().isDiffingAvailable());

    // setVisibleItem(this.changeIconItem, false);
    pickFormItem.removeStyleName("ode-AddedScreen");
    pickFormItem.removeStyleName("ode-ModifiedScreen");
    pickFormItem.removeStyleName("ode-DeletedScreen");

    // Gray out the Designer button and enable the blocks button
    toggleEditor(false);
    // Ode.getInstance().getTopToolbar().updateFileMenuButtons(0);
    toggleView();
  }

  public void bindUI() {
    DesignToolbarUiBinder uibinder = GWT.create(DesignToolbarUiBinder.class);
    populateToolbar(uibinder.createAndBindUi(this));
  }

  private void doSwitchScreen(final long projectId, final String screenName, final View view) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          if (Ode.getInstance().screensLocked()) { // Wait until I/O complete
            Scheduler.get().scheduleDeferred(this);
          } else {
            doSwitchScreen1(projectId, screenName, view);
          }
        }
      });
  }

  private void doSwitchScreen1(long projectId, String screenName, View view) {
    if (!projectMap.containsKey(projectId)) {
      if (projectId == -1 && this.missingScreens.contains(screenName) && Ode.getInstance().isInDiffView() && currentProject.screens.containsKey(YoungAndroidSourceNode.SCREEN1_FORM_NAME)) {
        LOG.warning("Trying to switch to missing screen " + screenName +
          " in project " + currentProject.name + ". diff view.");
        switchDiffScreen(projectId, screenName, view);
        return;
      }
      LOG.warning("DesignToolbar: no project with id " + projectId
          + ". Ignoring SwitchScreenAction.execute().");
      return;
    }
    DesignProject project = projectMap.get(projectId);
    if (currentProject != project) {
      // need to switch projects first. this will not switch screens.
      if (!switchToProject(projectId, project.name)) {
        return;
      }
    }
    String newScreenName = screenName;
    if (this.missingScreens.contains(newScreenName) && Ode.getInstance().isInDiffView() && currentProject.screens.containsKey(YoungAndroidSourceNode.SCREEN1_FORM_NAME)) {
      LOG.warning("Trying to switch to missing screen " + newScreenName +
          " in project " + currentProject.name + ". diff view.");
      switchDiffScreen(projectId, newScreenName, view);
      return;
    }
    if (!currentProject.screens.containsKey(newScreenName)) {
      // Can't find the requested screen in this project. This shouldn't happen, but if it does
      // for some reason, try switching to Screen1 instead.
      LOG.warning("Trying to switch to non-existent screen " + newScreenName +
          " in project " + currentProject.name + ". Trying Screen1 instead.");
      if (currentProject.screens.containsKey(YoungAndroidSourceNode.SCREEN1_FORM_NAME)) {
        newScreenName = YoungAndroidSourceNode.SCREEN1_FORM_NAME;
      } else {
        // something went seriously wrong!
        ErrorReporter.reportError("Something is wrong. Can't find Screen1 for project "
            + currentProject.name);
        return;
      }
    }
    
    Screen screen = currentProject.screens.get(newScreenName);
    IProjectEditor projectEditor = screen.designerEditor.getProjectEditor();
    currentProject.setCurrentScreen(newScreenName);
    setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, newScreenName);
    // Shuffle columns first so the correct editor layout (Designer vs Blocks) is applied
    // and ViewerBox (with ProjectEditor) is in the DOM. Do not call shuffleColumns from
    // ProjectEditor.onLoad/onShow or we get a loop: add ViewerBox -> onLoad -> onShow ->
    // shuffleColumns -> clear -> re-add ViewerBox -> onLoad again.
    currentView = view;
    // Inform the Blockly Panel which project/screen (aka form) we are working on 
    // otherwise in diffview the wrong workspace is used
    BlocklyPanel.setCurrentForm(projectId + "_" + newScreenName);
    screen.blocksEditor.makeActiveWorkspace();
    if (view == View.DESIGNER) {
      Ode.getInstance().getWorkColumnsEditor().shuffleColumns(screen.designerEditor);
      projectEditor.selectFileEditor(screen.designerEditor);
      // screen.designerEditor.onShow();

      toggleEditor(false);
    } else {  // must be View.BLOCKS
      Ode.getInstance().getWorkColumnsEditor().shuffleColumns(screen.blocksEditor);
      projectEditor.selectFileEditor(screen.blocksEditor);
      // screen.blocksEditor.onShow();
      toggleEditor(true);
    }
  
    Ode.getInstance().getTopToolbar().updateFileMenuButtons(1);
    
    // Do not persist synthetic/missing diff screens as last-opened for the real project.
    // Otherwise reopening the project later can target a screen that doesn't exist.
    if (currentProject.screens.containsKey(newScreenName)) {
      projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED, newScreenName);
    }

    updateScreenDropdownCaptionAndStyle(newScreenName);
  }

  // should we make an empty screen specifically for this?
  private void switchDiffScreen(long projectId, String newScreenName, View view) {
    DiffProjectEditor diffProjectEditor = Ode.getInstance().getDiffProjectEditor();
    
    FileEditor screen1Editor = currentProject.screens.get(YoungAndroidSourceNode.SCREEN1_FORM_NAME).designerEditor;
    IProjectEditor projectEditor = screen1Editor.getProjectEditor();

    FileEditor emptyFormEditor = new YaFormEditor(projectEditor, screen1Editor.getProjectId());
    projectEditor.addFileEditorByType(emptyFormEditor);
    FileEditor emptyBlocksEditor = new YaBlocksEditor(projectEditor, screen1Editor.getProjectId());
    projectEditor.addFileEditorByType(emptyBlocksEditor);

    if (projectEditor instanceof YaProjectEditor) {
      ((YaProjectEditor) projectEditor).addEmptyDesignerEditor("$diff$empty$", (YaFormEditor) emptyFormEditor);
      ((YaProjectEditor) projectEditor).addEmptyBlocksEditor("$diff$empty$", (YaBlocksEditor) emptyBlocksEditor);
    }
    // currentProject.setCurrentScreen(YoungAndroidSourceNode.SCREEN1_FORM_NAME);
    currentProject.setCurrentScreen(newScreenName);
    setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, newScreenName);
    // Shuffle columns first so the correct editor layout (Designer vs Blocks) is applied
    // and ViewerBox (with ProjectEditor) is in the DOM. Do not call shuffleColumns from
    // ProjectEditor.onLoad/onShow or we get a loop: add ViewerBox -> onLoad -> onShow ->
    // shuffleColumns -> clear -> re-add ViewerBox -> onLoad again.
    currentView = view;
    // Inform the Blockly Panel which project/screen (aka form) we are working on 
    // otherwise in diffview the wrong workspace is used
    BlocklyPanel.setCurrentForm("$diff$empty$");
    emptyBlocksEditor.makeActiveWorkspace();
    if (view == View.DESIGNER) {
      Ode.getInstance().getWorkColumnsEditor().shuffleColumnsMissingScreen(diffProjectEditor.getFormFileEditor(newScreenName), emptyFormEditor);
      toggleEditor(false);
    } else {  // must be View.BLOCKS
      projectEditor.insertFileEditor(emptyBlocksEditor, 0);
      projectEditor.selectFileEditor(emptyBlocksEditor);
      Ode.getInstance().getWorkColumnsEditor().shuffleColumnsMissingScreen(diffProjectEditor.getBlocksFileEditor(newScreenName), emptyFormEditor);
      toggleEditor(true);
    }
  
    Ode.getInstance().getTopToolbar().updateFileMenuButtons(1);
    
    projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED, newScreenName);

    updateScreenDropdownCaptionAndStyle(newScreenName);
  }

  public void addProject(long projectId, String projectName) {
    if (!projectMap.containsKey(projectId)) {
      projectMap.put(projectId, new DesignProject(projectName, projectId));
      LOG.info("DesignToolbar added project " + projectName + " with id " + projectId);
    } else {
      LOG.warning("DesignToolbar ignoring addProject for existing project " + projectName
          + " with id " + projectId);
    }
  }

  // Switch to an existing project. Note that this does not switch screens.
  // TODO(sharon): it might be better to throw an exception if the
  // project doesn't exist.
  private boolean switchToProject(long projectId, String projectName) {
    if (projectMap.containsKey(projectId)) {
      DesignProject project = projectMap.get(projectId);
      if (project == currentProject) {
        LOG.warning("DesignToolbar: ignoring call to switchToProject for current project");
        return true;
      }
      pushedScreens.clear();  // Effectively switching applications; clear stack of screens.
      clearDropDownMenu(WIDGET_NAME_SCREENS_DROPDOWN);
      LOG.info("DesignToolbar: switching to existing project " + projectName + " with id "
          + projectId);
      currentProject = project;

      // TODO(sharon): add screens to drop-down menu in the right order
      for (Screen screen : currentProject.screens.values()) {
        addDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, new DropDownItem(screen.screenName,
            screen.screenName, new SwitchScreenAction(projectId, screen.screenName),
            new Image(Ode.getImageBundle().form())));
      }
      projectNameLabel.setText(projectName);
      YaBlocksEditor.resendAssetsAndExtensions();  // Send assets for active project
    } else {
      ErrorReporter.reportError("Design toolbar doesn't know about project " + projectName +
          " with id " + projectId);
      LOG.warning("Design toolbar doesn't know about project " + projectName + " with id "
          + projectId);
      return false;
    }
    return true;
  }

  /*
   * Add a screen name to the drop-down for the project with id projectId.
   * name is the form name, designerEditor is the file editor for the form UI,
   * and blocksEditor is the file editor for the form's blocks.
   */
  public void addScreen(long projectId, String name, FileEditor formEditor,
      FileEditor blocksEditor) {
    if (!projectMap.containsKey(projectId)) {
      LOG.warning("DesignToolbar can't find project " + name + " with id " + projectId
          + ". Ignoring addScreen().");
      return;
    }
    DesignProject project = projectMap.get(projectId);
    if (project.addScreen(name, formEditor, blocksEditor)) {
      if (currentProject == project) {
        addDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, new DropDownItem(name,
            name, new SwitchScreenAction(projectId, name), new Image(Ode.getImageBundle().form())));
      }
    }
  }

  public void setIntoDiffView() {
    // setVisibleItem(this.changeIconItem, true);
    // setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, currentProject.currentScreen + " (\u25cf  2 screens changed) ");
    setVisibleItem(this.addFormItem, false);
    setVisibleItem(this.removeFormItem, false);
    setVisibleItem(this.projectPropertiesDialog, false);
    setVisibleItem(this.sendToGalleryItem, false);
    setTutorialToggleVisible(false);
  }

  public void resetToNormalView() {
    // setVisibleItem(this.changeIconItem, false);
    pickFormItem.removeStyleName("ode-AddedScreen");
    pickFormItem.removeStyleName("ode-ModifiedScreen");
    pickFormItem.removeStyleName("ode-DeletedScreen");
    // remove styles from dropdownitems
    String[] styles = {"ode-DeletedScreen", "ode-AddedScreen", "ode-ModifiedScreen"};
    removeDropDownItemsStyles(WIDGET_NAME_SCREENS_DROPDOWN, styles, (new Image(Ode.getImageBundle().form())).getUrl());
    setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, currentProject != null ? currentProject.currentScreen : "");

    setVisibleItem(this.projectPropertiesDialog, true);
    if (Ode.getInstance().isReadOnly() || !AppInventorFeatures.allowMultiScreenApplications()) {
      setVisibleItem(addFormItem, false);
      setVisibleItem(removeFormItem, false);
    } else {
      setVisibleItem(this.addFormItem, true);
      setVisibleItem(this.removeFormItem, true);
    }
    // Is the Gallery Enabled (new gallery)?
    setVisibleItem(sendToGalleryItem, Ode.getSystemConfig().getGalleryEnabled()
        && !Ode.getInstance().getGalleryReadOnly());

    this.missingScreens = new ArrayList<String>();
    this.deletedScreens = new ArrayList<String>();
    this.modifiedScreens = new ArrayList<String>();
    this.totalScreensChanged = 0;
  }

  private void updateScreenDropdownCaptionAndStyle(String newScreenName) {
    if (Ode.getInstance().isInDiffView()) {
      pickFormItem.removeStyleName("ode-AddedScreen");
      pickFormItem.removeStyleName("ode-ModifiedScreen");
      pickFormItem.removeStyleName("ode-DeletedScreen");
      if (this.missingScreens.contains(newScreenName)) {
        pickFormItem.addStyleName("ode-AddedScreen");
        pickFormItem.setCaption(currentProject.currentScreen + " (+" + (totalScreensChanged-1) + " screens changed)");
      } else if (this.modifiedScreens.contains(newScreenName)) {
        pickFormItem.addStyleName("ode-ModifiedScreen");
        pickFormItem.setCaption(currentProject.currentScreen + " (+" + (totalScreensChanged-1) + " screens changed)");
      } else if (this.deletedScreens.contains(newScreenName)) {
        pickFormItem.addStyleName("ode-DeletedScreen");
        pickFormItem.setCaption(currentProject.currentScreen + " (+" + (totalScreensChanged-1) + " screens changed)");
      } else {
        pickFormItem.setCaption(currentProject.currentScreen + " (+" + totalScreensChanged + " screens changed)");
      }
    }
  }

  public void updateMissingScreens(long projectId, List<String> screenNames, List<String> newScreens, List<String> modifiedScreens, List<String> unchangedScreens) {
    // setVisibleItem(this.changeIconItem, true);
    DesignProject project = projectMap.get(projectId);
    Set<String> currentScreens = project.screens.keySet();
    for (String screen: newScreens) {
      addDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, new DropDownItem(screen, 
                                                                            screen, 
                                                                            new SwitchScreenAction(projectId, screen), 
                                                                            new Image(Ode.getImageBundle().form())));
      String content = "<img width=\"16px\" height=\"16px\" src=\"" + 
                        (new Image(Ode.getImageBundle().added())).getUrl() + 
                        "\"> <img src=\"" +  
                        (new Image(Ode.getImageBundle().form())).getUrl() + 
                        "\"> " + screen;
        setDropDownItemStyleAndCaption(WIDGET_NAME_SCREENS_DROPDOWN, screen, content, "ode-ContextMenuItem ode-AddedScreen");
    }

    this.missingScreens = newScreens;
    this.modifiedScreens = modifiedScreens;

    for (String screen: currentScreens) {
      if (!screenNames.contains(screen)) {
        String content = "<img width=\"16px\" height=\"16px\" src=\"" + 
                        (new Image(Ode.getImageBundle().deleted())).getUrl() + 
                        "\"> <img src=\"" + 
                        (new Image(Ode.getImageBundle().form())).getUrl() + 
                        "\"> " + screen;
        setDropDownItemStyleAndCaption(WIDGET_NAME_SCREENS_DROPDOWN, screen, content, "ode-ContextMenuItem ode-DeletedScreen");
        deletedScreens.add(screen);
      } else if (modifiedScreens.contains(screen)) {
        String content = "<img width=\"16px\" height=\"16px\" src=\"" + 
                        (new Image(Ode.getImageBundle().changed())).getUrl() + 
                        "\"> <img src=\"" + 
                        (new Image(Ode.getImageBundle().form())).getUrl() + 
                        "\"> " + screen;
        setDropDownItemStyleAndCaption(WIDGET_NAME_SCREENS_DROPDOWN, screen, content, "ode-ContextMenuItem ode-ModifiedScreen");
      } else if (unchangedScreens.contains(screen)) {
        String content = "<img width=\"16px\" height=\"16px\" src=\"" + 
                        (new Image(Ode.getImageBundle().unchanged())).getUrl() + 
                        "\"> <img src=\"" + 
                        (new Image(Ode.getImageBundle().form())).getUrl() + 
                        "\"> " + screen;
        setDropDownItemStyleAndCaption(WIDGET_NAME_SCREENS_DROPDOWN, screen, content, "ode-ContextMenuItem");
      }
    }

    this.totalScreensChanged = this.missingScreens.size() + this.deletedScreens.size() + this.modifiedScreens.size();
    updateScreenDropdownCaptionAndStyle(project.currentScreen);
  }

  /*
 * PushScreen -- Static method called by Blockly when the Companion requests
 * That we switch to a new screen. We keep track of the Screen we were on
 * and push that onto a stack of Screens which we pop when requested by the
 * Companion.
 */
  public static boolean pushScreen(String screenName) {
    DesignToolbar designToolbar = Ode.getInstance().getDesignToolbar();
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    String currentScreen = designToolbar.currentProject.currentScreen;
    if (!designToolbar.currentProject.screens.containsKey(screenName)) // No such screen -- can happen
      return false;                                                    // because screen is user entered here.
    pushedScreens.addFirst(currentScreen);
    designToolbar.doSwitchScreen(projectId, screenName, View.BLOCKS);
    return true;
  }

  public static void popScreen() {
    DesignToolbar designToolbar = Ode.getInstance().getDesignToolbar();
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    String newScreen;
    if (pushedScreens.isEmpty()) {
      return;                   // Nothing to do really
    }
    newScreen = pushedScreens.removeFirst();
    designToolbar.doSwitchScreen(projectId, newScreen, View.BLOCKS);
  }

  // Called from Javascript when Companion is disconnected
  public static void clearScreens() {
    pushedScreens.clear();
  }

  /*
   * Switch to screen name in project projectId. Also switches projects if
   * necessary.
   */
  public void switchToScreen(long projectId, String screenName, View view) {
    doSwitchScreen(projectId, screenName, view);
  }

  /**
   * Remove screen name (if it exists) from project projectId
   *
   * @param  projectId The project ID
   * @param  name   Name of the Screen
   */
  public void removeScreen(long projectId, String name) {
    LOG.info("called from remove screen");
    if (!projectMap.containsKey(projectId)) {
      LOG.warning("DesignToolbar can't find project " + name + " with id " + projectId
          + " Ignoring removeScreen().");
      return;
    }
    LOG.info("DesignToolbar: got removeScreen for project " + projectId
        + ", screen " + name);
    DesignProject project = projectMap.get(projectId);
    if (!project.screens.containsKey(name)) {
      // already removed this screen
      return;
    }
    if (currentProject == project) {
      // if removing current screen, choose a new screen to show
      if (currentProject.currentScreen.equals(name)) {
        // TODO(sharon): maybe make a better choice than screen1, but for now
        // switch to screen1 because we know it is always there
        switchToScreen(projectId, YoungAndroidSourceNode.SCREEN1_FORM_NAME, View.DESIGNER);
      }
      removeDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, name);
    }
    project.removeScreen(name);
  }

  public void toggleEditor(boolean blocks) {
    setButtonEnabled(switchToBlocks.getName(), !blocks);
    setButtonEnabled(switchToDesign.getName(), blocks);
    setButtonEnabled(switchToDiff.getName(), Ode.getInstance().isDiffingAvailable() && !Ode.getInstance().isInDiffView());

    boolean notOnScreen1 = getCurrentProject() != null
        && !"Screen1".equals(getCurrentProject().currentScreen);
    setButtonEnabled(WIDGET_NAME_REMOVEFORM, notOnScreen1);
  }

  public void toggleView() {
    SwitchToBlocksEditorAction blockView = new SwitchToBlocksEditorAction();
    SwitchToFormEditorAction designView = new SwitchToFormEditorAction();
    RootPanel.get().addDomHandler(new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        if (event.isControlKeyDown() && event.isAltKeyDown()) {
          if (currentView == View.DESIGNER) {
            blockView.execute();
          } else if (currentView == DesignToolbar.View.BLOCKS) {
            designView.execute();
          }
        }
      }
    }, KeyDownEvent.getType());
  }

  public DesignProject getCurrentProject() {
    return currentProject;
  }

  public View getCurrentView() {
    return currentView;
  }

  public void setTutorialToggleVisible(boolean value) {
    setButtonVisible(WIDGET_NAME_TUTORIAL_TOGGLE, value);
  }

  public void setSwitchFromDiffButtonVisible(boolean value) {
    setVisibleItem(switchFromDiff, value);
    setButtonEnabled(switchToDiff.getName(), !value && Ode.getInstance().isDiffingAvailable());
  }

}
