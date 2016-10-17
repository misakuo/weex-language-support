package com.taobao.weex.quickfix;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by moxun on 16/10/14.
 */
public class QuickFixAction extends BaseIntentionAction {

    private String name = null;
    private String type = null;
    private static Map<String, String> defValues = new HashMap<String, String>();

    static {
        defValues.put("var", "null");
        defValues.put("boolean", "false");
        defValues.put("number", "0");
        defValues.put("object", "{}");
        defValues.put("array", "[]");
        defValues.put("string", "''");
    }

    public QuickFixAction(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @NotNull
    @Override
    public String getText() {
        String t = "function".equals(type) ? "function " : "variable ";
        return "Create " + t + name;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Weex properties";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (!"function".equals(type)) {
            int offset = WeexFileUtil.getExportsEndOffset(psiFile, "data");
            hasComma(offset, editor);
            if (offset < 0) {
                return;
            }
            String template = name + ": " + getDefaultValue(type) + ",\n";

            if (!hasComma(offset, editor)) {
                template = "," + template;
            }

            editor.getDocument().insertString(offset, template);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            CodeStyleManager.getInstance(project).reformat(psiFile);
        } else {
            int offset = WeexFileUtil.getExportsEndOffset(psiFile, "methods");
            if (offset < 0) {
                return;
            }

            String template = name + ": " + "function () {\n\n}\n";

            if (!hasComma(offset, editor)) {
                template = "," + template;
            }

            editor.getDocument().insertString(offset, template);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            CodeStyleManager.getInstance(project).reformat(psiFile);
        }
    }

    private boolean hasComma(int start, Editor editor) {
        Document document = editor.getDocument();
        TextRange range = new TextRange(start - 1, start);
        while (true) {
            String s = document.getText(range);
            if (range.getStartOffset() <= 0) {
                return false;
            }
            if (s.equals(",")) {
                return true;
            } else if (Pattern.compile("\\s+").matcher(s).matches()) {
                range = new TextRange(range.getStartOffset() - 1, range.getStartOffset());
            } else {
                return false;
            }
        }
    }

    private String getDefaultValue(String name) {
        return defValues.get(name);
    }
}
