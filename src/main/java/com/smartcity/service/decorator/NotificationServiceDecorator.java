package com.smartcity.service.decorator;

import com.smartcity.model.Notification;
import com.smartcity.service.INotificationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class NotificationServiceDecorator implements INotificationService {

    protected final INotificationService wrappee;

    @Override
    public void sendNotification(Notification notification) {
        wrappee.sendNotification(notification);
    }
}
