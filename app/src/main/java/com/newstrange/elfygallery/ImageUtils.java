package com.newstrange.elfygallery;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/** Alınan resmi 224x224x3 boyutuna çekerek Bitmap türünde döndürür */
public class ImageUtils {

    public static Bitmap prepareImageForClassification(Bitmap bitmap) {
        Paint paint = new Paint();
        Bitmap finalBitmap = Bitmap.createScaledBitmap(
                bitmap,
                ModelConfig.INPUT_IMG_SIZE_WIDTH,
                ModelConfig.INPUT_IMG_SIZE_HEIGHT,
                false);
        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(finalBitmap, 0, 0, paint);
        return finalBitmap;
    }
}
