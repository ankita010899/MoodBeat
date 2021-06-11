package com.example.mood;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button btnCamera , btnemo, btnplay;
    TextView tv;
    Bitmap croppedbmp;
    String result = "No mood detected";
    int count = 0;
    ColorMatrix matrix = new ColorMatrix();
    private int mInputSize = 224;
    private String mModelPath = "converted_model2.tflite";
    private String mLabelPath = "labels.txt";
    private Classifier classifier;
    private static final int MAX_FACES = 4;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing the classifier and views
        initViews();
        try {
            initClassifier();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initClassifier() throws IOException
    {
        classifier = new Classifier(getAssets(), mModelPath, mLabelPath, mInputSize);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews()
    {
        btnCamera = (Button)findViewById(R.id.btnCamera);
        btnemo = (Button)findViewById(R.id.btnemo);
        btnplay = (Button)findViewById(R.id.btnplay);
        imageView = (ImageView)findViewById(R.id.imageView);
        tv = (TextView)findViewById(R.id.textview1);

        //EVENTS
        //take snap button
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.defaultpic));
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        //go to emoji page
        btnemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EmojiPage.class);
                MainActivity.this.startActivity(intent);
            }
        });

        //if count is 0 it means user wants to directly listen to songs which is not allowed

        //playlist generation
        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goto music list page if textview contains mood
                if(!(result.equals("No mood detected")))
                {
                    Intent intent = new Intent(MainActivity.this, MusicList.class);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra("mood", tv.getText().toString());
                    System.out.println("#######################################"+tv.getText().toString());
                    intent.setType("text/plain");
                    MainActivity.this.startActivity(intent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Take Snap first or use emoji", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        Bitmap imgmood = bitmap; //for setting clicked image to imageview
        //imageView.setImageBitmap(imgmood);

        //convert to grayscale image using filter
        //matrix.setSaturation(0);
        //ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //imageView.setColorFilter(filter);

        imageView.setImageBitmap(imgmood);

        //face detection
        int fcount;
        Bitmap facedet = convert(bitmap, Bitmap.Config.RGB_565);
        fcount = setFace(facedet);
        Toast.makeText(this, "Face count - "+fcount, Toast.LENGTH_SHORT).show();

        //calling mood detection method
        if(fcount==1)
        {
            //Toast.makeText(this, "Detecting mood...", Toast.LENGTH_SHORT).show();
            detect();
        }
        if(fcount>1)
        {
            Toast.makeText(this, "MULTIPLE FACES DETECTED - RETAKE SNAP WITH SINGLE FACE", Toast.LENGTH_SHORT).show();
            tv.setText("No mood detected");
        }
        if(fcount==0)
        {
            Toast.makeText(this, "NO FACE DETECTED - RETAKE SNAP", Toast.LENGTH_SHORT).show();
            tv.setText("No mood detected");
        }

    }

    //convert bitmap to RGB 565 for FaceDetector
    private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth()+1, bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }

    //detection of faces
    public int setFace(Bitmap b) {
        Bitmap mFaceBitmap = b;
        FaceDetector fd;
        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
        int count = 0;
        int mFaceHeight = mFaceBitmap.getHeight();
        int mFaceWidth = mFaceBitmap.getWidth();
        try {
            fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);
            count = fd.findFaces(mFaceBitmap, faces);
            imageView.invalidate();
            //return count;
        } catch (Exception e) {
            Log.e("FaceDetection - ", "setFace(): " + e.toString());
            //return count;
        }

        //get cropped face
        PointF midpoint = new PointF();
        int [] fpx = null;
        int [] fpy = null;
        int i = 0,width=0,height=0;
        int myEyesDistance = 0;
        if (count > 0) {
            fpx = new int[count];
            fpy = new int[count];

            for (i = 0; i < count; i++) {
                try {
                    faces[i].getMidPoint(midpoint);
                    fpx[i] = (int) midpoint.x;
                    fpy[i] = (int) midpoint.y;
                    myEyesDistance = (int) faces[i].eyesDistance();
                    imageView.invalidate();
                } catch (Exception e) {
                    Log.e("Cropped image - ", "setFace(): face " + i + ": " + e.toString());
                }
            }

            int x = fpx[0] - myEyesDistance*2;
            int y = fpy[0] - myEyesDistance*2;
            int x2 = fpx[0] + myEyesDistance*2;
            int y2 = fpy[0] + myEyesDistance*2;
            width = x2 - x;
            height = y2 - y;

            System.out.println("###############################################################################");
            System.out.println("midpt.x = " + fpx[0] + " midpt.y = " + fpy[0] + " eyedis = " + myEyesDistance);
            System.out.println("x, y = " + x + " , " + y + " x2, y2 = " + x2 + " , " + y2);
            System.out.println("crop width, height = " + width + " , " + height);
            System.out.println("bitmap w, h = " + b.getWidth() + " , " + b.getHeight());
            if((width + x) >b.getWidth())
            {
                x = fpx[0] - myEyesDistance;
                x2 = fpx[0] + myEyesDistance;
                width = x2 - x;
                System.out.println("New x = "+x+" x2 = "+" width = "+width);
            }
            if((height + y) >b.getHeight())
            {
                y = fpy[0] - myEyesDistance;
                y2 = fpy[0] + myEyesDistance;
                height = y2 - y;
                System.out.println("New y = "+y+" y2 = "+y2+" height = "+height);
            }

            if(x<0 || y<0 || x2<0 || y2<0) {
                Toast.makeText(this, "PLEASE RETAKE SNAP AND POSITION YOURSELF A BIT FAR FROM CAMERA", Toast.LENGTH_LONG).show();
            }
            else {
                croppedbmp = Bitmap.createBitmap(b, x, y, width, height);
                //Bitmap done = Bitmap.createScaledBitmap(croppedbmp,imageView.getWidth(),imageView.getHeight(),false);
                imageView.setImageBitmap(croppedbmp);
            }
        }
    return count;

    }


    //setting the result to textview
    private void detect()
    {
        Bitmap bitmap = ((BitmapDrawable)((ImageView)imageView).getDrawable()).getBitmap();
        result = classifier.recognizemood(bitmap);
        tv.setText(result);
    }

}