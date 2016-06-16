package ca.on.tradeport.calibrationnetwork;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TaskDetails extends AppCompatActivity {

    public static String MY_PREFS_NAME = "session";
    private final String getSingleTaskURL = "http://com.tradeport.on.ca/view_single_task.php";
    private final String getAccreditedCertificateURL = "http://com.tradeport.on.ca/accredited-certificate.php";
    private final String getTraceableCertificateURL = "http://com.tradeport.on.ca/traceable-certificate.php";

    ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;
    getPDF getNewPDF;

    SessionManager sessionManager;

    Button backButton;
    TaskInfoAdapter taskInfoAdapter;
    ArrayList<Settings> taskDetails = new ArrayList<>();
    String error_message, taskID;
    GetSingleTask getSingleTask;

    CoordinatorLayout coordinatorLayout;

    RequestBody formBody;
    TextView instrumentNameTV, instrumentSerialTV, instrumentAssetTV, taskDetailsTitle;
    String instrumentName, instrumentSerial, instrumentAsset, calcURL;
    ListView taskInfoListView;
    int calcOrCert = 0;
    int isTraceable = 0;
    String cert = "";

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.taskDetailsCoordinatorLayout);
        sessionManager = new SessionManager(this, coordinatorLayout);

        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        pwOne = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();

        taskInfoAdapter = new TaskInfoAdapter(this, taskDetails);


        taskDetailsTitle = (TextView) findViewById(R.id.textTaskDetails);
        instrumentNameTV = (TextView) findViewById(R.id.TaskInstrumentTitle);
        instrumentSerialTV = (TextView) findViewById(R.id.taskInstrumentSerial);
        instrumentAssetTV = (TextView) findViewById(R.id.taskInstrumentAsset);
        taskInfoListView = (ListView) findViewById(R.id.taskInfoListView);
        taskInfoListView.setAdapter(taskInfoAdapter);
        taskInfoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Settings setting = taskDetails.get(position);

                if (setting.label == "certificate" && setting.value != "") {

                    calcOrCert = 0;

                    String pdfURL = (isTraceable == 1) ? getTraceableCertificateURL : getAccreditedCertificateURL;

                    getNewPDF = new getPDF();
                    getNewPDF.execute(pdfURL);

                }

                if (setting.label == "calculation file") {

                    if (!calcURL.matches("")) {
                        calcOrCert = 1;
                        getPDF getNewPDF = new getPDF();
                        getNewPDF.execute(calcURL);
                    } else {
                        Toast.makeText(getApplicationContext(), "This calibration did not have any reports attached to it.", Toast.LENGTH_LONG).show();
                    }


                }

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
        taskID = intent.getStringExtra("taskID");
        cert = intent.getStringExtra("cert");

        taskDetailsTitle.setText("Task Details for Certificate : "+cert);

        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSingleTask = new GetSingleTask();
        getSingleTask.execute();


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //    finish();

    }

    public class GetSingleTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            error_message = "";
            spinnerContainer.setAlpha(1);

        }

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
            String key = prefs.getString("session_key", null);
            String username = prefs.getString("username", null);
            OkHttpClient client = new OkHttpClient();

            formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("key", key)
                    .add("tag", "view_single_task")
                    .add("taskID", taskID)
                    .build();

            Request request = new Request.Builder().url(getSingleTaskURL).post(formBody).build();

            taskDetails.clear();

            Response response = null;
            String result;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("testing_taskinfo", result);

                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);

                    taskDetails.add(new Settings("label", "Task Details"));

                    JSONArray taskInfoArray = jsonObject.getJSONArray("task");
                    JSONObject object3 = taskInfoArray.getJSONObject(0);

                    String tasktype = object3.getString("taskType");
                    isTraceable = (tasktype.matches("traceable"))? 1 : 2;
                    String status = object3.getString("status");
                    String technician = object3.getString("technician");
                    String dateCompleted = object3.getString("dateCompleted");

                    taskDetails.add(new Settings("task type", tasktype));
                    taskDetails.add(new Settings("status", status));
                    taskDetails.add(new Settings("technician", technician));
                    taskDetails.add(new Settings("date completed", dateCompleted));


                    taskDetails.add(new Settings("label", "Calibration Details"));

                    JSONArray taskCalArray = jsonObject.getJSONArray("cal");
                    JSONObject object4 = taskCalArray.getJSONObject(0);

                    String foundinspec = (object4.getString("foundInSpec").equals("1")) ? "Yes" : "No";
                    String returnedinspec = (object4.getString("returnedInSpec").equals("1")) ? "Yes" : "No";
                    String temperature = object4.getString("temperature");
                    String humidity = object4.getString("humidity");
                    String calDate = object4.getString("calDate");
                    String calDueDate = object4.getString("calDueDate");
                    String calcFile = object4.getString("calculationFile");
                    calcURL = calcFile;

                    String certificate = object4.getString("certificate");

                    taskDetails.add(new Settings("found in spec", foundinspec));
                    taskDetails.add(new Settings("returned in spec", returnedinspec));
                    taskDetails.add(new Settings("temperature", temperature));
                    taskDetails.add(new Settings("humidity", humidity+"%"));
                    taskDetails.add(new Settings("calibration date", calDate));
                    taskDetails.add(new Settings("calibration due date", calDueDate));
                    taskDetails.add(new Settings("calculation file", calcFile));
                    taskDetails.add(new Settings("certificate", certificate));




                    JSONArray instrumentInfoArray = jsonObject.getJSONArray("instrument");
                    JSONObject instrumentInfo = instrumentInfoArray.getJSONObject(0);

                    instrumentName = instrumentInfo.getString("name");
                    instrumentSerial = instrumentInfo.getString("serial");
                    instrumentAsset = instrumentInfo.getString("assetID");



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

            instrumentNameTV.setText(instrumentName);
            instrumentSerialTV.setText(instrumentSerial);
            instrumentAssetTV.setText(instrumentAsset);

            taskInfoAdapter.notifyDataSetChanged();


        }
    }


    public class getPDF extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinnerContainer.setAlpha(1);
        }

        @Override
        protected String doInBackground(String... params) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {

                URL url;

                if(calcOrCert == 0) {

                    url = new URL(params[0] + "?tag=get_completed_certificate&taskID=" + taskID);
                }else {
                    url = new URL("http://tradeport.on.ca/calibrationnetwork.com"+params[0]);
                }
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Log.i("testing_url", url.toString());

                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();


                input = connection.getInputStream();

                if(calcOrCert == 0){
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) ,instrumentName+"_"+instrumentSerial+"_certificate.pdf");
                }else{
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) ,instrumentName+"_"+instrumentSerial+"_report.pdf");
                }

                output = new FileOutputStream(file);


                byte data[] = new byte[4096];

                long total = 0;
                int count;
                while((count = input.read(data)) != -1){

                    if(isCancelled()) {
                        input.close();
                        return null;
                    }

                    total += count;

                    if(fileLength > 0)
                        publishProgress((int) (total * 100) / fileLength);
                    output.write(data, 0, count);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(output != null)
                        output.close();
                    if(input != null)
                        input.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(connection != null)
                    connection.disconnect();

            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            if(isCancelled()){
                return;
            }

            spinnerContainer.setAlpha(0);

            if (result != null) {
                Toast.makeText(TaskDetails.this, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {

                if(file != null) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    try {
                        startActivity(intent);
                    }
                    catch (ActivityNotFoundException e) {
                        Toast.makeText(TaskDetails.this,
                                "No Application Available to View PDF. Download a PDF Reader from the Google Play Store and try again.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(TaskDetails.this, "There was an error fetching the file. Please try again later.", Toast.LENGTH_LONG).show();
                }
            }


        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getSingleTask != null && getSingleTask.getStatus() == AsyncTask.Status.RUNNING){
            getSingleTask.cancel(true);
        }

        if(getNewPDF != null && getNewPDF.getStatus() == AsyncTask.Status.RUNNING){
            getNewPDF.cancel(true);
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
    protected void onResume() {
        super.onResume();
        sessionManager.isSessionValid();
    }


}
