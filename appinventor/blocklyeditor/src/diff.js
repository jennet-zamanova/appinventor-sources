'use strict';

goog.provide('AI.Blockly.Diff');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('zhangShasha');


AI.Blockly.Diff = class {

    static async diff(blocksContent1, blocksContent2, ids1, ids2) {
        
        const removedIds = new Set();
        for (const id of ids1) {
            if (!ids2.has(id)) {
                removedIds.add(id);
            }
        }

        const newIds = new Set();
        for (const id of ids2) {
            if (!ids1.has(id)) {
                newIds.add(id);
            }
        }

        const insertInfo = AI.Blockly.Diff.getInsertionOrMoveInfo(newIds, blocksContent2);

        const tree1 = AI.Blockly.Diff.levelOrder(blocksContent1, removedIds);
        console.log("tree1: ", tree1);
        const tree2 = AI.Blockly.Diff.levelOrder(blocksContent2, newIds);

        const movedIds = AI.Blockly.Diff.getMoveIds(tree1, tree2);

        console.log("removedIds: ", removedIds);
        console.log("newIds: ", newIds);
        console.log("movedIds: ", movedIds);

        const moveInfo = AI.Blockly.Diff.getInsertionOrMoveInfo(movedIds, blocksContent2);

        return {
            newIds: insertInfo,
            removedIds: removedIds,
            movedIds: moveInfo,
        }; 
    }

    static levelOrder(roots, idsToIgnore = new Set()) {
        let ans = [];
        if (!roots)
            console.log("N-Ary tree does not any nodes");

        // Create a queue namely main_queue
        let main_queue=[];

        // Push the root value in the main_queue
        for (const root of roots) {
            main_queue.push([root, "root"]);
        }

        // Create a temp vector to store the all the node values
        // present at a particular level
        let temp=[];

        // Run a while loop until the main_queue is empty
        while (main_queue.length) {

            // Get the front of the main_queue
            let n = main_queue.length;

            // Iterate through the current level
            while (n) {
                let [cur, parentid] = main_queue.shift();
                temp.push([cur.id, parentid]);
                const next = cur.getNextBlock();
                for (let u of cur.getChildren()) {
                    if (next && next.id === u.id || idsToIgnore.has(cur.id)) {
                        main_queue.unshift([u, parentid]);
                        n += 1;
                    } else {
                        main_queue.push([u, cur.id]);
                    }
                }
                    
                n -= 1;
            }
            ans.push(temp);
            temp=[];
        }
        return ans;
    }

    static updateMapAfterDeletionsOrInsertion(roots, deletedIds) {
        let ans = new Map();
        if (!roots)
            console.log("N-Ary tree does not any nodes");

        // Create a queue namely main_queue
        let main_queue=[];

        // Push the root value in the main_queue
        for (const root of roots) {
            main_queue.push([root, "root"]);
            ans.set(root.id, "root");
        }

        // Run a while loop until the main_queue is empty
        while (main_queue.length) {

            let [cur, parentid] = main_queue.shift();
            const next = cur.getNextBlock();
            for (let u of cur.getChildren()) {
                if (next && next.id === u.id || deletedIds.has(cur.id)) {
                    main_queue.unshift([u, parentid]);
                    ans.set(u.id, parentid);
                } else {
                    main_queue.push([u, cur.id]);
                    ans.set(u.id, cur.id);
                }
            }
        }
        return ans;
    }

    // static parentMap(roots) {
    //     let ans = new Map();
    //     if (!roots)
    //         console.log("N-Ary tree does not any nodes");

    //     // Create a queue namely main_queue
    //     let main_queue=[];

    //     // Push the root value in the main_queue
    //     for (const root of roots) {
    //         main_queue.push([root, "root"]);
    //         ans.set(root.id, "root");
    //     }

    //     // Run a while loop until the main_queue is empty
    //     while (main_queue.length) {

    //         let [cur, parentid] = main_queue.shift();
    //         const next = cur.getNextBlock();
    //         for (let u of cur.getChildren()) {
    //             if (next && next.id === u.id) {
    //                 main_queue.unshift([u, parentid]);
    //                 ans.set(u.id, parentid);
    //             } else {
    //                 main_queue.push([u, cur.id]);
    //                 ans.set(u.id, cur.id);
    //             }
    //         }
    //     }
    //     return ans;
    // }

    static getMoveIds(t1, t2) {
        const movedIds = new Set();
        for (let i = 0; i < t1.length; i++) {
            if (i >= t2.length) {
                // rest of the nodes in t1 have been moved
                for (let k = 0; k < nodes1.length; k++) {
                    movedIds.add(nodes1[k][0]);
                }
            } else {
                const nodes1 = t1[i];
                const nodes2 = t2[i];
                for (let j = 0; j < nodes1.length; j++) {
                    if (j >= nodes2.length) {
                        // rest of the nodes in that layer have been moved
                        for (let k = j; k < nodes1.length; k++) {
                            movedIds.add(nodes1[k][0]);
                        }
                    } else {
                        const itemOne = nodes1[j];
                        const itemTwo = nodes2[j];
                        if (itemOne[1] !== itemTwo[1]) {
                            console.log("moved node: ", itemOne[0], " from parent: ", itemOne[1], " to parent: ", itemTwo[1]);
                            movedIds.add(itemOne[0]);
                        }
                    }
                }
            }
        } 
        if (t1.length < t2.length) {
            // rest of the nodes in t2 have been moved
            for (let i = t1.length; i < t2.length; i++) {
                const nodes2 = t2[i];
                for (let j = 0; j < nodes2.length; j++) {
                    movedIds.add(nodes2[j][0]);
                }
            }
        }
        return movedIds;
    }

    static getInsertionOrMoveInfo(ids, blocks) {
        const insertionInfo = [];
        const queue = [...blocks]; // Use an array as a queue (FIFO)

        for (const block of blocks) {
            if (ids.has(block.id)) {
                insertionInfo.push({
                    id: block.id,
                    block: block,
                    newParentId: null,
                    isNextBlock: false,
                    inputName: null,
                });
            }
        }

        // how to get xml that does not have the next connection as child?
        // specifically, if there was a move
        // top moved, next stayed
        // here we will be add xml of the whole thing

        // but also need to recognize when it does move with the block
        // if moved with the block, the parent is different so will get as new id
        // so for moves need to separate the xml
        // for inserts do not need to separate the xml, just insert the whole thing
        // because even if did not happen, stuff moved inside, so must be the case that
        // that stuff inside should be green anyway, so no need to separate the xml for moves, just insert the whole thing as well
        // actually no, because then the moved block will also be inserted.🤦🏻‍♀️😭

        while (queue.length > 0) {
            const node = queue.shift(); // Dequeue the first node (FIFO)
            // Enqueue the children of the current node
            for (const child of (node.getChildren() || [])) {
               if (ids.has(child.id) && !ids.has(node.id)) {
                    const nextBlock = node?.getNextBlock();
                    let inputName = null;
                    if (!nextBlock) {
                        inputName = node?.getInputWithBlock(child)?.name;
                    }
                    insertionInfo.push({
                        id: child.id,
                        block: child,
                        newParentId: node.id,
                        isNextBlock: nextBlock, // if the block is connected to the parent as a next block
                        inputName: inputName,
                    });
               }
               queue.push(child); // Enqueue the child node
            }
        }
        return insertionInfo;
    }




//     static getListOfIds(nodes) {
//         const queue = []; // Use an array as a queue (FIFO)
//         const visited = new Set([...nodes.map(node => node.id)]); // Track visited nodes to prevent cycles
//         const tree = []; // Store the tree structure

//         const root = [];
//         for (const node of nodes) {
//             queue.push({node, parentID: "root"}); // Enqueue the root node
//             root.push({nodeID: node.id, parentID: "root"}); // Start with the root node
//         }
//         tree.push(root);

//         let level = 0;

//         while (queue.length > 0) {
//             level += 1;
//             const nodeInfo = queue.shift(); // Dequeue the first node (FIFO)
//             const next = nodeInfo.node.getNextBlock();
//             // Iterate over children
//             const nextLevel = [];
//             for (const child of (nodeInfo.node.getChildren() || [])) {
//                 if (!visited.has(child.id)) {
//                     if (next && next.id === child.id) {
//                         queue.unshift(child); // Enqueue the unvisited child at the front of the queue (DFS)
//                         // nodes parent is childs parent, should also same level as node
//                         root[level - 1].push({nodeID: child.id, parentID: nodeInfo.parentID}); // set the parent of the node as parent of child
//                         parents.set(child.id, {parentID: parents.get(node.id), block: child}); // set the parent of the node as parent of child
//                     } else {
//                         parents.set(child.id, {parentID: node.id, block: child}); // Set the current node as the parent
//                     }
//                     visited.add(child.id);
//                     queue.push(child); // Enqueue the unvisited child
//                 }
//             }
//         }

//         console.log("parents: ", parents);
//         return parents;
//     }

//     static getNewBlocks(targetMap, oldIDs) {
//         const newIDs = new Set();
//         const newBlocks = [];
//         // TODO: change to BFS to get the correct order of blocks for insertion
//         for (const [blockID, blockInfo] of targetMap.entries()) {
//             if (!oldIDs.has(blockID)) {
//                 console.log("New block found: ", blockID);
//                 const parentID = blockInfo.parentID;
//                 if (newIDs.has(parentID)) {
//                     console.warn("Parent block is also new, skipping for now: ", blockID, parentID);
//                     newIDs.add(blockID);
//                     continue;
//                 }
//                 const parent = targetMap.get(parentID)?.block;
//                 // TODO: is there a better way to this info?
//                 const nextBlock = parent?.getNextBlock();
//                 let inputName = null;
//                 if (!nextBlock) {
//                     inputName = parent?.getInputWithBlock(blockInfo.block)?.name;
//                 }
//                 newIDs.add(blockID);
//                 newBlocks.push({
//                     id: blockID,
//                     block: blockInfo.block,
//                     newParentId: parentID,
//                     isNextBlock: nextBlock, // if the block is connected to the parent as a next block
//                     inputName: inputName,
//                 });
//             }
//         }
//         console.log("newIDs: ", newBlocks);
//         return [newIDs, newBlocks];
//     }

//     static getUpdatedBlocks(tree1, targetMap, deletedNodes) {
//         const queue = []; // Use an array as a queue with parentIDs (FIFO)
//         const visited = new Set([...tree1.map(node => node.id)]); // Track visited nodes to prevent cycles
//         const movedNodes = [];
//         for (const node of tree1) {
//             queue.push([node, "root"]); // Start with the root node
//         }

//         while (queue.length > 0) {
//             const [node, parentID] = queue.shift(); // Dequeue the first node (FIFO)
//             console.log("Processing node: ", node.id, " with parentID: ", parentID);
//             const next = node.getNextBlock();
//             // Iterate over children
//             for (const child of (node.getChildren() || [])) {
//                 if (!visited.has(child.id)) {
//                     let childParentID = null;
//                     if ((next && next.id === child.id) || deletedNodes.has(node.id)) {
//                         childParentID = parentID; // set the parent of the node as parent of child
//                     } else {
//                         childParentID = node.id; // Set the current node as the parent
//                     }
//                     visited.add(child.id);
//                     queue.push([child, childParentID]); // Enqueue the unvisited child
//                     // Check if the child node has been moved
//                     if (!deletedNodes.has(child.id)) {
//                         const targetBlockInfo = targetMap.get(child.id);
//                         const targetParentID = targetBlockInfo?.parentID;
//                         const targetParent = targetMap.get(targetParentID)?.block;
//                         const targetBlock = targetBlockInfo?.block;
//                         if (childParentID !== targetParentID) {
//                             movedNodes.push({
//                                 id: child.id,
//                                 block: child,
//                                 oldParentId: childParentID,
//                                 newParentId: targetParentID,
//                                 isNextBlock: targetParent?.getNextBlock()?.id === targetBlock?.id, // if the block is connected to the new parent as a next block
//                                 inputName: targetParent?.getInputWithBlock(targetBlock)?.name,
//                             });
//                         }
//                     }
//                 }
//             }
//         }
//         return movedNodes;
//     }

// //     traverse(Node node) {
//     //     if (node==NULL)
//     //         return;

//     //     stack<Node> stk;
//     //     stk.push(node);

//     //     while (!stk.empty()) {
//     //         Node top = stk.pop();
//     //         for (Node child in top.getChildren()) {
//     //             stk.push(child);
//     //         }
//     //         process(top);
//     //     }
//     // }

//     static getTargetMap(nodes) {
//         const queue = [...nodes]; // Use an array as a queue (FIFO)
//         const visited = new Set([...nodes.map(node => node.id)]); // Track visited nodes to prevent cycles
//         const parents = new Map(); // Store parent of each node
//         for (const node of nodes) {
//             parents.set(node.id, {parentID: "root", block: node}); // Source node has no parent
//         }

//         while (queue.length > 0) {
//             const node = queue.shift(); // Dequeue the first node (FIFO)
//             const next = node.getNextBlock();
//             // Iterate over children
//             for (const child of (node.getChildren() || [])) {
//                 if (!visited.has(child.id)) {
//                     if (next && next.id === child.id) {
//                         parents.set(child.id, {parentID: parents.get(node.id), block: child}); // set the parent of the node as parent of child
//                     } else {
//                         parents.set(child.id, {parentID: node.id, block: child}); // Set the current node as the parent
//                     }
//                     visited.add(child.id);
//                     queue.push(child); // Enqueue the unvisited child
//                 }
//             }
//         }

//         console.log("parents: ", parents);
//         return parents;
//     }

//     static getFlatIds(nodes) {
//         const ids = [];
//         for (const node of nodes) {
//             if (node && node.id) {
//                 ids.push(node.id);
//             }
//         }
//         return ids;
//     }

    // static getAllIdsForNodes(nodes) {
    //     console.log("nodes: ", nodes.length, nodes);
    //     const ids = new Set();
    //     for (const node of nodes) {
    //         console.log("another node");
    //         ids.add(...AI.Blockly.Diff.getAllIdsForNode(node, ids));
    //     }
    //     return ids;
    // }

    // static getAllIdsForNode(node, ids = new Set()) {
    //     if (node && node.id) {
    //         ids.add(node.id);
    //     }
    //     const children = node.getChildren();
    //     console.log('Children of node', node.id, ':', children.map(c => c.id));
    //     children.forEach(child => AI.Blockly.Diff.getAllIdsForNode(child, ids));
    //     return ids;
    // }

};

