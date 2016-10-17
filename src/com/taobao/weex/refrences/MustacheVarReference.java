package com.taobao.weex.refrences;

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by moxun on 16/10/12.
 */
public class MustacheVarReference extends PsiReferenceBase<PsiElement> {

    private PsiElement value;
    private String type;

    public MustacheVarReference(PsiElement element, String type) {
        super(element);
        this.value = element;
        this.type = type;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return findDeclaration(value, type);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    private PsiElement findDeclaration(PsiElement element, String type) {

        if (!(element instanceof XmlAttributeValue)) {
            return null;
        }

        JSObjectLiteralExpression exports = WeexFileUtil.getExportsStatement(element);

        if (exports == null) {
            return null;
        } else {
            String valueName = ((XmlAttributeValue) element).getValue().replaceAll("\\{+", "").replaceAll("\\}+", "");
            if ("function".equals(type)) {
                return WeexFileUtil.getFunctionDeclaration(value, valueName);
            } else {
                return WeexFileUtil.getVarDeclaration(value, valueName);
            }
        }
    }
}
