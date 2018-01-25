package com.thefloow.gradle.gitversion;

import groovy.util.MapEntry;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Collections.enumeration;

public class SortedProperties extends Properties {

    private final Map<String, String> values;

    public SortedProperties(final Map<String, String> values) {
        this.values = values;
        putAll(values);
    }

    @Override
    public Enumeration<Object> keys() {
        List<Object> list = new ArrayList<>(values.keySet());
        return enumeration(list);
    }

    @Override
    public Set<Object> keySet() {
        return new LinkedHashSet<>(values.keySet());
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        Set<Map.Entry<Object, Object>> result = new LinkedHashSet<>();
        values.forEach((key, value) -> result.add(new MapEntry(key, value)));
        return result;
    }

}
