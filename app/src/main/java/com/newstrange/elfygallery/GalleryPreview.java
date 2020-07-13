package com.newstrange.elfygallery;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;

import java.util.List;

public class GalleryPreview extends AppCompatActivity {
    private Classifier myClassifier;
    ImageView GalleryPreviewImg;
    String path;
    Bitmap croppedImage;
    TextView ageLabel;
    float[][] recognitions;
    FirebaseVisionFaceDetectorOptions realTimeOpts =
            new FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                    .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS )
                    .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // FirebaseApp.initializeApp(this);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_gallery_preview);
        ageLabel = findViewById(R.id.ageLabel);
        Intent intent = getIntent();
        path = intent.getStringExtra("path"); // resmin peti

        // load classifier
        loadMyClassifier();

        // Face detection
        File imgFile = new  File(path);
        final Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts);

        // Get detected faces and run inference
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                processFaceContourDetectionResult(faces, myBitmap);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                processNotDetected(myBitmap);
                            }
                        });
    }
    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
    private void loadMyClassifier() {
        try {
            myClassifier = Classifier.classifier(getAssets(), ModelConfig.MODEL_FILENAME);
        } catch (IOException e) {
            Toast.makeText(this, "Model couldn't be loaded. Check logs.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void processNotDetected(Bitmap bm){
        GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);
        Glide.with(GalleryPreview.this)
                .load(bm)
                .into(GalleryPreviewImg);
    }
    private void processFaceContourDetectionResult(List<FirebaseVisionFace> faces, Bitmap bitmapp) {
        // Task completed successfully
        if (faces.size() == 0) {

            GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);
            Glide.with(GalleryPreview.this)
                    .load(new File(path)) // Uri of the picture
                    .into(GalleryPreviewImg);
            ageLabel.setVisibility(View.INVISIBLE);

        }else {

            for (FirebaseVisionFace face : faces) {
                Rect rect = face.getBoundingBox(); // detected faces
                Log.d("Sizes", "TOP: " + rect.top);
                croppedImage = Bitmap.createBitmap(bitmapp, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
                Bitmap squareBitmap = ThumbnailUtils.extractThumbnail(croppedImage, getScreenWidth(), getScreenWidth());
                Bitmap preprocessedImage = ImageUtils.prepareImageForClassification(squareBitmap);

                recognitions = myClassifier.recognizeImage(preprocessedImage);
//              float[] gender = recognitions[0]; // almıyor
                float[] ageBins = recognitions[0];

                ageLabel.setVisibility(View.VISIBLE);
                ageLabel.setText("Age: " + String.valueOf(getMax(ageBins)*5));

                GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);
                Glide.with(GalleryPreview.this)
                        .load(croppedImage)
                        .into(GalleryPreviewImg);

            }
        }
    }
    public static float getMax(float[] inputArray){
        float maxValue = inputArray[0];
        for(int i=1;i < inputArray.length;i++){
            if(inputArray[i] > maxValue){
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }
}
