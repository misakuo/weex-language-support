package com.taobao.weex.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by moxun on 16/10/27.
 */
public class TagTextIntention extends PsiElementBaseIntentionAction {
    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Insert mustache variable";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Weex";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {

        if (!WeexFileUtil.isOnWeexFile(element)) {
            return false;
        }

        int offset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        if (!element.isWritable() || element.getContext() == null || !element.getContext().isWritable()) {
            return false;
        }

        if (element instanceof XmlToken && ((XmlToken) element).getTokenType().toString().equals("XML_END_TAG_START")) {
            String next = document.getText(new TextRange(offset, offset + 1));
            if (next != null && next.equals("<")) {
                return true;
            }
        }

        return element.getContext() instanceof XmlText;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        final Map<String, String> maps = WeexFileUtil.getAllVarNames(element);

        ListPopup listPopup = JBPopupFactory.getInstance()
                .createListPopup(new BaseListPopupStep<String>(null, maps.keySet().toArray(new String[maps.keySet().size()])) {

                    @Override
                    public Icon getIconFor(String value) {
                        return PlatformIcons.VARIABLE_ICON;
                    }

                    @Override
                    public PopupStep onChosen(final String selectedValue, final boolean finalChoice) {
                        new WriteCommandAction(project) {
                            @Override
                            protected void run(@NotNull Result result) throws Throwable {
                                editor.getDocument().insertString(editor.getCaretModel().getOffset(), "{{" + selectedValue + "}}");
                            }
                        }.execute();
                        return super.onChosen(selectedValue, finalChoice);

                    }
                });
        listPopup.setMinimumSize(new Dimension(240, -1));
        listPopup.showInBestPositionFor(editor);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
