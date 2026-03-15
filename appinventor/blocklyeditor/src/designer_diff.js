'use strict';

goog.provide('AI.Blockly.DesignerDiff');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('zhangShasha');

// TODO when getting children need right order
// if moved to an empty spot wont recognize (in mutations)
AI.Blockly.DesignerDiff = class {

    static diff(designer1, designer2) {

        console.log("comps: ", designer1, designer2);
        const ids1 = AI.Blockly.DesignerDiff.getAllIdsForComponent(designer1);
        const ids2 = AI.Blockly.DesignerDiff.getAllIdsForComponent(designer2);
        
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

        const insertInfo = AI.Blockly.DesignerDiff.getInsertionOrMoveInfo(newIds, designer2);

        const tree1 = AI.Blockly.DesignerDiff.makeArray(designer1, removedIds);
        const tree2 = AI.Blockly.DesignerDiff.makeArray(designer2, newIds);
        const movedIds = AI.Blockly.DesignerDiff.movedIds(tree1, tree2);

        const moveInfo = AI.Blockly.DesignerDiff.getInsertionOrMoveInfo(movedIds, designer2);

        const props1 = AI.Blockly.DesignerDiff.getPropsMapping(designer1, removedIds);
        const props2 = AI.Blockly.DesignerDiff.getPropsMapping(designer2, newIds);
        const [updateIds, updateInfo] = AI.Blockly.DesignerDiff.getUpdateInfo(props1, props2);

        const unchangedIds = new Set();
        for (const id of ids1) {
            if (!movedIds.has(id) && !removedIds.has(id) && !updateIds.has(id)) {
                unchangedIds.add(id);
            }
        }

        return {
            unchangedIds: unchangedIds,
            newIds: newIds,
            movedIds: movedIds,
            newIdsInfo: insertInfo,
            removedIds: removedIds,
            movedIdsInfo: moveInfo,
            updateIds: updateIds,
            updateInfo: updateInfo
        }; 
    }

    static getAllIdsForComponent(node, ids = new Set()) {
        if (node && node["Uuid"]) {
            ids.add(node["Uuid"]);
        }
        const children = node["$Components"] || [];
        // console.log('Children of node', node.id, ':', children.map(c => c["Uuid"]));
        children.forEach(child => AI.Blockly.DesignerDiff.getAllIdsForComponent(child, ids));
        return ids;
    }

    // TODO: update to be simpler
    static makeArray(root, idsToIgnore = new Set()) {
        if (!root)
            console.log("N-Ary tree does not any nodes");

        const nodeMap = new Map();  // id -> { id, children }
        const childParent = new Map(); // childId -> parentId

        // Create a queue namely main_queue
        let main_queue=[];

        // Push the root value in the main_queue
        main_queue.push([root, "root"]);

        // Run a while loop until the main_queue is empty
        while (main_queue.length) {

            // Get the front of the main_queue
            let n = main_queue.length;

            // Iterate through the current level
            while (n) {
                let [cur, parentId] = main_queue.shift();
                if (!idsToIgnore.has(cur.Uuid)) {
                    if (!nodeMap.has(cur.Uuid)) {
                        nodeMap.set(cur.Uuid, { id: cur.Uuid, children: [] });
                    }
                    if (parentId !== "root") {
                        childParent.set(cur.Uuid, parentId);
                    }
                }
                if (idsToIgnore.has(cur.Uuid)) {
                    for (let u of cur["$Components"]?.toReversed() || []) {
                        main_queue.unshift([u, parentId]);
                        n += 1;
                    }
                } else {
                    for (let u of cur["$Components"] || []) {
                        main_queue.push([u, cur.Uuid]);
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
        return {id: "root", children: treeRoots};
    }

// ---- Tree helpers ----

    static buildMap(node, parent = null, map = new Map()) {
        map.set(node.id, { node, parentId: parent });
        for (const child of node?.children || []) {
            AI.Blockly.DesignerDiff.buildMap(child, node.id, map);
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
        const map1 = AI.Blockly.DesignerDiff.buildMap(root1);

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
                const stablePositions = AI.Blockly.DesignerDiff.mwisStableIndices(seq, weights); // get max weight sum increasing order
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
        const map2 = AI.Blockly.DesignerDiff.buildMap(root2);
        return new Set([...moved].filter(id => {
            let t1ParentId = map1.get(id).parentId;
            const t2ParentId = map2.get(id).parentId;
            if (t1ParentId === t2ParentId) return true; // parents are same but in moved
            if (moved.has(t1ParentId)) {
                // original parent moved somewhere and child moved to a different parent need to check whether its due to parent moving
                while (t1ParentId) {
                    // found closest original parent that did not move and its same as new parent
                    if (!moved.has(t1ParentId) && t1ParentId === t2ParentId) return false;
                    // found closest original parent that did not move and its different from new parent
                    else if (!moved.has(t1ParentId)) return true;
                    t1ParentId = map1.get(t1ParentId)?.parentId;
                }
                // all parents moved
                return true;
            } 
            return true; // parent is different but original parent did not move 
        }));
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

    static getInsertionOrMoveInfo(ids, rootComponent) {
        const insertionInfo = [];
        const queue = [rootComponent]; // Use an array as a queue (FIFO)

        if (ids.has(rootComponent.Uuid)) {

            insertionInfo.push({
                id: block.Uuid,
                component: rootComponent,
                newParentId: null,
            });
        }

        // for inserts do not need to separate the xml, just insert the whole thing
        // because even if did not happen, stuff moved inside, so must be the case that
        // that stuff inside should be green anyway, so no need to separate the xml for moves, just insert the whole thing as well
        // actually no, because then the moved block will also be inserted.🤦🏻‍♀️😭

        while (queue.length > 0) {
            const node = queue.shift(); // Dequeue the first node (FIFO)
            // Enqueue the children of the current node
            for (const child of (node["$Components"] || [])) {
                // if next remove node, ignore that parent is there already
                // and remove from the child any next connection, so that it does not get added as well
                if (ids.has(child.Uuid)) {
                                        
                    if (!ids.has(node.Uuid)) {
                        insertionInfo.push({
                            id: child.Uuid,
                            block: child,
                            newParentId: node.Uuid,
                        });
                    }
               }

               queue.push(child); // Enqueue the child node
            }
        }
        return insertionInfo;
    }

    static getPropsMapping(root, idsToIgnore = new Set()) {
        // Create a queue namely main_queue
        let main_queue=[];
        let map = new Map();

        // Push the root value in the main_queue
        main_queue.push(root);

        // Run a while loop until the main_queue is empty
        while (main_queue.length) {

            let cur = main_queue.shift();
            if (!idsToIgnore.has(cur.Uuid)) {
                const { ["$Components"]: deletedKey, ...newProps } = cur;
                map.set(cur.Uuid, newProps);
            }
            for (let u of cur["$Components"] || []) {
                main_queue.push(u);
            }
        }
        return map;
    }

    static getUpdateInfo(props1, props2) {
        let updateIds = new Set();
        let updateInfo = new Map();
        
        for (const [id, info1] of props1) {
            const info2 = props2.get(id);
            // attributes in props1
            const attrs1 = Object.entries(info1);
            for (const [key, value] of attrs1) {
                if (value !== info2[key]) {
                    if (updateInfo.has(id)) {
                        updateInfo.get(id).push({
                            attribute: key,
                            from: value,
                            to: info2[key]
                        })
                    } else {
                        updateIds.add(id);
                        updateInfo.set(id, [{
                            attribute: key,
                            from: value,
                            to: info2[key]
                        }]);
                    }
                }
            }
            // attributes in props2 that are not in props1
            const attrs2 = Object.entries(info2);
            for (const [key, value] of attrs2) {
                if (!info1[key]) {
                    if (updateInfo.has(id)) {
                        updateInfo.get(id).push({
                            attribute: key,
                            from: null,
                            to: value
                        })
                    } else {
                        updateIds.add(id);
                        updateInfo.set(id, [{
                            attribute: key,
                            from: null,
                            to: value
                        }]);
                    }
                }
            }
        }
        return [updateIds, updateInfo];
    }

};

