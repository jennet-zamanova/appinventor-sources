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
import com.google.appinventor.client.editor.youngandroid.ConsolePanel;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.GWT;

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
        LOG.info(" " + paletteBox.isAttached() + paletteBox.isVisible() + " palette is: " + paletteBox);
        paletteBox.setVisible(visible);
        sourceStructureBox.setVisible(visible);
        propertiesBox.setVisible(visible);
    }

    public final void shuffleColumns(FileEditor fileEditor) {
        Widget[] widgetsToShow = fileEditor.getWidgetsInRightOrder();
        workColumns.clear();
        for (Widget w : widgetsToShow) {
            LOG.info("widget is: " + w);
            workColumns.add(w);
            w.setVisible(true);
        }
        LOG.info("workColumns now has: " + workColumns.getWidgetCount() + workColumns.isAttached() + workColumns.isVisible());

        Ode.getInstance().setCurrentFileEditor(fileEditor);

        if (designToolbar.getCurrentView() == DesignToolbar.View.DESIGNER && fileEditor instanceof YaFormEditor) {
            YaFormEditor yaEditor = (YaFormEditor) fileEditor;
            yaEditor.refreshCurrentPropertiesPanel();
            sourceStructureBox.show(yaEditor.getForm());
            if (Ode.getInstance().isInDiffView()) {
                (yaEditor).refreshCurrentDiffPropertiesPanel();
                diffSourceStructureBox.show(yaEditor.getForm());
                DiffProjectEditor projectEditor = new DiffProjectEditor(Ode.getInstance().getUiStyleFactory());
                // load project???
                diffViewerBox.show(projectEditor);
            }
        }
    }
}
