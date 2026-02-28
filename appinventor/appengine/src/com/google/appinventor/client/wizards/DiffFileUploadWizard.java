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
import com.google.appinventor.client.jzip.JSZip;
import com.google.appinventor.client.jzip.LoadOptions;
import com.google.appinventor.client.utils.Promise;

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

    getFileBase64(upload.getElement())
        .then(zipBase64 -> getFileConents(zipBase64))
        .then(response -> {
          LOG.info("response" + response.toString());
          this.fileContentCallback.onContent(response);
          return null;
        })
        .error(error -> {
          this.fileContentCallback.onError("Failed to upload file: " + error.toString());
          return null;
        });
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
