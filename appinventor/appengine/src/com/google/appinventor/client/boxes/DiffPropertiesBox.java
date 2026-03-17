// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertiesPanel;
import com.google.appinventor.client.widgets.properties.PropertyEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Box implementation for properties panels.
 *
 */
public final class DiffPropertiesBox extends Box {

  // Singleton properties box instance
  private static final Logger LOG = Logger.getLogger(DiffPropertiesBox.class.getName());
  private static DiffPropertiesBox INSTANCE = new DiffPropertiesBox();
  private EditableProperties selectedProperties = null;
  private final PropertiesPanel diffDesignProperties = new PropertiesPanel();

  /**
   * Return the properties box.
   *
   * @return properties box
   */
  public static DiffPropertiesBox getPropertiesBox() {
    return INSTANCE;
  }

  /**
   * Creates new properties box.
   */
  private DiffPropertiesBox() {
    super(MESSAGES.propertiesBoxCaption(),
        200,    // height
        false,  // minimizable
        false,  // removable
        false,  // startMinimized
        false,  // bodyPadding
        false); // highlightCaption
    diffDesignProperties.addStyleName("diff-properties");
    setContent(diffDesignProperties);
  }

  public void refreshPropertiesPanel() {
    diffDesignProperties.clear();
    if (selectedProperties != null) {
      diffDesignProperties.setProperties(selectedProperties);
    }
  }

  public void load(DesignerEditor editor, List<MockComponent> components) {
    DiffPropertiesBox propertiesBox = getPropertiesBox();
    propertiesBox.updateDiffPropertiesPanel(editor, components, true);
    propertiesBox.setVisible(true);
  }

