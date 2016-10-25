package com.taobao.weex.lint;

import com.intellij.psi.PsiFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: moxun
 * Created by: ModelGenerator on 16/10/12
 */
public class WeexTag {
    public String tag;
    public CopyOnWriteArrayList<Attribute> attrs;
    public List<String> parent;
    public List<String> child;
    public PsiFile declare;

    public Set<String> getExtAttrs() {
        Set<String> ret = new HashSet<String>();
        if (attrs != null) {
            for (Attribute attr : attrs) {
                ret.add(attr.name);
            }
        }
        return ret;
    }

    public Attribute getAttribute(String name) {
        if (attrs != null) {
            for (Attribute attr : attrs) {
                if (name.equals(attr.name)) {
                    return attr;
                }
            }
        }
        return null;
    }
}