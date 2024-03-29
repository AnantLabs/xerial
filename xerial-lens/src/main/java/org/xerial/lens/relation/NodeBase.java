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
// NodeBase.java
// Since: 2009/05/13 22:28:04
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.lens.relation;

import org.xerial.core.XerialError;
import org.xerial.core.XerialErrorCode;

/**
 * 
 * Base implementation of the TreeNode
 * 
 * @author leo
 * 
 * @param <NodeType>
 */
public abstract class NodeBase<NodeType extends TupleElement<NodeType>> implements
        TupleElement<NodeType> {
    protected NodeBase() {}

    public boolean isAtom() {
        return true;
    }

    public boolean isTuple() {
        return false;
    }

    /**
     * Always return 1
     * 
     * @return
     */
    public int size() {
        return 1;
    }

    public Tuple<NodeType> castToTuple() {
        return null;
    }

    public TupleElement<NodeType> get(TupleIndex index) {
        if (index.size() == 0 && index.get(0) == 0)
            return this;
        else
            throw new XerialError(XerialErrorCode.INVALID_STATE);
    }

    public NodeType getNode(TupleIndex index) {
        if (!(index.size() == 1 && index.get(0) == 0))
            throw new XerialError(XerialErrorCode.INVALID_STATE);
        else
            return castToNode();
    }

    public TupleElement<NodeType> get(int index) {
        if (index != 0)
            throw new XerialError(XerialErrorCode.INVALID_STATE);
        else
            return castToNode();
    }

    @SuppressWarnings("unchecked")
    public NodeType castToNode() {
        return (NodeType) this;
    }

    public void accept(TupleVisitor<NodeType> visitor) {
        visitor.visitNode(this.castToNode());
    }

}