  /*
   * Show the given component's properties in the properties panel.
   */
//   public void show(YaFormEditor formEditor, boolean selected) {
//     MockForm form = formEditor.getForm();
//     List<MockComponent> components = form.getSelectedComponents();
//     if (components == null || components.size() == 0) {
//       throw new IllegalArgumentException("components must be a list of at least 1");
//     }
//     if (selectedProperties != null) {
//       // TODO: figure out what this property change listener should now point to.
//       selectedProperties.removePropertyChangeListener(formEditor);
//     }
//     if (components.size() == 1) {
//       selectedProperties = components.get(0).getProperties();
//     } else {
//       EditableProperties newProperties = new EditableProperties(true);
//       Map<String, EditableProperty> propertyMaps = new HashMap<>();
//       boolean first = true;
//       for (MockComponent component : components) {
//         Set<String> properties = new HashSet<>();
//         for (EditableProperty property : component.getProperties()) {
//           String propertyName = property.getName();
//           // Ignore UUID and NAME properties (can't be edited and always unique)
//           if ("Uuid".equals(propertyName) || "Name".equals(propertyName)) {
//             continue;
//           }
//           if (first) {
//             propertyMaps.put(propertyName + ":" + property.getType(), property);
//           } else {
//             properties.add(propertyName + ":" + property.getType());
//           }
//         }
//         if (properties.size() > 0) {
//           propertyMaps.keySet().retainAll(properties);
//         }
//         first = false;
//       }
//       for (EditableProperty property : propertyMaps.values()) {
//         String name = property.getName();
//         newProperties.addProperty(
//             name,
//             property.getDefaultValue(),
//             property.getCaption(),
//             property.getCategory(),
//             property.getDescription(),
//             PropertiesUtil.createPropertyEditor(property.getEditorType(),
//                 property.getDefaultValue(), formEditor, property.getEditorArgs()),
//             property.getType(),
//             property.getEditorType(),
//             property.getEditorArgs()
//         );

//         // Determine if all components have the same value and apply it
//         String sharedValue = components.get(0).getPropertyValue(name);
//         boolean collision = false;
//         for (MockComponent component : components) {
//           String propValue = component.getPropertyValue(name);
//           if (!sharedValue.equals(propValue)) {
//             sharedValue = "";
//             collision = true;
//             break;
//           }
//         }
//         newProperties.getProperty(name).getEditor().setMultipleValues(collision);
//         newProperties.getProperty(name).getEditor().setMultiselectMode(true);
//         newProperties.getProperty(name).setValue(sharedValue);
//       }
//       selectedProperties = newProperties;
//     }
//     if (selected) {
//       selectedProperties.addPropertyChangeListener(formEditor);
//     }
//     diffDesignProperties.setProperties(selectedProperties);
//     if (components.size() > 1) {
//       // TODO: Localize
//       diffDesignProperties.setPropertiesCaption(MESSAGES.componentsSelected(components.size()));
//     } else {
//       // need to update the caption after the setProperties call, since
//       // setProperties clears the caption!
//       String componentType = components.get(0).getType();
//       diffDesignProperties.setPropertiesCaption(components.get(0).getName() + " (" +
//           ComponentTranslationTable.getComponentName(componentType.equals("Form")
//               ? "Screen" : componentType) + ")");
//     }
//   }
  public void updateDiffPropertiesPanel(DesignerEditor mainEditor, List<MockComponent> components, boolean selected) {
      LOG.warning("called update diff properties panel" + components);
      String allUpdatedIds = Ode.getInstance().getUpdatedIds();
      List<MockComponent> filteredComponents = components.stream()
                                            .filter(c -> allUpdatedIds.contains(c.getUuid()))
                                            .collect(Collectors.toList());
      // if (filteredComponents == null || filteredComponents.size() == 0) {
      LOG.warning("got filtered components" + filteredComponents + "from components: " + components);
      //   return;
      // }
      if (selectedProperties != null) {
        selectedProperties.removePropertyChangeListener(mainEditor);
      }
      if (filteredComponents.size() == 1) {
        selectedProperties = filteredComponents.get(0).getProperties();
        if (Ode.getInstance().getModifiedAttributes().containsKey(filteredComponents.get(0).getUuid())) {
          List<String> keptProperties = Ode.getInstance().getModifiedAttributes().get(filteredComponents.get(0).getUuid());
          Iterator<EditableProperty> iterator = selectedProperties.iterator();
          while (iterator.hasNext()) {
            EditableProperty property = iterator.next();
            if (!keptProperties.contains(property.getName()) && !"Name".equals(property.getName()) && !"Uuid".equals(property.getName())) {
              selectedProperties.removeProperty(property.getName());
            }
          }
        }
      } else {
        EditableProperties newProperties = new EditableProperties(true);
        Map<String, EditableProperty> propertyMaps = new HashMap<>();
        boolean first = true;
        HashMap<String, List<String>> componentToAttributeMap = Ode.getInstance().getModifiedAttributes();
        for (MockComponent component : filteredComponents) {
          Set<String> properties = new HashSet<>();
          List<String> attributes = new ArrayList<>();
          if (componentToAttributeMap.containsKey(component.getUuid())) {
            attributes = componentToAttributeMap.get(component.getUuid());
          } 
          for (EditableProperty property : component.getProperties()) {
            String propertyName = property.getName();
            LOG.warning("prop: " + propertyName + "available properties: " + attributes);
            // Ignore UUID and NAME properties (can't be edited and always unique)
            if ("Uuid".equals(propertyName) || "Name".equals(propertyName) || !attributes.contains(propertyName)) {
              continue;
            }
            if (first) {
              propertyMaps.put(propertyName + ":" + property.getType(), property);
            } else {
              properties.add(propertyName + ":" + property.getType());
            }
          }
          if (properties.size() > 0) {
            propertyMaps.keySet().retainAll(properties);
          }
          first = false;
        }
        for (EditableProperty property : propertyMaps.values()) {
          PropertyEditor editor = property.getEditor();
          editor.addStyleName("ode-Property-Moved");
          LOG.info("editor: " + editor + "editor name" + editor.getStyleName());
          String name = property.getName();
          newProperties.addProperty(
              name,
              property.getDefaultValue(),
              property.getCaption(),
              property.getCategory(),
              property.getDescription(),
              PropertiesUtil.createPropertyEditor(property.getEditorType(),
                  property.getDefaultValue(), mainEditor, property.getEditorArgs()),
              property.getType(),
              property.getEditorType(),
              property.getEditorArgs()
          );
  
          // Determine if all components have the same value and apply it
          String sharedValue = filteredComponents.get(0).getPropertyValue(name);
          boolean collision = false;
          for (MockComponent component : filteredComponents) {
            String propValue = component.getPropertyValue(name);
            if (!sharedValue.equals(propValue)) {
              sharedValue = "";
              collision = true;
              break;
            }
          }
          newProperties.getProperty(name).getEditor().setMultipleValues(collision);
          newProperties.getProperty(name).getEditor().setMultiselectMode(true);
          newProperties.getProperty(name).setValue(sharedValue);
        }
        selectedProperties = newProperties;
      }
      LOG.info("left the loops");
      if (selected) {
        selectedProperties.addPropertyChangeListener(mainEditor);
      }
      Iterator<EditableProperty> iterator = selectedProperties.iterator();
      while (iterator.hasNext()) {
        EditableProperty property = iterator.next();
        if (property.getName().equals("SlotEditorUsed")) {
          property.getEditor().refresh();
        }
      }
      LOG.info("setting designer properties");
      diffDesignProperties.setDiffProperties(selectedProperties);
      setContent(diffDesignProperties);
      if (filteredComponents.size() == 1) {
        
        // need to update the caption after the setProperties call, since
        // setProperties clears the caption!
        diffDesignProperties.setPropertiesCaption(filteredComponents.get(0).getName());
        diffDesignProperties.addStyleName("ode-PropertiesPanel-Moved");
        
      } else {
        // TODO: Localize
        diffDesignProperties.setPropertiesCaption("no properties changed for this component");
  
      }
  
    }
}
