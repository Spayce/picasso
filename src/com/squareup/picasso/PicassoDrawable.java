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
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.widget.ImageView;

import static android.graphics.Color.WHITE;
import static com.squareup.picasso.Picasso.LoadedFrom.MEMORY;

final class PicassoDrawable extends Drawable {
    // Only accessed from main thread.
    private static final Paint DEBUG_PAINT = new Paint();

    private static final float FADE_DURATION = 200f; //ms

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied bitmap
     * image.
     */
    static void setBitmap(final ImageView target, final Context context, final Bitmap bitmap,
                          final Picasso.LoadedFrom loadedFrom, final boolean noFade, final boolean debugging) {
        final Drawable placeholder = target.getDrawable();
        if (placeholder instanceof AnimationDrawable) {
            ((AnimationDrawable) placeholder).stop();
        }
        final PicassoDrawable drawable =
                new PicassoDrawable(context, placeholder, bitmap, loadedFrom, noFade, debugging);
        target.setImageDrawable(drawable);
    }

    /**
     * Create or update the drawable on the target {@link ImageView} to display the supplied
     * placeholder image.
     */
    static void setPlaceholder(final ImageView target, final int placeholderResId, final Drawable placeholderDrawable) {
        if (placeholderResId != 0) {
            target.setImageResource(placeholderResId);
        } else {
            target.setImageDrawable(placeholderDrawable);
        }
        if (target.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) target.getDrawable()).start();
        }
    }

    private final boolean debugging;
    private final float density;
    private final Picasso.LoadedFrom loadedFrom;
    final BitmapDrawable image;

    Drawable placeholder;

    long startTimeMillis;
    boolean animating;
    int alpha = 0xFF;

    PicassoDrawable(final Context context, final Drawable placeholder, final Bitmap bitmap,
                    final Picasso.LoadedFrom loadedFrom, final boolean noFade, final boolean debugging) {
        final Resources res = context.getResources();

        this.debugging = debugging;
        this.density = res.getDisplayMetrics().density;

        this.loadedFrom = loadedFrom;

        this.image = new BitmapDrawable(res, bitmap);

        final boolean fade = loadedFrom != MEMORY && !noFade;
        if (fade) {
            this.placeholder = placeholder;
            animating = true;
            startTimeMillis = SystemClock.uptimeMillis();
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        if (!animating) {
            image.draw(canvas);
        } else {
            final float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
            if (normalized >= 1f) {
                animating = false;
                placeholder = null;
                image.draw(canvas);
            } else {
                if (placeholder != null) {
                    placeholder.draw(canvas);
                }

                final int partialAlpha = (int) (alpha * normalized);
                image.setAlpha(partialAlpha);
                image.draw(canvas);
                image.setAlpha(alpha);
                invalidateSelf();
            }
        }

        if (debugging) {
            drawDebugIndicator(canvas);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return image.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return image.getIntrinsicHeight();
    }

    @Override
    public void setAlpha(final int alpha) {
        this.alpha = alpha;
        if (placeholder != null) {
            placeholder.setAlpha(alpha);
        }
        image.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(final ColorFilter cf) {
        if (placeholder != null) {
            placeholder.setColorFilter(cf);
        }
        image.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return image.getOpacity();
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);

        image.setBounds(bounds);
        if (placeholder != null) {
            placeholder.setBounds(bounds);
        }
    }

    private void drawDebugIndicator(final Canvas canvas) {
        DEBUG_PAINT.setColor(WHITE);
        Path path = getTrianglePath(new Point(0, 0), (int) (16 * density));
        canvas.drawPath(path, DEBUG_PAINT);

        DEBUG_PAINT.setColor(loadedFrom.debugColor);
        path = getTrianglePath(new Point(0, 0), (int) (15 * density));
        canvas.drawPath(path, DEBUG_PAINT);
    }

    private static Path getTrianglePath(final Point p1, final int width) {
        final Point p2 = new Point(p1.x + width, p1.y);
        final Point p3 = new Point(p1.x, p1.y + width);

        final Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        return path;
    }
}
