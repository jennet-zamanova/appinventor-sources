'use strict';

goog.provide('AI.Blockly.Diff');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('zhangShasha');


AI.Blockly.Diff = class {

    static async diff(blocksContent1, blocksContent2) {
        const ids1 = AI.Blockly.Diff.getAllIdsForNodes(blocksContent1);

        console.log("ids1: ", ids1);
        
        const removedIds = new Set();

        // process second tree
        const targetMap = AI.Blockly.Diff.getTargetMap(blocksContent2);
        const ids2 = new Set(targetMap.keys());

        console.log("ids2: ", ids2);

        for (const id of ids1) {
            if (!ids2.has(id)) {
                removedIds.add(id);
            }
        }

        const [newIds, newBlocks] = AI.Blockly.Diff.getNewBlocks(targetMap, ids1);

        const movedIds = AI.Blockly.Diff.getUpdatedBlocks(blocksContent1, targetMap, removedIds)

        return {
            newIds: newBlocks,
            removedIds: removedIds,
            movedIds: movedIds,
        }; 
    }

    static getNewBlocks(targetMap, oldIDs) {
        const newIDs = new Set();
        const newBlocks = [];
        // TODO: change to BFS to get the correct order of blocks for insertion
        for (const [blockID, blockInfo] of targetMap.entries()) {
            if (!oldIDs.has(blockID)) {
                console.log("New block found: ", blockID);
                const parentID = blockInfo.parentID;
                if (newIDs.has(parentID)) {
                    console.warn("Parent block is also new, skipping for now: ", blockID, parentID);
                    newIDs.add(blockID);
                    continue;
                }
                const parent = targetMap.get(parentID)?.block;
                // TODO: is there a better way to this info?
                const nextBlock = parent?.getNextBlock();
                let inputName = null;
                if (!nextBlock) {
                    inputName = parent?.getInputWithBlock(blockInfo.block)?.name;
                }
                newIDs.add(blockID);
                newBlocks.push({
                    id: blockID,
                    block: blockInfo.block,
                    newParentId: parentID,
                    isNextBlock: nextBlock, // if the block is connected to the parent as a next block
                    inputName: inputName,
                });
            }
        }
        console.log("newIDs: ", newBlocks);
        return [newIDs, newBlocks];
    }

    static getUpdatedBlocks(tree1, targetMap, deletedNodes) {
        const queue = []; // Use an array as a queue with parentIDs (FIFO)
        const visited = new Set([...tree1.map(node => node.id)]); // Track visited nodes to prevent cycles
        const movedNodes = [];
        for (const node of tree1) {
            queue.push([node, "root"]); // Start with the root node
        }

        while (queue.length > 0) {
            const [node, parentID] = queue.shift(); // Dequeue the first node (FIFO)
            console.log("Processing node: ", node.id, " with parentID: ", parentID);
            const next = node.getNextBlock();
            // Iterate over children
            for (const child of (node.getChildren() || [])) {
                if (!visited.has(child.id)) {
                    let childParentID = null;
                    if ((next && next.id === child.id) || deletedNodes.has(node.id)) {
                        childParentID = parentID; // set the parent of the node as parent of child
                    } else {
                        childParentID = node.id; // Set the current node as the parent
                    }
                    visited.add(child.id);
                    queue.push([child, childParentID]); // Enqueue the unvisited child
                    // Check if the child node has been moved
                    if (!deletedNodes.has(child.id)) {
                        const targetBlockInfo = targetMap.get(child.id);
                        const targetParentID = targetBlockInfo?.parentID;
                        const targetParent = targetMap.get(targetParentID)?.block;
                        const targetBlock = targetBlockInfo?.block;
                        if (childParentID !== targetParentID) {
                            movedNodes.push({
                                id: child.id,
                                block: child,
                                oldParentId: childParentID,
                                newParentId: targetParentID,
                                isNextBlock: targetParent?.getNextBlock()?.id === targetBlock?.id, // if the block is connected to the new parent as a next block
                                inputName: targetParent?.getInputWithBlock(targetBlock)?.name,
                            });
                        }
                    }
                }
            }
        }
        return movedNodes;
    }

//     traverse(Node node) {
    //     if (node==NULL)
    //         return;

    //     stack<Node> stk;
    //     stk.push(node);

    //     while (!stk.empty()) {
    //         Node top = stk.pop();
    //         for (Node child in top.getChildren()) {
    //             stk.push(child);
    //         }
    //         process(top);
    //     }
    // }

    static getTargetMap(nodes) {
        const queue = [...nodes]; // Use an array as a queue (FIFO)
        const visited = new Set([...nodes.map(node => node.id)]); // Track visited nodes to prevent cycles
        const parents = new Map(); // Store parent of each node
        for (const node of nodes) {
            parents.set(node.id, {parentID: "root", block: node}); // Source node has no parent
        }

        while (queue.length > 0) {
            const node = queue.shift(); // Dequeue the first node (FIFO)
            const next = node.getNextBlock();
            // Iterate over children
            for (const child of (node.getChildren() || [])) {
                if (!visited.has(child.id)) {
                    if (next && next.id === child.id) {
                        parents.set(child.id, {parentID: parents.get(node.id), block: child}); // set the parent of the node as parent of child
                    } else {
                        parents.set(child.id, {parentID: node.id, block: child}); // Set the current node as the parent
                    }
                    visited.add(child.id);
                    queue.push(child); // Enqueue the unvisited child
                }
            }
        }

        console.log("parents: ", parents);
        return parents;
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
        const ids = new Set();
        for (const node of nodes) {
            console.log("another node");
            ids.add(...AI.Blockly.Diff.getAllIdsForNode(node, ids));
        }
        return ids;
    }

    static getAllIdsForNode(node, ids = new Set()) {
        if (node && node.id) {
            ids.add(node.id);
        }
        const children = node.getChildren();
        console.log('Children of node', node.id, ':', children.map(c => c.id));
        children.forEach(child => AI.Blockly.Diff.getAllIdsForNode(child, ids));
        return ids;
    }

};

