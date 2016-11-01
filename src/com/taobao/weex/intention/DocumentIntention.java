package com.taobao.weex.intention;

import com.google.gson.Gson;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlToken;
import com.intellij.ui.EditorTextField;
import com.intellij.util.IncorrectOperationException;
import com.taobao.weex.WeexFileType;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by moxun on 16/10/27.
 * TODO: Not completed.
 */
public class DocumentIntention extends PsiElementBaseIntentionAction {
    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement psiElement) throws IncorrectOperationException {

        JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<String>("Chosen", "Document", "Sample") {

            @Override
            public PopupStep onChosen(String selectedValue, boolean finalChoice) {

                if ("Document".equals(selectedValue)) {
                    openDocument(psiElement.getText());
                } else if ("Sample".equals(selectedValue)) {
                    openSample(project, editor);
                }

                return super.onChosen(selectedValue, finalChoice);
            }
        }).showInBestPositionFor(editor);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement instanceof XmlToken && false) {
            String tokenType = ((XmlToken) psiElement).getTokenType().toString();
            if ("XML_NAME".equals(tokenType)) {
                String tagName = psiElement.getText();
                return DirectiveLint.containsTag(tagName);
            }
        }
        return false;
    }

    @NotNull
    @Override
    public String getText() {
        return "Open document or sample";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    private void openDocument(String tagName) {
        WeexTag tag = DirectiveLint.getWeexTag(tagName);
        if (tag != null && tag.document != null) {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(URI.create(tag.document));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openSample(Project project, Editor editor) {

        EditorTextField field = new EditorTextField(editor.getDocument(), project, WeexFileType.INSTANCE, true, false) {
            @Override
            protected EditorEx createEditor() {
                EditorEx editor1 = super.createEditor();
                editor1.setVerticalScrollbarVisible(true);
                editor1.setHorizontalScrollbarVisible(true);
                return editor1;

            }
        };

        field.setFont(editor.getContentComponent().getFont());

        JBPopup jbPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(field, null)
                .createPopup();

        jbPopup.setSize(new Dimension(500, 500));
        jbPopup.showInBestPositionFor(editor);
    }

    private ComponentBean load(String name) {
        InputStream is = DirectiveLint.class.getResourceAsStream("/samples/index.json");
        Gson gson = new Gson();
        ComponentBean[] beans = gson.fromJson(new InputStreamReader(is), ComponentBean[].class);
        for (ComponentBean bean : beans) {
            if (name.equals(bean.component)) {
                return bean;
            }
        }
        return null;
    }
}
