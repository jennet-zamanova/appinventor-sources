'use strict';

goog.provide('AI.Blockly.Tests.MoveDetection');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('AI.Blockly.Diff');

AI.Blockly.Tests.MoveDetection = class {
    static async runAllTests() {
        const tests = [

            { name: 'M: should return one move id', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock1 },
            { name: 'M: should return two move ids', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock2 },
            { name: 'M: move but all children are moved from somewhere else', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock3 },
            { name: 'M: move and all children are moved with it', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock4 },
            { name: 'M: move and some children are moved with it and some children are moved from somewhere else', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock5 },
            { name: 'M: move input child', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock6 },
            { name: 'M: move at root', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock7 },
            { name: 'M: move as next', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock8 },
            { name: 'M: move from between blocks', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock9 },
            { name: 'M: move from beginning of blocks', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock10 },
            { name: 'M: move from end of blocks', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock11 },
            { name: 'M: swap blocks', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock12 },
            { name: 'M: swap blocks ||', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock15 },
            { name: 'M: move block in a stack (but not full stack)', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock13 },
            { name: 'M: move to between blocks', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock14 },
            { name: 'M: move parent block but child stays in place', fn: AI.Blockly.Tests.MoveDetection.testMoveBlock16 }
        ];

        const results = await Promise.allSettled(
            tests.map(test => test.fn(test.name))
        );

        let passed = 0;
        let failed = 0;

        results.forEach((result, index) => {
            const testName = tests[index].name;
            if (result.status === 'fulfilled') {
            console.log(`✓ ${testName} passed`);
            passed++;
            } else {
            console.error(`✗ ${testName} failed:`, result.reason);
            failed++;
            }
        });

        console.warn(`\nResults: ${passed} passed, ${failed} failed`);
        
        return { passed, failed, results };
    }

    static async testMoveBlock1(name) {
        console.log(name);
        // 012
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // 022
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: []},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]}
            ]},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} move complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['x#ww8BrXT*5A{XM^8#Nq']), moved); 
    }

    static async testMoveBlock2(name) {
        console.log(name);
        // 015
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // 016
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: []},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} move complete:`, moved);

        assertEquals('moved ids length', 2, moved.size);   
        assertEqualSets('moved ids', new Set(['x#ww8BrXT*5A{XM^8#Nq', 'Wb{Er@h_7Z`abf+S?e{_']), moved); 
    }

    // TODO: order at root should not matter, need to verify that as well. 
    static async testMoveBlock3(name) {
        console.log(name);
        // 023
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'random_id', children: []},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: []}
            ]},
            {id: 'BUaFURbz8x;xH+$#SEBY', children: []}
        ]};
        // TODO: separate set of children for if different inputs?
        // 018
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
                    {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
            {id: 'random_id', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} move complete:`, moved);

        assertEquals('moved ids length', 4, moved.size);   
        assertEqualSets('moved ids', new Set(['YRHgY.0tOrfK-xwr!~S2', 'BUaFURbz8x;xH+$#SEBY', 'x#ww8BrXT*5A{XM^8#Nq', 'Wb{Er@h_7Z`abf+S?e{_']), moved); 
        
    }

    static async testMoveBlock4(name) {
        console.log(name);
        // test_018
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
                    {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // test_024
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: []},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
                    {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} move complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['YRHgY.0tOrfK-xwr!~S2']), moved); 
    }

    static async testMoveBlock5(name) {
        console.log(name);
        // 021
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // 018
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
                    {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_021_b1_click_b1_text_if_t1_hide_b1_focus.xml", "static/media/test_018_b1_click_if_b1_text_t1_hide_b1_focus.xml");
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['x#ww8BrXT*5A{XM^8#Nq']), moved); 
    }

    // move input child
    static async testMoveBlock6(name) {
        console.log(name);
        // 013
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
        ]};
        // 016
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: []},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['x#ww8BrXT*5A{XM^8#Nq']), moved); 
    }

    // move at root
    static async testMoveBlock7(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
            {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},
                    {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                    {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
                ]}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['BUaFURbz8x;xH+$#SEBY']), moved); 
    }

    // move as next
    static async testMoveBlock8(name) {
        // 015
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // 013
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
        ]};
        console.log(name);
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['Wb{Er@h_7Z`abf+S?e{_']), moved); 
    }

    // move from between blocks
    static async testMoveBlock9(name) {
        console.log(name);
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
            {id: 'random_id', children: []},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
            ]},
            {id: 'random_id', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['YRHgY.0tOrfK-xwr!~S2']), moved);
    }

    // move from beginning of blocks
    static async testMoveBlock10(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
            {id: 'random_id', children: []},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                        id: 'A%L/*!(?[qhngbz7(ymy', children: []
                    }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
            ]},
            {id: 'random_id', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['YRHgY.0tOrfK-xwr!~S2']), moved);
    }

    // move from end of blocks
    static async testMoveBlock11(name) {
        // 015'
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        // 013'
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
            ]},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['Wb{Er@h_7Z`abf+S?e{_']), moved);
    }

    // swap blocks
    static async testMoveBlock12(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['Wb{Er@h_7Z`abf+S?e{_']), moved);
    }

    // swap blocks
    static async testMoveBlock15(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: []},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: []},
                
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['YRHgY.0tOrfK-xwr!~S2']), moved);
    }

    // move block in a stack (but not full stack)
    static async testMoveBlock13(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: []},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: []},
            ]},
        ]};

        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 2, moved.size);   
        assertEqualSets('moved ids', new Set(['x#ww8BrXT*5A{XM^8#Nq', 'YRHgY.0tOrfK-xwr!~S2']), moved);
    }

    // move to between blocks
    static async testMoveBlock14(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
            ]},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: []},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 1, moved.size);   
        assertEqualSets('moved ids', new Set(['YRHgY.0tOrfK-xwr!~S2']), moved);
    }

    // move parent block but child stays in place
    static async testMoveBlock16(name) {
        const b1 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: [{
                    id: 'A%L/*!(?[qhngbz7(ymy', children: []
                }]},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
            ]},
        ]};
        const b2 = {id: 'root', children: [
            {id: 'S1DtWUK}krc|(xEc7{Ye', children: [
                {id: 'A%L/*!(?[qhngbz7(ymy', children: []},
                {id: 'Wb{Er@h_7Z`abf+S?e{_', children: []}
            ]},
            {id: 'HYW/b5Ae79{HFc.u^M_1', children: [
                {id: 'YRHgY.0tOrfK-xwr!~S2', children: [
                    {id: 'BUaFURbz8x;xH+$#SEBY', children: []},   
                ]},
                {id: 'x#ww8BrXT*5A{XM^8#Nq', children: []},
            ]},
        ]};
        const moved = await AI.Blockly.Diff.movedIds(b1, b2);
        console.log(`${name} diff complete:`, moved);

        assertEquals('moved ids length', 2, moved.size);   
        assertEqualSets('moved ids', new Set(['x#ww8BrXT*5A{XM^8#Nq', 'A%L/*!(?[qhngbz7(ymy']), moved);
    }
};
