// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.wizards.DiffFileUploadWizard;
import com.google.appinventor.client.wizards.DiffFileUploadWizard.FileContentCallback;
import com.google.gwt.user.client.Command;
import java.util.logging.Logger;

public class SwitchToDiffAction implements Command {
  private static final Logger LOG = Logger.getLogger(SwitchToDiffAction.class.getName());
  // private YoungAndroidAssetsFolder diffFolder;

  @Override
  public void execute() {
    final DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar.getCurrentProject() == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
          + "Ignoring SwitchToBlocksEditorAction.execute().");
      return;
    }
    if (toolbar.getCurrentView() == DesignToolbar.View.BLOCKS) { 
      FileContentCallback callback = new FileContentCallback() {
        @Override
        public void onContent(String content) {
          // parse it, save it, display it raw — whatever you want
          SwitchToDiffAction.openSecondaryWorkspace(content);
          LOG.info("file got from upload: " + content.length());
        }
        @Override
        public void onError(String message) {
            LOG.info("something went wrong");
        }
      };
      new DiffFileUploadWizard(callback).show();
      

    }
  }

  public static native void openSecondaryWorkspace(String file) /*-{
    $wnd.openSecondaryWorkspace(file);
  }-*/;
}
