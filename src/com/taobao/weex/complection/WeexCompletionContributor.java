package com.taobao.weex.complection;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.taobao.weex.WeexIcons;
import com.taobao.weex.utils.CodeUtil;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by moxun on 16/10/13.
 */
public class WeexCompletionContributor extends CompletionContributor {
    public WeexCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PsiElement.class),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {

                        if (!WeexFileUtil.isOnWeexFile(completionParameters.getPosition())) {
                            return;
                        }

                        if (completionParameters.getPosition().getContext() instanceof XmlAttributeValue) {
                            final XmlAttributeValue value = (XmlAttributeValue) completionParameters.getPosition().getContext();
                            if (WeexFileUtil.getValueType(value).equals("function")) {
                                for (String s : WeexFileUtil.getAllFunctionNames(value)) {
                                    resultSet.addElement(LookupElementBuilder.create("{{" + s + "}}")
                                            .withLookupString(s)
                                            .withIcon(WeexIcons.TYPE)
                                            .withInsertHandler(new InsertHandler<LookupElement>() {
                                                @Override
                                                public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
                                                    performInsert(value, insertionContext, lookupElement);
                                                }
                                            })
                                            .withBoldness(true)
                                            .withTypeText("Function"));
                                }
                            } else {
                                Map<String, String> vars = WeexFileUtil.getAllVarNames(value);
                                for (String s : vars.keySet()) {
                                    String typeText = vars.get(s);
                                    String vType = WeexFileUtil.getValueType(value);
                                    if (!WeexFileUtil.hasSameType(vType, vars.get(s))) {
                                        JSProperty property = WeexFileUtil.getVarDeclaration(completionParameters.getPosition(), s);
                                        if (property == null || property.getValue() == null) {
                                            continue;
                                        } else {
                                            String v = property.getValue().getText();
                                            String guessedType = CodeUtil.guessStringType(v);
                                            if (!WeexFileUtil.hasSameType(vType, guessedType)) {
                                                continue;
                                            } else {
                                                typeText = guessedType + " string";
                                            }
                                        }
                                    }
                                    resultSet.addElement(LookupElementBuilder.create("{{" + s + "}}")
                                            .withLookupString(s)
                                            .withIcon(WeexIcons.TYPE)
                                            .withInsertHandler(new InsertHandler<LookupElement>() {
                                                @Override
                                                public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
                                                    performInsert(value, insertionContext, lookupElement);
                                                }
                                            })
                                            .withBoldness(true)
                                            .withTypeText(typeText));

                                }
                            }
                        }
                    }
                });
    }

    private void performInsert(XmlAttributeValue value, InsertionContext insertionContext, LookupElement lookupElement) {
        if (value.getText().startsWith("\"")) {
            insertionContext.getDocument().replaceString(
                    value.getTextOffset(),
                    value.getTextOffset() + getTailLength(value) + lookupElement.getLookupString().length(),
                    lookupElement.getLookupString());
        } else {
            insertionContext.getDocument().replaceString(
                    value.getTextOffset() - 1,
                    value.getTextOffset() + getTailLength(value) + lookupElement.getLookupString().length() - 1,
                    "\"" + lookupElement.getLookupString() + "\"");
        }
    }

    private int getTailLength(XmlAttributeValue value) {
        String[] temp = value.getValue().split(CompletionInitializationContext.DUMMY_IDENTIFIER);
        if (temp.length == 2) {
            return temp[1].length();
        }
        return 0;
    }
}
