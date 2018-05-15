/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.wlh.builder.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.wlh.builder.generator.beans.ClassFileInfo;
import com.wlh.builder.generator.utils.PsiUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * Created by weilh on 2018/1/4.
 */
public class BuilderClassCreator {
    private Project mProject;
    private PsiFile mCurrentPsiFile;
    private Config mConfig;
    private PsiElementFactory mElementFactory;

    private PsiClass mBuilderPsiClass;

    private ClassFileInfo mClassFileInfo;
    public BuilderClassCreator(Project project, PsiFile psiFIle, Config config) {
        this.mProject = project;
        this.mCurrentPsiFile = psiFIle;
        this.mConfig = config;

        this.mElementFactory = JavaPsiFacade.getInstance(mProject).getElementFactory();
        mClassFileInfo = ClassFileInfo.parse(psiFIle);
        if (mConfig == null) {
            //设置默认的Config
            mConfig = new Config();
        }
    }

    /**
     * 主入口
     */
    public void generate() {
        new Writter(mProject, mCurrentPsiFile).execute();
    }

    private void changeFieldModifyType() {
        for (PsiField field : mClassFileInfo.psiFields) {
            if (field.getModifierList().hasModifierProperty("static")) {
                continue;
            }
            field.delete();
            PsiField newField =  mElementFactory.createFieldFromText(String.format("private final %s %s;",
                    field.getType().getPresentableText(), field.getName()), mClassFileInfo.psiClass);
            newField.getModifierList().checkSetModifierProperty("private", true);
            newField.getModifierList().checkSetModifierProperty("final", true);
            mClassFileInfo.psiClass.add(newField);
        }
    }
    /**
     * 创建构造函数
     */
    private void createConstructMethod() {
        StringBuilder methodStr = new StringBuilder();
        methodStr.append(String.format("private %s(Builder builder) {", mClassFileInfo.psiClass.getName()));
        for (PsiField field : mClassFileInfo.psiFields) {
            methodStr.append(String.format("this.%s = builder.%s;", field.getName(), field.getName()));
        }
        methodStr.append("}");
        PsiMethod psiMethod = mElementFactory.createMethodFromText(methodStr.toString(), mClassFileInfo.psiClass);
        mClassFileInfo.psiClass.add(psiMethod);
    }

    private void createStaticBuildMethod() {
        PsiMethod method = mElementFactory.createMethodFromText("public static Builder builder() { return new Builder();}", mClassFileInfo.psiClass);
        mClassFileInfo.psiClass.add(method);

    }

    private void createGetterMethods() {
        for(PsiField psiField : mClassFileInfo.psiFields) {
            PsiMethod method = mElementFactory.createMethodFromText(
                    String.format("public %s %s() { return %s;}", psiField.getType().getPresentableText(),
                            getGetterMethodName(psiField), psiField.getName()), mClassFileInfo.psiClass);
            mClassFileInfo.psiClass.add(method);
        }
    }

    private String getGetterMethodName(PsiField psiField) {
        String name = "get";
        if (psiField.getType().getPresentableText().equals("boolean") ||
                psiField.getType().getPresentableText().equals("Boolean")) {
            name = "is";
        }

        String fieldName = psiField.getName();
        if (Pattern.matches("m[A-Z].*", psiField.getName())) {
            name += fieldName.substring(1);
        } else {
            name += PsiUtils.firstToUpper(fieldName);
        }
        return name;
    }

    /**********************************/

    /**
     * 创建内部的Builder类
     */
    private void createInnerBuilderClass() {
        mBuilderPsiClass = mElementFactory.createClass("Builder");
        mBuilderPsiClass.getModifierList().setModifierProperty("static", true);
        appendFields();
        if (mConfig.overlay) {
            createReplaceConcurent();
            createOverlayMethod();
        }
        appendSetMethods();
        appendBuildMethod();
        //添加到当前的Class中
        mClassFileInfo.psiClass.add(mBuilderPsiClass);
    }

