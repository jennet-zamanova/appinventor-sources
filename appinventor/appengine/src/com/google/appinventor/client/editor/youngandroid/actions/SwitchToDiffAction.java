// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.json.JsObject;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.wizards.DiffFileUploadWizard;
import com.google.appinventor.client.wizards.DiffFileUploadWizard.FileContentCallback;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsArray;

import com.google.gwt.user.client.Command;

import java.util.Map;
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
    FileContentCallback callback = new FileContentCallback() {
      @Override
      public void onContent(Map<String, String> files) {
        if (toolbar.getCurrentView() == DesignToolbar.View.BLOCKS) {
          for (String fileName : files.keySet()) {
            if (fileName.endsWith("Screen1.bky")) {
              String content = files.get(fileName);
              // parse it, save it, display it raw — whatever you want
              SwitchToDiffAction.openSecondaryWorkspace(content);
              LOG.info("file got from upload: " + content.length());
              return;
            }
          }
          LOG.warning("did not find screen 1 blocks!!");
        } else if (toolbar.getCurrentView() == DesignToolbar.View.DESIGNER) {
          for (String fileName : files.keySet()) {
            if (fileName.endsWith("Screen1.scm")) {
              String content = files.get(fileName);
              // parse it, save it, display it raw — whatever you want
              // SwitchToDiffAction.openSecondaryWorkspace(content);
              LOG.info("screen file got from upload: " + content);
              JSONObject j = YoungAndroidSourceAnalyzer.parseSourceFile(content, new ClientJsonParser());
              LOG.info("output is: " + j.toString() + j.toJson() + j);

              if (Ode.getInstance().getCurrentFileEditor() instanceof YaFormEditor) {
                String screen1 = Ode.getInstance().getCurrentFileEditor().getRawFileContent();
                JSONObject j1 = YoungAndroidSourceAnalyzer.parseSourceFile(screen1, new ClientJsonParser());
                LOG.info("screen1: " + j1);
                JavaScriptObject out = SwitchToDiffAction.openDesignerDiff(j1.toJson(), j.toJson());
                LOG.info("result" + out);
                DiffResult diffResult = out.cast();
                JsArrayString newIds = diffResult.getNewIds();
                JsArrayString deletedIds = diffResult.getRemovedIds();
                JsArrayString movedIds = diffResult.getMovedIds();
                JsArrayString updatedIds = diffResult.getUpdatedIds();
                JsObject<JsArray<JsObject<String>>> updateInfo = diffResult.getUpdatedIdsInfo();
                LOG.info("new " + newIds);
                LOG.info("deleted " + deletedIds);
                LOG.info("moved " + movedIds);
                LOG.info("updated " + updatedIds);
                LOG.info("updated info " + updateInfo);
                LOG.info("updateInfo get key " + JsObject.keys(updateInfo));
                Ode.getInstance().toggleDiffView();
                return;
              }
              
            }
          }
          LOG.warning("did not find screen 1 blocks!!");
        }
      }

      @Override
      public void onError(String message) {
        LOG.warning("something went wrong");
      }
    };
    new DiffFileUploadWizard(callback).show();
  }

  public static native JavaScriptObject openDesignerDiff(String designer1, String designer2) /*-{
    return $wnd.openDesignerDiff(JSON.parse(designer1), JSON.parse(designer2));
  }-*/;


  public static native void openSecondaryWorkspace(String file) /*-{
    $wnd.openSecondaryWorkspace(file);
  }-*/;

  private static class DiffResult extends JavaScriptObject {
    protected DiffResult() {}

    private final native JsArrayString getUnchangedIds()/*-{ 
      return Array.from(this["unchangedIds"]); 
    }-*/;

    private final native JsArrayString getNewIds()/*-{ 
      return Array.from(this["newIds"]);       
    }-*/;

    private final native JsArrayString getRemovedIds()/*-{ 
      return Array.from(this["removedIds"]);   
    }-*/; 

    private final native JsArrayString getMovedIds()/*-{ 
      return Array.from(this["movedIds"]);   
    }-*/;

    private final native JsArrayString getUpdatedIds()/*-{ 
      return Array.from(this["updateIds"]);   
    }-*/;

    private final native JsObject<JsArray<JsObject<String>>> getUpdatedIdsInfo()/*-{ 
      return this["updateInfo"];   
    }-*/;
  }
}

