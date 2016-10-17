package com.taobao.weex.attributes;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by moxun on 16/10/11.
 */
public class WeexAttrDescriptorProvider implements XmlAttributeDescriptorsProvider {
    @Override
    public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
        if (!WeexFileUtil.isOnWeexFile(xmlTag)) {
            return new XmlAttributeDescriptor[0];
        }
        final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
        WeexTag tag = DirectiveLint.getWeexTag(xmlTag.getName());
        if (tag == null) {
            return new XmlAttributeDescriptor[0];
        }
        for (String attributeName : tag.getExtAttrs()) {
            result.put(attributeName, new WeexAttrDescriptor(attributeName,
                    tag.getAttribute(attributeName).valueEnum,
                    null));
        }
        return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }

    @Nullable
    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String s, XmlTag xmlTag) {
        if (!WeexFileUtil.isOnWeexFile(xmlTag)) {
            return null;
        }
        WeexTag tag = DirectiveLint.getWeexTag(xmlTag.getName());
        if (tag == null) {
            return null;
        }
        if (tag.getExtAttrs().contains(s)) {
            return new WeexAttrDescriptor(s, tag.getAttribute(s).valueEnum, null);
        }
        return null;
    }
}
