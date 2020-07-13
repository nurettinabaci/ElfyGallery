package com.newstrange.elfygallery;

public class ModelConfig {

    // Eğitilmiş modelin ismi
    public static String MODEL_FILENAME = "converted.tflite";

    // Image input size
    public static final int INPUT_IMG_SIZE_WIDTH = 224;
    public static final int INPUT_IMG_SIZE_HEIGHT = 224;

    // Resim 3 kanallı
    public static final int PIXEL_SIZE = 3;

    //  Tensorflow inference türü
    public static final int FLOAT_TYPE_SIZE = 4;

    // Memory'de bu kadar yer açılacak
    public static final int MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE;

    // Preprocessing işlemiyle alakalı değerler
    public static final int IMAGE_MEAN = 0;
    public static final float IMAGE_STD = 255.0f;

}
