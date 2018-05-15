/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.wlh.builder.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.wlh.builder.generator.ui.SettingDialog;

/**
 * Created by weilh on 2018/1/4.
 */
public class BuilderGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getDataContext().getData(LangDataKeys.PSI_FILE);
        Project project = anActionEvent.getData(LangDataKeys.PROJECT);

        Config config = new Config();
        config.forceChangeToFinal = true;
        config.overlay = true;
        config.createGetter = true;
        SettingDialog settingDialog = new SettingDialog();
        settingDialog.setOnClickListner(new SettingDialog.OnClickListner() {
            @Override
            public void onClick(Config config) {
                new BuilderClassCreator(project, psiFile, config).generate();
            }
        });
        settingDialog.setVisible(true);
    }
}
