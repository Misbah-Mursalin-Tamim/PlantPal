package com.plantpal.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.plantpal.app.data.AppDatabase;
import com.plantpal.app.data.MyPlant;

import java.util.List;

// Read-only provider that shares "today's watering list".
// URI: content://com.plantpal.app.provider/today
// Columns: _id, name, watered (1 = watered today, 0 = still needs water)
public class PlantProvider extends ContentProvider {

    public static final String AUTHORITY = "com.plantpal.app.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/today");

    private static final int TODAY = 1;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, "today", TODAY);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (MATCHER.match(uri) != TODAY) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context == null) return null;

        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "name", "watered"});
        List<MyPlant> all = AppDatabase.getInstance(context).myPlantDao().getAll();
        int row = 0;
        for (MyPlant p : all) {
            boolean watered = PlantUtils.wateredToday(p);
            if (watered || PlantUtils.isDue(p)) {
                cursor.addRow(new Object[]{row++, p.name, watered ? 1 : 0});
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd.com.plantpal.app.today";
    }

    // Read-only: the other operations do nothing
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}