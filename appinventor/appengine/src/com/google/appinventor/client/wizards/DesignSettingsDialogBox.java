package com.google.appinventor.client.wizards;

import com.google.appinventor.client.ComponentsTranslation;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidThemeChoicePropertyEditor;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.PropertyHelpWidget;
import com.google.appinventor.client.widgets.properties.SubsetJSONPropertyEditor;
import com.google.appinventor.client.wizards.DesignSettingsDialogBox.DesignSettingsDialogBoxUiBinder;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent; 
import com.google.gwt.event.dom.client.ChangeHandler; 
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * A dialog for updating design of appinventor website
 */

public class DesignSettingsDialogBox {

  interface DesignSettingsDialogBoxUiBinder extends UiBinder<Widget, DesignSettingsDialogBox> {}
  private static final DesignSettingsDialogBoxUiBinder UI_BINDER = GWT.create(DesignSettingsDialogBoxUiBinder.class);


  @UiField 
  Dialog designSettings;

  @UiField
  ListBox categoryList;

  @UiField
  DeckPanel settingsDeckPanel;

  @UiField
  Button closeDialogBox;

  /**
   * preliminary list of options to display 
   * currently it is just : Select Design and Descriptions
   */
  private static final List<String> designSettingsCategoryTitle = Arrays.asList(
    new String[] {
      MESSAGES.selectDesignTitle(),
      MESSAGES.descriptionsTitle()
    }
  );

  /**
   * Maps the design skin to List of Description and image which
   * describe that particular skin
   */
  private HashMap<String, List<String>> skinToDescription = new HashMap<>();

  /**
   * Show the designer settings dialog
   */

  public DesignSettingsDialogBox() {
    UI_BINDER.createAndBindUi(this);
    designSettings.setAutoHideEnabled(false);
    designSettings.setModal(true);
    designSettings.setCaption(MESSAGES.designSettingsText());
    designSettings.center();

    categoryList.getElement().getStyle().setProperty("height", "400px");

    // Add the Categories to ListBox - categoryList
    for (String categoryTitle : designSettingsCategoryTitle) {
      categoryList.addItem(categoryTitle);
      settingsDeckPanel.add(getPanel(categoryTitle));
    }

    // When category is changed by the user, display related properties
    categoryList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        settingsDeckPanel.showWidget(categoryList.getSelectedIndex());
      }
    });

    categoryList.setVisibleItemCount(designSettingsCategoryTitle.size());
      
    // When dialog is opened, properties related to the General category is shown
    settingsDeckPanel.showWidget(0);
  }

  /**
   * Build vertical panel for each categories
   * 
   * @param category indicates the category for which we need to build the vertical panel
   * @return vertical panel which contains the all the Editable Property belongs to the particualt category passed as argument
   */
  private FlowPanel getPanel(String category) {
    // Main 
    FlowPanel propertiesContainer = new FlowPanel();
    propertiesContainer.setStyleName("ode-propertyDialogVerticalPanel");
    // select design
    if (category == MESSAGES.selectDesignTitle()){

    } 
    // descriptions
    else if (category == MESSAGES.descriptionsTitle()){
      for (String skin : skinToDescription.keySet()){
        // container for displaing one editable property
        FlowPanel propertyContainer = new FlowPanel();
        propertyContainer.setStyleName("ode-propertyDialogPropertyContainer");

        // name of the Skin
        Label name = new Label(skin);
        name.setStyleName("ode-propertyDialogPropertyTitle");

        // Description of the Skin
        HTML description = new HTML(skinToDescription.get(skin).get(0));
        description.setStyleName("ode-propertyDialogPropertyDescription");

        // Image of the Skin


        // add to the container
        propertyContainer.add(name);
        propertyContainer.add(description);
        // propertyContainer.add(editor);

        // add to the main container
        propertiesContainer.add(propertyContainer);
      }
    }
    
    return propertiesContainer;
  }

  void applyDesignSettingChanges() {
    // if (!"Screen1".equals(currentScreen)) {
    //   MockForm currentform = projectEditor.getFormFileEditor(currentScreen).getForm();
    //   if (currentform != null) {
    //     currentform.projectPropertyChanged();
    //   }
    // }
  }

  @UiHandler("closeDialogBox")
  void handleClose(ClickEvent e) {
    designSettings.hide();
    applyDesignSettingChanges();
  }   
}
