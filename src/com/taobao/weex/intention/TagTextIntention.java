package com.taobao.weex.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.javascript.psi.JSBlockStatement;
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
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

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

        return available(element);
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        Set<String> vars = new HashSet<String>();
        boolean isFunc = false;

        if (guessFunction(element)) {
            vars = WeexFileUtil.getAllFunctionNames(element);
            isFunc = true;
        } else {
            vars = WeexFileUtil.getAllVarNames(element).keySet();
            isFunc = false;
        }
        final boolean isFuncFinal = isFunc;

        ListPopup listPopup = JBPopupFactory.getInstance()
                .createListPopup(new BaseListPopupStep<String>(null, vars.toArray(new String[vars.size()])) {

                    @Override
                    public Icon getIconFor(String value) {
                        return isFuncFinal ? PlatformIcons.FUNCTION_ICON : PlatformIcons.VARIABLE_ICON;
                    }

                    @Override
                    public PopupStep onChosen(final String selectedValue, final boolean finalChoice) {
                        new WriteCommandAction(project) {
                            @Override
                            protected void run(@NotNull Result result) throws Throwable {
                                editor.getDocument().insertString(editor.getCaretModel().getOffset(), "{{" + selectedValue + "}}");
                                int start = editor.getSelectionModel().getSelectionStart();
                                int end = editor.getSelectionModel().getSelectionEnd();
                                editor.getDocument().replaceString(start, end, "");
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

    private boolean available(PsiElement element) {
        PsiElement context = element.getContext();
        return context instanceof JSBlockStatement
                || context instanceof CssDeclaration
                || context instanceof XmlAttributeValue
                || context instanceof XmlText;
    }

    private boolean guessFunction(PsiElement element) {
        if (element.getContext() instanceof XmlAttributeValue) {
            try {
                XmlAttributeValue value = (XmlAttributeValue) element.getContext();
                return ((XmlAttribute) value.getContext()).getName().startsWith("on");
            } catch (Exception e) {
                return false;
            }
        }
        return element.getContext() instanceof JSBlockStatement;
    }
}
