package com.example.projek_mobile.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static String getTimeAgo(String publishedAt) {
        if (publishedAt == null || publishedAt.isEmpty()) {
            return "Unknown time";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setLenient(false);

        try {
            Date newsDate = sdf.parse(publishedAt);
            long diffInMillis = new Date().getTime() - newsDate.getTime();

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (hours < 24) {
                return hours + " hours ago";
            } else if (days < 3) {
                return days + " day" + (days == 1 ? "" : "s") + " ago";
            } else {
                // Format tanggal jika sudah lewat 3 hari
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(newsDate);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }
}
