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
// SilkNode.java
// Since: Feb 2, 2009 10:41:58 AM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.silk;

import org.xerial.silk.model.SilkElement;
import org.xerial.silk.model.SilkFunction;
import org.xerial.silk.model.SilkNode;
import org.xerial.silk.model.SilkNodeOccurrence;

/**
 * Event data of {@link SilkLinePullParser}
 * 
 * @author leo
 * 
 */
public class SilkEvent {
    public final SilkEventType type;
    public final SilkElement element;

    public SilkEvent(SilkEventType type, SilkElement element) {
        this.type = type;
        this.element = element;
    }

    public static SilkEvent createEvent(SilkElement elem) {
        if (elem instanceof SilkNode) {
            SilkNode n = SilkNode.class.cast(elem);
            if (n.occurrence == SilkNodeOccurrence.SEQUENCE_PRESERVING_WHITESPACES)
                return new SilkEvent(SilkEventType.BLOCK_NODE, elem);
            else
                return new SilkEvent(SilkEventType.NODE, elem);
        }
        else if (elem instanceof SilkFunction)
            return new SilkEvent(SilkEventType.FUNCTION, elem);
        else
            return null;
    }

    public SilkEventType getType() {
        return type;
    }

    public SilkElement getElement() {
        return element;
    }

    @Override
    public String toString() {
        if (element == null)
            return String.format("[%s]", type);
        else
            return String.format("[%s] %s", type, element);
    }

}
