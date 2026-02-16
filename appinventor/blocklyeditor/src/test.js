'use strict';

// const { assert } = require("chai");

goog.provide('AI.Blockly.Tests.Diff');

// App Inventor extensions to Blockly
goog.require('AI.Blockly.Util');
goog.require('AI.Blockly.Util.xml');
goog.require('AI.Blockly.Diff');

// goog.require('goog.testing.asserts');

async function logFile(file) {
  try {
    // const filePath = resolve(file);
    // const contents = await readFile(filePath, { encoding: 'utf8' });
    const contents = await fetch(file).then(r => r.text());
    return contents;
  } catch (err) {
    console.error(err.message);
  }
}

const assertEquals = (message, expected, actual) => {
  if (expected !== actual) {
    throw new Error(`${message}: expected ${expected}, got ${actual}`);
  }
};

const assertNull = (message, value) => {
  if (value !== null) {
    throw new Error(`${message}: expected null, got ${value}`);
  }
};

const assertNullOrFalse = (message, value) => {
  if (value !== null && value !== false) {
    throw new Error(`${message}: expected null or false, got ${value}`);
  }
};

const assertTrue = (message, condition) => {
  if (!condition) {
    throw new Error(`${message}: expected true`);
  }
};

const assertEqualSets = (message, expected, actual) => {
  if (expected.size !== actual.size) {
    throw new Error(`${message}: expected set of size ${expected.size}, got ${actual.size}`);
  }
  for (const item of expected) {
    if (!actual.has(item)) {
      throw new Error(`${message}: expected set to contain ${item}`);
    }
  }
};

const assertEqualDoms = (actual, expected) => {
    const expectedElement = Blockly.utils.xml.textToDom(expected);
    const actualElement = Blockly.Xml.blockToDom(actual);
    expectedElement.removeAttribute('x');
    expectedElement.removeAttribute('y');
    expectedElement.removeAttribute('xmlns');

    actualElement.removeAttribute('x');
    actualElement.removeAttribute('y');
    actualElement.removeAttribute('xmlns');

    const normalize = (el) => Blockly.Xml.domToText(el).replace(/>\s+</g, '><').trim();


    const expectedStr = normalize(expectedElement);
    const actualStr = normalize(actualElement);
    
    if (expectedStr !== actualStr) {
        console.error('Expected:', expectedStr);
        console.error('Actual:', actualStr);
        throw new Error('XML does not match');
    }
}

