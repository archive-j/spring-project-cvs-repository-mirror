/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ToStringBuilderTests extends TestCase {
    private Object s1, s2, s3;
    private Map map;

    public void testDefaultStyleMap() {
        Object stringy = new Object() {
            public String toString() {
                return new ToStringBuilder(this)
                    .append("familyFavoriteSport", map)
                    .toString();
            }
        };
        assertEquals(
            "[ToStringBuilderTests.1@"
                +  ObjectUtils.getIdentity(stringy) 
                + " familyFavoriteSport = map['Keri' -> 'Softball', 'Scot' -> 'Fishing', 'Keith' -> 'Flag Football']]",
            stringy.toString());
    }

    public void testDefaultStyleArray() {
        Object array = new Object[] { s1, s2, s3 };
        String stringy = new ToStringBuilder(array).toString();
        assertEquals(
            "[Object;@" + ObjectUtils.getIdentity(array) + " array[A, B, C]]",
            stringy);
    }

    public void testPrimitiveArrays() {
        int[] integers = new int[] { 0, 1, 2, 3, 4 };
        String str = new ToStringBuilder(integers).toString();
        assertEquals(
            "[[I@" + ObjectUtils.getIdentity(integers) + " array[0, 1, 2, 3, 4]]",
            str);
    }

    public void testSet() {
        List list = new ArrayList();
        list.add(s1);
        list.add(s2);
        list.add(s3);
        String str =
            new ToStringBuilder(this).append("myLetters", list).toString();
        assertEquals(
            "[ToStringBuilderTests@"
                + ObjectUtils.getIdentity(this)
                + " myLetters = list[A, B, C]]",
            str);
    }

    protected void setUp() throws Exception {
        s1 = new Object() {
            public String toString() {
                return "A";
            }
        };
        s2 = new Object() {
            public String toString() {
                return "B";
            }
        };
        s3 = new Object() {
            public String toString() {
                return "C";
            }
        };

        this.map = new HashMap();
        map.put("Keith", "Flag Football");
        map.put("Keri", "Softball");
        map.put("Scot", "Fishing");
    }

}
