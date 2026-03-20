// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.dom.client.Element;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DiffProjectEditor;
// import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.client.jzip.JSZip;
import com.google.appinventor.client.jzip.LoadOptions;
import com.google.appinventor.client.jzip.Type;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceFolderNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Wizard for uploading previously archived (downloaded) projects.
 *
 */
public class DiffFileUploadWizard {
  interface DiffFileUploadWizardUiBinder extends UiBinder<Dialog, DiffFileUploadWizard> {
  }

  private static final Logger LOG = Logger.getLogger(ProjectUploadWizard.class.getName());

  private static final DiffFileUploadWizardUiBinder uibinder = GWT.create(DiffFileUploadWizardUiBinder.class);

  @UiField
  Dialog uploadDialog;
  @UiField
  FileUpload upload;
  @UiField
  Button okButton;
  @UiField
  Button cancelButton;

  // Project archive extension
  private static final String PROJECT_ARCHIVE_EXTENSION = ".aia";
  private final FileContentCallback fileContentCallback;

  public interface FileContentCallback {
    void onContent(Map<String, String> content);

    void onError(String message);
  }

  /**
   * Creates a new project upload wizard.
   */
  public DiffFileUploadWizard(FileContentCallback callback) {
    LOG.warning("Create DiffFileUploadWizard");
    // Initialize UI
    uibinder.createAndBindUi(this);
    upload.setName("Upload AIA Archive");
    upload.getElement().setAttribute("accept", PROJECT_ARCHIVE_EXTENSION);
    this.fileContentCallback = callback;
  }

  public void show() {
    uploadDialog.center();
  }

  @UiHandler("cancelButton")
  void cancelUpload(ClickEvent e) {
    uploadDialog.hide();
  }

  @UiHandler("okButton")
  void executeUpload(ClickEvent e) {
    String filename = upload.getFilename();
    LOG.info("upload: " + upload + upload.getElement() + upload.getElement());
    if (filename.endsWith(PROJECT_ARCHIVE_EXTENSION)) {

      this.handleProjectUpload(upload);

      uploadDialog.hide();
    } else {
      this.fileContentCallback.onError("The selected project is not a project source file!\n" +
          "Project source files are aia files.");
    }
  }

  private void handleProjectUpload(FileUpload upload) {

    Promise<String> uploadedContent = getFileBase64(upload.getElement());
    uploadedContent.then(zipBase64 -> newProjectFromExternalTemplate("$diff", zipBase64))
                  .then(root -> {
                    Ode.getInstance().setDiffRoot(root); 
                    DiffProjectEditor projectEditor = new DiffProjectEditor(Ode.getInstance().getUiStyleFactory());
                    Ode.getInstance().setDiffProjectEditor(projectEditor);
                    projectEditor.processProject();
                    return null;
                  });
    uploadedContent.then(zipBase64 -> getFileConents(zipBase64))
        .then(response -> {
          LOG.info("response" + response.toString().length());
          this.fileContentCallback.onContent(response);
          return null;
        })
        .error(error -> {
          this.fileContentCallback.onError("Failed to upload file: " + error.toString());
          return null;
        });
    
  }

