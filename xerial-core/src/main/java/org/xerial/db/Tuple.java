/*--------------------------------------------------------------------------
 *  Copyright 2007 Taro L. Saito
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
// Tuple.java
// Since: Aug 31, 2007 1:23:30 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.db;

import org.xerial.db.cache.BufferReader;
import org.xerial.db.cache.BufferWriter;

/**
 * {@link Tuple} class represents an instance of a {@link Relation}.
 * @author leo
 *
 */
public abstract class Tuple
{
    private final Relation relation;
    
    public Tuple(Relation relation)
    {
        this.relation = relation;
    }

    public Relation getRelation()
    {
        return relation;
    }
    
    public abstract void setValue(int relationIndex, Object value);
    public void setValue(String parameterName, Object value)
    {
        setValue(relation.getDataTypeIndex(parameterName), value);
    }
    
    public abstract Object getValue(int relationIndex);
    public Object getValue(String parameterName)
    {
        return getValue(relation.getDataTypeIndex(parameterName));
    }
    
    /**
     * Loads the tuple from the buffer 
     * @param reader the {@link BufferReader}
     * @return the byte size read (tuple size)
     */
    public abstract int load(BufferReader reader);
    /**
     * Saves the tuple to the buffer
     * @param writer the {@link BufferWriter}
     * @return the byte size wrote (tuple size)
     */
    public abstract int save(BufferWriter writer);

}
