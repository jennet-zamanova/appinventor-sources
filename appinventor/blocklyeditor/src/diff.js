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
        console.log("tree2: ", tree2);
        const movedIds = AI.Blockly.Diff.getMoveIds(tree1, tree2);

        const unchangedIds = new Set();
        for (const id of ids1) {
            if (!movedIds.has(id) && !removedIds.has(id)) {
                unchangedIds.add(id);
            }
        }

        const moveInfo = AI.Blockly.Diff.getInsertionOrMoveInfo(movedIds, blocksContent2);

        return {
            unchangedIds: unchangedIds,
            newIds: newIds,
            movedIds: movedIds,
            newIdsInfo: insertInfo,
            removedIds: removedIds,
            movedIdsInfo: moveInfo,
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
                if (!idsToIgnore.has(cur.id)) {
                    temp.push([cur.id, parentid]);
                }
                const next = cur.getNextBlock();
                if (next && next.id) {
                    main_queue.unshift([next, parentid]);
                    n += 1;
                }
                for (let u of cur.getChildren()) {
                    if ((!next || next?.id !== u.id) && idsToIgnore.has(cur.id)) {
                        main_queue.unshift([u, parentid]);
                        n += 1;
                    } else if ((!next || next?.id !== u.id)) {
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

    static getMoveIds(t1, t2) {
        const movedIds = new Set();
        for (let i = 0; i < t1.length; i++) {
            const nodes1 = t1[i];
            if (i >= t2.length) {
                console.log("moving ids from t1 all layers");
                // rest of the nodes in t1 have been moved
                for (let k = 0; k < nodes1.length; k++) {
                    movedIds.add(nodes1[k][0]);
                }
            } else {
                const nodes2 = t2[i];
                // console.log("comparing layer ", i, " nodes1: ", nodes1, " nodes2: ", nodes2);
                for (let j = 0; j < nodes1.length; j++) {
                    if (j >= nodes2.length) {
                        // rest of the nodes in that layer have been moved
                        console.log("moving ids from t1 ");
                        for (let k = j; k < nodes1.length; k++) {
                            movedIds.add(nodes1[k][0]);
                        }
                    } else {
                        const itemOne = nodes1[j];
                        const itemTwo = nodes2[j];
                        // console.log("comparing node ", itemOne[0], " with ", itemTwo[0]);
                        if (itemOne[1] !== itemTwo[1] || itemOne[0] !== itemTwo[0]) {
                            console.log("moving ids distinct present in t1 and t2", itemOne, itemTwo);
                            // console.log("moved node: ", itemOne[0], " from parent: ", itemOne[1], " to parent: ", itemTwo[1]);
                            movedIds.add(itemOne[0]);
                        }
                    }
                }
                for (let j = nodes1.length; j < nodes2.length; j++) {
                    console.log("moving ids from t2");
                    movedIds.add(nodes2[j][0]);
                }
            }
        } 
        if (t1.length < t2.length) {
            // rest of the nodes in t2 have been moved
            for (let i = t1.length; i < t2.length; i++) {
                const nodes2 = t2[i];
                console.log("moving ids from t2 all layers");
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

        console.log("blocks to check for new/moved ids: ", blocks);

        for (const block of blocks) {
            if (ids.has(block.id)) {
                let dom = Blockly.Xml.blockToDom(block);

                // Find all <next> tags
                let nextTags = dom.getElementsByTagName('next');

                // Remove the first <next> tag found
                if (nextTags.length > 0) {
                    nextTags[0].parentNode.removeChild(nextTags[0]);
                }

                insertionInfo.push({
                    id: block.id,
                    block: dom,
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
                // if next remove node, ignore that parent is there already
                // and remove from the child any next connection, so that it does not get added as well
                console.log("checking child ", child.id, child, " with parent ", node.id);
                if (ids.has(child.id)) {
                    // if the block is connected to the parent as a next block, remove the next connection for insertion
                    // child.getNextBlock()?.dispose();
                    let dom = Blockly.Xml.blockToDom(child);
                    console.log("dom for child ", child.id, " is ", dom);

                    // Find all <next> tags
                    let nextTags = dom.getElementsByTagName('next');

                    // Remove the first <next> tag found (or loop through to remove all)
                    if (nextTags.length > 0) {
                        console.log("before removing tag: ", dom);
                        nextTags[0].parentNode.removeChild(nextTags[0]);
                        console.log("after removing tag: ", dom);
                    }
                    
                    const nextBlock = node?.getNextBlock();
                    console.log("checking child ", child.id, " with parent ", node.id, " next block: ", nextBlock?.id);
                    if (nextBlock && nextBlock.id === child.id) {
                        console.log("child ", child.id, " is connected to parent ", node.id, " as a next block, removing next connection for insertion");
                        // if the block is connected to the parent as a next block, remove the next connection for insertion
                        insertionInfo.push({
                            id: child.id,
                            block: dom,
                            newParentId: node.id,
                            isNextBlock: nextBlock, // if the block is connected to the parent as a next block
                            inputName: null,
                        });
                    } else if (!ids.has(node.id)) {
                        // do we need this check at all?
                        // if child moved with parent it wont be in this list
                        let inputName = node?.getInputWithBlock(child)?.name;
                        insertionInfo.push({
                            id: child.id,
                            block: dom,
                            newParentId: node.id,
                            isNextBlock: false, // if the block is connected to the parent as a next block would have been in previous if condition
                            inputName: inputName,
                        });
                    }
               }

               queue.push(child); // Enqueue the child node
            }
        }
        return insertionInfo;
    }

};

