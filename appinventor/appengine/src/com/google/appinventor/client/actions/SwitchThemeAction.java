package com.google.appinventor.client.actions;
import java.util.logging.Logger;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.user.client.Command;

public class SwitchThemeAction implements Command {
    private boolean userThemePreference = true;

    @Override
    public void execute() {
        if (Ode.getUserDarkThemeEnabled()) {
          userThemePreference = false;
        } else {
          userThemePreference = true;
        }
        Ode.setUserDarkThemeEnabled(userThemePreference);
    }
}


