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
// FieldSetter.java
// Since: Oct 27, 2008 3:28:03 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.util.opt.impl;

import java.lang.reflect.Field;

import org.xerial.core.XerialException;
import org.xerial.util.ReflectionUtil;
import org.xerial.util.TypeInfo;
import org.xerial.util.opt.OptionParserException;

/**
 * Option setter that bind arguments directory to a field variable
 * 
 * @author leo
 * 
 */
public class OptionSetterViaField implements OptionSetter {
    private final Field field;

    public OptionSetterViaField(Field field) {
        this.field = field;
    }

    public Class< ? > getOptionDataType() {
        return field.getType();
    }

    public void setOption(Object bean, Object value) throws XerialException {
        ReflectionUtil.setFieldValue(bean, field, value);
    }

    public boolean takesArgument() {
        Class< ? > type = getOptionDataType();
        return !TypeInfo.isBoolean(type);
    }

    public void initialize(Object bean) throws OptionParserException {
        try {
            ReflectionUtil.initializeCollectionField(bean, field);
        }
        catch (XerialException e) {
            throw new OptionParserException(e);
        }
    }

    public String getParameterName() {
        return field.getName();
    }

}
