package com.example.challenfaceswap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    /**
     * If the user requests to take an image with their camera
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Bitmap challenBitmap;

    private Bitmap userBitmap;

    //private Bitmap tempChallen;

    //private Bitmap tempUser;

    private Rect challenBounds;

    //private Rect userBounds;

    private Rect[] multipleBounds;

    //private int[] imageArray2 = {R.drawable.challen, R.drawable.challen2, R.drawable.challen3, R.drawable.challen4, R.drawable.challen5};

    //private int[] imageArray = {R.drawable.challen2};


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

        Button randomChallen = findViewById(R.id.randomChallen);
        randomChallen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetChallen();
            }
        });

        Bitmap challen = BitmapFactory.decodeResource(getResources(), R.drawable.challen2);
        challenBitmap = challen.copy(Bitmap.Config.ARGB_8888, true);
        //tempChallen = challen.copy(Bitmap.Config.ARGB_8888, true);
        ImageView challenPic = findViewById(R.id.challenPic);
        challenPic.setImageBitmap(challenBitmap);
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

        Button swap = findViewById(R.id.faceSwap);
        swap.setVisibility(View.GONE);

        if (multipleBounds == null || multipleBounds.length == 0) {
            swap.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Error: No faces found" + "\n\nPress OK to take another picture.").setTitle("Face Swap Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dispatchTakePictureIntent();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        try {
            swap.setVisibility(View.GONE);

            Canvas userCanvas = new Canvas(userBitmap);
            Canvas challenCanvas = new Canvas(challenBitmap);

            int challenLeft = challenBounds.left;
            int challenTop = challenBounds.top;
            int challenWidth = Math.abs(challenBounds.width());
            int challenHeight = Math.abs(challenBounds.height());
            Bitmap challenFace = challenBitmap.createBitmap(challenBitmap, challenLeft, challenTop, challenWidth, challenHeight, null, false);

            for (Rect bounds : multipleBounds) {
                int userLeft = bounds.left;
                int userTop = bounds.top;
                int userWidth = Math.abs(bounds.width());
                int userHeight = Math.abs(bounds.height());
                Bitmap userFace = userBitmap.createBitmap(userBitmap, userLeft, userTop, userWidth, userHeight, null, false);

                Matrix challenMatrix = new Matrix();
                challenMatrix.setScale((float) userWidth / challenFace.getWidth(), (float) userHeight / challenFace.getHeight());
                challenMatrix.postTranslate((float) userLeft, (float) userTop);


                Matrix userMatrix = new Matrix();
                userMatrix.setScale((float) challenWidth / userFace.getWidth(), (float) challenHeight / userFace.getHeight());
                userMatrix.postTranslate((float) challenLeft, (float) challenTop);

                userCanvas.drawBitmap(challenFace, challenMatrix, new Paint());
                challenCanvas.drawBitmap(userFace, userMatrix, new Paint());
            }

        } catch (Exception e) {
            swap.setVisibility(View.GONE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Error: " + e.getMessage() + "\n\nPress OK to take another picture.").setTitle("Face Swap Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dispatchTakePictureIntent();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        //userCanvas.drawBitmap(challenFace, challenBounds, userBounds, new Paint());
        //challenCanvas.drawBitmap(userFace, userBounds, challenBounds, new Paint());

    }

    private void resetChallen() {

        //Random random = new Random();
        //int randomInt = random.nextInt(imageArray.length);

        /**
        if (tempUser != null) {
            userBitmap = tempUser.copy(Bitmap.Config.ARGB_8888, true);
        }
         */
        ImageView userPic = findViewById(R.id.yourPic);

        if (userPic.getDrawable() != null) {
            Button swap = findViewById(R.id.faceSwap);
            swap.setVisibility(View.VISIBLE);
        }

        //Bitmap challen = BitmapFactory.decodeResource(getResources(), imageArray[randomInt]);
        Bitmap challen = BitmapFactory.decodeResource(getResources(), R.drawable.challen2);
        challenBitmap = challen.copy(Bitmap.Config.ARGB_8888, true);
        //tempChallen = challen.copy(Bitmap.Config.ARGB_8888, true);
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
                                        int count = 0;
                                        for (FirebaseVisionFace face : faces) {
                                            if (count == 0) {
                                                challenBounds = face.getBoundingBox();
                                            }
                                            count++;
                                            //Rect bounds = face.getBoundingBox();
                                            /**
                                            //Top left to top right
                                            challenCanvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            //Top right to bottom right
                                            challenCanvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            //Bottom right to bottom left
                                            challenCanvas.drawLine(bounds.right, bounds.bottom, bounds.left, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            //Bottom left to top left
                                            challenCanvas.drawLine(bounds.left, bounds.bottom, bounds.left, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                            */
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
            multipleBounds = new Rect[0];
            resetChallen();

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
                                            //tempUser = userBitmap.copy(Bitmap.Config.ARGB_8888, true);

                                            Rect[] userFaces = new Rect[faces.size()];
                                            int counter = 0;

                                            for (FirebaseVisionFace face : faces) {

                                                /**
                                                if (counter == 0) {
                                                    userBounds = face.getBoundingBox();
                                                } */

                                                Rect bounds = face.getBoundingBox();
                                                userFaces[counter] = bounds;
                                                counter++;

                                                /**
                                                //Top left to top right
                                                userCanvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Top right to bottom right
                                                userCanvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Bottom right to bottom left
                                                userCanvas.drawLine(bounds.right, bounds.bottom, bounds.left, bounds.bottom, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                //Bottom left to top left
                                                userCanvas.drawLine(bounds.left, bounds.bottom, bounds.left, bounds.top, new Paint(Paint.FILTER_BITMAP_FLAG));
                                                */
                                            }

                                            multipleBounds = userFaces;
                                            Button swap = findViewById(R.id.faceSwap);
                                            swap.setVisibility(View.VISIBLE);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception

                                        }
                                    });


        }
    }

}
