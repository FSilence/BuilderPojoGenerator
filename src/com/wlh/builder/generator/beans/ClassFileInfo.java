/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.wlh.builder.generator.beans;

import com.intellij.psi.*;
import com.wlh.builder.generator.utils.PsiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weilh on 2018/1/4.
 */
public class ClassFileInfo {

    public PsiFile psiFile;

    public PsiClass psiClass;

    public List<PsiMethod> psiMethods = new ArrayList<>();

    public List<PsiImportStatement> psiImports = new ArrayList<>();

    public List<PsiField> psiFields = new ArrayList<>();

    public String getClassName() {
        return psiClass.getName();
    }

    public String getNameOfClassField() {
        return PsiUtils.firstToLower(getClassName());
    }

    public static ClassFileInfo parse(PsiFile psiFile) {
        ClassFileInfo classFileInfo = new ClassFileInfo();
        classFileInfo.psiClass = PsiUtils.getPsiClass(psiFile);
        classFileInfo.psiFile = psiFile;
        for (PsiElement element : classFileInfo.psiClass.getChildren()) {
            if (element instanceof PsiMethod) {
                classFileInfo.psiMethods.add((PsiMethod) element);
            } else if (element instanceof PsiField) {
                classFileInfo.psiFields.add((PsiField) element);
            } else if (element instanceof PsiImportStatement) {
                classFileInfo.psiImports.add((PsiImportStatement) element);
            }
        }
        return classFileInfo;
    }

}
