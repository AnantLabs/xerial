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
// Import.java
// Since: Feb 9, 2009 3:40:27 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk.plugin;

import org.xerial.silk.SilkEnv;

/**
 * <em>import</em> function
 * 
 * @author leo
 * 
 */
public class Import implements SilkFunctionPlugin
{

    @SilkFunctionArgument
    String filePath = null;

    public void eval(SilkEnv env)
    {
        if (filePath == null)
        {
            env.getLogger().warn("no file path is specified");
            return;
        }

    }

}