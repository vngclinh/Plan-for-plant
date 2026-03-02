package com.example.planforplant.ui;

import com.example.planforplant.DTO.NotificationResponse;

public class NotificationListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public final int type;
    public final String headerText;          // dùng cho header
    public final NotificationResponse noti;  // dùng cho item

    private NotificationListItem(int type, String headerText, NotificationResponse noti) {
        this.type = type;
        this.headerText = headerText;
        this.noti = noti;
    }

    public static NotificationListItem header(String text) {
        return new NotificationListItem(TYPE_HEADER, text, null);
    }

    public static NotificationListItem item(NotificationResponse noti) {
        return new NotificationListItem(TYPE_ITEM, null, noti);
    }
}
