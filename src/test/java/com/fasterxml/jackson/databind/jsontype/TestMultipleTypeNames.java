package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;

// Tests for [databind#2761] (and [annotations#171]
public class TestMultipleTypeNames extends BaseMapTest
{
    private final ObjectMapper MAPPER = newJsonMapper();

    // common classes
    static class MultiTypeName { }

    static class A extends MultiTypeName {
        private long x;
        public long getX() { return x; }
    }

    static class B extends MultiTypeName {
        private float y;
        public float getY() { return y; }
    }

    // data for test 1
    static class WrapperForNamesTest {
        private List<BaseForNamesTest> base;
        public List<BaseForNamesTest> getBase() { return base; }
    }

    static class BaseForNamesTest {
        private String type;
        public String getType() { return type; }

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "type"
        )
        @JsonSubTypes(value = {
                @JsonSubTypes.Type(value = A.class, names = "a"),
                @JsonSubTypes.Type(value = B.class, names = {"b","c"}),
        })
        private MultiTypeName data;
        public MultiTypeName getData() { return data; }
    }

    static class WrapperForNameAndNamesTest {
        private List<BaseForNameAndNamesTest> base;
        public List<BaseForNameAndNamesTest> getBase() { return base; }
    }

    static class BaseForNameAndNamesTest {
        private String type;
        public String getType() { return type; }

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "type"
        )
        @JsonSubTypes(value = {
                @JsonSubTypes.Type(value = A.class, name = "a"),
                @JsonSubTypes.Type(value = B.class, names = {"b","c"}),
        })
        private MultiTypeName data;
        public MultiTypeName getData() { return data; }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    public void testOnlyNames() throws Exception
    {
        String json;
        WrapperForNamesTest w;

        // TC 1 : all KV serialisation
        json = "{\"base\": [{\"type\":\"a\", \"data\": {\"x\": 5}}, {\"type\":\"b\", \"data\": {\"y\": 3.1}}, {\"type\":\"c\", \"data\": {\"y\": 33.8}}]}";
        w = MAPPER.readValue(json, WrapperForNamesTest.class);
        assertNotNull(w);
        assertEquals(3, w.base.size());
        assertTrue(w.base.get(0).data instanceof A);
        assertEquals(5l, ((A) w.base.get(0).data).x);
        assertTrue(w.base.get(1).data instanceof B);
        assertEquals(3.1f, ((B) w.base.get(1).data).y, 0);
        assertTrue(w.base.get(2).data instanceof B);
        assertEquals(33.8f, ((B) w.base.get(2).data).y, 0);


        // TC 2 : incorrect serialisation
        json = "{\"data\": [{\"type\":\"a\", \"data\": {\"x\": 2.2}}, {\"type\":\"b\", \"data\": {\"y\": 5.3}}, {\"type\":\"c\", \"data\": {\"y\": 9.8}}]}";
        try  {
            MAPPER.readValue(json, WrapperForNamesTest.class);
            fail("This serialisation should fail 'coz of x being float");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "Unrecognized field \"data\"");
        }
    }

    public void testNameAndNames() throws Exception
    {
        String json;
        WrapperForNameAndNamesTest w;

        // TC 1 : all KV serialisation
        json = "{\"base\": [{\"type\":\"a\", \"data\": {\"x\": 5}}, {\"type\":\"b\", \"data\": {\"y\": 3.1}}, {\"type\":\"c\", \"data\": {\"y\": 33.8}}]}";
        w = MAPPER.readValue(json, WrapperForNameAndNamesTest.class);
        assertNotNull(w);
        assertEquals(3, w.base.size());
        assertTrue(w.base.get(0).data instanceof A);
        assertEquals(5l, ((A) w.base.get(0).data).x);
        assertTrue(w.base.get(1).data instanceof B);
        assertEquals(3.1f, ((B) w.base.get(1).data).y, 0);
        assertTrue(w.base.get(2).data instanceof B);
        assertEquals(33.8f, ((B) w.base.get(2).data).y, 0);


        // TC 2 : incorrect serialisation
        json = "{\"data\": [{\"type\":\"a\", \"data\": {\"x\": 2.2}}, {\"type\":\"b\", \"data\": {\"y\": 5.3}}, {\"type\":\"c\", \"data\": {\"y\": 9.8}}]}";
        try  {
            MAPPER.readValue(json, WrapperForNameAndNamesTest.class);
            fail("This serialisation should fail 'coz of x being float");
        } catch (UnrecognizedPropertyException e) {
            verifyException(e, "Unrecognized field \"data\"");
        }
    }
}
