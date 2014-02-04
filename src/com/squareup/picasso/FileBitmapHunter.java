/*
 * Copyright (C) 2013 Square, Inc.
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

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.IOException;

import static android.media.ExifInterface.*;

class FileBitmapHunter extends ContentStreamBitmapHunter {

    FileBitmapHunter(final Context context, final Picasso picasso, final Dispatcher dispatcher, final Cache cache,
                     final Stats stats, final Action action) {
        super(context, picasso, dispatcher, cache, stats, action);
    }

    @Override
    Bitmap decode(final Request data)
            throws IOException {
        setExifRotation(getFileExifRotation(data.uri));
        return super.decode(data);
    }

    static int getFileExifRotation(final Uri uri) throws IOException {
        final ExifInterface exifInterface = new ExifInterface(uri.getPath());
        final int orientation = exifInterface.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL);
        switch (orientation) {
            case ORIENTATION_ROTATE_90:
                return 90;
            case ORIENTATION_ROTATE_180:
                return 180;
            case ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
}
