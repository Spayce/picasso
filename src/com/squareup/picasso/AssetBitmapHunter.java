package com.squareup.picasso;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentResolver.SCHEME_FILE;
import static com.squareup.picasso.Picasso.LoadedFrom.DISK;

class AssetBitmapHunter extends BitmapHunter {
    protected static final String ANDROID_ASSET = "android_asset";
    private static final int ASSET_PREFIX_LENGTH =
            (SCHEME_FILE + ":///" + ANDROID_ASSET + "/").length();

    private final AssetManager assetManager;

    public AssetBitmapHunter(final Context context, final Picasso picasso, final Dispatcher dispatcher, final Cache cache,
                             final Stats stats, final Action action) {
        super(picasso, dispatcher, cache, stats, action);
        assetManager = context.getAssets();
    }

    @Override
    Bitmap decode(final Request data) throws IOException {
        final String filePath = data.uri.toString().substring(ASSET_PREFIX_LENGTH);
        return decodeAsset(filePath);
    }

    @Override
    Picasso.LoadedFrom getLoadedFrom() {
        return DISK;
    }

    Bitmap decodeAsset(final String filePath) throws IOException {
        final BitmapFactory.Options options = createBitmapOptions(data);
        if (data.hasSize()) {
            options.inJustDecodeBounds = true;
            InputStream is = null;
            try {
                is = assetManager.open(filePath);
                BitmapFactory.decodeStream(is, null, options);
            } finally {
                Utils.closeQuietly(is);
            }
            calculateInSampleSize(data.targetWidth, data.targetHeight, options);
        }
        final InputStream is = assetManager.open(filePath);
        try {
            return BitmapFactory.decodeStream(is, null, options);
        } finally {
            Utils.closeQuietly(is);
        }
    }
}
