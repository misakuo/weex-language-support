package com.taobao.weex;

import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * Created by moxun on 16/10/11.
 */
public abstract class NewWeFileActionBase extends CreateElementActionBase {

    public NewWeFileActionBase(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    @Override
    protected PsiElement[] invokeDialog(Project project, PsiDirectory psiDirectory) {
        MyInputValidator inputValidator = new MyInputValidator(project, psiDirectory);
        Messages.showInputDialog(project, getDialogPrompt(), getDialogTitle(), null, "", inputValidator);
        return inputValidator.getCreatedElements();
    }

    @Override
    protected PsiElement[] create(String s, PsiDirectory psiDirectory) throws Exception {
        return doCreate(s, psiDirectory);
    }

    @Override
    protected String getErrorTitle() {
        return CommonBundle.getErrorTitle();
    }

    protected PsiFile createFileFromTemplate(final PsiDirectory directory,
                                             String className,
                                             @NonNls String templateName,
                                             @NonNls String... parameters) throws IncorrectOperationException {
        final String ext = "." + WeexFileType.INSTANCE.getDefaultExtension();
        String filename = (className.endsWith(ext)) ? className : className + ext;
        return WeexTemplateFactory.createFromTemplate(directory, className, filename, templateName, parameters);
    }

    protected abstract PsiElement[] doCreate(String name, PsiDirectory directory);

    protected abstract String getDialogPrompt();

    protected abstract String getDialogTitle();
}
