package com.taobao.weex.custom;

import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by moxun on 16/10/17.
 */
public class Settings implements Configurable {
    private JTextField rulesPath;
    private JButton select;
    private JPanel root;
    private JButton reset;

    private static final String KEY_RULES_PATH = "RULES_PATH";

    @Nls
    @Override
    public String getDisplayName() {
        return "Weex language support";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "";
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = FileChooserDescriptorFactory
                        .createSingleFileDescriptor(JsonFileType.INSTANCE);

                VirtualFile virtualFile = FileChooser.chooseFile(descriptor,
                        ProjectUtil.guessCurrentProject(select),
                        null);
                if (virtualFile != null && !virtualFile.isDirectory()) {
                    rulesPath.setText(virtualFile.getCanonicalPath());
                }
            }
        });

        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rulesPath.setText("");
            }
        });

        rulesPath.setText(PropertiesComponent.getInstance().getValue(KEY_RULES_PATH, ""));

        return root;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        try {
            PropertiesComponent.getInstance().setValue(KEY_RULES_PATH, rulesPath.getText());
            if (!TextUtils.isEmpty(rulesPath.getText())) {
                load(rulesPath.getText());
                DirectiveLint.prepare();
            } else {
                DirectiveLint.reset();
            }
        } catch (Exception e) {
            ProjectUtil.guessCurrentProject(select).getMessageBus().syncPublisher(Notifications.TOPIC).notify(
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID,
                            "Weex language support - bad rules",
                            e.toString(),
                            NotificationType.ERROR));
        }
    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }

    private static WeexTag[] load(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        return new Gson().fromJson(new InputStreamReader(is), WeexTag[].class);
    }

    public static WeexTag[] getRules() {
        String path = PropertiesComponent.getInstance().getValue(KEY_RULES_PATH, "");
        if (!TextUtils.isEmpty(path)) {
            try {
                return load(path);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
