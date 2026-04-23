package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.DiffAssetListBox;
import com.google.appinventor.client.boxes.DiffPropertiesBox;
import com.google.appinventor.client.boxes.DiffSourceStructureBox;
import com.google.appinventor.client.boxes.DiffViewerBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.client.editor.youngandroid.ConsolePanel;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.DiffProjectEditor;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

public class WorkColumnsEditor extends Composite {
    interface WorkColumnsEditorUiBinder extends UiBinder<FlowPanel, WorkColumnsEditor> {
    }

    WorkColumnsEditorUiBinder uibinder = GWT.create(WorkColumnsEditorUiBinder.class);

    private static final Logger LOG = Logger.getLogger(WorkColumnsEditor.class.getName());

    @UiField
    protected FlowPanel workColumns;  //done
    @UiField
    protected FlowPanel structureAndAssets; //done
    @UiField 
    protected ConsolePanel consolePanel; //done
    @UiField 
    protected Label missingScreenLabel;
    @UiField 
    protected Label missingUploadedScreenLabel;

    @UiField
    protected DesignToolbar designToolbar; //done, done
    @UiField(provided = true)
    protected PaletteBox paletteBox; //done, do not show palette in diff mode
    @UiField(provided = true)
    protected ViewerBox viewerBox; //done, update show function
    @UiField(provided = true)
    protected AssetListBox assetListBox; //done, done
    @UiField(provided = true)
    protected SourceStructureBox sourceStructureBox; // maybe
    @UiField(provided = true)
    protected BlockSelectorBox blockSelectorBox; // do not show in diff mode
    @UiField(provided = true)
    protected PropertiesBox propertiesBox; //done, done
    @UiField(provided = true)
    protected CombinedStructurePropertiesBox combinedDiffBox;

    @UiField
    protected FlowPanel diffWorkColumns;  //done
    @UiField
    protected FlowPanel diffStructureAndAssets; //done

    @UiField(provided = true)
    protected DiffViewerBox diffViewerBox; //done, update show function
    @UiField(provided = true)
    protected DiffAssetListBox diffAssetListBox; //done, done
    @UiField(provided = true)
    protected DiffSourceStructureBox diffSourceStructureBox; // maybe
    @UiField(provided = true)
    protected DiffPropertiesBox diffPropertiesBox; //done, done

    private boolean consoleVisible = false;

    // Singleton palette box instance
    // private static final WorkColumnsEditor INSTANCE = new WorkColumnsEditor();

    /**
     * Return the WorkColumnsEditor.
     *
     * @return  WorkColumnsEditor
     */
    // public static WorkColumnsEditor getWorkColumnsEditor() {
    //     return INSTANCE;
    // }

    public WorkColumnsEditor() {
        LOG.info("creating work columns!");
        initializeUi();
        bindUI();
    }

    public void initializeUi() {
        sourceStructureBox = SourceStructureBox.getSourceStructureBox();
        blockSelectorBox = BlockSelectorBox.getBlockSelectorBox();
        paletteBox = PaletteBox.getPaletteBox(); //done
        viewerBox = ViewerBox.getViewerBox(); //done
        assetListBox = AssetListBox.getAssetListBox(); //done
        propertiesBox = PropertiesBox.getPropertiesBox();
        combinedDiffBox = CombinedStructurePropertiesBox.get(); //only used if in diff

        
        diffViewerBox = DiffViewerBox.getViewerBox(); //done
        diffAssetListBox = DiffAssetListBox.getAssetListBox(); //done
        diffSourceStructureBox = DiffSourceStructureBox.getSourceStructureBox();
        diffPropertiesBox = DiffPropertiesBox.getPropertiesBox();
    }

    public void bindUI() {
        
        initWidget(uibinder.createAndBindUi(this));
        LOG.info("finished init");
    }

    /**
     * Returns the structureAndAssets panel.
     *
     * @return {@link VerticalPanel}
     */
    public FlowPanel getStructureAndAssets() {
        return structureAndAssets;
    }

    /**
     * Returns the diff structureAndAssets panel.
     *
     * @return {@link VerticalPanel}
     */
    public FlowPanel getDiffStructureAndAssets() {
        return diffStructureAndAssets;
    }

