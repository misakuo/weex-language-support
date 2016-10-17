package com.taobao.weex.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.taobao.weex.lint.DirectiveLint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Created by moxun on 16/10/12.
 */
public class WeexTagNameProvider implements XmlTagNameProvider, XmlElementDescriptorProvider {
    @Override
    public void addTagNameVariants(List<LookupElement> list, @NotNull XmlTag xmlTag, String prefix) {
        if (!(xmlTag instanceof HtmlTag)) {
            return;
        }

        Set<String> tags = DirectiveLint.getWeexTagNames();

        for (String s : tags) {
            LookupElement element = LookupElementBuilder.create(s).withInsertHandler(XmlTagInsertHandler.INSTANCE);
            list.add(element);
        }
    }

    @Nullable
    @Override
    public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
        Set<String> tags = DirectiveLint.getWeexTagNames();
        List<String> htmlTags = DirectiveLint.getHtmlTags();
        if (tags.contains(xmlTag.getName()) && !htmlTags.contains(xmlTag.getName())) {
            return new WeexTagDescriptor(xmlTag.getName(), null);
        }
        return null;
    }
}
