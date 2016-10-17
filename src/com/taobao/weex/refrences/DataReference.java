package com.taobao.weex.refrences;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlTag;
import com.taobao.weex.WeexIcons;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moxun on 16/10/14.
 */
public class DataReference extends PsiReferenceBase<XmlTag> {

    private String varName;
    private PsiElement element;
    private TextRange rangeInValue;
    private int offset;

    public DataReference(XmlTag element, TextRange range, String varName) {
        super(element);
        this.rangeInValue = range;
        this.varName = varName;
        this.element = element;
        offset = getRangeInElement().getStartOffset();
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        setRangeInElement(new TextRange(offset + rangeInValue.getStartOffset(),
                offset + rangeInValue.getEndOffset()));
        return WeexFileUtil.getVarDeclaration(element, varName);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> variants = new ArrayList<LookupElement>();
        for (String s : WeexFileUtil.getAllVarNames(element).keySet()) {
            variants.add(LookupElementBuilder.create(s).
                    withIcon(WeexIcons.TYPE).
                    withTypeText(WeexFileUtil.getAllVarNames(element).get(s))
            );
        }
        return variants.toArray();
    }
}
