package com.example.gatherforgood;

import java.util.ArrayList;

public class MockDataHelper {
    public static ArrayList<PrayerGathering> getHardcodedGatherings() {
        ArrayList<PrayerGathering> list = new ArrayList<>();

        list.add(new PrayerGathering("1", "uid1", "Ahmed Khan", "Fajr",
                "2025-01-20", "5:15 AM", "Community Center Hall A",
                31.5204, 74.3587, "All", "Upcoming", 12));

        list.add(new PrayerGathering("2", "uid2", "Fatima Zahra", "Dhuhr",
                "2025-01-20", "1:30 PM", "South Mosque Library",
                31.5100, 74.3400, "Sisters Only", "Upcoming", 6));

        list.add(new PrayerGathering("3", "uid3", "Omar Hassan", "Asr",
                "2025-01-20", "4:00 PM", "University Prayer Room",
                31.5300, 74.3700, "Brothers Only", "Upcoming", 20));

        list.add(new PrayerGathering("4", "uid4", "Yusuf Ali", "Maghrib",
                "2025-01-20", "6:45 PM", "Green Valley Mosque",
                31.5150, 74.3500, "All", "Ongoing", 8));

        return list;
    }
}
