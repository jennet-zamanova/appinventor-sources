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

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.dom.client.Element;
import com.google.appinventor.client.utils.Promise;

import java.util.logging.Logger;

/**
 * Wizard for uploading previously archived (downloaded) projects.
 *
 */
public class DiffFileUploadWizard {
  interface DiffFileUploadWizardUiBinder extends UiBinder<Dialog, DiffFileUploadWizard> {}
  private static final Logger LOG = Logger.getLogger(ProjectUploadWizard.class.getName());

  private static final DiffFileUploadWizardUiBinder uibinder =
      GWT.create(DiffFileUploadWizardUiBinder.class);

  @UiField Dialog uploadDialog;
  @UiField FileUpload upload;
  @UiField Button okButton;
  @UiField Button cancelButton;

  // Project archive extension
  private static final String PROJECT_ARCHIVE_EXTENSION = ".xml";
  private final FileContentCallback fileContentCallback;

  public interface FileContentCallback {
    void onContent(String content);
    void onError(String message);
  }

  /**
   * Creates a new project upload wizard.
   */
  public DiffFileUploadWizard(FileContentCallback callback) {
    LOG.warning("Create DiffFileUploadWizard");
    // Initialize UI
    uibinder.createAndBindUi(this);
    upload.setName("Upload XML Archive");
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
      // String content = upload.getElement().getInnerText();

      // LOG.info("contents: " + content + " file " + filename);
      
      getFileText(upload.getElement())
        .then(xmlContent -> {
            LOG.info("contents: " + xmlContent + " file " + filename);
            this.fileContentCallback.onContent(xmlContent);
            // sendContentToSecondaryWorkspace(xmlContent);
            return Promise.resolve(xmlContent);
        })
        .error(error -> {
            LOG.warning("Failed to read .xml file: " + error);
            return Promise.reject(error);
        });
      uploadDialog.hide();
    } else {
      this.fileContentCallback.onError("The selected project is not a project source file!\n" +
      "Project source files are aia files.");
    }
  }

  private static native Promise<String> getFileText(Element element) /*-{
    return new Promise(function(resolve, reject) {
      var reader = new FileReader();
      reader.onload = function(event) {
        resolve(event.target.result); // plain text, no Base64 needed
      };
      reader.onerror = function(event) {
        reject(new Error("Failed to read file: " + event.target.error));
      };
      reader.readAsText(element.files[0]); // text instead of ArrayBuffer
    });
  }-*/;
}
