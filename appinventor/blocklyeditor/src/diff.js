'use strict';

goog.provide('AI.Blockly.Diff');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('zhangShasha');

// import zs from 'zhang-shasha';
// import fs from 'fs';

AI.Blockly.Diff = class {

    // const v2 = await fetch('v2.xml').then(r => r.text());

    /**
     * Parses an XML string into a JS object, like xml2js
     * @param {string} xmlString
     * @return {<Object>}
     */
    static parseXmlString(xmlString) {
        try {
            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(xmlString, 'text/xml');

            const errorNode = xmlDoc.getElementsByTagName('parsererror')[0];
            if (errorNode) {
                console.log('Error node:', errorNode.textContent);
                new Error('Invalid XML: ' + errorNode.textContent);
                return;
            }

            // Elements that MUST always be arrays per Blockly schema
            // TODO: double check this list
            const ALWAYS_ARRAY = new Set([
                'block',
                'shadow',
                'field',
                'value',
                'statement',
                'next',
                'comment',
                'category',
                'sep',
                'label',
                'button',
                'variable',
                'mutation',
                'variables',
            ]);

            function nodeToObject(node) {
                if (node.nodeType === Node.TEXT_NODE) {
                    const trimmed = node.textContent.trim();
                    return trimmed ? trimmed : null;
                }

                const obj = {};

                // attributes
                if (node.attributes && node.attributes.length > 0) {
                    obj['$'] = {};
                    for (let attr of node.attributes) {
                        obj['$'][attr.name] = attr.value;
                    }
                }

                // children
                for (let child of node.childNodes) {
                    const childObj = nodeToObject(child);
                    if (!childObj) continue;

                    const childName = child.nodeName;

                    // text-only simpleContent (field, comment, data, variable)
                    if (typeof childObj === 'string') {
                        obj._ = childObj;
                        continue;
                    }

                    // force array if schema allows multiple
                    if (ALWAYS_ARRAY.has(childName)) {
                        if (!obj[childName]) obj[childName] = [];
                        obj[childName].push(childObj);
                    } else {
                        obj[childName] = childObj;
                    }
                }

                return Object.keys(obj).length === 0 ? null : obj;
            }

            return nodeToObject(xmlDoc.documentElement);

        } catch (err) {
            console.log('Error parsing XML string:', err);
        }
    }

    static async diff(blocksContent1, blocksContent2) {
        const t1 = AI.Blockly.Diff.parseXmlString(blocksContent1);
        const t2 = AI.Blockly.Diff.parseXmlString(blocksContent2);
        console.log('T1:', t1);
        console.log('-------------------');
        console.log('T2:', t2);
        const mapping = zhangShasha.treeEditDistance(t1, t2, AI.Blockly.Diff.children, AI.Blockly.Diff.insertCost, AI.Blockly.Diff.removeCost, AI.Blockly.Diff.updateCost);
        console.log('Mapping:', mapping);
        return mapping;
        // const changelog = AI.Blockly.Diff.generateChangelog(mapping);
        // console.log(changelog.join('\n'));
    }

    static children(node) {
        const children = [];
        
        // Define which keys just contain more blocks/data
        const containerKeys = ['xml', 'category', 'sep', 'label', 'button', 'variables', 
                                'variable', 'block', 'shadow', 'value', 'statement', 'next', 
                                'comment', 'data'];

        containerKeys.forEach(key => {
            if (node && node[key] && Array.isArray(node[key])) {
                children.push(...node[key]);
            } else if (node && node[key]) {
                children.push(node[key]);
            }
        });

        return children;
    }

    static insertCost () { return 1 }
    static removeCost () { return 1 }

    static countDiffs (obj1, obj2) {
        let diffs = 0;

        if (obj1 === obj2) return diffs;
        if (typeof obj1 !== 'object' || obj1 === null || typeof obj2 !== 'object' || obj2 === null) return 1;

        const keys1 = Object.keys(obj1).filter(k => !['xmlns', 'x', 'y'].includes(k));
        const keys2 = Object.keys(obj2).filter(k => !['xmlns', 'x', 'y'].includes(k));

        const allKeys = new Set([...keys1, ...keys2]);

        allKeys.forEach(key => {
            if (!keys1.includes(key) || !keys2.includes(key)) {
                diffs += 1; // Key exists in one but not the other
            } else {
                diffs += AI.Blockly.Diff.countDiffs(obj1[key], obj2[key]);
            }
        });

        return 2*diffs/allKeys.size; // Normalize by number of keys
    }


    static updateCost(from, to) {
        // $ is some metadata in xml2js
        const a = from.$ || {};
        const b = to.$ || {};
        var diffs = AI.Blockly.Diff.countDiffs(a, b);

        // 1. Identity Match: If IDs match, they are the same block (0 cost) but can have different parameters
        if (a.id && b.id && a.id === b.id) {
            if (from.mutation && to.mutation) {
                // Check if mutations differ
                const mutationDiffs = AI.Blockly.Diff.countDiffs(from.mutation[0], to.mutation[0]);
                diffs += mutationDiffs;
            }
            const totalFields = from.field.length + to.field.length
            for (const field in from.field) {
                const toField = to.field.find(f => f.$.name === from.field[field].$.name);
                if (toField) {
                    diffs += (from.field[field]._ === toField._ ? 0 : 1)/totalFields;
                } else {
                    diffs += 1; // Field removed
                }
            }

            if (from.comment || to.comment) {
                const fromComment = from.comment ? from.comment[0]._ : '';
                const toComment = to.comment ? to.comment[0]._ : '';
                diffs += fromComment === toComment ? 0 : 0.1;
            }
            return  diffs; 
        }

        return diffs;
    }

    static INSERT = 'insert';
    static REMOVE = 'remove';
    static UPDATE = 'update';

    /** CHANGE = {TYPE: , BLOCKID: , MUTATION_CHANGES: , FIELD_CHANGES: , COMMENT CHANGE: } */

    static generateMutationChanges(mutation1, mutation2) {
        const mutationChanges = [];
        if (!mutation1 && !mutation2) {
            return mutationChanges;
        } else if (!mutation1) {
            // All mutation attributes are added
            const m2 = mutation2[0].$ || {};
            for (const key in m2) {
                if (['xmlns', 'x', 'y'].includes(key)) continue;
                mutationChanges.push({
                    type: AI.Blockly.Diff.INSERT,
                    key: key,
                    from: null,
                    to: m2[key],
                });
            }
            return mutationChanges;
        } else if (!mutation2) {
            // All mutation attributes are removed
            const m1 = mutation1[0].$ || {};
            for (const key in m1) {
                if (['xmlns', 'x', 'y'].includes(key)) continue;
                mutationChanges.push({
                    type: AI.Blockly.Diff.REMOVE,
                    key: key,
                    from: m1[key],
                    to: null,
                });
            }
            return mutationChanges;
        }
        const m1 = mutation1[0].$ || {};
        const m2 = mutation2[0].$ || {};
        const keys1 = Object.keys(m1).filter(k => !['xmlns', 'x', 'y'].includes(k));
        const keys2 = Object.keys(m2).filter(k => !['xmlns', 'x', 'y'].includes(k));

        const allKeys = new Set([...keys1, ...keys2]);

        allKeys.forEach(key => {
            if (!keys1.includes(key) || !keys2.includes(key)) {
                mutationChanges.push({
                    type: !keys1.includes(key) ? AI.Blockly.Diff.INSERT : AI.Blockly.Diff.REMOVE,
                    key: key,
                    from: m1[key],
                    to: m2[key],
                });
            } else if (m1[key] !== m2[key]) {
                mutationChanges.push({
                    type: AI.Blockly.Diff.UPDATE,
                    key: key,
                    from: m1[key],
                    to: m2[key],
                });
            }
        });
        return mutationChanges;
    }

    static generateFieldChanges(field1, field2) {
        const fieldChanges = [];
        if (!field1 && !field2) {
            return fieldChanges;
        } else if (!field1) {
            // All fields are added
            for (const field of field2) {
                fieldChanges.push({
                    type: AI.Blockly.Diff.INSERT,
                    fieldName: field.$.name,
                    from: null,
                    to: field._,
                });
            }
            return fieldChanges;
        } else if (!field2) {
            // All fields are removed
            for (const field of field1) {
                fieldChanges.push({
                    type: AI.Blockly.Diff.REMOVE,
                    fieldName: field.$.name,
                    from: field._,
                    to: null,
                });
            }
            return fieldChanges;
        }
        for (const field of field1) {
            const toField = field2.find(f => f.$.name === field.$.name);
            if (toField) {
                if (field._ !== toField._) {
                    fieldChanges.push({
                        type: AI.Blockly.Diff.UPDATE,
                        fieldName: field.$.name,
                        from: field._,
                        to: toField._,
                    });
                }
            } else {
                fieldChanges.push({
                    type: AI.Blockly.Diff.REMOVE,
                    fieldName: field.$.name,
                    from: field._,
                    to: null,
                });
            }
        }
        for (const field of field2) {
            const fromField = field1.find(f => f.$.name === field.$.name);
            if (!fromField) {
                fieldChanges.push({
                    type: AI.Blockly.Diff.INSERT,
                    fieldName: field.$.name,
                    from: null,
                    to: field._,
                });
            }
        }
        return fieldChanges;
    }

    static generateCommentChange(comment1, comment2) {
        var commentChange = null;
        if (comment1 && !comment2) {
            commentChange = {
                type: AI.Blockly.Diff.REMOVE,
                from: comment1[0]._,
                to: null,
            };
        } else if (!comment1 && comment2) {
            commentChange = {
                type: AI.Blockly.Diff.INSERT,
                from: null,
                to: comment2[0]._,
            };
        } else if (comment1 && comment2) {
            const fromComment = comment1[0]._ || '';
            const toComment = comment2[0]._ || '';
            if (fromComment !== toComment) {
                commentChange = {
                    type: AI.Blockly.Diff.UPDATE,
                    from: fromComment,
                    to: toComment,
                };
            }
        }
        return commentChange;
    }
    /**
     * Generate change steps from mapping to be displayed in the UI
     * @param {*} mapping 
     * @returns 
     */

    static generateChangeSteps(mapping) {
        const changes = [];

        const currentParent = null;
        
        mapping.forEach(op => {
            const t1 = op.t1;
            const t2 = op.t2;
            const type = op.type;

            if (type === "match") {
                currentParent = t2;
            } // No change
            else if (type === "insert") {
                // mapping[].find(op => op.type === 'match' && op.t1) ;
                changes.push({
                    action: AI.Blockly.Diff.INSERT,
                    insertXml: t2,  
                    insertAt: currentParent,
                });
            } else if (type === "remove") {
                changes.push({
                    action: AI.Blockly.Diff.REMOVE,
                    blockId: t1.$.id,
                });
            } else if (type === "update") {
                currentParent = t2;
                const changeDetail = {
                    action: AI.Blockly.Diff.UPDATE,
                    blockId: t1.$.id,
                    mutationChanges: [],
                    fieldChanges: [],
                    commentChange: null,
                };
                // Check mutation changes
                changeDetail.mutationChanges = AI.Blockly.Diff.generateMutationChanges(t1.mutation, t2.mutation);

                // Check field changes
                changeDetail.fieldChanges = AI.Blockly.Diff.generateFieldChanges(t1.field, t2.field);

                // Check comment change
                changeDetail.commentChange = AI.Blockly.Diff.generateCommentChange(t1.comment, t2.comment);
                changes.push(changeDetail);
            }
        });
        return changes;
    }

    static generateChangelog(mapping) {
        const changes = [];

        mapping.forEach(op => {
            const t1 = op.t1;
            const t2 = op.t2;
            const type = op.type;

            if (type === 'remove') {
            console.log('Removed node details:', JSON.stringify(t1, null, 2));
            }

        // 1. Handle Blocks (Nodes with IDs)
        if ((t1 && t1.$ && t1.$.id) || (t2 && t2.$ && t2.$.id)) {
            const blockId = (t1?.$?.id || t2?.$?.id);
            const blockType = (t1?.$?.type || t2?.$?.type);

            if (type === 'remove') {
                changes.push(`[Deleted] Block ${blockType} (ID: ${blockId})`);
                console.log('Deleted block details:', JSON.stringify(t1, null, 2));
            } else if (type === 'insert') {
                changes.push(`[Added] Block ${blockType} (ID: ${blockId})`);
            } else if (type === 'update') {
                // If IDs match but it's an update, the internal structure changed
                changes.push(`[Modified] Block ${blockType} (ID: ${blockId})`);
                if (t1 && t2 && t1.id === t2.id) {
                    const m1 = t1.mutation ? t1.mutation[0]?.$ : null;
                    const m2 = t2.mutation ? t2.mutation[0]?.$ : null;

                    const keys1 = Object.keys(m1).filter(k => !['xmlns', 'x', 'y'].includes(k));
                    const keys2 = Object.keys(m2).filter(k => !['xmlns', 'x', 'y'].includes(k));

                    for (const key of keys1) {
                        if (!keys2.includes(key) || (m1[key] !== m2[key])){
                            changes.push(`   -> Change Mutation "${key}": from "${JSON.stringify(m1[key])}" to "${JSON.stringify(m2[key])}"`);
                        }
                    }

                    for (const field of t1.field) {
                        const toField = t2.field.find(f => f.$.name === field.$.name);
                        if (toField && field._ !== toField._) {
                            changes.push(`   -> Change Field "${field.$.name}": from "${field._}" to "${toField._}"`);
                        } else if (!toField) {
                            changes.push(`   -> Remove Field "${field.$.name}": "${field._}"`);
                        }
                    }

                    for (const field of t2.field) {
                        const fromField = t1.field.find(f => f.$.name === field.$.name);
                        if (!fromField) {
                            changes.push(`   -> Add Field "${field.$.name}": "${field._}"`);
                        }
                    }

                    if (t1.comment || t2.comment) {
                        const fromComment = t1.comment ? t1.comment[0]._ : '';
                        const toComment = t2.comment ? t2.comment[0]._ : '';
                        if (fromComment !== toComment) {
                            changes.push(`   -> Change Comment: from "${fromComment}" to "${toComment}"`);
                        }
                    }
                }
            }
            } 
        });

        return changes;
    }



};

