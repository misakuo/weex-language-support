package com.taobao.weex.utils;

import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.xml.*;
import com.taobao.weex.lint.Attribute;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by moxun on 16/10/13.
 */
public class WeexFileUtil {

    private static String currentFileName = "";
    private static JSObjectLiteralExpression cachedExportsStatement;
    private static Map<String, String> vars = new ConcurrentHashMap<String, String>();
    private static Set<String> functions = new HashSet<String>();

    public static boolean isOnWeexFile(PsiElement element) {
        if (element.getContainingFile() != null) {
            return element.getContainingFile().getName().toLowerCase().endsWith(".we");
        } else {
            return false;
        }
    }

    public static String getValueType(XmlAttributeValue value) {
        if (value.getContext() != null && value.getContext() instanceof XmlAttribute) {
            String attrName = ((XmlAttribute) value.getContext()).getName();
            if (value.getContext().getContext() != null && value.getContext().getContext() instanceof XmlTag) {
                String tagName = ((XmlTag) value.getContext().getContext()).getName();
                WeexTag tag = DirectiveLint.getWeexTag(tagName);
                if (tag != null) {
                    Attribute attribute = tag.getAttribute(attrName);
                    if (attribute != null) {
                        return attribute.valueType;
                    }
                }
            }
        }
        return "var";
    }

    public static String getJSPropertyType(JSProperty jsProperty) {
        JSType t = jsProperty.getType();
        String typeString = "var";
        if (t == null) {
            if (jsProperty.getValue() instanceof JSObjectLiteralExpression) {
                typeString = "Object";
            }
        } else {
            typeString = t.getResolvedTypeText();
        }
        return typeString;
    }

    public static boolean hasSameType(XmlAttributeValue value, JSProperty property) {
        String valueType = getValueType(value);
        String JsType = getJSPropertyType(property);
        return hasSameType(valueType, JsType);
    }

    public static boolean hasSameType(String value, String property) {
        if (value.toLowerCase().equals(property.toLowerCase())) {
            return true;
        }
        if (value.equals("var")) {
            return true;
        }
        return false;
    }

    private static void ensureFile(PsiElement element) {
        String path = String.valueOf(System.currentTimeMillis());
        if (element != null
                && element.getContainingFile() != null
                && element.getContainingFile().getVirtualFile() != null) {
            path = element.getContainingFile().getVirtualFile().getPath();
        }
        if (!currentFileName.equals(path)) {
            cachedExportsStatement = null;
            vars.clear();
            functions.clear();
            currentFileName = path;
        }
    }

