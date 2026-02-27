// -*- mode: java; c-basic-offset: 2; -*-
/**
 * Visual Blocks Editor
 *
 * Copyright 2012 Google Inc.
 * Copyright © 2013-2016 Massachusetts Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Object representing a DeletedBlock for MIT App Inventor.
 */

'use strict';

goog.provide('AI.DeletedBlockIcon');

/**
 * Class for an add.
 * @param {!Blockly.Block} block The block associated with this add.
 * @constructor
 */
AI.DeletedBlockIcon = class extends Blockly.icons.Icon {
  constructor(block) {
    super(block);
  }

  getType() {
    return AI.DeletedBlockIcon.TYPE;
  }

  initView(listener) {
    if (this.svgRoot) {
      return;
    }

    super.initView(listener);
    /* Here's the markup that will be generated:
    <g class="blocklyIconGroup">
      <path class="blocklyIconShield" d="..."/>
      <text class="blocklyIconMark" x="8" y="13">!</text>
    </g>
    */
    Blockly.utils.dom.createSvgElement('circle',
      {'class': 'blocklyDeletedBlockIconOutline',
        'r': AI.DeletedBlockIcon.ICON_RADIUS,
        'cx': AI.DeletedBlockIcon.ICON_RADIUS,
        'cy': AI.DeletedBlockIcon.ICON_RADIUS,
      },
      this.svgRoot);

    Blockly.utils.dom.createSvgElement('path',
      {'class': 'blocklyDeletedBlockIconMinus',
        'd': 'M2,8 H14'},
      // X fills circle vvv
      //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      this.svgRoot);
    Blockly.utils.dom.addClass(this.svgRoot, 'blockly-icon-deleted-block');
  }

  getSize() {
    return AI.DeletedBlockIcon.SIZE;
  }

  getWeight() {
    return 0;
  }

  isShownWhenCollapsed() {
    return true;
  }

  updateCollapsed() {
    // Do nothing
  }

  getAnchorLocation() {
    const size = this.getSize();
    const midIcon = new Blockly.utils.Coordinate(size.width / 2, size.height / 2);
    return Blockly.utils.Coordinate.sum(this.workspaceLocation, midIcon);
  }

  dispose() {
    super.dispose();
  }

  onLocationChange(blockOrigin) {
    super.onLocationChange(blockOrigin);
  }

  onClick() {
    super.onClick();
  }
};

/**
 * Radius of the DeletedBlock icon.
 */
AI.DeletedBlockIcon.ICON_RADIUS = 8;

/**
 * Type for the DeletedBlock icon.
 */
AI.DeletedBlockIcon.TYPE = new Blockly.icons.IconType('deleted_block');

/**
 * Size of the DeletedBlock icon.
 */
AI.DeletedBlockIcon.SIZE = new Blockly.utils.Size(
  AI.DeletedBlockIcon.ICON_RADIUS * 2, AI.DeletedBlockIcon.ICON_RADIUS * 2);

Blockly.icons.registry.register(AI.DeletedBlockIcon.TYPE, AI.DeletedBlockIcon);
