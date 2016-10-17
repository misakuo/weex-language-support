package com.taobao.weex.attributes;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by moxun on 16/10/11.
 */
public class WeexAttrDescriptor extends BasicXmlAttributeDescriptor implements XmlAttributeDescriptorEx {

    private final String attributeName;
    private final List<String> enumValue;
    private final StubIndexKey<String, JSImplicitElementProvider> index;

    public WeexAttrDescriptor(String attributeName, List<String> enumValue, final StubIndexKey<String, JSImplicitElementProvider> index) {
        this.enumValue = enumValue;
        this.attributeName = attributeName;
        this.index = index;
    }


    @Nullable
    @Override
    public String handleTargetRename(@NotNull @NonNls String newTargetName) {
        return newTargetName;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean hasIdType() {
        return false;
    }

    @Override
    public boolean hasIdRefType() {
        return false;
    }

    @Override
    public boolean isEnumerated() {
        return index != null;
    }

    @Override
    public PsiElement getDeclaration() {
        return null;
    }

    @Override
    public String getName() {
        return attributeName;
    }

    @Override
    public void init(PsiElement element) {

    }

    @Override
    public Object[] getDependences() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String[] getEnumeratedValues() {
        if (enumValue == null) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
        }
        return enumValue.toArray(new String[enumValue.size()]);
    }
}
