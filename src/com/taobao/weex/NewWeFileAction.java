package com.taobao.weex;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * Created by moxun on 16/10/11.
 */
public class NewWeFileAction extends NewWeFileActionBase {
    public NewWeFileAction() {
        super(WeexBundle.message("newfile.menu.action.text"),
                WeexBundle.message("newfile.menu.action.description"),
                WeexIcons.FILE);
    }

    @Override
    protected PsiElement[] doCreate(String name, PsiDirectory directory) {
        PsiFile file = createFileFromTemplate(directory, name, WeexTemplateFactory.NEW_WEEX_TEMPLATE_NAME);
        PsiElement child = file.getLastChild();
        return child != null ? new PsiElement[]{file, child} : new PsiElement[]{file};
    }

    @Override
    protected String getDialogPrompt() {
        return WeexBundle.message("newfile.dialog.prompt");
    }

    @Override
    protected String getDialogTitle() {
        return WeexBundle.message("newfile.dialog.title");
    }

    @Override
    protected String getCommandName() {
        return WeexBundle.message("newfile.command.name");

    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return WeexBundle.message("newfile.menu.action.text");
    }
}
