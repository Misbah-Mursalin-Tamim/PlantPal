package com.plantpal.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Keeps a copy of the user's plants in Firestore: users/{uid}/plants/{plantId}
public class CloudSync {

    private static final String TAG = "PlantPalCloud";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static CollectionReference plantsRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        return FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid()).collection("plants");
    }

    // Add or update one plant in the cloud
    public static void upload(MyPlant p) {
        CollectionReference ref = plantsRef();
        if (ref == null) return;
        Map<String, Object> m = new HashMap<>();
        m.put("name", p.name);
        m.put("wateringText", p.wateringText);
        m.put("intervalDays", p.intervalDays);
        m.put("sunlight", p.sunlight);
        m.put("lastWateredMillis", p.lastWateredMillis);
        ref.document(p.id).set(m)
                .addOnFailureListener(e -> Log.w(TAG, "Upload failed: " + e));
    }

    public static void delete(String plantId) {
        CollectionReference ref = plantsRef();
        if (ref == null) return;
        ref.document(plantId).delete()
                .addOnFailureListener(e -> Log.w(TAG, "Delete failed: " + e));
    }

    // Called when the main screen opens:
    // - phone has plants: push them to the cloud (keeps both in step)
    // - phone has none (fresh install): pull the plants back from the cloud
    public static void syncOnStart(Context context, Runnable onRestored) {
        final Context app = context.getApplicationContext();
        AppDatabase.IO.execute(() -> {
            List<MyPlant> local = AppDatabase.getInstance(app).myPlantDao().getAll();
            if (!local.isEmpty()) {
                for (MyPlant p : local) upload(p);
                return;
            }

            CollectionReference ref = plantsRef();
            if (ref == null) return;
            ref.get().addOnSuccessListener(snap -> {
                List<MyPlant> restored = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    MyPlant p = fromDoc(d);
                    if (p != null) restored.add(p);
                }
                if (restored.isEmpty()) return;
                AppDatabase.IO.execute(() -> {
                    AppDatabase.getInstance(app).myPlantDao().insertAll(restored);
                    MAIN.post(onRestored);
                });
            }).addOnFailureListener(e -> Log.w(TAG, "Restore failed: " + e));
        });
    }

    private static MyPlant fromDoc(DocumentSnapshot d) {
        String name = d.getString("name");
        if (name == null) return null;
        MyPlant p = new MyPlant();
        p.id = d.getId();
        p.name = name;
        p.wateringText = d.getString("wateringText");
        Long interval = d.getLong("intervalDays");
        p.intervalDays = interval == null ? 7 : interval.intValue();
        p.sunlight = d.getString("sunlight");
        Long last = d.getLong("lastWateredMillis");
        p.lastWateredMillis = last == null ? System.currentTimeMillis() : last;
        return p;
    }
}