package com.deven.virtualchips;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {
    private static FirebaseFirestore db;

    public static FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }
}