  public Promise<YoungAndroidProjectNode> newProjectFromExternalTemplate(String projectName, String zipData) {
    // UserProject project = new UserProject(projectName.hashCode(), projectName, "YoungAndroid", System.currentTimeMillis(), System.currentTimeMillis(), false);
    // projects.put(projectName, project);
    long hash = projectName.hashCode();
    UserProject diffProject = new UserProject(projectName.hashCode(), projectName, "YoungAndroid", System.currentTimeMillis(), System.currentTimeMillis(), false);
    Project pr = new Project(projectName);
    YoungAndroidProjectNode root = new YoungAndroidProjectNode(projectName, hash);
    ProjectNode assetsNode = new YoungAndroidAssetsFolder("assets");
    ProjectNode sourcesNode = new YoungAndroidSourceFolderNode("src");
    ProjectNode compsNode = new YoungAndroidComponentsFolder("assets/external_comps");
    root.addChild(assetsNode);
    root.addChild(sourcesNode);
    root.addChild(compsNode);
    // projectData.put(hash, root);

    // Process the zip data
    final JSZip zip = new JSZip();
    final Map<String, ArrayBuffer> contents = new HashMap<>();
    return zip.loadAsync(zipData, LoadOptions.create(true))
        .then0(() -> {
          final Map<String, ProjectNode> packagesMap = new HashMap<>();
          final List<Promise> promises = new ArrayList<>();
          zip.forEach((name, zipObject) -> {
            promises.add(zipObject.get(Type.ARRAY_BUFFER).then((buffer) -> {
              contents.put("$diff:" + name, buffer);
              return Promise.resolve(buffer);
            }));
            if (name.startsWith("assets/")) {
              if (name.startsWith("assets/external_comps/")) {
                // This is a file in the external components directory
                compsNode.addChild(new YoungAndroidComponentNode(StorageUtil.basename(name), name));
              } else {
                // This is a file in the assets directory
                assetsNode.addChild(new YoungAndroidAssetNode(StorageUtil.basename(name), name));
              }
            } else if (name.startsWith("src/")) {
              YoungAndroidSourceNode sourceNode = null;
              if (name.endsWith(".scm")) {
                sourceNode = new YoungAndroidFormNode(name);
              } else if (name.endsWith(".bky")) {
                sourceNode = new YoungAndroidBlocksNode(name);
              } else if (name.endsWith(".yail")) {
                sourceNode = new YoungAndroidYailNode(name);
              }
              if (sourceNode != null) {
                String packageName = StorageUtil.getPackageName(sourceNode.getQualifiedName());
                ProjectNode packageNode = packagesMap.get(packageName);
                if (packageNode == null) {
                  packageNode = new YoungAndroidPackageNode(packageName, packageNameToPath(packageName));
                  packagesMap.put(packageName, packageNode);
                  sourcesNode.addChild(packageNode);
                }
                packageNode.addChild(sourceNode);
              }
            }
          });
          Promise<?>[] promisesArray = promises.toArray(new Promise[0]);
          return Promise.allOf(promisesArray).then0(() -> {
            Ode.getInstance().setDiffContents(contents);
            return Promise.resolve(root);
          });
        });
  }

  private static String packageNameToPath(String packageName) {
    return "src/" + packageName.replace('.', '/');
  }



  public Promise<Map<String, String>> getFileConents(String zipData) {

    final JSZip zip = new JSZip();

    return zip.loadAsync(zipData, LoadOptions.create(true))
        .then0(() -> {
          Map<String, String> filesMap = new HashMap<>();
          final List<Promise<?>> promises = new ArrayList<>();

          zip.forEach((name, zipObject) -> {
            // Skip directories
            if (!zipObject.dir) {

              Promise<?> p = zipObject.async("string")
                  .then(content -> {
                    filesMap.put(name, (String) content);
                    return null;
                  });

              promises.add(p);
            }
          });

          Promise<?>[] promisesArray = promises.toArray(new Promise[0]);

          return Promise.allOf(promisesArray).then0(() -> Promise.resolve(filesMap));
        });
  }

  private static native Promise<String> getFileBase64(Element element) /*-{
    function _arrayBufferToBase64(buffer) {
      var binary = '';
      var bytes = new Uint8Array(buffer);
      var len = bytes.byteLength;
      for (var i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      return $wnd.btoa(binary);
    }
    return new Promise(function(resolve, reject) {
      var reader = new FileReader();
      reader.onload = function(event) {
        resolve(_arrayBufferToBase64(event.target.result));
      };
      reader.onerror = function(event) {
        reject(new Error("Failed to read file: " + event.target.error));
      };
      reader.readAsArrayBuffer(element.files[0]);
    });
  }-*/;
}
