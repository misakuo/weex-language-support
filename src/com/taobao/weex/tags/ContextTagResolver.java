package com.taobao.weex.tags;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.taobao.weex.lint.Attribute;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by moxun on 16/10/25.
 */
public class ContextTagResolver {
    public static List<WeexTag> resolve(PsiElement any) {
        List<WeexTag> result = new ArrayList<WeexTag>();
        PsiFile file = any.getContainingFile();
        PsiDirectory directory = file.getOriginalFile().getContainingDirectory();
        PsiFile[] files = directory.getFiles();
        for (PsiFile p : files) {
            if (!p.getName().equals(any.getContainingFile().getName())) {
                WeexTag common = DirectiveLint.getCommonTag();
                if (common != null) {
                    WeexTag fake = new WeexTag();
                    fake.tag = p.getName().replace(".we","");
                    fake.attrs = new CopyOnWriteArrayList<Attribute>(common.attrs);
                    fake.declare = p;
                    result.add(fake);
                }
            }
        }
        return result;
    }
}
