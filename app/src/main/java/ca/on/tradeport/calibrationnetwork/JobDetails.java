package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JobDetails extends AppCompatActivity {

    public static String MY_PREFS_NAME = "session";
    private final String getTasksURL = "http://com.tradeport.on.ca/view_tasks_attached_to_job.php";

    SessionManager sessionManager;
    Boolean isInternetActive;


    ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;


    CoordinatorLayout coordinatorLayout;
    ListView attachedTasksListView;
    ArrayList<Task> taskArray = new ArrayList<>();
    TasksAdapter adapter;
    String error_message, jobID = "";
    RequestBody formBody;
    GetAttachedTasks attachedTasks;

    Button backButton;

    TextView jobDescriptionTV, jobStatusTV, jobDateCreatedTV;
    String jobDescription, jobStatus, jobDateCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.jobDetailsCoordinatorLayout);

        sessionManager = new SessionManager(this, coordinatorLayout);

        isInternetActive = sessionManager.isOnline();

        adapter = new TasksAdapter(this, taskArray);

        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        pwOne = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();

        backButton = (Button) findViewById(R.id.backButton);

        jobDescriptionTV = (TextView) findViewById(R.id.jobDetailsTitle);
        jobStatusTV = (TextView) findViewById(R.id.jobDetailsStatus);
        jobDateCreatedTV = (TextView) findViewById(R.id.jobDetailsDateCreated);

        attachedTasksListView = (ListView) findViewById(R.id.attachedTasksListView);
        attachedTasksListView.setAdapter(adapter);

        attachedTasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Task tasktemp = taskArray.get(position);

                String taskID = tasktemp.taskID;

                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
                intent.putExtra("taskID", taskID);
                startActivity(intent);
            }
        });

        LinearLayout logoutButton = (LinearLayout) findViewById(R.id.logout_footer_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SessionManager.Logout logoutTask = sessionManager.new Logout();
                logoutTask.execute();

            }
        });

        Intent intent = getIntent();
        jobID = intent.getStringExtra("jobID");

        attachedTasks = new GetAttachedTasks();
        attachedTasks.execute();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //    finish();

    }

    @Override
    protected void onResume() {
        super.onResume();

        sessionManager.isSessionValid();


    }

    public class GetAttachedTasks extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            error_message = "";
            spinnerContainer.setAlpha(1);
        }

        @Override
        protected String doInBackground(String... params) {

            TimeZone tz = TimeZone.getDefault();
            String tzid = tz.getID();

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
            String userID = prefs.getString("userID", null);
            String key = prefs.getString("session_key", null);
            String username = prefs.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;

            formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("uid", userID)
                    .add("key", key)
                    .add("tag", "view_attached_tasks")
                    .add("tz", tzid)
                    .add("jobID", jobID)
                    .build();

            request = new Request.Builder().url(getTasksURL).post(formBody).build();



            taskArray.clear();

            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("testing", result);

                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);


                    JSONArray taskInfoArray = jsonObject.getJSONArray("tasks");



                    for (int i = 0; i < taskInfoArray.length(); i++) {

                        JSONObject object3 = taskInfoArray.getJSONObject(i);

                        String taskID = object3.getString("taskID");
                        String wo = object3.getString("wo");
                        String taskType = object3.getString("taskType");
                        String status = object3.getString("status");
                        String model = object3.getString("model");
                        String serial = object3.getString("serial");
                        String manufacturer = object3.getString("manufacturer");
                        String certificate = object3.getString("certificate");

                        taskArray.add(new Task(taskID, wo, taskType, status, model, serial, manufacturer, certificate));

                    }

                    JSONArray jobInfoArray = jsonObject.getJSONArray("job");

                    JSONObject jobInfo = jobInfoArray.getJSONObject(0);

                    jobDescription = jobInfo.getString("title");
                    jobStatus = jobInfo.getString("status");
                    jobDateCreated = jobInfo.getString("dateCreated");

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

            if(isCancelled()){
                return;
            }

            spinnerContainer.setAlpha(0);
            jobDescriptionTV.setText(jobDescription);
            jobStatusTV.setText(jobStatus);
            jobDateCreatedTV.setText(jobDateCreated);

            Log.i("testing_array_size", String.valueOf(taskArray.size()));

            adapter.notifyDataSetChanged();


        }
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(attachedTasks != null && attachedTasks.getStatus() == AsyncTask.Status.RUNNING){
            attachedTasks.cancel(true);
        }
    }
}
