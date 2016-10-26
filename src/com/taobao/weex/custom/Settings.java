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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.ui.table.JBTable;
import com.taobao.weex.lint.DirectiveLint;
import com.taobao.weex.lint.WeexTag;
import com.taobao.weex.utils.ExtraModulesUtil;
import com.taobao.weex.utils.Logger;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by moxun on 16/10/17.
 */
public class Settings implements Configurable {
    private JTextField rulesPath;
    private JButton select;
    private JPanel root;
    private JButton reset;
    private JButton add;
    private JButton remove;
    private JScrollPane panel;
    private JLabel message;
    private JButton rebuild;
    private JButton dump;
    private DefaultTableModel model;
    private Vector<Vector> data = new Vector<Vector>();
    private Vector<String> names = new Vector<String>();

    {
        names.add("Name");
        names.add("Path");
    }

    private static final String KEY_RULES_PATH = "RULES_PATH";
    private static final String KEY_GLOBAL_COMPONENTS = "WEEX_GLOBAL_COMPONENTS";

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
        message.setVisible(false);

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

        model = new DefaultTableModel(data, names);
        final JBTable compsList = new JBTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        panel.setViewportView(compsList);

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                VirtualFile vf = FileChooser.chooseFile(descriptor, ProjectUtil.guessCurrentProject(add), null);
                if (vf != null && vf.isDirectory()) {
                    PsiDirectory dir = PsiDirectoryFactory.getInstance(ProjectUtil.guessCurrentProject(add)).createDirectory(vf);
                    if (ExtraModulesUtil.isNodeModule(dir)) {
                        message.setVisible(false);
                        Vector<String> vector = new Vector<String>();
                        vector.add(ExtraModulesUtil.getModuleName(dir));
                        vector.add(dir.getVirtualFile().getCanonicalPath());
                        data.add(vector);
                        model.fireTableDataChanged();
                    } else {
                        message.setVisible(true);
                        message.setText("Directory " + dir.getName() + " is not a node module.");
                    }
                }
            }
        });

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (compsList.getSelectedRow() < 0) {
                    return;
                }
                data.remove(compsList.getSelectedRow());
                model.fireTableDataChanged();
            }
        });

        rebuild.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DirectiveLint.reset();
            }
        });

        dump.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Logger.warn("======== Begin to debug info ========");
                DirectiveLint.dump();
                Logger.warn("========  End to debug info  ========");
            }
        });

        loadPaths();

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
        savePaths();
    }

    private void savePaths() {
        List<String> items = new ArrayList<String>();
        for (Vector vector : data) {
            if (vector.size() == 2) {
                String var = vector.get(0).toString() + "#@#" + vector.get(1).toString();
                items.add(var);
            }
        }
        PropertiesComponent.getInstance().setValues(KEY_GLOBAL_COMPONENTS, items.toArray(new String[items.size()]));
    }

    private void loadPaths() {
        data.clear();
        String[] paths = PropertiesComponent.getInstance().getValues(KEY_GLOBAL_COMPONENTS);
        if (paths == null) {
            return;
        }
        for (String s : paths) {
            String[] temp = s.split("#@#");
            if (temp.length == 2) {
                Vector<String> vector = new Vector<String>();
                vector.add(temp[0]);
                vector.add(temp[1]);
                data.add(vector);
            }
        }
        model.fireTableDataChanged();
    }

    public static List<String> getGlobalModules() {
        List<String> result = new ArrayList<String>();
        String[] paths = PropertiesComponent.getInstance().getValues(KEY_GLOBAL_COMPONENTS);
        if (paths == null) {
            return result;
        }
        for (String s : paths) {
            String[] temp = s.split("#@#");
            if (temp.length == 2) {
                result.add(temp[1]);
            }
        }
        return result;
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
