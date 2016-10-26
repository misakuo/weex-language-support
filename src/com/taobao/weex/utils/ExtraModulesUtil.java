package com.taobao.weex.utils;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.taobao.weex.custom.Settings;
import com.taobao.weex.lint.Attribute;
import com.taobao.weex.lint.WeexTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by moxun on 16/10/24.
 */
public class ExtraModulesUtil {
    public static List<WeexTag> getTagsFromNodeModules() {
        List<WeexTag> list = new ArrayList<WeexTag>();
        Project project = ProjectUtil.guessCurrentProject(null);
        Logger.debug(project.toString());
        VirtualFile vf = project.getBaseDir();
        PsiDirectory[] localModules = new PsiDirectory[0];
        if (vf != null && vf.isDirectory()) {
            Logger.debug("Project root dir: " + vf.toString());
            PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(vf);
            localModules = getNodeModules(dir);
            for (PsiDirectory directory : localModules) {
                List<PsiFile> comps = getComponents(directory, getMain(directory));
                for (PsiFile comp : comps) {
                    list.add(parseToTag(comp));
                }
            }
        } else {
            Logger.debug("Project base dir is null");
        }

        for (PsiDirectory dir : getGlobalModules(localModules)) {
            List<PsiFile> comps = getComponents(dir, getMain(dir));
            Logger.debug(comps.toString());
            for (PsiFile comp : comps) {
                list.add(parseToTag(comp));
            }
        }

        return list;
    }

    private static List<PsiDirectory> getGlobalModules(PsiDirectory[] localModules) {
        if (localModules == null) {
            localModules = new PsiDirectory[0];
        }
        List<PsiDirectory> result = new ArrayList<PsiDirectory>();
        List<String> globals = Settings.getGlobalModules();
        List<String> locals = new ArrayList<String>();
        for (PsiDirectory dir : localModules) {
            if (dir != null) {
                locals.add(dir.getVirtualFile().getCanonicalPath());
            }
        }
        for (String global : globals) {
            if (!locals.contains(global)) {
                VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(global);
                if (vf != null && vf.isDirectory()) {
                    PsiDirectory dir = PsiDirectoryFactory.getInstance(ProjectUtil.guessCurrentProject(null)).createDirectory(vf);
                    if (dir != null) {
                        result.add(dir);
                    }
                }
            } else {
                Logger.info("Module " + global + " already exists locally, skip it.");
            }
        }
        Logger.debug(result.toString());
        return result;
    }

    private static WeexTag parseToTag(PsiFile comp) {
        WeexTag weexTag = new WeexTag();
        weexTag.tag = comp.getContainingFile().getName().replace(".we", "");
        weexTag.attrs = new CopyOnWriteArrayList<Attribute>();
        weexTag.declare = comp;
        Map<String, String> vars = WeexFileUtil.getAllVarNames(comp);
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            Attribute attribute = new Attribute();
            attribute.name = convertAttrName(entry.getKey());
            attribute.valueType = getType(entry.getValue());
            weexTag.attrs.add(attribute);
        }
        return weexTag;
    }

    private static String convertAttrName(String name) {
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c: chars) {
            if (Character.isUpperCase(c)) {
                if (sb.length() == 0) {
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append('-').append(Character.toLowerCase(c));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String getType(String realType) {
        realType = realType.toLowerCase();
        if ("number".equals(realType) || "boolean".equals(realType) || "array".equals(realType)) {
            return realType.toLowerCase();
        }
        return "var";
    }

    private static PsiDirectory[] getNodeModules(PsiDirectory root) {
        PsiDirectory node_modules = root.findSubdirectory("node_modules");
        if (node_modules != null) {
            return node_modules.getSubdirectories();
        }
        return new PsiDirectory[0];
    }

    private static PsiFile getMain(PsiDirectory moduleRoot) {
        PsiFile pkg = moduleRoot.findFile("package.json");
        if (pkg != null && pkg instanceof JsonFile) {
            if (((JsonFile) pkg).getTopLevelValue() instanceof JsonObject) {
                JsonObject object = (JsonObject) ((JsonFile) pkg).getTopLevelValue();
                if (object != null) {
                    JsonProperty property = object.findProperty("main");
                    if (property != null && property.getValue() != null && property.getValue() instanceof JsonStringLiteral) {
                        JsonStringLiteral propValue = (JsonStringLiteral) property.getValue();
                        String value = propValue.getValue();
                        PsiFile psiFile = moduleRoot.findFile(value.replace("./", ""));
                        return psiFile;
                    }
                }
            }
        }
        return null;
    }

    private static List<PsiFile> getComponents(PsiDirectory root, PsiFile file) {
        List<PsiFile> results = new ArrayList<PsiFile>();
        if (file != null && file instanceof JSFile) {
            for (PsiElement element : file.getChildren()) {
                if (element instanceof JSExpressionStatement) {
                    JSExpression expression = ((JSExpressionStatement) element).getExpression();
                    if (expression instanceof JSCallExpression
                            && ((JSCallExpression) expression).getArguments().length == 1
                            && ((JSCallExpression) expression).getArguments()[0] instanceof JSLiteralExpression) {
                        JSLiteralExpression expression1 = (JSLiteralExpression) ((JSCallExpression) expression).getArguments()[0];
                        Object val = expression1.getValue();
                        if (val != null) {
                            String[] temp = val.toString().replace("./", "").split("/");
                            if (temp != null && temp.length > 0) {
                                String fileName = temp[temp.length - 1];
                                if (fileName.toLowerCase().endsWith(".we")) {
                                    PsiDirectory start = root;
                                    for (int i = 0; i < temp.length - 1; i++) {
                                        PsiDirectory dir = start.findSubdirectory(temp[i]);
                                        if (dir != null) {
                                            start = dir;
                                        }
                                    }
                                    PsiFile weexScript = start.findFile(temp[temp.length - 1]);
                                    if (weexScript != null) {
                                        results.add(weexScript);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    public static boolean isNodeModule(PsiDirectory dir) {
        PsiFile pkg = dir.findFile("package.json");
        return pkg != null && pkg instanceof JsonFile;
    }

    public static String getModuleName(PsiDirectory dir) {
        PsiFile pkg = dir.findFile("package.json");
        String name = dir.getName();
        if (pkg != null && pkg instanceof JsonFile) {
            if (((JsonFile) pkg).getTopLevelValue() instanceof JsonObject) {
                JsonObject object = (JsonObject) ((JsonFile) pkg).getTopLevelValue();
                if (object != null) {
                    JsonProperty property = object.findProperty("name");
                    JsonProperty property1 = object.findProperty("version");
                    if (property != null && property.getValue() != null && property.getValue() instanceof JsonStringLiteral) {
                        JsonStringLiteral propValue = (JsonStringLiteral) property.getValue();
                        name = propValue.getValue();
                        if (property1 != null && property1.getValue() != null && property1.getValue() instanceof JsonStringLiteral) {
                            JsonStringLiteral propValue1 = (JsonStringLiteral) property1.getValue();
                            name = name + ":" + propValue1.getValue();
                        }
                    }
                }
            }
        }
        return name;
    }

    public static void main(String[] args) {
        System.out.println(convertAttrName("Foo"));
        System.out.println(convertAttrName("HowAreYou"));
        System.out.println(convertAttrName("areYouOK"));
    }
}
