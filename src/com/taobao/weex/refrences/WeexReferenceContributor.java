package com.taobao.weex.refrences;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by moxun on 16/10/12.
 */
public class WeexReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(XmlAttributeValue.class).withLanguage(HTMLLanguage.INSTANCE), new WeexReferenceProvider());

        psiReferenceRegistrar.registerReferenceProvider(
                XmlPatterns.xmlTag().withLanguage(HTMLLanguage.INSTANCE)
                        .andNot(XmlPatterns.xmlTag().withLocalName("script"))
                        .andNot(XmlPatterns.xmlTag().withLocalName("style")),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                        if (psiElement instanceof XmlTag) {
                            if (((XmlTag) psiElement).getSubTags().length == 0) {
                                String text = ((XmlTag) psiElement).getValue().getText();
                                if (WeexFileUtil.containsMustacheValue(text)) {
                                    List<PsiReference> references = new ArrayList<PsiReference>();
                                    Map<String, TextRange> vars = WeexFileUtil.getVars(text);
                                    for (Map.Entry<String, TextRange> entry : vars.entrySet()) {
                                        if (WeexFileUtil.getAllVarNames(psiElement).keySet().contains(entry.getKey())) {
                                            references.add(new DataReference((XmlTag) psiElement, entry.getValue(), entry.getKey()));
                                        }
                                    }
                                    return references.toArray(new PsiReference[references.size()]);
                                }
                            }
                        }
                        return new PsiReference[0];
                    }
                }
        );
    }
}
