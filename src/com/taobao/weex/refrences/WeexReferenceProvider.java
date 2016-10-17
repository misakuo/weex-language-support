package com.taobao.weex.refrences;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Created by moxun on 16/10/12.
 */
public class WeexReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

        if (!WeexFileUtil.isOnWeexFile(psiElement)) {
            return new PsiReference[0];
        }

        String text = psiElement.getText().replaceAll("\"+", "");
        if (Pattern.compile("\\{\\{.*\\}\\}").matcher(text).matches() && text.length() > 4) {
            String valueType = "var";
            if (psiElement.getParent() != null && psiElement.getParent().getParent() != null) {
                PsiElement tag = psiElement.getParent().getParent();
                String attr = null;
                if (psiElement.getContext() != null) {
                    attr = ((XmlAttribute) psiElement.getContext()).getName();
                }
                if (attr != null && tag instanceof XmlTag) {
                    String tagName = ((XmlTag) tag).getName();
                    WeexTag weexTag = DirectiveLint.getWeexTag(tagName);
                    if (weexTag != null && weexTag.getAttribute(attr) != null) {
                        valueType = weexTag.getAttribute(attr).valueType;
                    }
                }
            }
            return new PsiReference[]{new MustacheVarReference(psiElement, valueType.toLowerCase())};
        }
        return new PsiReference[0];
    }
}
