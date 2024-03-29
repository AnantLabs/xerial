/*--------------------------------------------------------------------------
 *  Copyright 2009 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// XerialJ
//
// SilkLineFastParserTest.java
// Since: Jun 5, 2009 3:11:31 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xerial.core.XerialException;
import org.xerial.json.JSONStreamWalker;
import org.xerial.util.FileResource;
import org.xerial.util.log.Logger;
import org.xerial.util.tree.TreeWalkLog;

public class SilkLineFastParserTest
{
    private static Logger _logger = Logger.getLogger(SilkLineFastParserTest.class);

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    public static void compare(String silkFile, String jsonFile) throws IOException, XerialException {
        SilkParserConfig config = new SilkParserConfig();
        config.numWorkers = 2; // use SilkLineFastParser
        SilkWalker walker = new SilkWalker(FileResource.find(SilkLineFastParserTest.class, silkFile), config);
        TreeWalkLog l1 = new TreeWalkLog();
        TreeWalkLog l2 = new TreeWalkLog();

        walker.walk(l1);
        _logger.debug(l1);

        JSONStreamWalker jWalker = new JSONStreamWalker(FileResource.open(SilkWalkerTest.class, jsonFile));
        jWalker.walk(l2);

        Assert.assertTrue(TreeWalkLog.compare(l1, l2));
    }

    @Test
    public void parse() throws Exception {
        compare("small.silk", "small.json");
    }

}
