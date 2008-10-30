/*--------------------------------------------------------------------------
 *  Copyright 2008 Taro L. Saito
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
// OptionParserTest.java
// Since: Oct 28, 2008 4:44:53 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.util.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xerial.util.cui.OptionParserException;
import org.xerial.util.log.LogLevel;
import org.xerial.util.log.Logger;

public class OptionParserTest
{
    private static Logger _logger = Logger.getLogger(OptionParserTest.class);

    @Before
    public void setUp() throws Exception
    {}

    @After
    public void tearDown() throws Exception
    {}

    class MyOption
    {
        @Option(symbol = "h", longName = "help", description = "display help message")
        private boolean      displayHelp;

        @Argument(name = "sub command name", index = 0)
        private String       subCommand;

        @Argument(name = "input files ...", index = 1, required = false)
        private List<String> fileList;
    }

    @Test
    public void optionBinding() throws OptionParserException
    {
        MyOption myOption = new MyOption();
        OptionParser parser = new OptionParser(myOption);

        parser.parse(new String[] { "add", "-h", "1.txt", "2.txt" });

        assertEquals("add", myOption.subCommand);
        assertTrue(myOption.displayHelp);
        assertNotNull(myOption.fileList);
        assertEquals(2, myOption.fileList.size());
        assertEquals("1.txt", myOption.fileList.get(0));
        assertEquals("2.txt", myOption.fileList.get(1));

    }

    @Test
    public void testDuplicatedOptionFlag()
    {
        try
        {
            MyOption myOption = new MyOption();
            OptionParser parser = new OptionParser(myOption);

            parser.parse(new String[] { "-h", "-h" });

            fail("must detect the presence of duplicated option flags");
        }
        catch (OptionParserException e)
        {

        }
    }

    @Test
    public void testDuplicatedLongOptionFlag()
    {
        try
        {
            MyOption myOption = new MyOption();
            OptionParser parser = new OptionParser(myOption);

            parser.parse(new String[] { "--help", "--help" });

            fail("must detect the presence of duplicated option flags");
        }
        catch (OptionParserException e)
        {

        }
    }

    @Test
    public void detectMissingArgument()
    {
        try
        {
            MyOption myOption = new MyOption();
            OptionParser parser = new OptionParser(myOption);

            parser.parse(new String[] {});

            fail("must detect some argument is missing");
        }
        catch (OptionParserException e)
        {

        }

    }

    @Test
    public void initializeCollectionsInOptionHolder() throws OptionParserException
    {
        MyOption myOption = new MyOption();
        OptionParser parser = new OptionParser(myOption);

        parser.parse(new String[] { "add" });

        assertEquals("add", myOption.subCommand);
        assertFalse(myOption.displayHelp);
        assertNotNull(myOption.fileList);
        assertEquals(0, myOption.fileList.size());

    }

    class LoglevelCommand
    {
        @Option(symbol = "l", longName = "loglevel", varName = "LOG_LEVEL")
        private LogLevel loglevel = LogLevel.INFO;
    }

    @Test
    public void enumOption() throws OptionParserException
    {
        LoglevelCommand opt = new LoglevelCommand();
        OptionParser parser = new OptionParser(opt);
        parser.parse(new String[] { "--loglevel=DEBUG" });

        assertEquals(LogLevel.DEBUG, opt.loglevel);
    }

    class MultipleName
    {
        @Option(longName = "name")
        private List<String> name;
    }

    @Test
    public void mulpleOptionOccurences() throws OptionParserException
    {
        MultipleName mn = new MultipleName();
        OptionParser parser = new OptionParser(mn);
        parser.parse(new String[] { "--name=leo", "--name=yui" });

        assertNotNull(mn.name);
        assertEquals(2, mn.name.size());
        assertEquals("leo", mn.name.get(0));
        assertEquals("yui", mn.name.get(1));
    }

    class IntArg
    {
        @Option(symbol = "i")
        int           num;

        @Argument(name = "value")
        List<Integer> value;
    }

    @Test
    public void intArgument() throws OptionParserException
    {
        IntArg intArg = new IntArg();
        OptionParser parser = new OptionParser(intArg);

        parser.parse(new String[] { "-i", "3", "1", "4", "10" });

        assertNotNull(intArg.value);
        assertEquals(3, intArg.num);
        assertEquals(3, intArg.value.size());
        assertEquals(1, intArg.value.get(0).intValue());
        assertEquals(4, intArg.value.get(1).intValue());
        assertEquals(10, intArg.value.get(2).intValue());

    }

    class AmbiguousTypeArg
    {
        @Argument(name = "value")
        List< ? > value;

        /**
         * for testing purpose, use untyped list
         */
        @SuppressWarnings("unchecked")
        @Option(symbol = "d")
        List      d;
    }

    @Test
    public void ambiguousType() throws OptionParserException
    {
        AmbiguousTypeArg arg = new AmbiguousTypeArg();
        OptionParser parser = new OptionParser(arg);

        parser.parse(new String[] { "1", "2", "3", "-d", "5", "-d", "10" });

        assertNotNull(arg.value);
        assertEquals(3, arg.value.size());
        assertEquals("1", arg.value.get(0));
        assertEquals("2", arg.value.get(1));
        assertEquals("3", arg.value.get(2));

        assertNotNull(arg.d);
        assertEquals(2, arg.d.size());
        assertEquals("5", arg.d.get(0));
        assertEquals("10", arg.d.get(1));

    }

}