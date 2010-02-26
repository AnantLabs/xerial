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
// SilkMain.java
// Since: Apr 9, 2009 5:00:14 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk.cui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.xerial.core.XerialError;
import org.xerial.core.XerialErrorCode;
import org.xerial.core.XerialException;
import org.xerial.util.FileResource;
import org.xerial.util.log.LogLevel;
import org.xerial.util.log.Logger;
import org.xerial.util.log.SimpleLogWriter;
import org.xerial.util.opt.Argument;
import org.xerial.util.opt.Option;
import org.xerial.util.opt.OptionParser;

/**
 * Command-line interface for managing Silk data format.
 * 
 * @author leo
 * 
 */
public class SilkMain {
    private static Logger _logger = Logger.getLogger(SilkMain.class);

    public static class SilkGlobalOption {
        @Option(symbol = "h", longName = "help", description = "display help message")
        boolean displayHelp = false;

        @Option(symbol = "l", longName = "loglevel", description = "set loglevel to one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL or ALL")
        protected LogLevel logLevel = LogLevel.INFO;

        @Argument(index = 0, name = "sub command", required = false)
        String subCommand = "help";
    }

    static Set<Class<SilkCommand>> availableCommands = new HashSet<Class<SilkCommand>>();

    static {
        availableCommands.addAll(FileResource.findClasses(SilkMain.class.getPackage(),
                SilkCommand.class, SilkMain.class.getClassLoader()));

    }

    public static int execute(String[] args) throws Exception {
        Logger.getRootLogger().setLogWriter(new SimpleLogWriter(System.err));

        SilkGlobalOption globalOption = new SilkGlobalOption();
        OptionParser parser = new OptionParser(globalOption);

        try {
            parser.parse(args, true);
            Logger.getRootLogger().setLogLevel(globalOption.logLevel);

            if (globalOption.subCommand == null)
                throw new XerialException(XerialErrorCode.INVALID_INPUT, "no command");

            SilkCommand command = null;
            for (Class<SilkCommand> each : availableCommands) {
                try {
                    command = each.newInstance();
                    if (command.getName() != null
                            && command.getName().equals(globalOption.subCommand))
                        break;
                }
                catch (InstantiationException e) {
                    _logger.warn(e);
                }
                catch (IllegalAccessException e) {
                    _logger.warn(e);
                }
            }

            if (command == null) {
                _logger.error("command is not specified");
                return -1;
            }

            // 
            OptionParser subCommandOptionParser = new OptionParser(command);
            // run the sub command
            if (globalOption.displayHelp) {
                // display help messsage of the command
                String helpFileName = String.format("help-%s.txt", command.getName());
                URL helpFileAddr = FileResource.find(SilkMain.class, helpFileName);
                if (helpFileAddr == null)
                    throw new XerialError(XerialErrorCode.RESOURCE_NOT_FOUND,
                            "help file not found: " + helpFileName);

                BufferedReader helpReader = new BufferedReader(new InputStreamReader(helpFileAddr
                        .openStream()));
                String line;
                while ((line = helpReader.readLine()) != null) {
                    System.out.println(line);
                }
                subCommandOptionParser.printUsage();

                return 0;
            }

            subCommandOptionParser.parse(parser.getUnusedArguments());
            command.execute();

            return 0;
        }
        catch (Exception e) {
            throw e;
        }

    }

    public static void main(String[] args) {

        try {
            SilkMain.execute(args);
        }
        catch (Exception e) {
            _logger.error(e);
            e.printStackTrace();
        }
        catch (Error e) {
            e.printStackTrace();
        }

    }

}