    /**
     * Returns the workColumns panel.
     *
     * @return {@link HorizontalPanel}
     */
    // public FlowPanel getWorkColumns() {
    //     return workColumns;
    // }

    /**
     * Returns the palette box.
     *
     * @return {@link PaletteBox}
     */
    // public PaletteBox getPaletteBox() {
    //     return paletteBox;
    // }

    /**
     * Returns the viewer box.
     *
     * @return {@link ViewerBox}
     */
    public ViewerBox getViewerBox() {
        return viewerBox;
    }

    /**
     * Returns the diff viewer box.
     *
     * @return {@link ViewerBox}
     */
    public DiffViewerBox getDiffViewerBox() {
        return diffViewerBox;
    }

    /**
     * Returns the assetlist box.
     *
     * @return {@link AssetListBox}
     */
    public AssetListBox getAssetListBox() {
        return assetListBox;
    }

    /**
     * Returns the design tool bar.
     *
     * @return {@link DesignToolbar}
     */
    public DesignToolbar getDesignToolbar() {
        return designToolbar;
    }

    /**
     * Returns the structure box.
     *
     * @return {@link SourceStructureBox}
     */
    public SourceStructureBox getSourceStructureBox() {
        return sourceStructureBox;
    }

    /**
     * Returns the structure box.
     *
     * @return {@link DiffSourceStructureBox}
     */
    public DiffSourceStructureBox getDiffSourceStructureBox() {
        return diffSourceStructureBox;
    }

    /**
     * Returns the properties box.
     *
     * @return {@link PropertiesBox}
     */
    // public PropertiesBox getPropertiesBox() {
    //     return propertiesBox;
    // }

    public void setConsoleVisible(boolean visible) {
        consoleVisible = visible;
        if (visible) {
            consolePanel.setVisible(true);
            consolePanel.setWidth("300px");
        } else {;
            consolePanel.setVisible(false);
        }
    }

    public boolean isConsoleVisible() {
        return consoleVisible;
    }

    public void setDesignerComponentsVisible(boolean visible) {
        paletteBox.setVisible(visible);
        sourceStructureBox.setVisible(visible);
        propertiesBox.setVisible(visible);
    }

    public final void resetToDesignerNormalView() {
        Ode.getInstance().getDesignToolbar().setSwitchFromDiffButtonVisible(false);
        structureAndAssets.insert(sourceStructureBox, 0);
        Widget[] widgetsToShow = new Widget[]{PaletteBox.getPaletteBox(), viewerBox, structureAndAssets, propertiesBox};
        workColumns.clear();
        for (Widget w : widgetsToShow) {
            workColumns.add(w);
            w.setVisible(true);
        }
        missingScreenLabel.setVisible(false);
        missingUploadedScreenLabel.setVisible(false);
    }

    public final void shuffleColumnsMissingScreen(FileEditor fileEditor, FileEditor screen1Editor) {
        missingScreenLabel.setVisible(true);
        missingUploadedScreenLabel.setVisible(false);
        Widget[] widgetsToShow = fileEditor.getWidgetsInRightOrder();
        workColumns.clear();
        for (Widget w : widgetsToShow) {
            workColumns.add(w);
            w.setVisible(true);
        }

        Ode.getInstance().setCurrentFileEditor(screen1Editor);

        if (designToolbar.getCurrentView() == DesignToolbar.View.DESIGNER && fileEditor instanceof YaFormEditor) {
            DiffProjectEditor projectEditor = Ode.getInstance().getDiffProjectEditor();
            YaFormEditor yaDiffEditor = (YaFormEditor) fileEditor;
            yaDiffEditor.refreshCurrentPropertiesPanel();
            diffSourceStructureBox.show(yaDiffEditor.getForm());
            // load project???
            diffViewerBox.showFile(projectEditor, yaDiffEditor);

            // todo
            viewerBox.clear();
            sourceStructureBox.clear();
            propertiesBox.clear();
            
            
            sourceStructureBox.addStyleName("diff-Split-Props");
            propertiesBox.addStyleName("diff-Split-Props");
            viewerBox.setCaption("Original Viewer");
            propertiesBox.setCaption("Original Properties");
        } else if (designToolbar.getCurrentView() == DesignToolbar.View.BLOCKS && fileEditor instanceof YaBlocksEditor) {
            YaBlocksEditor yaEditor = (YaBlocksEditor) fileEditor;
            Map<String, String> files = Ode.getInstance().getDiffFileContents();
            for (String fileName : files.keySet()) {
                if (fileName.endsWith(yaEditor.getEntityName() + ".bky")) {
                  String content = files.get(fileName);
                  // parse it, save it, display it raw — whatever you want
                //   todo: or just make it main workspace?
                  WorkColumnsEditor.openSecondaryWorkspace(content);
                  viewerBox.setCaption("Viewer");
                  viewerBox.show();
                  return;
                }
            }
        }
    }

