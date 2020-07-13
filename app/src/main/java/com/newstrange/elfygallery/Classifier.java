package com.newstrange.elfygallery;


import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static com.newstrange.elfygallery.ModelConfig.INPUT_IMG_SIZE_HEIGHT;
import static com.newstrange.elfygallery.ModelConfig.INPUT_IMG_SIZE_WIDTH;
import static com.newstrange.elfygallery.ModelConfig.MODEL_INPUT_SIZE;
import static com.newstrange.elfygallery.ModelConfig.IMAGE_STD;
import static com.newstrange.elfygallery.ModelConfig.IMAGE_MEAN;

public class Classifier {
    private final Interpreter interpreter;

    private Classifier(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public static Classifier classifier(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath);
        Interpreter interpreter = new Interpreter(byteBuffer);
        return new Classifier(interpreter);
    }

    // Eğitilmiş modeli yükle
    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Inference kodu
    public float[][] recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);

        float[][] result = new float[1][21];
        interpreter.run(byteBuffer, result);
        return result;
    }

    // Prediction yapmak için ByteBuffer türüne dönüştürür
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_IMG_SIZE_WIDTH; ++i) {
            for (int j = 0; j < INPUT_IMG_SIZE_HEIGHT; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                byteBuffer.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            }
        }
        return byteBuffer;
    }

}

