package com.taobao.weex.lint;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Author: moxun
 * Created by: ModelGenerator on 16/10/12
 */
public class Attribute {
    public String name;
    public String valuePattern;
    public List<String> valueEnum;
    public String valueType;

    public boolean match(String value) {
        if (TextUtils.isEmpty(valuePattern)) {
            return false;
        }
        if (valuePattern.toLowerCase().equals("mustache")) {
            return Pattern.compile("\\{\\{.*\\}\\}").matcher(value).matches();
        }  else if (valuePattern.toLowerCase().equals("number")) {
            return Pattern.compile("[0-9]+([.][0-9]+)?$").matcher(value).matches();
        } else if (valuePattern.toLowerCase().equals("boolean")) {
            return Pattern.compile("(true|false)$").matcher(value).matches();
        }  else {
            try {
                return Pattern.compile(valuePattern).matcher(value).matches();
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void merge(Attribute attribute) {
        if (attribute.valuePattern != null) {
            valuePattern = attribute.valuePattern;
        }

        if (attribute.valueEnum != null) {
            if (valueEnum != null) {
                Set<String> set = new HashSet<String>(valueEnum);
                set.addAll(attribute.valueEnum);
                valueEnum = new ArrayList<String>(set);
            } else {
                valueEnum = attribute.valueEnum;
            }
        }

        if (attribute.valueType != null) {
            valueType = attribute.valueType;
        }
    }
}