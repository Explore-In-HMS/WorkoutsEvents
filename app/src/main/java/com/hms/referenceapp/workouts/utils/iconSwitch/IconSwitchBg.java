/*
 * Copyright 2021. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hms.referenceapp.workouts.utils.iconSwitch;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

class IconSwitchBg extends Drawable {

    private RectF bounds;
    private Paint paint;

    private float radiusX;

    public IconSwitchBg() {
        bounds = new RectF();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public void init(int imageSize, int width, int height) {
        final float centerX = width * 0.5f;
        final float centerY = height * 0.5f;
        final float halfWidth = imageSize * 1.75f;
        final float halfHeight = imageSize * 0.75f;

        bounds.set(
                centerX - halfWidth, centerY - halfHeight,
                centerX + halfWidth, centerY + halfHeight);

        radiusX = bounds.height() * 0.5f;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRoundRect(bounds, radiusX, radiusX, paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }
}
