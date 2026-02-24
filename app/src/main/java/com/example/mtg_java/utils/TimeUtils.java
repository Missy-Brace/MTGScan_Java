package com.example.mtg_java.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(String isoDate) {

        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }

        try {
            // For format like: 2026-02-23T15:30:00.000Z
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setLenient(false);

            Date past = sdf.parse(isoDate);
            Date now = new Date();

            long diff = now.getTime() - past.getTime();

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) {
                return seconds + "s ago";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else {
                return days + "d ago";
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}