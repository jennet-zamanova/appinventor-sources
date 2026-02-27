// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle warnings in the block editor.
 *
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('AI.Blockly.DiffHandler');

goog.require('AI.BlockUtils');
goog.require('AI.Blockly.FieldLexicalVariable');

Blockly.DiffHandler = function(mainWorkspace, secondaryWorkspace, ids) {
  this.mainWorkspace = mainWorkspace;
  this.secondaryWorkspace = secondaryWorkspace

  this.allDiffIds = new Array(...ids);
  // this.newIds = newIds;
  // this.deletedIds = deletedIds;
  // this.movedIds = movedIds;

  // this.errorIdHash = Object.create(null);
  this.diffCollapseStack1 = [];
  this.diffCollapseStack2 = [];
  this.currentDiffBlockId = '';
  this.updateDiffCount();
  this.diffCount = this.allDiffIds.length;
};

// Blockly.DiffHandler.prototype.diffCount = this.allDiffIds.length;
Blockly.DiffHandler.prototype.getDiffCount = function() {
  return this.diffCount;
};
// Blockly.DiffHandler.DiffState = {
//   NO_ERROR: 0,
//   ERROR: 1
// };

/**
 * The currently selected index into the array of block IDs with errors. If nothing has been
 * selected (i.e., if we are not stepping through errors), this should be -1 so that the next
 * index will be 0.
 * @type {number}
 */
Blockly.DiffHandler.prototype.currentDiff = -1;

/**
 * Tells the indicator to stop displaying the current error/warning, and update
 * the total number of warnings and errors it is displaying.
 */
Blockly.DiffHandler.prototype.updateDiffCount = function() {
  console.log("workspace: ", this.mainWorkspace);
  var indicator = this.mainWorkspace.getDiffIndicator();
  if (indicator) {
    // Indicator is only available after the workspace has been drawn.
    indicator.updateDiffCount();
  }
};

/**
 * Tells the indicator to display the currently selected warnings and errors.
 */
Blockly.DiffHandler.prototype.updateCurrentDiff = function() {
  var indicator = this.mainWorkspace.getDiffIndicator();
  if (indicator) {
    // Indicator is only available after the workspace has been drawn.
    indicator.updateCurrentDiff(this.currentDiff);
  }
};

Blockly.DiffHandler.prototype.previousDiff = function() {
  console.log("trying to get previous block: ", this.allDiffIds, this.currentDiffBlockId, this.currentDiff);
  var length = this.allDiffIds.length;
  if (!length) return;
  if (this.currentDiffBlockId) {
    this.unHighlightBlock_(this.currentDiffBlockId, this.diffCollapseStack1, this.mainWorkspace);
    this.unHighlightBlock_(this.currentDiffBlockId, this.diffCollapseStack2, this.secondaryWorkspace);
  }
  if (this.currentDiff > 0) {
    this.currentDiff--;
  } else {
    this.currentDiff = length - 1;
  }
  this.currentDiffBlockId = this.allDiffIds[this.currentDiff];
  this.diffCollapseStack1 = this.highlightBlock_(this.currentDiffBlockId, this.mainWorkspace);
  this.diffCollapseStack2 = this.highlightBlock_(this.currentDiffBlockId, this.secondaryWorkspace);
};

Blockly.DiffHandler.prototype.nextDiff = function() {
  console.log("trying to get next block: ", this.allDiffIds, this.currentDiffBlockId, this.currentDiff);
  var length = this.allDiffIds.length;
  if (!length) return;
  if (this.currentDiffBlockId) {
    this.unHighlightBlock_(this.currentDiffBlockId, this.diffCollapseStack1, this.mainWorkspace);
    this.unHighlightBlock_(this.currentDiffBlockId, this.diffCollapseStack2, this.secondaryWorkspace);
  }
  console.log("length: ", length);
  if (this.currentDiff < length - 1) {
    this.currentDiff++;
  } else {
    this.currentDiff = 0;
  }
  this.currentDiffBlockId = this.allDiffIds[this.currentDiff];
  this.diffCollapseStack1 = this.highlightBlock_(this.currentDiffBlockId, this.mainWorkspace);
  this.diffCollapseStack2 = this.highlightBlock_(this.currentDiffBlockId, this.secondaryWorkspace);
};

/**
 * Highlights the block with the given block id. Expands all collapsed parent
 * blocks so that the highlighted block is visible.
 * @return A list of the ids of all of the blocks that had to be expanded.
 * @private
 */
Blockly.DiffHandler.prototype.highlightBlock_ = function(blockId, workspace) {
  console.log("blockid: ", blockId);
  var collapseStack = [];
  var block = workspace.getBlockById(blockId);
  console.log("highlight: ", block);
  if (block) {
    block.setHighlighted(true);
    console.log("should've highlighted blocks");

    do {
      if (block.isCollapsed()) {
        collapseStack.push(block.id);
        block.setCollapsed(false);
      }
    } while ((block = block.getSurroundParent()))

    workspace.centerOnBlock(blockId);
    this.updateCurrentDiff();
  }

  return collapseStack;
};

/**
 * Unhighlights the block with the given block id, and collapses all blocks
 * with the ids in the passed collapseStack.
 * @private
 */
Blockly.DiffHandler.prototype.unHighlightBlock_ =
  function(blockId, collapseStack, workspace) {
    const block = workspace.getBlockById(blockId);
    if (block) {
      block.setHighlighted(false);
      
      for (var i = 0, blockId; (blockId = collapseStack[i]); i++) {
        const tempBlock = workspace.getBlockById(blockId);
        if (tempBlock) {
          tempBlock.setCollapsed(true);
        }
      }
    }
  };

/**
 * Hides any currently highlighted blocks (either highlighted for warning or
 * error).
 */
Blockly.DiffHandler.prototype.hideCurrentHighlight_ = function() {
  if (this.currentDiffBlockId) {
    this.unHighlightBlock_(this.currentErrorBlockId, this.diffCollapseStack1, this.mainWorkspace);
    this.unHighlightBlock_(this.currentErrorBlockId, this.diffCollapseStack2, this.secondaryWorkspace);
    this.currentDiffBlockId = '';
  }
}

/**
 * Recursively counts all child blocks of a given block.
 * @param {Blockly.Block} block The parent block.
 * @return {number} The total number of child blocks (including nested children).
 */
Blockly.DiffHandler.prototype.countChildBlocks = function(block) {
  var count = 0;
  var childBlocks = block.getChildren();

  for (var i = 0; i < childBlocks.length; i++) {
    var childBlock = childBlocks[i];
    if (childBlock) {
      console.log(`Found child block: ${childBlock.type} (ID: ${childBlock.id})`);
      // Count the child block itself
      count += 1;
      // Recursively count the child block's children
      var nestedChildCount = this.countChildBlocks(childBlock);
      count += nestedChildCount;
    }
  }

  return count;
};