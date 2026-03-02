// -*- mode: Javascript; js-indent-level: 2; -*-
// Copyright © 2013-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle workspace name in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author ewpatton@mit.edu (Evan W. Patton);
 */

'use strict';

goog.provide('AI.Blockly.WorkspaceName');

goog.require('goog.Timer');


/**
 * Class for a workspace name.
 * @implements {Blockly.IPositionable}
 */
Blockly.WorkspaceName = class {
  /**
   * Create a workspace name.
   * @param workspace
   */
  constructor(workspace) {
    this.workspace_ = workspace;
  }
}

/**
 * Height of the workspace name.
 * @type {number}
 * @private
 */
Blockly.WorkspaceName.prototype.WORKSPACE_NAME_HEIGHT_ = 20;

/**
 * Distance between workspace name and bottom edge of workspace.
 * @type {number}
 * @private
 *///
Blockly.WorkspaceName.prototype.MARGIN_TOP_ = 5;

/**
 * Distance between workspace name and right edge of workspace.
 * @type {number}
 * @private
 */
Blockly.WorkspaceName.prototype.MARGIN_SIDE_ = 5;

/**
 * The SVG group containing the workspace name.
 * @type {Element}
 * @private
 */
Blockly.WorkspaceName.prototype.svgGroup_ = null;

/**
 * Left coordinate of the workspace name.
 * @type {number}
 * @private
 */
Blockly.WorkspaceName.prototype.left_ = 0;

/**
 * Top coordinate of the workspace name.
 * @type {number}
 * @private
 */
Blockly.WorkspaceName.prototype.top_ = 0;

Blockly.WorkspaceName.prototype.id = 'workspaceName';

/**
 * Create the workspace name elements.
 * @return {!Element} The workspace name's SVG group.
 */
Blockly.WorkspaceName.prototype.createDom = function(name) {

  this.svgGroup_ = Blockly.utils.dom.createSvgElement('g',
      {'id': "workspaceName"}, null);
  this.workspaceName_ = Blockly.utils.dom.createSvgElement('text',
      {'fill': "black", 'transform':"translate(5,-1)"},
      this.svgGroup_);
  this.workspaceName_.textContent = name;
  return this.svgGroup_;
};

/**
 * Initialize the workspace name.
 */
Blockly.WorkspaceName.prototype.init = function() {
  if (this.initialized_) return;
  this.initialized_ = true;

  this.workspace_.getComponentManager().addComponent({
    component: this,
    weight: 2,
    capabilities: [Blockly.ComponentManager.Capability.POSITIONABLE],
  });
};

/**
 * Dispose of this workspace name.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.WorkspaceName.prototype.dispose = function() {
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }

  this.diffCount_ = null;

};

/**
 * Move the workspace name to the bottom-left corner.
 * @param {Blockly.MetricsManager.UiMetrics} metrics The workspace metrics.
 * @private
 */
Blockly.WorkspaceName.prototype.position_ = function(metrics) {
  if (!metrics) {
    // There are no metrics available (workspace is probably not visible).
    return;
  }
  if (Blockly.RTL) {
    this.left_ = this.MARGIN_SIDE_;
  } else {
    this.left_ = metrics.absoluteMetrics.left + this.MARGIN_SIDE_;
  }
  this.top_ = metrics.absoluteMetrics.top + this.WORKSPACE_NAME_HEIGHT_ + this.MARGIN_TOP_;
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};

/**
 * Positions the workspace name.
 * It is positioned in the lower left corner of the workspace.
 * @param metrics The workspace metrics.
 * @param savedPositions List of rectangles that
 *     are already on the workspace.
 */
Blockly.WorkspaceName.prototype.position = function(metrics, savedPositions) {
  this.position_(metrics);
}

Blockly.WorkspaceName.prototype.getBoundingRectangle = function() {
  var width = 120;  // TODO: this is a guess
  return new Blockly.utils.Rect(this.left_, this.left_ + width, this.top_, this.top_ + this.WORKSPACE_NAME_HEIGHT_)
}
