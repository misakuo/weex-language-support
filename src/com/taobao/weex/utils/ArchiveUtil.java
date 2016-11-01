package com.taobao.weex.utils;

import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by moxun on 16/10/31.
 */
public class ArchiveUtil {
    public static VirtualFile getFileFromArchive(String name) {
        String[] subPath = name.split("/");
        try {
            URL def = ArchiveUtil.class.getClassLoader().getResource("/");
            if (def != null) {
                String path = URLDecoder.decode(def.getPath(), "utf-8").replace("file:", "");
                String[] temp = path.split("!");
                if (temp.length > 1 && path.toLowerCase().contains(".jar")) {
                    path = temp[0];
                }
                VirtualFile root = JarFileSystem.getInstance().findLocalVirtualFileByPath(path);
                if (root == null) {
                    root = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
                }

                VirtualFile target = root;
                for (String s : subPath) {
                    if (target != null) {
                        target = target.findChild(s);
                    }
                }
                return target;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
