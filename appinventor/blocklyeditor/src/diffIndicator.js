// -*- mode: Javascript; js-indent-level: 2; -*-
// Copyright © 2013-2016 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 * @author ewpatton@mit.edu (Evan W. Patton);
 */

'use strict';

goog.provide('AI.Blockly.DiffIndicator');

goog.require('goog.Timer');


/**
 * Class for a warning indicator.
 * @implements {Blockly.IPositionable}
 */
Blockly.DiffIndicator = class {
  /**
   * Create a warning indicator.
   * @param workspace
   */
  constructor(workspace) {
    this.workspace_ = workspace;
  }
}

/**
 * Height of the warning indicator.
 * @type {number}
 * @private
 */
Blockly.DiffIndicator.prototype.INDICATOR_HEIGHT_ = 40;

/**
 * Distance between warning indicator and bottom edge of workspace.
 * @type {number}
 * @private
 *///
Blockly.DiffIndicator.prototype.MARGIN_BOTTOM_ = 105;

/**
 * Distance between warning indicator and right edge of workspace.
 * @type {number}
 * @private
 */
Blockly.DiffIndicator.prototype.MARGIN_SIDE_ = 35;

/**
 * The SVG group containing the warning indicator.
 * @type {Element}
 * @private
 */
Blockly.DiffIndicator.prototype.svgGroup_ = null;

/**
 * Left coordinate of the warning indicator.
 * @type {number}
 * @private
 */
Blockly.DiffIndicator.prototype.left_ = 0;

/**
 * Top coordinate of the warning indicator.
 * @type {number}
 * @private
 */
Blockly.DiffIndicator.prototype.top_ = 0;

Blockly.DiffIndicator.prototype.id = 'diffIndicator';

/**
 * Create the warning indicator elements.
 * @return {!Element} The warning indicator's SVG group.
 */
Blockly.DiffIndicator.prototype.createDom = function() {

  this.svgGroup_ = Blockly.utils.dom.createSvgElement('g',
      {'id': "indicatorDiff"}, null);
  this.diffCount_ = Blockly.utils.dom.createSvgElement('text',
      {'fill': "black", 'transform':"translate(85,-1)"},
      this.svgGroup_);
  this.diffCount_.textContent = "0";

  this.iconDiffGroup_ = Blockly.utils.dom.createSvgElement('g',
      {'class': 'blocklyIconGroup', 'transform':"translate(65,0)"}, this.svgGroup_);
  Blockly.utils.dom.createSvgElement('circle',
      {'class': 'blocklyDiffIconOutline',
       'r': AI.ErrorIcon.ICON_RADIUS,
       'cx': AI.ErrorIcon.ICON_RADIUS,
       'cy': AI.ErrorIcon.ICON_RADIUS - 15}, this.iconDiffGroup_);
  Blockly.utils.dom.createSvgElement('path',
      {'class': 'blocklyDiffIconDelta',
       'd': 'M2,-3 L8,-14 L13,-3 L2,-3'},
                           // X fills circle vvv
       //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      this.iconDiffGroup_);

  this.diffNavPrevious_ = Blockly.utils.dom.createSvgElement('path',
      {"d": "M 67,7 L 77,17 L 87,7 Z", 'class':"warningNav"},
      this.svgGroup_);

  this.diffNavNext_ = Blockly.utils.dom.createSvgElement('path',
      {"d": "M 87,-21 L 67,-21 L 77,-31 Z", 'class':"warningNav"},
      this.svgGroup_);

  return this.svgGroup_;
};

/**
 * Initialize the warning indicator.
 */
Blockly.DiffIndicator.prototype.init = function() {
  if (this.initialized_) return;
  this.initialized_ = true;

  this.workspace_.getComponentManager().addComponent({
    component: this,
    weight: 2,
    capabilities: [Blockly.ComponentManager.Capability.POSITIONABLE],
  });
  // If the document resizes, reposition the warning indicator.
  // Blockly.browserEvents.bind(window, 'resize', this, this.position_);
  Blockly.browserEvents.bind(this.diffNavPrevious_, 'click', this, Blockly.DiffIndicator.prototype.onclickDiffNavPrevious);
  Blockly.browserEvents.bind(this.diffNavNext_, 'click', this, Blockly.DiffIndicator.prototype.onclickDiffNavNext);

  // We stop propagating the mousedown event so that Blockly doesn't prevent click events in Firefox, which breaks
  // the click event handler above.
  Blockly.browserEvents.bind(this.diffNavPrevious_, 'mousedown', this, function(e) { e.stopPropagation() });
  Blockly.browserEvents.bind(this.diffNavNext_, 'mousedown', this, function(e) { e.stopPropagation() });
};

/**
 * Dispose of this warning indicator.
 * Unlink from all DOM elements to prevent memory leaks.
 */
Blockly.DiffIndicator.prototype.dispose = function() {
  if (this.svgGroup_) {
    goog.dom.removeNode(this.svgGroup_);
    this.svgGroup_ = null;
  }

  this.diffCount_ = null;
  this.iconDiffGroup_ = null;
  this.iconDiffMark_ = null;

};

/**
 * Move the warning indicator to the bottom-left corner.
 * @param {Blockly.MetricsManager.UiMetrics} metrics The workspace metrics.
 * @private
 */
Blockly.DiffIndicator.prototype.position_ = function(metrics) {
  if (!metrics) {
    // There are no metrics available (workspace is probably not visible).
    return;
  }
  if (Blockly.RTL) {
    this.left_ = this.MARGIN_SIDE_;
  } else {
    this.left_ = metrics.absoluteMetrics.left + this.MARGIN_SIDE_;
  }
  this.top_ = metrics.viewMetrics.height + metrics.absoluteMetrics.top -
      (this.INDICATOR_HEIGHT_) - this.MARGIN_BOTTOM_;
  this.svgGroup_.setAttribute('transform',
      'translate(' + this.left_ + ',' + this.top_ + ')');
};

/**
 * Update the diff and warning count on the indicator.
 *
 */
Blockly.DiffIndicator.prototype.updateDiffCount = function() {
  this.diffCount_.textContent = this.workspace_.getDiffHandler().getDiffCount();
}

Blockly.DiffIndicator.prototype.updateCurrentDiff = function(currentDiff) {
  var handler = this.workspace_.getDiffHandler();
  currentDiff++;  // make it 1-based
  this.diffCount_.textContent = currentDiff + "/" + handler.getDiffCount();
}

/**
 * Change the warning toggle button to have the correct text.
 *
 */

Blockly.DiffIndicator.prototype.onclickDiffNavPrevious = function() {
  Blockly.hideChaff();
  this.workspace_.getDiffHandler().previousDiff();
};

Blockly.DiffIndicator.prototype.onclickDiffNavNext = function() {
  Blockly.hideChaff();
  this.workspace_.getDiffHandler().nextDiff();
};

// Blockly.IPositionable implementation

/**
 * Positions the warning inicator.
 * It is positioned in the lower left corner of the workspace.
 * @param metrics The workspace metrics.
 * @param savedPositions List of rectangles that
 *     are already on the workspace.
 */
Blockly.DiffIndicator.prototype.position = function(metrics, savedPositions) {
  this.position_(metrics);
}

Blockly.DiffIndicator.prototype.getBoundingRectangle = function() {
  var width = 120;  // TODO: this is a guess
  return new Blockly.utils.Rect(this.left_, this.left_ + width, this.top_, this.top_ + this.INDICATOR_HEIGHT_)
}
