/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.wlh.builder.generator.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by weilh on 2017/11/28.
 */
public class FormatUtils {

    static Map<String, String> typeMap = new HashMap<>();
    static {
        typeMap.put("Integer", "int");
        typeMap.put("Number", "int");
        typeMap.put("I", "int");

        typeMap.put("string", "String");

        typeMap.put("Bool", "boolean");
        typeMap.put("Boolean", "boolean");
        typeMap.put("bool", "boolean");

        typeMap.put("Long", "long");
        typeMap.put("Float", "float");
        typeMap.put("Double", "double");

    }

    public static String formatType(String original) {
        String result = typeMap.get(original);
        return result == null ? original : result;
    }
}
