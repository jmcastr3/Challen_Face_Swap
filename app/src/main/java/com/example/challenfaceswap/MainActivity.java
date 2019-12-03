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

    private Rect userBounds;

    private Rect challenBounds;

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

        Button swapFaces = findViewById(R.id.faceSwap);
        swapFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapFacesFunction();
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
                                            challenBounds = face.getBoundingBox();
                                            Rect bounds = face.getBoundingBox();
                                            //Top left to top right
                                            challenCanvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            //Top right to bottom right
                                            challenCanvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            //Bottom right to bottom left
                                            challenCanvas.drawLine(bounds.right, bounds.bottom, bounds.left, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            //Bottom left to top left
                                            challenCanvas.drawLine(bounds.left, bounds.bottom, bounds.left, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));

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

    private void swapFacesFunction() {
        final Canvas userCanvas = new Canvas(userBitmap);
        final Canvas challenCanvas = new Canvas(challenBitmap);

        int challenLeft = (int) Math.floor(challenBounds.left);
        int challenTop = (int) Math.floor(challenBounds.top);
        int challenWidth = ((int) Math.floor(challenBounds.right)) - challenLeft;
        int challenHeight = Math.abs(((int) Math.floor(challenBounds.bottom)) - challenTop);
        Bitmap challenFace = challenBitmap.createBitmap(challenBitmap, challenLeft, challenTop, challenWidth, challenHeight, null, false);

        int userLeft = (int) Math.floor(userBounds.left);
        int userTop = (int) Math.floor(userBounds.top);
        int userWidth = ((int) Math.floor(userBounds.right)) - userLeft;
        int userHeight = Math.abs(((int) Math.floor(userBounds.bottom)) - userTop);
        Bitmap userFace = userBitmap.createBitmap(userBitmap, userLeft, userTop, userWidth, userHeight, null, false);

        userCanvas.drawBitmap(challenFace, challenBounds, userBounds, new Paint());
        challenCanvas.drawBitmap(userFace, userBounds, challenBounds, new Paint());
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

                                            int counter = 0;
                                            for (FirebaseVisionFace face : faces) {
                                                if (counter == 0) {
                                                    userBounds = face.getBoundingBox();
                                                }
                                                Rect bounds = face.getBoundingBox();
                                                //Top left to top right
                                                userCanvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Top right to bottom right
                                                userCanvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Bottom right to bottom left
                                                userCanvas.drawLine(bounds.right, bounds.bottom, bounds.left, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Bottom left to top left
                                                userCanvas.drawLine(bounds.left, bounds.bottom, bounds.left, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                counter++;
                                            }
                                            Button swap = findViewById(R.id.faceSwap);
                                            swap.setVisibility(View.VISIBLE);
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
