'use strict';

goog.provide('AI.Blockly.Diff');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('zhangShasha');


AI.Blockly.Diff = class {

    static async diff(blocksContent1, blocksContent2) {
        const ids1 = AI.Blockly.Diff.getFlatIds(blocksContent1);
        const ids2 = AI.Blockly.Diff.getFlatIds(blocksContent2);
        const newIds = [];
        const removedIds = [];
        const movedIds = [];

        for (const block of blocksContent1) {
            if (ids2.includes(block.id)) {
                const parentId1 = block.getParent() ? block.getParent().id : null;
                const b2 = blocksContent2[ids2.indexOf(block.id)];
                const parentId2 = b2.getParent()?.id;
                if (parentId1 && parentId2 && parentId1 !== parentId2) {
                    movedIds.push({
                        id: block.id, 
                        block: block, 
                        from: parentId1, 
                        parentId: parentId2, 
                        isNextBlock: b2.getPreviousBlock()?.id === parentId2,
                        inputName: b2.getParent() ? b2.getParent().getInputWithBlock(b2)?.name : null,
                    }); // if the block is connected to the parent as a next block
                }
            } else {
                removedIds.push(block.id);
            }
        }

        for (const block of blocksContent2) {
            if (!ids1.includes(block.id)) {
                const parent = block.getParent();
                newIds.push({
                    id: block.id,
                    block: block,
                    parentId: parent?.id,
                    isNextBlock: block.getPreviousBlock()?.id === parent?.id, // if the block is connected to the parent as a next block
                    inputName: parent ? parent.getInputWithBlock(block)?.name : null,
                });
            }
        }

        return {
            newIds: newIds,
            removedIds: removedIds,
            movedIds: movedIds,
        }; 
    }

    static getFlatIds(nodes) {
        const ids = [];
        for (const node of nodes) {
            if (node && node.id) {
                ids.push(node.id);
            }
        }
        return ids;
    }

    static getAllIdsForNodes(nodes) {
        console.log("nodes: ", nodes.length, nodes);
        const ids = [];
        for (const node of nodes) {
            console.log("another node");
            ids.push(...AI.Blockly.Diff.getAllIdsForNode(node));
        }
        return ids;
    }

    static getAllIdsForNode(node, ids = new Array()) {
        if (node && node.id) {
            ids.push(node.id);
        }
        const children = node.getChildren();
        console.log('Children of node', node.id, ':', children.map(c => c.id));
        children.forEach(child => AI.Blockly.Diff.getAllIdsForNode(child, ids));
        return ids;
    }

};

