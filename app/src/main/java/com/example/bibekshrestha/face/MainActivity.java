package com.example.bibekshrestha.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private String pictureFilePath;
    private ImageView img;
    private Button browsebtn;
    private Button captbtn;
    private Button detect;
    private Button gallery;
    private final int PICK_IMAGE = 1;
    private final int SAVE_IMAGE = 2;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = (ImageView)findViewById(R.id.image);
        browsebtn = (Button)findViewById(R.id.browse);
        captbtn = (Button)findViewById(R.id.capture);
        detect = (Button) findViewById(R.id.detect);
      //  gallery =(Button) findViewById(R.id.gallery);

        browsebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select Picture"), PICK_IMAGE);
            }
        });


       detect.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               Paint myRectPaint = new Paint();
               myRectPaint.setStrokeWidth(5);
               myRectPaint.setColor(Color.RED);
               myRectPaint.setStyle(Paint.Style.STROKE);

               Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                       Bitmap.Config.RGB_565);
               Canvas tempCanvas = new Canvas(tempBitmap);
               tempCanvas.drawBitmap(bitmap, 0, 0, null);

               final FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                       .setTrackingEnabled(false)
                       .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                       .build();

               if(!detector.isOperational()) {
                   new AlertDialog.Builder(view.getContext()).setMessage("Could not set up the face detected!").show();
                   return;
               }


               Frame frame = new Frame.Builder().setBitmap(bitmap).build();
               SparseArray<Face> faces = detector.detect(frame);

               for (int i = 0; i < faces.size(); i++) {
                   Face thisFace = faces.valueAt(i);
                   float x1 = thisFace.getPosition().x;
                   float y1 = thisFace.getPosition().y;
                   float x2 = x1 + thisFace.getWidth();
                   float y2 = y1 + thisFace.getHeight();
                   tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

               }

               Paint paint = new Paint();
               paint.setColor(Color.GREEN);
               paint.setStyle(Paint.Style.STROKE);
               paint.setStrokeWidth(5);

               for (int i = 0; i < faces.size(); ++i) {
                   Face face = faces.valueAt(i);
                   for (Landmark landmark : face.getLandmarks()) {
                       int cx = (int) (landmark.getPosition().x);
                       int cy = (int) (landmark.getPosition().y);
                       tempCanvas.drawCircle(cx, cy, 10, paint);
                   }
               }

               img.setImageBitmap(tempBitmap);
           }
       });

       captbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               sendTakePictureIntent();

         }
       });
    }

    //take a picture and save to storage
    private void sendTakePictureIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       // cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
           //startActivityForResult(cameraIntent, SAVE_IMAGE);

            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.bibekshrestha.face.provider",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, PICK_IMAGE);
            }
        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "cam_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

//    private void galleryAddPic() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(pictureFilePath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }


    //activity to browse the image and display in imageview,,,,
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case PICK_IMAGE:
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
                    data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    img.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
//            case SAVE_IMAGE:
//             if(requestCode ==SAVE_IMAGE && resultCode == RESULT_OK ) {
//                Bundle bund = data.getExtras();
//                bitmap = (Bitmap) bund.get("data");
//                img.setImageBitmap(bitmap);
//            }

        }
    }
}
