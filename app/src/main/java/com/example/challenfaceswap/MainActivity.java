package com.example.challenfaceswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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

    private Bitmap challenBitmap;

    private Bitmap userBitmap;

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


        Bitmap challen = BitmapFactory.decodeResource(getResources(), R.drawable.challen);
        challenBitmap = challen.copy(Bitmap.Config.ARGB_8888, true);
        final Canvas challenCanvas = new Canvas(challenBitmap);
        ImageView challenPic = findViewById(R.id.challenPic);
        challenPic.setImageBitmap(challenBitmap);

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
                                            //Top left to top right
                                            challenCanvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, new Paint());
                                            //Top right to bottom right
                                            challenCanvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, new Paint());
                                            //Bottom right to bottom left
                                            challenCanvas.drawLine(bounds.right, bounds.bottom, bounds.left, bounds.bottom, new Paint());
                                            //Bottom left to top left
                                            challenCanvas.drawLine(bounds.left, bounds.bottom, bounds.left, bounds.top, new Paint());

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
            userBitmap = (Bitmap) extras.get("data");
            ImageView userView = findViewById(R.id.yourPic);
            userView.setImageBitmap(userBitmap);
            final Canvas userCanvas = new Canvas(userBitmap);

            //Configure the face detector to high accuracy.
            FirebaseVisionFaceDetectorOptions userFace =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build();

            //Create a FirebaseVisionImage object from your image.
            //To create a FirebaseVisionImage object from a Bitmap object:
            FirebaseVisionImage userImage = FirebaseVisionImage.fromBitmap(userBitmap);

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
                                            userPic.setImageBitmap(userBitmap);

                                            for (FirebaseVisionFace face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                //Top left to top right
                                                userCanvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Top right to bottom right
                                                userCanvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Bottom right to bottom left
                                                userCanvas.drawLine(bounds.right, bounds.bottom, bounds.left, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Bottom left to top left
                                                userCanvas.drawLine(bounds.left, bounds.bottom, bounds.left, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
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