    public static JSObjectLiteralExpression getExportsStatement(PsiElement anyElementOnWeexScript) {
        ensureFile(anyElementOnWeexScript);

        if (isValid(cachedExportsStatement)) {
            return cachedExportsStatement;
        }

        //WELCOME TO HELL!!!
        PsiFile file = anyElementOnWeexScript.getContainingFile();
        if (file instanceof XmlFile) {
            XmlDocument document = ((XmlFile) file).getDocument();
            if (document != null) {
                for (PsiElement e : document.getChildren()) {
                    if (e instanceof XmlTag) {
                        if ("script".equals(((XmlTag) e).getName())) {
                            for (PsiElement e1 : e.getChildren()) {
                                if (e1 instanceof JSEmbeddedContent) {
                                    for (PsiElement e2 : e1.getChildren()) {
                                        if (e2 instanceof JSExpressionStatement) {
                                            for (PsiElement e3 : e2.getChildren()) {
                                                if (e3 instanceof JSAssignmentExpression) {
                                                    PsiElement[] children = e3.getChildren();
                                                    if (children.length == 2) {
                                                        if (children[0].getText().equals("module.exports")) {
                                                            if (children[1] instanceof JSObjectLiteralExpression) {
                                                                cachedExportsStatement = (JSObjectLiteralExpression) children[1];
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
                    }
                }
            }
        }
        return cachedExportsStatement;
    }

    private static boolean isValid(JSObjectLiteralExpression expression) {
        if (expression == null) {
            return false;
        }
        try {
            PsiFile file = expression.getContainingFile();
            if (file == null) {
                return false;
            }
        } catch (PsiInvalidElementAccessException e) {
            return false;
        }
        JSProperty data = expression.findProperty("data");
        if (data == null || data.getValue() == null) {
            return false;
        }
        return true;
    }

    public static Map<String, String> getAllVarNames(PsiElement any) {
        ensureFile(any);
        getVarDeclaration(any, String.valueOf(System.currentTimeMillis()));
        return vars;
    }

    public static Set<String> getAllFunctionNames(PsiElement any) {
        ensureFile(any);
        getFunctionDeclaration(any, String.valueOf(System.currentTimeMillis()));
        return functions;
    }

    public static JSProperty getVarDeclaration(PsiElement anyElementOnWeexScript, String valueName) {
        valueName = CodeUtil.getVarNameFromMustache(valueName);
        JSObjectLiteralExpression exports = getExportsStatement(anyElementOnWeexScript);
        vars.clear();
        JSProperty ret = null;
        if (exports != null) {
            try {
                PsiFile file = exports.getContainingFile();
                if (file == null) {
                    return null;
                }
            } catch (PsiInvalidElementAccessException e) {
                return null;
            }
            JSProperty data = exports.findProperty("data");
            if (data == null || data.getValue() == null) {
                return null;
            }
            for (PsiElement pe : data.getValue().getChildren()) {
                if (pe instanceof JSProperty) {
                    String varName = ((JSProperty) pe).getName();
                    String varValue = getJSPropertyType((JSProperty) pe);
                    if (varName != null && varValue != null) {
                        vars.put(varName, varValue);
                    }
                    if (valueName.equals(varName)) {
                        ret = (JSProperty) pe;
                    }
                }

            }
        }
        return ret;
    }

    public static JSFunctionExpression getFunctionDeclaration(PsiElement anyElementOnWeexScript, String valueName) {
        valueName = CodeUtil.getFunctionNameFromMustache(valueName);
        JSObjectLiteralExpression exports = getExportsStatement(anyElementOnWeexScript);
        functions.clear();
        JSFunctionExpression ret = null;
        if (exports != null) {
            try {
                PsiFile file = exports.getContainingFile();
                if (file == null) {
                    return null;
                }
            } catch (PsiInvalidElementAccessException e) {
                return null;
            }
            JSProperty data = exports.findProperty("methods");
            if (data != null && data.getValue() != null) {
                for (PsiElement e : data.getValue().getChildren()) {
                    if (e instanceof JSProperty) {
                        for (PsiElement e1 : e.getChildren()) {
                            if (e1 instanceof JSFunctionExpression) {
                                functions.add(((JSFunctionExpression) e1).getName());
                                if (valueName.equals(((JSFunctionExpression) e1).getName())) {
                                    ret = (JSFunctionExpression) e1;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static int getExportsEndOffset(PsiElement anyElementOnWeexScript, String name) {
        JSObjectLiteralExpression exports = getExportsStatement(anyElementOnWeexScript);
        if (exports != null) {
            try {
                PsiFile file = exports.getContainingFile();
                if (file == null) {
                    return -1;
                }
            } catch (PsiInvalidElementAccessException e) {
                return -1;
            }
            JSProperty data = exports.findProperty(name);
            if (data == null || data.getValue() == null) {
                return -1;
            }
            return data.getValue().getTextRange().getEndOffset() - 1;
        }
        return -1;
    }

    public static boolean isMustacheValue(String value) {
        if (value == null) {
            return false;
        }
        return Pattern.compile("\\{\\{.+?\\}\\}").matcher(value).matches();
    }

    public static boolean containsMustacheValue(String value) {
        if (value == null) {
            return false;
        }
        return Pattern.compile(".*\\{\\{.+?\\}\\}.*").matcher(value).matches();
    }

    public static Map<String, TextRange> getVars(String src) {
        Map<String, TextRange> results = new IdentityHashMap<String, TextRange>();
        Pattern p = Pattern.compile("\\{\\{.+?\\}\\}");
        Matcher m = p.matcher(src);
        while (m.find()) {
            String g = m.group().replaceAll("\\{+", "").replaceAll("\\}+", "").trim();
            TextRange textRange = new TextRange(m.start(), m.end());
            results.put(g, textRange);
        }
        return results;
    }
}
