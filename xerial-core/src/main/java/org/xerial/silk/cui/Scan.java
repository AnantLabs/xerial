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
// Scan.java
// Since: Apr 23, 2009 6:50:10 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk.cui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.xerial.lens.ObjectLens;
import org.xerial.silk.SilkEvent;
import org.xerial.silk.SilkEventHandler;
import org.xerial.silk.SilkEventType;
import org.xerial.silk.SilkLineFastParser;
import org.xerial.silk.SilkLinePushParser;
import org.xerial.silk.SilkParser;
import org.xerial.silk.SilkParserConfig;
import org.xerial.util.StopWatch;
import org.xerial.util.log.Logger;
import org.xerial.util.opt.Argument;
import org.xerial.util.opt.Option;
import org.xerial.util.opt.Usage;
import org.xerial.util.tree.TreeEventHandlerBase;

/**
 * Scan command
 * 
 * @author leo
 * 
 */
@Usage(command = "silk scan", description = "silk file scanner")
public class Scan implements SilkCommand {
    private static Logger _logger = Logger.getLogger(Scan.class);

    public static enum ScanMode {
        LINE, NODE, FASTLINE, READONLY
    }

    @Argument(index = 0)
    private final String inputSilkFile = null;

    @Option(symbol = "m", longName = "mode", description = "scan mode: line, fastline, node, readonly")
    private final ScanMode mode = ScanMode.NODE;

    @Option(symbol = "b", longName = "buffer", description = "buffer size in MB (default = 1)")
    private final int bufferSizeInMB = 1;

    @Option(symbol = "n", longName = "thread", description = "num workder threads")
    private final int numThreads = 2;

    @Option(symbol = "c", longName = "lines", description = "num assigned lines for each worker threads")
    private final int numLines = 1000;

    private void reportReadSpeed(double time, long fileSize) {
        double speedInMBS = fileSize / 1024 / 1024 / time;
        _logger.info(String.format("\ntime=%.2f, %3.2f MB/s", time, speedInMBS));

    }

    private void reportLinesPerSec(double time, long lineCount) {
        double speed = lineCount / time;
        System.err.print(String.format("time=%5.2f line=%,10d %,10.0f lines/s\r", time, lineCount,
                speed));
    }

    public void execute() throws Exception {
        File f = new File(inputSilkFile);
        final long fileSize = f.length();

        final SilkParserConfig config = new SilkParserConfig();
        config.bufferSize = bufferSizeInMB * 1024 * 1024;
        config.numWorkers = numThreads;
        config.numLinesInBlock = numLines;

        _logger.info("config: " + ObjectLens.toJSON(config));

        switch (mode) {
        case NODE: {
            SilkParser parser = new SilkParser(f.toURI().toURL(), config);

            parser.parse(new TreeEventHandlerBase() {

                int count = 0;
                StopWatch timer = new StopWatch();

                @Override
                public void init() throws Exception {
                    timer.reset();
                }

                @Override
                public void visitNode(String nodeName, String immediateNodeValue) throws Exception {
                    count++;
                    if (count % 1000000 == 0) {
                        double time = timer.getElapsedTime();
                        double speed = count / time;
                        System.err.print(String.format("node=%,15d time=%5.2f %,10.0f nodes/s\r",
                                count, time, speed));
                    }

                }

                @Override
                public void finish() throws Exception {
                    double time = timer.getElapsedTime();
                    double speedPerNode = (count) / time;
                    double speedInMBS = fileSize / 1024 / 1024 / time;
                    _logger.info(String.format("\ntime=%.2f %,10.0f nodes/s, %3.2f MB/s", time,
                            speedPerNode, speedInMBS));
                }

            });
            break;
        }
        case LINE: {
            SilkLinePushParser parser = new SilkLinePushParser(f.toURI().toURL(), config);
            parser.parse(new SilkEventHandler() {

                int lineCount = 0;
                StopWatch timer = new StopWatch();

                public void handle(SilkEvent event) throws Exception {
                    if (event.getType() == SilkEventType.END_OF_FILE) {
                        reportReadSpeed(timer.getElapsedTime(), fileSize);
                        return;
                    }

                    lineCount++;
                    if (lineCount % 100000 == 0) {
                        reportLinesPerSec(timer.getElapsedTime(), lineCount);
                    }

                }
            });

            break;
        }
        case FASTLINE: {
            SilkLineFastParser parser = new SilkLineFastParser(f.toURI().toURL(), config);
            parser.parse(new SilkEventHandler() {

                int lineCount = 0;
                StopWatch timer = new StopWatch();

                public void handle(SilkEvent event) throws Exception {
                    if (event.getType() == SilkEventType.END_OF_FILE) {
                        reportReadSpeed(timer.getElapsedTime(), fileSize);
                        return;
                    }

                    lineCount++;
                    if (lineCount % 100000 == 0) {
                        reportLinesPerSec(timer.getElapsedTime(), lineCount);
                    }

                }
            });

            break;
        }
        case READONLY: {
            BufferedReader reader = new BufferedReader(new FileReader(f), config.bufferSize);
            String line;

            int lineCount = 0;
            StopWatch timer = new StopWatch();

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount % 100000 == 0) {
                    reportLinesPerSec(timer.getElapsedTime(), lineCount);
                }

            }

            reportReadSpeed(timer.getElapsedTime(), fileSize);

            break;
        }
        }

    }

    public String getName() {
        return "scan";
    }

    public String getOneLineDescription() {
        return "scanning the input Silk file";
    }

}
