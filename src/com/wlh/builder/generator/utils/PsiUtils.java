/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.wlh.builder.generator.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.regex.Pattern;

/**
 *
 * Created by weilh on 2017/11/28.
 */
public class PsiUtils {

    public static PsiClass getPsiClass(PsiFile psiFile) {
        for (PsiElement element : psiFile.getChildren()) {
            if (element instanceof PsiClass) {
                //find first psiClass
                return (PsiClass) element;
            }
        }
        return null;
    }

    /**
     * 将 "a_b" 转换为 "aB"
     *
     * @return
     */
    public static String formatFieldNameToaB(String str) {
        StringBuilder result = new StringBuilder();
        boolean changeToUp = false;
        for (int i = 0; i < str.length(); i++) {
            if ('_' == str.charAt(i)) {
                changeToUp = true;
            } else if (changeToUp) {
                result.append(toUp(str.charAt(i)));
                changeToUp = false;
            } else {
                result.append(str.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * 获取成员变量名称 如果名称是m + 大写字母开头, 则去掉m 首字母小写
     *
     * @param fieldName
     * @return
     */
    public static String getMemberFieldName(String fieldName) {
        if (Pattern.matches("m[A-Z].*", fieldName)) {
            return firstToLower(fieldName.substring(1));
        }
        return fieldName;
    }

    public static String firstToUpper(String name) {
        char firstIndex = name.charAt(0);
        if (firstIndex >= 'a' && firstIndex <= 'z') {
            char newIndex = (char) (name.charAt(0) + ('A' - 'a'));
            return name.replaceFirst(firstIndex + "", newIndex + "");
        }
        return name;
    }

    /**
     * 首字母小写
     *
     * @return
     */
    public static String firstToLower(String name) {
        char firstIndex = name.charAt(0);
        if (firstIndex >= 'A' && firstIndex <= 'Z') {

            char newIndex = (char) (name.charAt(0) - ('A' - 'a'));
            return name.replaceFirst(firstIndex + "", newIndex + "");
        }
        return name;
    }

    public static char toUp(char a) {
        if (a >= 'A' && a <= 'Z') {
            return a;
        }
        return (char) (a + ('A' - 'a'));
    }

    public static char toLow(char a) {
        if (a >= 'a' && a <= 'z') {
            return a;
        }
        return (char) (a - ('A' - 'a'));
    }
}
