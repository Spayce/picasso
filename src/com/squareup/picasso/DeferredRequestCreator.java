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

import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

class DeferredRequestCreator implements ViewTreeObserver.OnPreDrawListener {

    final RequestCreator creator;
    final WeakReference<ImageView> target;
    Callback callback;

    DeferredRequestCreator(final RequestCreator creator, final ImageView target) {
        this(creator, target, null);
    }

    DeferredRequestCreator(final RequestCreator creator, final ImageView target, final Callback callback) {
        this.creator = creator;
        this.target = new WeakReference<ImageView>(target);
        this.callback = callback;
        target.getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    public boolean onPreDraw() {
        final ImageView target = this.target.get();
        if (target == null) {
            return true;
        }
        final ViewTreeObserver vto = target.getViewTreeObserver();
        if (!vto.isAlive()) {
            return true;
        }

        final int width = target.getMeasuredWidth();
        final int height = target.getMeasuredHeight();

        if (width <= 0 || height <= 0) {
            return true;
        }

        vto.removeOnPreDrawListener(this);

        this.creator.unfit().resize(width, height).into(target, callback);
        return true;
    }

    void cancel() {
        callback = null;
        final ImageView target = this.target.get();
        if (target == null) {
            return;
        }
        final ViewTreeObserver vto = target.getViewTreeObserver();
        if (!vto.isAlive()) {
            return;
        }
        vto.removeOnPreDrawListener(this);
    }
}
