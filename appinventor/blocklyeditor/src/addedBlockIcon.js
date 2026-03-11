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
 * @fileoverview Object representing a AddedBlock for MIT App Inventor.
 */

'use strict';

goog.provide('AI.AddedBlockIcon');

/**
 * Class for an add.
 * @param {!Blockly.Block} block The block associated with this add.
 * @constructor
 */
AI.AddedBlockIcon = class extends Blockly.icons.Icon {
  constructor(block) {
    super(block);
  }

  getType() {
    return AI.AddedBlockIcon.TYPE;
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
      {'class': 'blocklyAddedBlockIconOutline',
        'r': AI.AddedBlockIcon.ICON_RADIUS,
        'cx': AI.AddedBlockIcon.ICON_RADIUS,
        'cy': AI.AddedBlockIcon.ICON_RADIUS,
      },
      this.svgRoot);

    Blockly.utils.dom.createSvgElement('path',
      {'class': 'blocklyDiffIcon',
        'd': 'M8,2 V14 M2,8 H14'},
      // X fills circle vvv
      //'d': 'M 3.1931458,3.1931458 12.756854,12.756854 8,8 3.0931458,12.756854 12.756854,3.0931458'},
      this.svgRoot);
    Blockly.utils.dom.addClass(this.svgRoot, 'blocklyIconAddedBlock');
  }

  getSize() {
    return AI.AddedBlockIcon.SIZE;
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
 * Radius of the AddedBlock icon.
 */
AI.AddedBlockIcon.ICON_RADIUS = 8;

/**
 * Type for the AddedBlock icon.
 */
AI.AddedBlockIcon.TYPE = new Blockly.icons.IconType('added_block');

/**
 * Size of the AddedBlock icon.
 */
AI.AddedBlockIcon.SIZE = new Blockly.utils.Size(
  AI.AddedBlockIcon.ICON_RADIUS * 2, AI.AddedBlockIcon.ICON_RADIUS * 2);

Blockly.icons.registry.register(AI.AddedBlockIcon.TYPE, AI.AddedBlockIcon);
