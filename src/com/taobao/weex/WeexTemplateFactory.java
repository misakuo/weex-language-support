package com.taobao.weex;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

/**
 * Created by moxun on 16/10/11.
 */
public class WeexTemplateFactory {

    public static final String NEW_WEEX_TEMPLATE_NAME = "weex_file";

    public static PsiFile createFromTemplate(final PsiDirectory directory, final String name,
                                             String fileName, String templateName,
                                             @NonNls String... parameters) throws IncorrectOperationException {

        final FileTemplate template = FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(templateName);
        String text;

        try {
            text = template.getText();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for " +
                    FileTemplateManager.getInstance().internalTemplateToSubject(templateName), e);
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());

        final PsiFile file = factory.createFileFromText(fileName, WeexFileType.INSTANCE, text);
        CodeStyleManager.getInstance(directory.getProject()).reformat(file);
        return (PsiFile) directory.add(file);
    }
}
