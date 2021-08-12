package com.example.dailyshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherActivity extends AppCompatActivity {

    String city;
    String info;
    final String apiKey = "99ac7c18ce762985d551c977a77dd8a4";

    Button submit;
    EditText cityNameEditText;
    TextView weatherTextView;
    TextView visibilityTextView;
    TextView cityNameTextView;

    private class Downloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            StringBuilder sb = new StringBuilder();
            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1){
                    char temp = (char)data;
                    sb.append(temp);
                    data = reader.read();
                }
                Log.i("weather", sb.toString());
                return sb.toString();
            } catch (Exception e){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Enter a VALID city name", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            Log.i("Weather info", s);
        }
    }

    /*
    The onclick method for submit button
    Function:
    update everything according to text in cityNameEditText
     */
    public void submit(View view){
        if (cityNameEditText.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),"Enter a city name", Toast.LENGTH_SHORT).show();
        }else{
            try {
                String encodedCityName = URLEncoder.encode(
                        cityNameEditText.getText().toString(), "UTF-8");
                city = encodedCityName;
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(getApplicationContext(),"Enter a VALID city name", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            refresh();
        }
    }

    /*
    Function:
    Download current information of weather from https://openweathermap.org
     */
    public void downLoadInfo(){
        Downloader downloader = new Downloader();
        try {
            info = downloader.execute("https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+apiKey).get();
//            Log.i("infor",info);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(cityNameEditText.getWindowToken(), 0);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Enter a VALID city name", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Function:
    set up the weather text view
     */
    public void setWeather(){
        try {
            JSONObject in = new JSONObject(info);
            JSONArray arrWeather = in.getJSONArray("weather");
            String res = "";
            for (int i = 0; i < arrWeather.length();i++){
                JSONObject obj = arrWeather.getJSONObject(i);
                String main = obj.getString("main");
                String description = obj.getString("description");
                if(!main.equals("") && !description.equals("")){
                    res += main + ": " + description;
                }
            }
            if(!res.equals("")){
                weatherTextView.setText(res);
                weatherTextView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e){
//            Toast.makeText(getApplicationContext(),"Could not get weather", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


    }

    /*
    Function:
    set up visibility textView
     */
    public void setVisibility(){
        try {
            JSONObject in = new JSONObject(info);
            int v = in.getInt("visibility");
            visibilityTextView.setText("Visibility: " + v);
            visibilityTextView.setVisibility(View.VISIBLE);
        } catch (Exception e){
//            Toast.makeText(getApplicationContext(),"Could not get visibility", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /*
    Function:
    set up city name textView
     */
    public void setName(){
        try {
            JSONObject in = new JSONObject(info);
            String name = in.getString("name");
            cityNameTextView.setText("City Name: "+name);
            cityNameTextView.setVisibility(View.VISIBLE);
        } catch (Exception e){
//            Toast.makeText(getApplicationContext(),"Could not get name", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /*
    a helper method to refresh the weather data
    Function:
    download information
    set up text views
     */
    public void refresh(){
        downLoadInfo();
        setWeather();
        setVisibility();
        setName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        submit = (Button)findViewById(R.id.submit);
        weatherTextView = (TextView)findViewById(R.id.weatherTextView);
        visibilityTextView = (TextView)findViewById(R.id.visibilityTextView);
        cityNameEditText = (EditText)findViewById(R.id.cityNameEditText);
        cityNameTextView = (TextView)findViewById(R.id.cityNameTextView);

        visibilityTextView.setVisibility(View.INVISIBLE);
        cityNameTextView.setVisibility(View.INVISIBLE);
        weatherTextView.setVisibility(View.INVISIBLE);
    }
}