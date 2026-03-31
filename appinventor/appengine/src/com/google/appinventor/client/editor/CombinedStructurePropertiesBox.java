package com.google.appinventor.client.editor;
import java.util.logging.Logger;

import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CombinedStructurePropertiesBox extends Composite {
    // interface CombinedStructurePropertiesBoxUiBinder extends UiBinder<FlowPanel, CombinedStructurePropertiesBox> {
    // }

    // CombinedStructurePropertiesBoxUiBinder uibinder = GWT.create(CombinedStructurePropertiesBoxUiBinder.class);

    private static final Logger LOG = Logger.getLogger(CombinedStructurePropertiesBox.class.getName());

    // @UiField(provided = true)
    // protected SourceStructureBox diffSourceStructureBox; // maybe
    // @UiField(provided = true)
    // protected PropertiesBox diffPropertiesBox; //done, done

    private static CombinedStructurePropertiesBox INSTANCE = new CombinedStructurePropertiesBox();
    private final FlowPanel panel;

    public static CombinedStructurePropertiesBox get() {
        if (INSTANCE == null) {
            INSTANCE = new CombinedStructurePropertiesBox();
        }
        INSTANCE.attach();
        return INSTANCE;
    }

    private CombinedStructurePropertiesBox() {
        // diffSourceStructureBox = SourceStructureBox.getSourceStructureBox();
        // diffPropertiesBox = PropertiesBox.getPropertiesBox();
        panel = new FlowPanel();
        // panel.add(SourceStructureBox.getSourceStructureBox());
        // panel.add(PropertiesBox.getPropertiesBox());
        panel.setHeight("100%");
        panel.getElement().getStyle().setProperty("display", "flex");
        panel.getElement().getStyle().setProperty("flexDirection", "column");
        initWidget(panel);
        // LOG.info("panel: "+ panel);
        // initWidget(uibinder.createAndBindUi(this));
    }

    public void setVisible(boolean visible) {
        SourceStructureBox.getSourceStructureBox().setVisible(visible);
        PropertiesBox.getPropertiesBox().setVisible(visible);
        super.setVisible(visible);
    }

    public void attach() {
        panel.clear(); // detaches current children from panel (but singletons still exist)
        panel.add(SourceStructureBox.getSourceStructureBox());
        panel.add(PropertiesBox.getPropertiesBox());
        // SourceStructureBox.getSourceStructureBox().setHeight("50%");
        // PropertiesBox.getPropertiesBox().setHeight("50%");
    }
}