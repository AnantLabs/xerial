/*--------------------------------------------------------------------------
 *  Copyright 2004 Taro L. Saito
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
// NullOutputStreamTest.java
// Since: 2005/07/19 0:35:28
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.util.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.xerial.util.io.NullOutputStream;

import junit.framework.TestCase;

public class NullOutputStreamTest extends TestCase {

	PrintStream _out;
	
	protected void setUp() throws Exception {
		_out = new PrintStream(new NullOutputStream());
	}

	protected void tearDown() throws Exception {
		
	}

	public void testOutput()
	{
		_out.print("this message must be invisible to stdout");
	}
	
}
