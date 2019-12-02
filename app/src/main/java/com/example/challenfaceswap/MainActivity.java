package com.example.challenfaceswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * If the user requests to take an image with their camera
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button to take a picture. When clicked it opens the camera application on the phone.
        Button takePic = findViewById(R.id.takePicture);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        Bitmap challenBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.challen);
        ImageView userPic = findViewById(R.id.challenPic);
        userPic.setImageBitmap(challenBitmap);

        FirebaseVisionFaceDetectorOptions challenFace =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        //Create a FirebaseVisionImage object from your image.
        //To create a FirebaseVisionImage object from a Bitmap object:
        FirebaseVisionImage challenImage = FirebaseVisionImage.fromBitmap(challenBitmap);

        //Get an instance of FirebaseVisionFaceDetector:
        FirebaseVisionFaceDetector challenDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(challenFace);

        //Finally, pass the image to the detectInImage method:
        Task<List<FirebaseVisionFace>> result =
                challenDetector.detectInImage(challenImage)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // ...
                                        for (FirebaseVisionFace face : faces) {
                                            Rect bounds = face.getBoundingBox();
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

    /**
     * Makes an intent and starts an activity to take a picture.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * This function runs when the user is done taking a picture. The image is converted into a bitmap.
     * @param requestCode The code to request
     * @param resultCode The resulting code
     * @param data The intent received from the picture
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Get the picture as a bitmap and store it in the instance variable.
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");

            //Configure the face detector to high accuracy.
            FirebaseVisionFaceDetectorOptions userFace =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build();

            //Create a FirebaseVisionImage object from your image.
            //To create a FirebaseVisionImage object from a Bitmap object:
            FirebaseVisionImage userImage = FirebaseVisionImage.fromBitmap(imageBitmap);

            //Get an instance of FirebaseVisionFaceDetector:
            FirebaseVisionFaceDetector userDetector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(userFace);

            //Finally, pass the image to the detectInImage method:
            Task<List<FirebaseVisionFace>> result =
                    userDetector.detectInImage(userImage)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            // Task completed successfully
                                            // ...
                                            ImageView userPic = findViewById(R.id.yourPic);
                                            userPic.setImageBitmap(imageBitmap);

                                            for (FirebaseVisionFace face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });


        }
    }

}
