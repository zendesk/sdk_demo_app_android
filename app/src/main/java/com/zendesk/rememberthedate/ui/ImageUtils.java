package com.zendesk.rememberthedate.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import androidx.annotation.DimenRes;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.zendesk.rememberthedate.R;

import java.util.Locale;


class ImageUtils {
    static void loadProfilePicture(Context context, Uri uri, ImageView imageView) {
        int diameter = context.getResources().getDimensionPixelSize(R.dimen.image_diameter);
        Picasso.with(context)
                .load(uri)
                .resize(diameter, diameter)
                .centerCrop()
                .transform(new RoundedTransformation(diameter / 2))
                .into(imageView);
    }

    private static class RoundedTransformation implements Transformation {

        static Transformation get(Context context, @DimenRes int radius) {
            return new RoundedTransformation(context.getResources().getDimensionPixelOffset(radius));
        }

        private final int radius;
        private final int strokeWidth;
        private final int strokeColor;

        private RoundedTransformation(int radius) {
            this(radius, -1);
        }

        private RoundedTransformation(int radius, int strokeWidth) {
            this.radius = radius;
            this.strokeColor = Color.TRANSPARENT;
            this.strokeWidth = strokeWidth;
        }

        @Override
        public Bitmap transform(final Bitmap source) {

            // draw border
            if (strokeWidth > 0) {
                final Canvas canvas = new Canvas(source);
                final Paint paint = strokePaint();

                final Path circle = new Path();
                circle.setFillType(Path.FillType.INVERSE_EVEN_ODD);

                final RectF borderMask = getMask(source.getWidth(), source.getHeight(), strokeWidth);
                circle.addRoundRect(borderMask, radius, radius, Path.Direction.CW);

                canvas.drawPath(circle, paint);
            }

            // cut out
            final Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(output);
            final Paint shapePaint = shapePaint(source);
            final RectF mask = getMask(source.getWidth(), source.getHeight(), 0);
            canvas.drawRoundRect(mask, radius, radius, shapePaint);

            if (source != output) {
                source.recycle();
            }

            return output;
        }

        private RectF getMask(int width, int height, int offset) {
            return new RectF(offset, offset, width - offset, height - offset);
        }

        private Paint shapePaint(Bitmap source) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            return paint;
        }

        private Paint strokePaint() {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(strokeColor);
            return paint;
        }

        @Override
        public String key() {
            String keyFormat = "rounded-%s-%s-%s";

            return String.format(Locale.US, keyFormat, radius, strokeColor, strokeWidth);
        }
    }
}