AI.Blockly.Tests.Diff = class {
    static async runAllTests() {
        const tests = [
            { name: 'should return one new id', fn: AI.Blockly.Tests.Diff.testNewBlock1 },
            { name: 'should return one new id 3', fn: AI.Blockly.Tests.Diff.testNewBlock2 },
            { name: 'insert but all children are moved (1 new)', fn: AI.Blockly.Tests.Diff.testNewBlock3 },
            { name: 'insert but all children are moved (not new)', fn: AI.Blockly.Tests.Diff.testNewBlock6 },
            { name: 'insert and all children are new', fn: AI.Blockly.Tests.Diff.testNewBlock4 },
            { name: 'insert and some children are new and some children are moved', fn: AI.Blockly.Tests.Diff.testNewBlock5 },
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

        console.log(`\nResults: ${passed} passed, ${failed} failed`);
        
        return { passed, failed, results };
    }

    static createInvisibleAiWorkspaceFrom(mainWorkspace) {
        const div = document.createElement('div');
        // div.style.display = 'none';
        document.body.appendChild(div);

        const ws = Blockly.inject(div, {
        readOnly: false,
        scrollbars: false,
        sounds: false,
        parentWorkspace: mainWorkspace, // ⭐ CRITICAL
        });

        // Copy AI-specific runtime state
        ws.componentDb_ = mainWorkspace.componentDb_;
        ws.getEventTypeObject = () => mainWorkspace.getEventTypeObject();
        ws.getProcedureDatabase = () => mainWorkspace.getProcedureDatabase();

        return ws;
    }

    static async setup(file1, file2) {
        const blocksText1 = await logFile(file1);
        const blocksText2 = await logFile(file2);
        // TODO: ask for a better way of doing this - we need a headless workspace to parse the blocks and generate the diff, 
        // but we also need the component database and other runtime state to properly interpret the blocks and generate a meaningful diff. 
        // Right now we're creating a hidden workspace attached to the main workspace just to reuse that state, but it feels hacky.
        const hiddenWs1 = AI.Blockly.Tests.Diff.createInvisibleAiWorkspaceFrom(Blockly.getMainWorkspace());
        const hiddenWs2 = AI.Blockly.Tests.Diff.createInvisibleAiWorkspaceFrom(Blockly.getMainWorkspace());
        const blocks1 = Blockly.utils.xml.textToDom(blocksText1);
        const blocks2 = Blockly.utils.xml.textToDom(blocksText2);
        Blockly.Xml.domToWorkspace(blocks1, hiddenWs1);
        Blockly.Xml.domToWorkspace(blocks2, hiddenWs2);

        const blocksContent1 = hiddenWs1.getTopBlocks();
        const blocksContent2 = hiddenWs2.getTopBlocks();

        return [blocksContent1, blocksContent2, new Set(hiddenWs1.blockDB.keys()), new Set(hiddenWs2.blockDB.keys())];
    }

    static async testNewBlock1(name) {
        console.log(name);
        const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_011_b1_click_b1_text.xml", "static/media/test_012_b1_click_b1_text_b1_focus.xml");
        const diff = await AI.Blockly.Diff.diff(b1, b2, i1, i2);

        assertEquals('new ids length', 1, diff.newIds.size);  
        assertEquals('new ids info length', 1, diff.newIdsInfo.length);  
        assertEquals('removed ids length', 0, diff.removedIds.size);  
        assertEquals('moved ids length', 0, diff.movedIds.size); 
        assertEqualSets('unchanged ids', new Set(['S1DtWUK}krc|(xEc7{Ye', 'x#ww8BrXT*5A{XM^8#Nq', 'A%L/*!(?[qhngbz7(ymy']), diff.unchangedIds); 
        
        assertEquals('new block id', 'HYW/b5Ae79{HFc.u^M_1', diff.newIdsInfo[0].id);
        assertNull('new parent id', diff.newIdsInfo[0].newParentId);
        assertNullOrFalse('is next block', diff.newIdsInfo[0].isNextBlock);
        assertNull('input name', diff.newIdsInfo[0].inputName);

        const expectedDOM = `<block xmlns="https://developers.google.com/blockly/xml" type="component_event" id="HYW/b5Ae79{HFc.u^M_1"> 
                                <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button" is_generic="false" 
                                    instance_name="Button1" event_name="GotFocus"></mutation> 
                                <field name="COMPONENT_SELECTOR">Button1</field> 
                            </block>`;
        assertEqualDoms(diff.newIdsInfo[0].block, expectedDOM);
    }

    static async testNewBlock2(name) {
        console.log(name);
        const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_01_blank.xml", "static/media/test_011_b1_click_b1_text.xml");
        const diff = await AI.Blockly.Diff.diff(b1, b2, i1, i2);

        assertEquals('new ids length', 3, diff.newIds.size);  
        assertEquals('new ids info length', 1, diff.newIdsInfo.length);  
        assertEquals('removed ids length', 0, diff.removedIds.size);  
        assertEquals('moved ids length', 0, diff.movedIds.size);  
        assertEquals('unchanged ids length', 0, diff.unchangedIds.size);

        assertTrue('new block id 1', diff.newIds.has('S1DtWUK}krc|(xEc7{Ye'));
        assertTrue('new block id 2', diff.newIds.has('x#ww8BrXT*5A{XM^8#Nq'));
        assertTrue('new block id 3', diff.newIds.has('A%L/*!(?[qhngbz7(ymy'));
        assertEquals('new block id', 'S1DtWUK}krc|(xEc7{Ye', diff.newIdsInfo[0].id);
        assertNull('new parent id', diff.newIdsInfo[0].newParentId);
        assertNullOrFalse('is next block', diff.newIdsInfo[0].isNextBlock);
        assertNull('input name', diff.newIdsInfo[0].inputName);

        const expectedDOM = `<block xmlns="https://developers.google.com/blockly/xml" type="component_event" id="S1DtWUK}krc|(xEc7{Ye">
        <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button" is_generic="false"
            instance_name="Button1" event_name="Click"></mutation>
        <field name="COMPONENT_SELECTOR">Button1</field>
        <statement name="DO">
            <block type="component_set_get" id="x#ww8BrXT*5A{XM^8#Nq">
                <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button"
                    set_or_get="set" property_name="Text" is_generic="false" instance_name="Button1"></mutation>
                <field name="COMPONENT_SELECTOR">Button1</field>
                <field name="PROP">Text</field>
                <value name="VALUE">
                    <block type="text" id="A%L/*!(?[qhngbz7(ymy">
                        <field name="TEXT"></field>
                    </block>
                </value>
            </block>
        </statement>
    </block>`;
        assertEqualDoms(diff.newIdsInfo[0].block, expectedDOM);
    }

    static async testNewBlock3(name) {
        const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_015_b1_click_b1_text_t1_hide_b1_focus.xml", "static/media/test_019_b1_click_if_b1_text_skip_t1_hide_b1_focus.xml");
        const diff = await AI.Blockly.Diff.diff(b1, b2, i1, i2);

        console.log(`${name} diff complete:`, diff);

        assertEquals('new ids length', 2, diff.newIds.size);  
        assertEquals('new ids info length', 1, diff.newIdsInfo.length);  
        assertEquals('removed ids length', 0, diff.removedIds.size);  
        assertEquals('moved ids length', 0, diff.movedIds.size);
        assertEqualSets('unchanged ids', new Set(['S1DtWUK}krc|(xEc7{Ye', 'x#ww8BrXT*5A{XM^8#Nq', 'A%L/*!(?[qhngbz7(ymy', 'Wb{Er@h_7Z`abf+S?e{_', 'HYW/b5Ae79{HFc.u^M_1']), diff.unchangedIds); 

        assertTrue('new block id 1', diff.newIds.has('YRHgY.0tOrfK-xwr!~S2'));
        assertTrue('new block id 2', diff.newIds.has('BUaFURbz8x;xH+$#SEBY'));

        assertEquals('new block id', 'YRHgY.0tOrfK-xwr!~S2', diff.newIdsInfo[0].id);
        assertEquals('new parent id', 'S1DtWUK}krc|(xEc7{Ye', diff.newIdsInfo[0].newParentId);
        assertNullOrFalse('is next block', diff.newIdsInfo[0].isNextBlock);
        assertEquals('input name', 'DO', diff.newIdsInfo[0].inputName);

        // assertTrue('move block id 1', diff.movedIds.has('x#ww8BrXT*5A{XM^8#Nq'));
        // assertTrue('move block id 2', diff.movedIds.has('Wb{Er@h_7Z`abf+S?e{_'));

        const expectedDOM = '<block xmlns="https://developers.google.com/blockly/xml" type="controls_if" id="YRHgY.0tOrfK-xwr!~S2"> \
                <value name="IF0"> \
                    <block type="logic_boolean" id="BUaFURbz8x;xH+$#SEBY"> \
                        <field name="BOOL">TRUE</field> \
                    </block> \
                </value> \
                <statement name="DO0"> \
                    <block type="component_set_get" id="x#ww8BrXT*5A{XM^8#Nq"> \
                        <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button" \
                            set_or_get="set" property_name="Text" is_generic="false" \
                            instance_name="Button1"></mutation> \
                        <field name="COMPONENT_SELECTOR">Button1</field> \
                        <field name="PROP">Text</field> \
                        <value name="VALUE"> \
                            <block type="text" id="A%L/*!(?[qhngbz7(ymy"> \
                                <field name="TEXT"></field> \
                            </block> \
                        </value> \
                    </block> \
                </statement> \
                <next> \
                            <block type="component_method" id="Wb{Er@h_7Z`abf+S?e{_"> \
                                <mutation xmlns="http://www.w3.org/1999/xhtml" \
                                    component_type="TextBox" method_name="HideKeyboard" \
                                    is_generic="false" instance_name="TextBox1"></mutation> \
                                <field name="COMPONENT_SELECTOR">TextBox1</field> \
                            </block> \
                </next> \
            </block>';
        assertEqualDoms(diff.newIdsInfo[0].block, expectedDOM);
    }

    static async testNewBlock6(name) {
        const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_015_b1_click_b1_text_t1_hide_b1_focus.xml", "static/media/test_020_b1_click_if_none_b1_text_skip_t1_hide_b1_focus.xml");
        const diff = await AI.Blockly.Diff.diff(b1, b2, i1, i2);

        console.log(`${name} diff complete:`, diff);

        assertEquals('new ids length', 1, diff.newIds.size);  
        assertEquals('new ids info length', 1, diff.newIdsInfo.length);  
        assertEquals('removed ids length', 0, diff.removedIds.size);  
        assertEquals('moved ids length', 0, diff.movedIds.size);
        assertEqualSets('unchanged ids', new Set(['S1DtWUK}krc|(xEc7{Ye', 'x#ww8BrXT*5A{XM^8#Nq', 'A%L/*!(?[qhngbz7(ymy', 'Wb{Er@h_7Z`abf+S?e{_', 'HYW/b5Ae79{HFc.u^M_1']), diff.unchangedIds); 

        assertTrue('new block id 1', diff.newIds.has('YRHgY.0tOrfK-xwr!~S2'));

        assertEquals('new block id', 'YRHgY.0tOrfK-xwr!~S2', diff.newIdsInfo[0].id);
        assertEquals('new parent id', 'S1DtWUK}krc|(xEc7{Ye', diff.newIdsInfo[0].newParentId);
        assertNullOrFalse('is next block', diff.newIdsInfo[0].isNextBlock);
        assertEquals('input name', 'DO', diff.newIdsInfo[0].inputName);

        // assertTrue('move block id 1', diff.movedIds.has('x#ww8BrXT*5A{XM^8#Nq'));
        // assertTrue('move block id 2', diff.movedIds.has('Wb{Er@h_7Z`abf+S?e{_'));

        const expectedDOM = '<block xmlns="https://developers.google.com/blockly/xml" type="controls_if" id="YRHgY.0tOrfK-xwr!~S2"> \
                <statement name="DO0"> \
                    <block type="component_set_get" id="x#ww8BrXT*5A{XM^8#Nq"> \
                        <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button" \
                            set_or_get="set" property_name="Text" is_generic="false" \
                            instance_name="Button1"></mutation> \
                        <field name="COMPONENT_SELECTOR">Button1</field> \
                        <field name="PROP">Text</field> \
                        <value name="VALUE"> \
                            <block type="text" id="A%L/*!(?[qhngbz7(ymy"> \
                                <field name="TEXT"></field> \
                            </block> \
                        </value> \
                    </block> \
                </statement> \
                <next> \
                    <block type="component_method" id="Wb{Er@h_7Z`abf+S?e{_"> \
                        <mutation xmlns="http://www.w3.org/1999/xhtml" \
                            component_type="TextBox" method_name="HideKeyboard" \
                            is_generic="false" instance_name="TextBox1"></mutation> \
                        <field name="COMPONENT_SELECTOR">TextBox1</field> \
                    </block> \
                </next> \
            </block>';
        assertEqualDoms(diff.newIdsInfo[0].block, expectedDOM);
    }


    static async testNewBlock4(name) {
        const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_011_b1_click_b1_text.xml", "static/media/test_013_b1_click_b1_text_b1_focus_t1_hide.xml");
        const diff = await AI.Blockly.Diff.diff(b1, b2, i1, i2);
        console.log(`${name} diff complete:`, diff);
        assertEquals('new ids length', 2, diff.newIds.size);  
        assertEquals('new ids info length', 1, diff.newIdsInfo.length);  
        assertEquals('removed ids length', 0, diff.removedIds.size);  
        assertEquals('moved ids length', 0, diff.movedIds.size); 
        assertEqualSets('unchanged ids', new Set(['S1DtWUK}krc|(xEc7{Ye', 'x#ww8BrXT*5A{XM^8#Nq', 'A%L/*!(?[qhngbz7(ymy']), diff.unchangedIds); 
 

        assertTrue('new block id 1', diff.newIds.has('HYW/b5Ae79{HFc.u^M_1'));
        assertTrue('new block id 2', diff.newIds.has('Wb{Er@h_7Z`abf+S?e{_'));

        assertEquals('new block id', 'HYW/b5Ae79{HFc.u^M_1', diff.newIdsInfo[0].id);
        assertNull('new parent id', diff.newIdsInfo[0].newParentId);
        assertNullOrFalse('is next block', diff.newIdsInfo[0].isNextBlock);
        assertNull('input name', diff.newIdsInfo[0].inputName);

        const expectedDOM = '<block xmlns="https://developers.google.com/blockly/xml" type="component_event" id="HYW/b5Ae79{HFc.u^M_1"> \
                                <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button" is_generic="false" \
                                    instance_name="Button1" event_name="GotFocus"></mutation> \
                                <field name="COMPONENT_SELECTOR">Button1</field> \
                                <statement name="DO"> \
                                    <block type="component_method" id="Wb{Er@h_7Z`abf+S?e{_"> \
                                        <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="TextBox" \
                                            method_name="HideKeyboard" is_generic="false" instance_name="TextBox1"></mutation> \
                                        <field name="COMPONENT_SELECTOR">TextBox1</field> \
                                    </block> \
                                </statement> \
                            </block>';
        assertEqualDoms(diff.newIdsInfo[0].block, expectedDOM);
    }

    static async testNewBlock5(name) {
        console.log(name);
        const [b1, b2, i1, i2] = await AI.Blockly.Tests.Diff.setup("static/media/test_015_b1_click_b1_text_t1_hide_b1_focus.xml", "static/media/test_018_b1_click_if_b1_text_t1_hide_b1_focus.xml");
        const diff = await AI.Blockly.Diff.diff(b1, b2, i1, i2);
        console.log(`${name} diff complete:`, diff);

        assertEquals('new ids length', 2, diff.newIds.size);  
        assertEquals('new ids info length', 1, diff.newIdsInfo.length);  
        assertEquals('removed ids length', 0, diff.removedIds.size);  
        assertEquals('moved ids length', 0, diff.movedIds.size);  
        assertEqualSets('unchanged ids', new Set(['S1DtWUK}krc|(xEc7{Ye', 'x#ww8BrXT*5A{XM^8#Nq', 'A%L/*!(?[qhngbz7(ymy', 'Wb{Er@h_7Z`abf+S?e{_', 'HYW/b5Ae79{HFc.u^M_1']), diff.unchangedIds); 


        assertTrue('new block id 1', diff.newIds.has('YRHgY.0tOrfK-xwr!~S2'));
        assertTrue('new block id 2', diff.newIds.has('BUaFURbz8x;xH+$#SEBY'));
        // assertTrue('moved block id 1', diff.movedIds.has('x#ww8BrXT*5A{XM^8#Nq'));
        // assertTrue('moved block id 2', diff.movedIds.has('Wb{Er@h_7Z`abf+S?e{_'));

        assertEquals('new block id', 'YRHgY.0tOrfK-xwr!~S2', diff.newIdsInfo[0].id);
        assertEquals('new parent id', 'S1DtWUK}krc|(xEc7{Ye', diff.newIdsInfo[0].newParentId);
        assertNullOrFalse('is next block', diff.newIdsInfo[0].isNextBlock);
        assertEquals('input name', 'DO', diff.newIdsInfo[0].inputName);

        const expectedDOM = '<block xmlns="https://developers.google.com/blockly/xml" type="controls_if" id="YRHgY.0tOrfK-xwr!~S2"> \
                <value name="IF0"> \
                    <block type="logic_boolean" id="BUaFURbz8x;xH+$#SEBY"> \
                        <field name="BOOL">TRUE</field> \
                    </block> \
                </value> \
                <statement name="DO0"> \
                    <block type="component_set_get" id="x#ww8BrXT*5A{XM^8#Nq"> \
                        <mutation xmlns="http://www.w3.org/1999/xhtml" component_type="Button" \
                            set_or_get="set" property_name="Text" is_generic="false" \
                            instance_name="Button1"></mutation> \
                        <field name="COMPONENT_SELECTOR">Button1</field> \
                        <field name="PROP">Text</field> \
                        <value name="VALUE"> \
                            <block type="text" id="A%L/*!(?[qhngbz7(ymy"> \
                                <field name="TEXT"></field> \
                            </block> \
                        </value> \
                        <next> \
                            <block type="component_method" id="Wb{Er@h_7Z`abf+S?e{_"> \
                                <mutation xmlns="http://www.w3.org/1999/xhtml" \
                                    component_type="TextBox" method_name="HideKeyboard" \
                                    is_generic="false" instance_name="TextBox1"></mutation> \
                                <field name="COMPONENT_SELECTOR">TextBox1</field> \
                            </block> \
                        </next> \
                    </block> \
                </statement> \
            </block>';
        assertEqualDoms(diff.newIdsInfo[0].block, expectedDOM);
    }

};



// describe("Diff", function () {
//     describe("new block", function () {
//         it("should return one new id", async function () {
            
//         });

//         it("insert but all children are moved (not new)", function () {
//             // TODO
//         });

//         it("insert and all children are new", function () {
//             // TODO
//         });

//         it("insert and some children are new and some children are moved", function () {
//             // TODO
//         });

//         it("insert as input child", function () {
//             // TODO
//         });

//         it("insert at root", function () {
//             // TODO
//         });

//         it("insert as next", function () {
//             // TODO
//         });

//         it("insert between blocks", function () {
//             // TODO
//         });
//     });

//     describe("remove block", function () {
//         it("should return one new id", function () {
//             // TODO
//         });

//         it("should return two new ids?", function () {
//             // TODO
//         });

//         it("remove but all children are moved (remain in workspace)", function () {
//             // TODO
//         });

//         it("remove and all children are also removed", function () {
//             // TODO
//         });

//         it("remove and some children are removed and some children are moved (remain in workspace)", function () {
//             // TODO
//         });

//         it("remove input child", function () {
//             // TODO
//         });

//         it("remove at root", function () {
//             // TODO
//         });

//         it("remove as next", function () {
//             // TODO
//         });

//         it("remove from between blocks", function () {
//             // TODO
//         });
//     });

//     describe("move block", function () {
//         it("should return one new id", function () {
//             // TODO
//         });

//         it("should return two new ids?", function () {
//             // TODO
//         });

//         it("move but all children are moved from somewhere else", function () {
//             // TODO
//         });

//         it("move and all children are moved with it", function () {
//             // TODO
//         });

//         it("move and some children are moved with it and some children are moved from somewhere else", function () {
//             // TODO
//         });

//         it("move input child", function () {
//             // TODO
//         });

//         it("move at root", function () {
//             // TODO
//         });

//         it("move as next", function () {
//             // TODO
//         });

//         it("move from between blocks", function () {
//             // TODO
//         });

//         it("move from beginning of blocks", function () {
//             // TODO
//         });

//         it("move from end of blocks", function () {
//             // TODO
//         });

//         it("swap blocks", function () {
//             // TODO
//         });

//         it("move block in a stack", function () {
//             // TODO
//         });
//     });
// });