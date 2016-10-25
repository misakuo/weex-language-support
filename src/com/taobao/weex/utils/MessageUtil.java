package com.taobao.weex.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * Created by moxun on 16/10/24.
 */
public class MessageUtil {
    public static void showTopic(Project project, String title, String content, NotificationType type) {
        project.getMessageBus().syncPublisher(Notifications.TOPIC).notify(
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID,
                        title,
                        content,
                        type));
    }
}
