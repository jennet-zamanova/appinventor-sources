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

        const tree1 = {id: "root", children: AI.Blockly.Diff.makeArray(blocksContent1, removedIds)};
        console.log("tree1: ", tree1);
        const tree2 = {id: "root", children: AI.Blockly.Diff.makeArray(blocksContent2, newIds)};
        console.log("tree2: ", tree2);
        const movedIds = AI.Blockly.Diff.movedIds(tree1, tree2);

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

    static makeArray(roots, idsToIgnore = new Set()) {
        if (!roots)
            console.log("N-Ary tree does not any nodes");

        const nodeMap = new Map();  // id -> { id, children }
        const childParent = new Map(); // childId -> parentId

        // Create a queue namely main_queue
        let main_queue=[];

        // Push the root value in the main_queue
        for (const root of roots) {
            main_queue.push([root, "root"]);
        }

        // Run a while loop until the main_queue is empty
        while (main_queue.length) {

            // Get the front of the main_queue
            let n = main_queue.length;

            // Iterate through the current level
            while (n) {
                let [cur, parentId] = main_queue.shift();
                if (!idsToIgnore.has(cur.id)) {
                    if (!nodeMap.has(cur.id)) {
                        nodeMap.set(cur.id, { id: cur.id, children: [] });
                    }
                    if (parentId !== "root") {
                        childParent.set(cur.id, parentId);
                    }
                }
                const next = cur.getNextBlock();
                if (next && next.id) {
                    main_queue.unshift([next, parentId]);
                    n += 1;
                }
                for (let u of cur.getChildren()) {
                    if ((!next || next?.id !== u.id) && idsToIgnore.has(cur.id)) {
                        main_queue.unshift([u, parentId]);
                        n += 1;
                    } else if ((!next || next?.id !== u.id)) {
                        main_queue.push([u, cur.id]);
                    }
                }
                    
                n -= 1;
            }
        }
        // Wire up children in insertion order (BFS order = correct child order)
        for (const [childId, parentId] of childParent) {
            const parentNode = nodeMap.get(parentId);
            const childNode  = nodeMap.get(childId);
            if (parentNode && childNode) {
                parentNode.children.push(childNode);
            }
        }

        // Return forest roots (nodes with no parent)
        const treeRoots = [...nodeMap.values()].filter(n => !childParent.has(n.id));
        return treeRoots.length === 1 ? treeRoots[0] : treeRoots;
    }

// ---- Tree helpers ----

    static buildMap(node, parent = null, map = new Map()) {
        map.set(node.id, { node, parentId: parent });
        for (const child of node?.children || []) {
            AI.Blockly.Diff.buildMap(child, node.id, map);
        }
        return map;
    }

// get ids that are in increasing order of weights and form maximum sum of weights
// seq: indices in T1
// weights: weights of those in T2
// TODO: probably makes more sense to process left to right

    static mwisStableIndices(seq, weights) {
        // dp[i] = max weight of increasing subsequence ending at i
        const n = seq.length;
        const dp = [...weights]; // base: just the node itself
        const predecessor = new Array(n).fill(null);

        for (let i = 1; i < n; i++) {
            // for each id
            for (let j = 0; j < i; j++) {
            // go through the ids that are smaller
            // if t1 index is smaller and sum of weights till j + weight of i in T2 is bigger than sum of weights till i
            if (seq[j] < seq[i] && dp[j] + weights[i] > dp[i]) {
                // set sum of weights till i
                dp[i] = dp[j] + weights[i];
                // the id to the left of i is j
                predecessor[i] = j;
            }
            }
        }

        // Find end of best subsequence
        let bestEnd = 0;
        for (let i = 1; i < n; i++) {
            if (dp[i] > dp[bestEnd]) bestEnd = i;
        }

        // Reconstruct
        const stable = new Set();
        let idx = bestEnd;
        while (idx != null) {
            stable.add(idx);
            idx = predecessor[idx];
        }
        return stable;
    }

    // find ids that moved between r1 and r2 such that it is minimal
    static movedIds(root1, root2) {
        const moved = new Set();
        // node.id, { node, parentId }
        const map1 = AI.Blockly.Diff.buildMap(root1);

        // Post-order: process children before parents
        function process(node2) {
            for (const child of node2.children || []) process(child);

            if (!node2.children?.length) return;

            const entry1 = map1.get(node2.id); // node and parentId in T1
            const currentIds = entry1.node.children.map(c => c.id); // 1 layer of children ids in T1
            const currentPos = new Map(currentIds.map((id, i) => [id, i])); // 1 layer of children ids in T1 -> order in T1

            const alreadyHere = [];
            // 1 layer of children in T2
            for (const child2 of node2.children || []) {
                // if child also in T1 as child of node2.id
                if (currentPos.has(child2.id)) {
                    alreadyHere.push({
                        t1Index: currentPos.get(child2.id), // order in T1
                        id: child2.id,
                        weight: effectiveSize(child2),  // weight in T2 with children already resolved
                    });
                } else {
                    moved.add(child2.id); // if child not in T1 as child of node1.id different parent
                }
            }

            // at root order does not matter
            if (node2.id !== 'root') {
                const seq     = alreadyHere.map(x => x.t1Index); // order in T1
                const weights = alreadyHere.map(x => x.weight); // effective weight in T2
                const stablePositions = AI.Blockly.Diff.mwisStableIndices(seq, weights); // get max weight sum increasing order
                alreadyHere.forEach((x, i) => {
                    if (!stablePositions.has(i)) moved.add(x.id);
                });
            }
        }

        // Subtree size excluding already-moved descendants
        function effectiveSize(node2) {
            if (moved.has(node2.id)) return 0;
            let size = 1;
            for (const child of node2.children || []) size += effectiveSize(child);
            return size;
        }

        process(root2);
        return moved;
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
                    
                    const nextBlock = node?.getNextBlock();
                    
                    if (child.getNextBlock()) {    
                        // Find all <next> tags
                        Blockly.Xml.deleteNext(dom);
                    }
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

