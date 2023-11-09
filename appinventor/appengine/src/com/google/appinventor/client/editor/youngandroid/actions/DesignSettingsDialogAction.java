package com.google.appinventor.client.editor.youngandroid.actions;

import java.util.logging.Logger;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.wizards.DesignSettingsDialogBox;
import com.google.gwt.user.client.Command;

import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class DesignSettingsDialogAction implements Command {
  @Override
  public void execute() {
    new DesignSettingsDialogBox();
  }
  // private boolean userThemePreference = true;

  // @Override
  // public void execute() {
  //     if (Ode.getUserDarkThemeEnabled()) {
  //       userThemePreference = false;
  //     } else {
  //       userThemePreference = true;
  //     }
  //     Ode.setUserDarkThemeEnabled(userThemePreference);
  // }
}







