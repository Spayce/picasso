package com.squareup.picasso;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeUrlBitmapHunter extends BitmapHunter {

    public static final String FAKE_SCHEME = "fakelocal://";

    private static Map<String, Bitmap> bitmapStorage = new ConcurrentHashMap<String, Bitmap>();
    private static int insertions = 0;

    public static void putBitmapToFakeStorage(final String fakeKey, final Bitmap bitmap) {
        bitmapStorage.put(fakeKey, bitmap);
        ++insertions;
    }

    public static void removeBitmapToFakeStorage(final String fakeKey) {
        bitmapStorage.remove(fakeKey);
    }

    public static int getFakeInsertionsCount() {
        return insertions;
    }

    //////////////////

    FakeUrlBitmapHunter(final Picasso picasso, final Dispatcher dispatcher, final Cache cache, final Stats stats, final Action action) {
        super(picasso, dispatcher, cache, stats, action);
    }

    @Override
    Bitmap decode(final Request data) throws IOException {
        return bitmapStorage.get(data.uri.getPath());
    }
}
