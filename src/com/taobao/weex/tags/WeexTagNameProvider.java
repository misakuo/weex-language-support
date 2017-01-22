package com.taobao.weex.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.taobao.weex.WeexIcons;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;
import com.taobao.weex.utils.ArchiveUtil;
import com.taobao.weex.utils.WeexFileUtil;
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

        if (!WeexFileUtil.isOnWeexFile(xmlTag)) {
            return;
        }

        if (!(xmlTag instanceof HtmlTag)) {
            return;
        }

        Set<String> tags = DirectiveLint.getWeexTagNames();

        for (String s : tags) {
            LookupElement element = LookupElementBuilder
                    .create(s)
                    .withInsertHandler(XmlTagInsertHandler.INSTANCE)
                    .withBoldness(true)
                    .withIcon(WeexIcons.TYPE)
                    .withTypeText("weex component");
            list.add(element);
        }

        List<WeexTag> contextTags = ContextTagResolver.resolve(xmlTag);
        for (WeexTag tag : contextTags) {
            LookupElement element = LookupElementBuilder
                    .create(tag.tag)
                    .withInsertHandler(XmlTagInsertHandler.INSTANCE)
                    .withIcon(WeexIcons.TYPE)
                    .withTypeText("local component");
            list.add(element);
        }
    }

    @Nullable
    @Override
    public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {

        if (!WeexFileUtil.isOnWeexFile(xmlTag)) {
            return null;
        }

        Set<String> tags = DirectiveLint.getWeexTagNames();
        List<String> htmlTags = DirectiveLint.getHtmlTags();
        if (tags.contains(xmlTag.getName()) && !htmlTags.contains(xmlTag.getName())) {
            PsiFile declare = null;
            WeexTag tag = DirectiveLint.getWeexTag(xmlTag.getName());
            if (tag != null) {
                declare = tag.declare;
            }
            if (declare == null) {

                VirtualFile virtualFile = ArchiveUtil.getFileFromArchive("constants/weex-built-in-components.xml");
                if (virtualFile != null) {
                    declare = PsiManager.getInstance(xmlTag.getProject()).findFile(virtualFile);
                }
            }
            if (declare == null) {
                declare = xmlTag.getContainingFile();
            }
            return new WeexTagDescriptor(xmlTag.getName(), declare);
        }

        List<WeexTag> contextTags = ContextTagResolver.resolve(xmlTag);
        for (WeexTag tag : contextTags) {
            if (xmlTag.getName().equals(tag.tag)) {
                return new WeexTagDescriptor(tag.tag, tag.declare);
            }
        }

        return new WeexTagDescriptor(xmlTag.getName(), xmlTag.getContainingFile());
    }
}
