package com.example.mood;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Classifier {

    private Interpreter interpreter;
    private List<String> labelList;
    private int INPUT_SIZE;
    private int IMAGE_MEAN = 0;
    private float IMAGE_STD = 255.0f;
    private float MAX_RESULTS = 3;
    private float THRESHOLD = 0.4f;
    Classifier(AssetManager assetManager, String modelPath, String labelPath, int inputSize) throws IOException
    {
        INPUT_SIZE = inputSize;
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(5);
        options.setUseNNAPI(true);
        interpreter = new Interpreter(loadModelFile(assetManager,modelPath), options);
        labelList = loadLabelList(assetManager, labelPath);
    }

    class Recognition
    {
        String id = "";
        String title = "";
        float confidence = 0F;

        public Recognition(String i, String s, float confidence) {
            id = i;
            title = s;
            this.confidence = confidence;
        }

        @NonNull
        @Override
        public String toString() {
            return "Title = " + title + ", Confidence = " + confidence;
        }
    }

    //loading model.tflite as mapped byte buffer
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String MODEL_FILE) throws IOException
    {
        AssetFileDescriptor assetFileDescriptor = assetManager.openFd(MODEL_FILE);
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,length);
    }

    //loading labels.txt as a list of class labels
    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException
    {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine())!= null)
        {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    //Returns result after making the prediction with the help of interpreter on given bitmap img
    String recognizemood(Bitmap bitmap)
    {
        //Resizing the bitmap
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE,false);

        //converting the image bitmap to byte buffer for input to interpreter
        ByteBuffer byteBuffer = convertBitmaptoByteBuffer(scaled);

        //Store results
        float [][] result = new float[1][labelList.size()];

        interpreter.run(byteBuffer, result);
        System.out.println("###########################################################################################");
        System.out.println("ROWS = "+result.length+" COLUMNS = "+result[0].length);
        float max = result[0][0];
        int index = 0;
        for(int i=0;i<7;i++)
        {
            System.out.println("index - "+i+" - "+result[0][i]);
            if(max<result[0][i])
            {
                max = result[0][i];
                index = i;
            }
        }
        System.out.println("MAX = "+max+" INDEX = "+index+"\n###########################################################################################");
        //Returns sorted list
        //return getSortedResult(result);
        String[] arr = {"Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise"};
        String answer = arr[index];
        return answer;
    }

    //conversion function
    private ByteBuffer convertBitmaptoByteBuffer(Bitmap bitmap)
    {
        ByteBuffer byteBuffer;
        byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE*INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel = 0;
        for(int i=0;i<INPUT_SIZE;++i)
        {
            for(int j=0;j<INPUT_SIZE;++j)
            {
                final int val = intValues[pixel++];

                byteBuffer.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
            //float value = (float) Color.red(pixel)/IMAGE_STD;
            //byteBuffer.putFloat(value);
        }
        return byteBuffer;
    }

    @SuppressLint("DefaultLocale")
    private List<Recognition> getSortedResult(float[][] labelProbArray)
    {
        PriorityQueue<Recognition> pq = new PriorityQueue<>(
                (int) MAX_RESULTS,
                new Comparator<Recognition>()
                {
                    @Override
                    public int compare(Recognition rhs, Recognition lhs)
                    {
                        return Float.compare(rhs.confidence, lhs.confidence);
                    }
                });

        for(int i=0; i <labelList.size(); ++i)
        {
            float confidence = (labelProbArray[0][i]*100)/254.0f;
            if(confidence>THRESHOLD)
            {
                pq.add(new Recognition(""+i, labelList.size()>i ? labelList.get(i) : "unknown", confidence));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionSize = (int)Math.min(pq.size(), MAX_RESULTS);
        for(int i=0; i<recognitionSize; ++i)
        {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }
}