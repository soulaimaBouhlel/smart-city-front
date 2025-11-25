package com.smartcity.service.decorator;

import com.smartcity.model.Notification;
import com.smartcity.service.INotificationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingDecorator extends NotificationServiceDecorator {

    public LoggingDecorator(INotificationService wrappee) {
        super(wrappee);
    }

    @Override
    public void sendNotification(Notification notification) {
        super.sendNotification(notification);
        // TODO: Implement logging functionality
    }
}
