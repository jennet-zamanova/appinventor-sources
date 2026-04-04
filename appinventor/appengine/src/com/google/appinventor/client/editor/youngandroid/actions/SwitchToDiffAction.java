// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.Ode.DiffIds;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.DiffProjectEditor;
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

import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
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
        Ode.getInstance().setDiffFileContents(files);
        // go through designer screens and save info
        HashMap<String, DiffIds> diffInfo = new HashMap<>();
        HashMap<String, HashMap<String, List<String>>> modifiedAttributes = new HashMap<>();

        for (String fileName : files.keySet()) {
          if (fileName.endsWith(".scm")) {
            String uploadedContent = files.get(fileName);
            JSONObject uploadedJsonObject = YoungAndroidSourceAnalyzer.parseSourceFile(uploadedContent, new ClientJsonParser());
            String entityName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length()-4);
            

            FileEditor correspondingFileEditor = Ode.getCurrentProjectEditor().getFileEditor(entityName, DesignerEditor.class.getSimpleName());
            if (correspondingFileEditor instanceof YaFormEditor) {
              String correspondingScreen = correspondingFileEditor.getRawFileContent();
              JSONObject correspondingJsonObject = YoungAndroidSourceAnalyzer.parseSourceFile(correspondingScreen, new ClientJsonParser());

              JavaScriptObject out = SwitchToDiffAction.openDesignerDiff(correspondingJsonObject.toJson(), uploadedJsonObject.toJson());
              DiffResult diffResult = out.cast();

              List<String> newIds = Arrays.asList(diffResult.getNewIds().toString().split(","));
              List<String> deletedIds = Arrays.asList(diffResult.getRemovedIds().toString().split(","));
              List<String> movedIds = Arrays.asList(diffResult.getMovedIds().toString().split(","));

              List<String> modifiedIds = Arrays.asList(diffResult.getUpdatedIds().toString().split(","));

              DiffIds ids = new DiffIds(newIds, deletedIds, movedIds, modifiedIds);
              diffInfo.put(entityName, ids);

              UpdateMap updateInfo = diffResult.getUpdatedIdsInfo();
              JsArrayString keys = updateInfo.getKeys();
              HashMap<String, List<String>> idToAttribute = new HashMap<>();
              for (int i = 0; i < keys.length(); i++) {
                  String key = keys.get(i);
                  JsArrayString attributes = updateInfo.getModifiedAttributes(key);
                  List<String> attributesList = new ArrayList<>();
                  for (int t = 0; t < attributes.length(); t++) {
                    attributesList.add(attributes.get(t));
                  }
                  idToAttribute.put(key, attributesList);
              }

              modifiedAttributes.put(entityName, idToAttribute);
            }
          }
        }

        Ode.getInstance().setDiffIds(diffInfo);
        Ode.getInstance().setModifiedAttributes(modifiedAttributes);
        Ode.getInstance().setInDiffView(true);
        Ode.getInstance().getWorkColumnsEditor().shuffleColumns(Ode.getInstance().getCurrentFileEditor());
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

  // <JsArray<JsObject<String>>>
  private static class UpdateMap extends JavaScriptObject {
    protected UpdateMap() {}

    private final native JsArrayString getKeys()/*-{ 
      return this.keys().toArray(); 
    }-*/;

    private final native JsArray<JsObject<String>> get(String k)/*-{
      return this.get(k);
    }-*/;

    private final native JsArrayString getModifiedAttributes(String k)/*-{
      var propertiesInfo = this.get(k);
      return propertiesInfo.map(function(prop) {
                                  return prop.attribute;
                                });
    }-*/;
  }

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

    private final native UpdateMap getUpdatedIdsInfo()/*-{ 
      return this["updateInfo"];   
    }-*/;
  }
}

