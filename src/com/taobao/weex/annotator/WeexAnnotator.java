package com.taobao.weex.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.*;
import com.taobao.weex.lint.*;
import com.taobao.weex.quickfix.QuickFixAction;
import com.taobao.weex.utils.WeexFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by moxun on 16/10/11.
 */
public class WeexAnnotator implements Annotator {

    private JSEmbeddedContent script;
    private JSObjectLiteralExpression moduleExports;

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!psiElement.getContainingFile().getVirtualFile().getName().toLowerCase().endsWith(".we")) {
            return;
        }
        if (psiElement instanceof XmlDocument) {
            checkStructure(psiElement, annotationHolder);
        }

        if (psiElement instanceof XmlTag && ((XmlTag) psiElement).getName().equals("script")) {
            label:
            for (PsiElement element : psiElement.getChildren()) {
                if (element instanceof JSEmbeddedContent) {
                    script = (JSEmbeddedContent) element;
                    for (PsiElement element1 : script.getChildren()) {
                        if (element1 instanceof JSExpressionStatement) {
                            for (PsiElement element2 : element1.getChildren()) {
                                if (element2 instanceof JSAssignmentExpression) {
                                    PsiElement[] children = element2.getChildren();
                                    if (children.length == 2) {
                                        if (children[0].getText().equals("module.exports")) {
                                            moduleExports = (JSObjectLiteralExpression) children[1];
                                            break label;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkAttributes(XmlTag tag, @NotNull AnnotationHolder annotationHolder) {
        if (tag.getLocalName().toLowerCase().equals("template")) {
            for (XmlTag child : tag.getSubTags()) {
                checkAttributeValue(child, annotationHolder);
            }
        }
    }

    private
    @NotNull
    LintResult verifyDataType(String type, String s) {
        LintResult result = new LintResult();
        if (moduleExports == null) {
            result.setCode(LintResultType.UNRESOLVED_VAR);
            result.setDesc("Unresolved property '" + s + "'");
            return result;
        }
        JSProperty data = moduleExports.findProperty("data");
        if ("function".equals(type.toLowerCase())) {
            return verifyFunction(s);
        }

        if (data == null || data.getValue() == null) {
            result.setCode(LintResultType.UNRESOLVED_VAR);
            result.setDesc("Unresolved property '" + s + "'");
            return result;
        }

        for (PsiElement element : data.getValue().getChildren()) {
            String varName = ((JSProperty) element).getName();
            if (varName == null) {
                result.setCode(LintResultType.UNRESOLVED_VAR);
                result.setDesc("Unresolved property '" + s + "'");
                return result;
            }
            if (varName.equals(s)) {
                String typeString = WeexFileUtil.getJSPropertyType((JSProperty) element);
                if (match(type, typeString)) {
                    result.setCode(LintResultType.PASSED);
                    result.setDesc("Lint passed");
                    return result;
                } else {
                    result.setCode(LintResultType.WRONG_VALUE_TYPE);
                    result.setDesc("Wrong property type. expect " + type + ", found " + typeString);
                    return result;
                }
            }
        }
        result.setCode(LintResultType.UNRESOLVED_VAR);
        result.setDesc("Unresolved property '" + s + "'");
        return result;
    }

    private LintResult verifyFunction(String s) {
        LintResult result = new LintResult();

        if (moduleExports == null) {
            result.setCode(LintResultType.UNRESOLVED_VAR);
            result.setDesc("Unresolved function '" + s + "'");
            return result;
        }
        JSProperty data = moduleExports.findProperty("methods");

        if (data == null || data.getValue() == null) {
            result.setCode(LintResultType.UNRESOLVED_VAR);
            result.setDesc("Unresolved function '" + s + "'");
            return result;
        }

        for (PsiElement e : data.getValue().getChildren()) {
            if (e instanceof JSProperty) {
                for (PsiElement e1 : e.getChildren()) {
                    if (e1 instanceof JSFunctionExpression) {
                        if (s.equals(((JSFunctionExpression) e1).getName())) {
                            result.setCode(LintResultType.PASSED);
                            result.setDesc("Lint passed");
                            return result;
                        }
                    }
                }
            }
        }

        result.setCode(LintResultType.UNRESOLVED_VAR);
        result.setDesc("Unresolved function '" + s + "'");
        return result;
    }

    private LintResult verifyVarAndFunction(String type, String s) {
        LintResult l1 = verifyDataType(type, s);
        if (!l1.passed()) {
            LintResult l2 = verifyFunction(s);
            if (l2.passed()) {
                return l2;
            } else {
                return l1;
            }
        } else {
            return l1;
        }
    }

    private boolean match(String type, String jsType) {
        if (type.equals(jsType.toLowerCase())) {
            return true;
        }
        if (type.equals("var")) {
            return true;
        }
        return false;
    }

    private void checkAttributeValue(XmlTag xmlTag, @NotNull AnnotationHolder annotationHolder) {
        WeexTag tag = DirectiveLint.getWeexTag(xmlTag.getName());
        if (tag != null) {
            //check self attrs

            List<String> parent = tag.parent;
            if (xmlTag.getParentTag() != null) {
                String name = xmlTag.getParentTag().getName();
                if (parent != null && !parent.contains(name)) {
                    annotationHolder.createErrorAnnotation(xmlTag, "Element '" + xmlTag.getName()
                            + "' only allowed " + tag.parent.toString() + " as parent element");
                }
            }

            Set<String> extAttrs = tag.getExtAttrs();
            for (XmlAttribute attr : xmlTag.getAttributes()) {

                String attrName = attr.getName();
                String attrValue = attr.getValue();
                Attribute validAttr = tag.getAttribute(attrName);

                if (WeexFileUtil.isMustacheValue(attrValue)) {
                    String bindVar = attrValue.replaceAll("\\{+", "").replaceAll("\\}+", "");
                    LintResult ret = null;
                    String type = "var";
                    if (validAttr == null) {
                        ret = verifyVarAndFunction("var", bindVar);
                    } else {
                        if (!inRepeat(xmlTag)) {
                            type = validAttr.valueType;
                            ret = verifyDataType(validAttr.valueType, bindVar);
                        }
                    }
                    if (inRepeat(xmlTag)) {
                        //repeat 绑定数组内的数据在lint时可能不存在, 跳过检测
                        ret = new LintResult(LintResultType.PASSED, "Skip repeat tag");
                    }
                    if (!ret.passed()) {
                        Annotation annotation = annotationHolder
                                .createErrorAnnotation(attr.getValueElement(), ret.getDesc());
                        if (ret.getCode() == LintResultType.UNRESOLVED_VAR) {
                            annotation.registerFix(new QuickFixAction(bindVar, type));
                        }
                    }
                }

                if (extAttrs.contains(attrName)) {
                    if (!WeexFileUtil.isMustacheValue(attrValue) && validAttr != null) {
                        //正则匹配
                        if (validAttr.valuePattern != null) {
                            if (!validAttr.match(attrValue)) {
                                annotationHolder.createErrorAnnotation(attr.getValueElement(), "Attribute '" + attr.getName()
                                        + "' only allowed value that match '" + validAttr.valuePattern + "'");
                            }
                        } else {
                            //枚举匹配
                            if (validAttr.valueEnum != null) {
                                if (!validAttr.valueEnum.contains(attr.getValue()) && attr.getValueElement() != null) {
                                    annotationHolder.createErrorAnnotation(attr.getValueElement(), "Attribute '" + attr.getName()
                                            + "' only allowed " + validAttr.valueEnum.toString() + " or mustache template as value");
                                }
                            }
                        }
                    }
                } else {
                    annotationHolder.createInfoAnnotation(attr, "Not weex defined attribute '" + attrName + "'");
                }
            }

            if (xmlTag.getSubTags().length == 0 && !inRepeat(xmlTag)) {
                String value = xmlTag.getValue().getText();
                if (WeexFileUtil.containsMustacheValue(value)) {
                    Map<String, TextRange> vars = WeexFileUtil.getVars(value);
                    for (Map.Entry<String, TextRange> entry : vars.entrySet()) {
                        String bindVar = entry.getKey();
                        LintResult ret = verifyDataType("var", bindVar);
                        if (!ret.passed()) {
                            TextRange base = xmlTag.getValue().getTextRange();
                            TextRange range = new TextRange(base.getStartOffset() + entry.getValue().getStartOffset() + 2,
                                    base.getStartOffset() + entry.getValue().getEndOffset() - 2);
                            Annotation annotator = annotationHolder.
                                    createErrorAnnotation(range, ret.getDesc());
                            if (ret.getCode() == LintResultType.UNRESOLVED_VAR) {
                                annotator.registerFix(new QuickFixAction(bindVar, "var"));
                            }
                        }
                    }
                }
            }

            //check sub tags
            XmlTag[] subTags = xmlTag.getSubTags();
            for (XmlTag t : subTags) {
                List<String> validChild = tag.child;
                if (validChild != null && !validChild.contains(t.getName().toLowerCase())) {
                    annotationHolder.createErrorAnnotation(t, "Element '" + xmlTag.getName()
                            + "' only allowed " + tag.child.toString() + " as child element");
                }
                checkAttributeValue(t, annotationHolder);
            }
        } else {
            //unsupported tag
        }
    }

    private boolean inRepeat(XmlTag tag) {
        if (tag == null) {
            return false;
        }

        if ("template".equals(tag.getName())) {
            return false;
        }

        if (tag.getAttribute("repeat") != null) {
            return true;
        } else {
            return inRepeat(tag.getParentTag());
        }
    }

    private void checkStructure(PsiElement document, @NotNull AnnotationHolder annotationHolder) {
        PsiElement[] children = document.getChildren();
        List<String> acceptedTag = Arrays.asList("template", "script", "style");
        for (PsiElement element : children) {
            if (element instanceof HtmlTag) {
                if (!acceptedTag.contains(((HtmlTag) element).getName().toLowerCase())) {
                    annotationHolder.createErrorAnnotation(element, "Invalid tag '"
                            + ((HtmlTag) element).getName() + "', only the [template, script, style] tags are allowed here.");
                }
                checkAttributes((XmlTag) element, annotationHolder);
            } else {
                if (!(element instanceof PsiWhiteSpace)
                        && !(element instanceof XmlProlog)
                        && !(element instanceof XmlText)) {
                    String s = element.getText();
                    if (s.length() > 20) {
                        s = s.substring(0, 20);
                    }
                    annotationHolder.createErrorAnnotation(element, "Invalid content '" + s +
                            "', only the [template, script, style] tags are allowed here.");
                }
            }
        }
    }
}
