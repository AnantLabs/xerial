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
// SilkMainTest.java
// Since: Apr 9, 2009 5:29:18 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.core.cui;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.xerial.util.log.LogLevel;
import org.xerial.util.log.Logger;

public class SilkMainTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @AfterClass
    public static void reset() throws Exception {
        Logger.getRootLogger().setLogLevel(LogLevel.INFO);
    }

    @Test
    public void testMain() throws Exception {
        XerialMain.execute(new String[] {});
        XerialMain.execute(new String[] { "help" });
        XerialMain.execute(new String[] { "scan", "--help" });
    }

}
