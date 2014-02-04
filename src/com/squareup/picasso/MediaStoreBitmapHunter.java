/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

import static android.content.ContentUris.parseId;
import static android.provider.MediaStore.Images.Thumbnails.*;
import static com.squareup.picasso.MediaStoreBitmapHunter.PicassoKind.*;

class MediaStoreBitmapHunter extends ContentStreamBitmapHunter {
    private static final String[] CONTENT_ORIENTATION = new String[]{
            MediaStore.Images.ImageColumns.ORIENTATION
    };

    MediaStoreBitmapHunter(final Context context, final Picasso picasso, final Dispatcher dispatcher, final Cache cache,
                           final Stats stats, final Action action) {
        super(context, picasso, dispatcher, cache, stats, action);
    }

    @Override
    Bitmap decode(final Request data) throws IOException {
        final ContentResolver contentResolver = context.getContentResolver();
        setExifRotation(getExitOrientation(contentResolver, data.uri));

        if (data.hasSize()) {
            final PicassoKind picassoKind = getPicassoKind(data.targetWidth, data.targetHeight);
            if (picassoKind == FULL) {
                return super.decode(data);
            }

            final long id = parseId(data.uri);

            final BitmapFactory.Options options = createBitmapOptions(data);
            options.inJustDecodeBounds = true;

            calculateInSampleSize(data.targetWidth, data.targetHeight, picassoKind.width,
                    picassoKind.height, options);

            final Bitmap result = getThumbnail(contentResolver, id, picassoKind.androidKind, options);

            if (result != null) {
                return result;
            }
        }

        return super.decode(data);
    }

    static PicassoKind getPicassoKind(final int targetWidth, final int targetHeight) {
        if (targetWidth <= MICRO.width && targetHeight <= MICRO.height) {
            return MICRO;
        } else if (targetWidth <= MINI.width && targetHeight <= MINI.height) {
            return MINI;
        }
        return FULL;
    }

    static int getExitOrientation(final ContentResolver contentResolver, final Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, CONTENT_ORIENTATION, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (final RuntimeException ignored) {
            // If the orientation column doesn't exist, assume no rotation.
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    enum PicassoKind {
        MICRO(MICRO_KIND, 96, 96),
        MINI(MINI_KIND, 512, 384),
        FULL(FULL_SCREEN_KIND, -1, -1);

        final int androidKind;
        final int width;
        final int height;

        PicassoKind(final int androidKind, final int width, final int height) {
            this.androidKind = androidKind;
            this.width = width;
            this.height = height;
        }
    }
}