    public final void shuffleColumns(FileEditor fileEditor) {
        missingUploadedScreenLabel.setVisible(false);
        missingScreenLabel.setVisible(false);
        Widget[] widgetsToShow = fileEditor.getWidgetsInRightOrder();
        workColumns.clear();
        for (Widget w : widgetsToShow) {
            workColumns.add(w);
            w.setVisible(true);
        }

        Ode.getInstance().setCurrentFileEditor(fileEditor);

        if (designToolbar.getCurrentView() == DesignToolbar.View.DESIGNER && fileEditor instanceof YaFormEditor) {
            YaFormEditor yaEditor = (YaFormEditor) fileEditor;
            yaEditor.refreshCurrentPropertiesPanel();
            sourceStructureBox.show(yaEditor.getForm());
            if (viewerBox.isCleared()) {
                viewerBox.show(yaEditor.getProjectRootNode());
            }
            if (Ode.getInstance().isInDiffView()) {
                DiffProjectEditor projectEditor = Ode.getInstance().getDiffProjectEditor();
                YaFormEditor yaDiffEditor = (YaFormEditor) projectEditor.getFileEditor(yaEditor.getEntityName(), yaEditor.getEditorType());
                if (yaDiffEditor != null) {
                    yaDiffEditor.refreshCurrentPropertiesPanel();
                    diffSourceStructureBox.show(yaDiffEditor.getForm());
                    // load project???
                    diffViewerBox.show(projectEditor);
                } else {
                    missingUploadedScreenLabel.setVisible(true);
                    diffViewerBox.clear();
                    diffSourceStructureBox.clear();
                    diffPropertiesBox.clear();
                    LOG.warning("there is no fileeditor matching name and type " + yaEditor.getEntityName() + yaEditor.getEditorType());
                }
                
                sourceStructureBox.addStyleName("diff-Split-Props");
                propertiesBox.addStyleName("diff-Split-Props");
                viewerBox.setCaption("Original Viewer");
                propertiesBox.setCaption("Original Properties");
                // .ode-SourceScrollPanel
            } else {
                sourceStructureBox.removeStyleName("diff-Split-Props");
                propertiesBox.removeStyleName("diff-Split-Props");
                viewerBox.setCaption("Viewer");
                propertiesBox.setCaption("Properties");
            }
        } else if (designToolbar.getCurrentView() == DesignToolbar.View.BLOCKS && Ode.getInstance().isInDiffView() && fileEditor instanceof YaBlocksEditor) {
            
            YaBlocksEditor yaEditor = (YaBlocksEditor) fileEditor;
            Map<String, String> files = Ode.getInstance().getDiffFileContents();
            for (String fileName : files.keySet()) {
                if (fileName.endsWith(yaEditor.getEntityName() + ".bky")) {
                  String content = files.get(fileName);
                  // parse it, save it, display it raw — whatever you want
                  WorkColumnsEditor.openSecondaryWorkspace(content);
                  viewerBox.setCaption("Viewer");
                  return;
                }
            }
            WorkColumnsEditor.openSecondaryEmptyWorkspace();
            viewerBox.setCaption("Viewer");
            viewerBox.show();
            missingUploadedScreenLabel.setVisible(true);
        }
    }

    public static native void openSecondaryWorkspace(String file) /*-{
        $wnd.openSecondaryWorkspace(file);
    }-*/;

    public static native void openSecondaryEmptyWorkspace() /*-{
        $wnd.openSecondaryEmptyWorkspace();
    }-*/;
}
