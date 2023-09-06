package com.google.appinventor.client.widgets;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidThemeChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.SubsetJSONPropertyEditor;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.common.collect.Lists;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Tree;


import java.util.logging.Logger;

import javax.swing.plaf.TreeUI;

public class BlocksToolkit extends Composite{

  interface BlocksToolkitUiBinder extends UiBinder<HorizontalPanel, BlocksToolkit> {}
  private static final BlocksToolkitUiBinder UI_BINDER = GWT.create(BlocksToolkitUiBinder.class);

  EditableProperty toolkit;
  @UiField SubsetJSONPropertyEditor blockstoolkitEditor;
  @UiField HorizontalPanel horizontalBlocksPanel;
  @UiField Label blocksLabel;
    
  public BlocksToolkit() {
    UI_BINDER.createAndBindUi(this);
    horizontalBlocksPanel.setCellWidth(blocksLabel, "40%");
  
  }

  public void setIsProjectLoaded(Boolean isProjectLoaded){
    EditableProperties toolkits = new EditableProperties(isProjectLoaded);
    toolkit = new EditableProperty(toolkits, MESSAGES.blocksToolkitTitle(), "", MESSAGES.blocksToolkitTitle(), null, MESSAGES.toolkitDescription(), new SubsetJSONPropertyEditor(), 0x01, "", null);
    blockstoolkitEditor.setProperty(toolkit);
    horizontalBlocksPanel.setCellWidth(blockstoolkitEditor, "40%");
  }

  public String getValue(){
    return toolkit.getValue();
  }
}
