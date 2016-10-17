package com.taobao.weex;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by moxun on 16/10/11.
 */
public class WeexFileType extends LanguageFileType {

    public static final WeexFileType INSTANCE = new WeexFileType();

    private WeexFileType() {
        super(HTMLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Weex script";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Weex language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "we";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return WeexIcons.FILE;
    }
}
