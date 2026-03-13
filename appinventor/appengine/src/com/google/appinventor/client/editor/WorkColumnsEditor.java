package com.google.appinventor.client.editor;

import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.youngandroid.ConsolePanel;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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
    protected DesignToolbar designToolbar; //done
    @UiField(provided = true)
    protected PaletteBox paletteBox; //done
    @UiField(provided = true)
    protected ViewerBox viewerBox; //done
    @UiField(provided = true)
    protected AssetListBox assetListBox; //done
    @UiField(provided = true)
    protected SourceStructureBox sourceStructureBox;
    @UiField(provided = true)
    protected BlockSelectorBox blockSelectorBox;
    @UiField(provided = true)
    protected PropertiesBox propertiesBox;

    private boolean consoleVisible = false;

    // Singleton palette box instance
    private static final WorkColumnsEditor INSTANCE = new WorkColumnsEditor();

    /**
     * Return the WorkColumnsEditor.
     *
     * @return  WorkColumnsEditor
     */
    public static WorkColumnsEditor getWorkColumnsEditor() {
        return INSTANCE;
    }

    public WorkColumnsEditor() {
        super();
        bindUI();
    }

    public void initializeUi() {
        sourceStructureBox = SourceStructureBox.getSourceStructureBox();
        blockSelectorBox = BlockSelectorBox.getBlockSelectorBox();
        paletteBox = PaletteBox.getPaletteBox(); //done
        viewerBox = ViewerBox.getViewerBox(); //done
        assetListBox = AssetListBox.getAssetListBox(); //done
        propertiesBox = PropertiesBox.getPropertiesBox();
    }

    public void bindUI() {
        initializeUi();
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
     * Returns the workColumns panel.
     *
     * @return {@link HorizontalPanel}
     */
    public FlowPanel getWorkColumns() {
        return workColumns;
    }

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
            workColumns.add(w);
            w.setVisible(true);
        }
        LOG.info("workColumns now has: " + workColumns.getWidgetCount() + workColumns.isAttached() + workColumns.isVisible());
    }
}
