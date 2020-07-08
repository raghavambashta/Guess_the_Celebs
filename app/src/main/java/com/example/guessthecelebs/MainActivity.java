package com.example.guessthecelebs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebsURLs = new ArrayList<String>();
    ArrayList<String> celebsNames = new ArrayList<String>();
    int chosenCeleb = 0;
    ImageView celebImageView;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;

    Button button0;
    Button button1;
    Button button2;
    Button button3;

    TextView scoreTextView;

    int score = 0;
    int questions = 0;

    public void optionChosen(View view)
    {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer)))
        {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
            score++;
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Wrong! It was : " + celebsNames.get(chosenCeleb - 10), Toast.LENGTH_SHORT).show();
        }
        questions++;
        newQuestion();
        scoreTextView.setText(Integer.toString(score) + " / " + Integer.toString(questions));
    }

    public class imageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1)
                {
                    char current = (char) data;
                    result+= current;
                    data = reader.read(); //to move to next one
                }
                return result;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public void newQuestion()
    {
        try {
            Random rand = new Random();

            chosenCeleb = rand.nextInt(100);

            imageDownloader imageTask = new imageDownloader();
            Bitmap celebImage = imageTask.execute(celebsURLs.get(chosenCeleb - 5)).get();

            celebImageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebsNames.get(chosenCeleb - 10);
                } else {
                    incorrectAnswerLocation = rand.nextInt(100);

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt(100);
                    }
                    answers[i] = celebsNames.get(incorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        }catch (Exception e){
            e.printStackTrace();
            Log.i("Sadly ","An error occured in newQuestion function");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImageView = (ImageView)findViewById(R.id.celebImageView);
        DownloadTask task = new DownloadTask();
        String result = null;
        button0 = (Button) findViewById(R.id.button1);
        button1 = (Button) findViewById(R.id.button2);
        button2 = (Button) findViewById(R.id.button3);
        button3 = (Button) findViewById(R.id.button4);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);

        scoreTextView.setText("0 / 0");

        try {
            result = task.execute("https://www.imdb.com/list/ls025929404/").get();

            String[] splitResult = result.split("> Arshad Warsi");

            Pattern p = Pattern.compile("src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebsURLs.add(m.group(1));
            }

            p = Pattern.compile("<img alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebsNames.add(m.group(1));
            }

            Log.i("Image list",Integer.toString(celebsURLs.size()));
            Log.i("Names list", Integer.toString(celebsNames.size()));

            newQuestion();

        }catch (Exception e) {
            e.printStackTrace();
            Log.i("Sadly ","An Error occured");
        }
    }
}