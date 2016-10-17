package com.taobao.weex.tags;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * Created by moxun on 16/10/12.
 */
public class WeexTagDescriptor implements XmlElementDescriptor{
    protected final String name;
    private final PsiFile file;

    public WeexTagDescriptor(String name, PsiFile file) {
        this.name = name;
        this.file = file;
    }

    @Override
    public String getQualifiedName() {
        return name;
    }

    @Override
    public String getDefaultName() {
        return name;
    }

    @Override
    public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
        XmlDocumentImpl xmlDocument = PsiTreeUtil.getParentOfType(context, XmlDocumentImpl.class);
        if (xmlDocument == null) return EMPTY_ARRAY;
        return xmlDocument.getRootTagNSDescriptor().getRootElementsDescriptors(xmlDocument);
    }

    @Override
    public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
        XmlTag parent = contextTag.getParentTag();
        if (parent == null) return null;
        final XmlNSDescriptor descriptor = parent.getNSDescriptor(childTag.getNamespace(), true);
        return descriptor == null ? null : descriptor.getElementDescriptor(childTag);
    }

    @Override
    public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
        XmlAttributeDescriptor[] attributeDescriptors = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context);
        XmlAttributeDescriptor[] customAttributes = new XmlAttributeDescriptor[1];
        customAttributes[0] = new AnyXmlAttributeDescriptor("weex");
        return ArrayUtil.mergeArrays(attributeDescriptors, customAttributes);
    }

    @Nullable
    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
        return getAttributeDescriptor(attribute.getName(), attribute.getParent());
    }

    @Nullable
    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(@NonNls final String attributeName, @Nullable XmlTag context) {
        final XmlAttributeDescriptor descriptor = ContainerUtil.find(getAttributesDescriptors(context), new Condition<XmlAttributeDescriptor>() {
            @Override
            public boolean value(XmlAttributeDescriptor descriptor) {
                return attributeName.equals(descriptor.getName());
            }
        });
        if (descriptor != null) return descriptor;
        return null;
    }

    @Override
    public XmlNSDescriptor getNSDescriptor() {
        return null;
    }

    @Nullable
    @Override
    public XmlElementsGroup getTopGroup() {
        return null;
    }

    @Override
    public int getContentType() {
        return CONTENT_TYPE_ANY;
    }

    @Nullable
    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public PsiFile getDeclaration() {
        return file;
    }

    @Override
    public String getName(PsiElement context) {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init(PsiElement element) {
    }

    @Override
    public Object[] getDependences() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
}
