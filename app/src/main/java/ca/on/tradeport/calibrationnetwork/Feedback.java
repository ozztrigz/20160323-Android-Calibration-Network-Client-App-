package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Feedback extends AppCompatActivity {

    public static String MY_PREFS_NAME = "session";
    RelativeLayout sendFeedbackButton, cancelFeedbackButton;
    private final String processFeedbackURL = "http://com.tradeport.on.ca/send_user_feedback.php";

    EditText feedbackText;
    String error_message = "", feedbackContent = "";
    int success = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        sendFeedbackButton = (RelativeLayout) findViewById(R.id.sendFeedbackButton);
        cancelFeedbackButton = (RelativeLayout) findViewById(R.id.cancelFeedbackButton);
        feedbackText = (EditText) findViewById(R.id.feedbackText);


        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                feedbackContent = feedbackText.getText().toString();

                if(!feedbackContent.equals("")) {

                    ProcessFeedback pf = new ProcessFeedback();
                    pf.execute();

                    //send feedback
                } else {
                    Toast.makeText(getApplicationContext(), "Oops! Looks like you forgot to write something before hitting the \"Send Feedback\" button.", Toast.LENGTH_LONG).show();
                }
            }
        });


        cancelFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }



    public class ProcessFeedback extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            error_message = "";
            success = 0;

        }

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
            String key = prefs.getString("session_key", null);
            String username = prefs.getString("username", null);
            String userID = prefs.getString("userID", null);
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("feedback", feedbackContent)
                    .add("platform", "android")
                    .add("tag", "process_user_feedback")
                    .add("username", username)
                    .add("uid", userID)
                    .add("key", key)
                    .build();



            Request request = new Request.Builder().url(processFeedbackURL).post(formBody).build();
            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();

                Log.i("testing_feedback", result.toString());
                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                success = jsonObject.getInt("success");

                if(success == 0){
                    error_message = jsonObject.getString("error_message");
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }




            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(error_message.matches("")) {

                Toast.makeText(getApplicationContext(), "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                onBackPressed();

            } else {

                Toast.makeText(getApplicationContext(), "The following error occured: "+error_message , Toast.LENGTH_LONG).show();

            }

        }
    }

}
