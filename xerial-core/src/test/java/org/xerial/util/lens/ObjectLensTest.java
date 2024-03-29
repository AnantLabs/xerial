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
// ObjectLensTest.java
// Since: May 19, 2009 12:23:25 PM
//
// $URL$
// $Author$
//--------------------------------------
package org.xerial.util.lens;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xerial.util.Pair;
import org.xerial.util.log.Logger;

public class ObjectLensTest {

    private static Logger _logger = Logger.getLogger(ObjectLensTest.class);

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void pickPairedName() throws Exception {
        Pair<String, String> p = ObjectLens.pickRelationName("Invoice_Order");
        assertEquals("invoice", p.getFirst());
        assertEquals("order", p.getSecond());

        p = ObjectLens.pickRelationName("LineItem_Order");
        assertEquals("lineitem", p.getFirst());
        assertEquals("order", p.getSecond());

        p = ObjectLens.pickRelationName("gene_JSON");
        assertEquals("gene", p.getFirst());
        assertEquals("json", p.getSecond());

        p = ObjectLens.pickRelationName("_");
        assertEquals("", p.getFirst());
        assertEquals("", p.getSecond());

    }

    @Test
    public void pickPropertyName() throws Exception {
        String c = ObjectLens.pickPropertyName("addSomething");
        assertEquals("Something", c);

        c = ObjectLens.pickPropertyName("addSomethingImportant");
        assertEquals("SomethingImportant", c);

        c = ObjectLens.pickPropertyName("add");
        assertEquals("", c);

    }

    @Test
    public void canonicalNameTest() throws Exception {
        assertEquals("itemrgb", ObjectLens.getCanonicalParameterName("itemRgb"));
        assertEquals("itemref", ObjectLens.getCanonicalParameterName("item_ref"));
        assertEquals("helloworld", ObjectLens.getCanonicalParameterName("Hello World"));
        assertEquals("helloworld", ObjectLens.getCanonicalParameterName("Hello-World"));

        assertEquals("gapopenpenalty", ObjectLens.getCanonicalParameterName("GAPOPEN PENALTY"));
    }

    public static class PropReader {
        Properties prop = new Properties();

        public void put(String key, String value) {
            prop.put(key, value);
        }
    }

    public static class Person {
        public int id;
        public String name;
    }

    @Test
    public void getParameter() throws Exception {
        Person p = new Person();
        p.id = 10;
        p.name = "leo";
        ObjectLens lens = ObjectLens.getObjectLens(Person.class);

        Object id = lens.getParameter(p, "id");
        assertNotNull(id);
        assertEquals(p.id, Integer.class.cast(id).intValue());
        Object name = lens.getParameter(p, "name");
        assertNotNull(name);
        assertEquals(p.name, String.class.cast(name));

    }

    @Test
    public void setParameter() throws Exception {
        Person p = new Person();
        p.id = 10;
        p.name = "leo";

        ObjectLens lens = ObjectLens.getObjectLens(Person.class);
        lens.setParameter(p, "id", 50);
        lens.setParameter(p, "name", "yui");

        assertEquals(50, p.id);
        assertEquals("yui", p.name);

    }

}
