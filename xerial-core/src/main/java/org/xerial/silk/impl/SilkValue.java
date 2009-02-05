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
// SilkValue.java
// Since: 2009/02/01 23:42:52
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk.impl;

/**
 * text value of nodes or functions
 * 
 * @author leo
 * 
 */
public interface SilkValue extends SilkElement
{
    public boolean isJSON();

    public boolean isFunction();

    /**
     * Returns the string value of this element
     * 
     * @return
     */
    public String getValue();

}