    private void appendFields() {
       for (PsiField psiField : mClassFileInfo.psiFields) {
           PsiField builderField = mElementFactory.createFieldFromText(
                   String.format("private %s %s;", psiField.getType().getPresentableText(), psiField.getName()), mBuilderPsiClass);
           mBuilderPsiClass.add(builderField);
       }
    }

    private void appendSetMethods() {
        for (PsiField psiField : mClassFileInfo.psiFields) {
            StringBuilder methodStr = new StringBuilder();
            String memberFieldName = PsiUtils.getMemberFieldName(psiField.getName());
            methodStr.append(String.format("public Builder %s(%s %s) {", memberFieldName,
                    psiField.getType().getPresentableText(),memberFieldName));
            methodStr.append(String.format("this.%s = %s;", psiField.getName(), memberFieldName));
            methodStr.append("return this; }");
            mBuilderPsiClass.add(mElementFactory.createMethodFromText(methodStr.toString(), mBuilderPsiClass));
        }
    }
    /**
     * 追加builder 方法
     *
     */
    private void appendBuildMethod() {
        PsiMethod psiMethod = mElementFactory.createMethodFromText(
                String.format("public %s build() { return new %s(this);}", mClassFileInfo.getClassName(),
                        mClassFileInfo.getClassName()), mBuilderPsiClass);
        mBuilderPsiClass.add(psiMethod);
    }

    private void createOverlayMethod() {
        String classFieldName = mClassFileInfo.getNameOfClassField();
        StringBuilder methodStr = new StringBuilder(String.format("public Builder overlay(%s %s) {", mClassFileInfo.getClassName(),
                classFieldName));
        methodStr.append(String.format("if (%s == null) { return this;}", classFieldName));
        for (PsiField psiField : mClassFileInfo.psiFields) {
            String getStr = String.format("%s.%s()", classFieldName, getGetterMethodName(psiField));
            if (psiField.getType().getPresentableText().equals("String")) {
                methodStr.append(String.format("if (!TextUtils.isEmpty(%s)) {%s = %s;}", getStr, psiField.getName(), getStr));
            } else if (Arrays.asList("int", "long").contains(psiField.getType().getPresentableText())){
                methodStr.append(String.format("if (%s != 0) {%s = %s;}", getStr, psiField.getName(), getStr));
            } else if (Arrays.asList("float", "double").contains(psiField.getType().getPresentableText())){
                methodStr.append(String.format("if (%s != 0.0) {%s = %s;}", getStr, psiField.getName(), getStr));
            } else if ("boolean".equals(psiField.getType().getPresentableText())) {
                methodStr.append(String.format("%s = %s;", psiField.getName(), getStr));
            } else {
                methodStr.append(String.format("if (%s != null) {%s = %s;}", getStr, psiField.getName(), getStr));
            }
        }
        methodStr.append("return this;}");
        mBuilderPsiClass.add(mElementFactory.createMethodFromText(methodStr.toString(), mBuilderPsiClass));
    }

    private void createReplaceConcurent() {
        mBuilderPsiClass.add(mElementFactory.createMethodFromText("public Builder() {}", mBuilderPsiClass));

        String classFieldName = mClassFileInfo.getNameOfClassField();
        StringBuilder replaceMethodStr = new StringBuilder(String.format("public Builder(%s %s) {", mClassFileInfo.getClassName(),
                classFieldName));
        for (PsiField psiField : mClassFileInfo.psiFields) {
            replaceMethodStr.append(String.format("this.%s = %s.%s;", psiField.getName(), classFieldName, psiField.getName()));
        }
        replaceMethodStr.append("}");
        mBuilderPsiClass.add(mElementFactory.createMethodFromText(replaceMethodStr.toString(), mBuilderPsiClass));
    }

    private class Writter extends WriteCommandAction.Simple {

        protected Writter(Project project, PsiFile... files) {
            super(project, files);
        }

        @Override
        protected void run() throws Throwable {
            if (mConfig.forceChangeToFinal) {
                changeFieldModifyType();
            }
            createConstructMethod();
            createStaticBuildMethod();
            if (mConfig.createGetter) {
                createGetterMethods();
            }
            createInnerBuilderClass();
        }
    }
}
