package ca.on.tradeport.calibrationnetwork;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InstrumentDetails extends AppCompatActivity {

    public static String MY_PREFS_NAME = "session";
    private final String getcalInstrumentHistoryURL = "http://com.tradeport.on.ca/view_instrument_calibration_history.php";

    SessionManager sessionManager;
    Boolean isInternetActive;


    ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;


    CoordinatorLayout coordinatorLayout;
    ListView calibrationHistoryListView;
    ArrayList<Calibration> calArray = new ArrayList<>();
    CalibrationsAdapter adapter;
    String error_message, instrumentID = "";
    RequestBody formBody;

    GetInstrumentCalibrationHistory getInstrumentCalibrationHistory;

    Button backButton;

    TextView instrumentDetailsNameTV, instrumentDetailsSerialTV, instrumentDetailsAssetTV;
    String instrumentName, instrumentSerial, instrumentAsset, instrumentCalDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_details);





        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.instrumentDetailsCoordinatorLayout);
        Intent intent = getIntent();
        instrumentID = intent.getStringExtra("instrumentID");
        sessionManager = new SessionManager(this, coordinatorLayout);
        isInternetActive = sessionManager.isOnline();
        adapter = new CalibrationsAdapter(this, calArray);

        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        pwOne = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();

        backButton = (Button) findViewById(R.id.backButton);

        instrumentDetailsNameTV = (TextView) findViewById(R.id.instrumentDetailsName);
        instrumentDetailsAssetTV = (TextView) findViewById(R.id.instrumentAssetID);
        instrumentDetailsSerialTV = (TextView) findViewById(R.id.instrumentSerial);

        calibrationHistoryListView = (ListView) findViewById(R.id.calibrationHistoryListView);
        calibrationHistoryListView.setAdapter(adapter);

        calibrationHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Calibration caltemp = calArray.get(position);

                String taskID = caltemp.taskID;
                String cert = caltemp.certificate;

                Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
                intent.putExtra("taskID", taskID);
                intent.putExtra("cert",cert);
                startActivity(intent);

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        getInstrumentCalibrationHistory = new GetInstrumentCalibrationHistory();
        getInstrumentCalibrationHistory.execute();

        LinearLayout logoutButton = (LinearLayout) findViewById(R.id.logout_footer_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SessionManager.Logout logoutTask = sessionManager.new Logout();
                logoutTask.execute();

            }
        });


        
    }


    public class GetInstrumentCalibrationHistory extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            error_message = "";
            spinnerContainer.setAlpha(1);
        }

        @Override
        protected String doInBackground(String... params) {

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
                    .add("tag", "view_instrument_calibration_history")
                    .add("instrumentID", instrumentID)
                    .build();

            request = new Request.Builder().url(getcalInstrumentHistoryURL).post(formBody).build();

            calArray.clear();

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


                    JSONArray calInstrumentHistoryArray = jsonObject.getJSONArray("calibrations");



                    for (int i = 0; i < calInstrumentHistoryArray.length(); i++) {

                        JSONObject object3 = calInstrumentHistoryArray.getJSONObject(i);

                        String taskID = object3.getString("taskID");
                        String calID = object3.getString("calID");
                        String certificate = object3.getString("certificate");

                        calArray.add(new Calibration(certificate, calID, taskID));

                    }

                    JSONArray InstrumentInfoArray = jsonObject.getJSONArray("instrument");

                    JSONObject InstrumentInfo = InstrumentInfoArray.getJSONObject(0);

                    instrumentName = InstrumentInfo.getString("name");
                    instrumentSerial = InstrumentInfo.getString("serial");
                    instrumentAsset = InstrumentInfo.getString("assetID");
                    instrumentCalDueDate = InstrumentInfo.getString("calDueDate");

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
            instrumentDetailsNameTV.setText(instrumentName);
            instrumentDetailsSerialTV.setText(instrumentSerial);
            instrumentDetailsAssetTV.setText(instrumentAsset);

            Log.i("testing_array_size", String.valueOf(calArray.size()));

            adapter.notifyDataSetChanged();
            checkIfDueForCalibration();

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

        if(getInstrumentCalibrationHistory != null && getInstrumentCalibrationHistory.getStatus() == AsyncTask.Status.RUNNING){
            getInstrumentCalibrationHistory.cancel(true);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //    finish();

    }


    public void checkIfDueForCalibration() {

        SQLiteDatabase instrumentsDB = InstrumentDetails.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
        Cursor c = instrumentsDB.rawQuery("SELECT * FROM instrumentsDueForCal WHERE instrumentID = '"+instrumentID+"'", null);

        if(c != null && c.getCount() > 0 ) {

            //don't even have to check further, it's in there.
            //let them know it's due
            final Dialog dialog = new Dialog(InstrumentDetails.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.general_dialog);
            dialog.setCancelable(false);

            TextView name = (TextView) dialog.findViewById(R.id.instrumentCalNameValue);
            TextView serial = (TextView) dialog.findViewById(R.id.instrumentCalSerialValue);
            TextView calDueDate = (TextView) dialog.findViewById(R.id.instrumentCalDueDateValue);
            TextView assetID = (TextView) dialog.findViewById(R.id.instrumentCalAssetValue);

            name.setText(instrumentName);
            serial.setText(instrumentSerial);
            calDueDate.setText(instrumentCalDueDate);
            assetID.setText(instrumentAsset);



            Button requestCalibrationButton = (Button) dialog.findViewById(R.id.dialogRequestCalibrationButton);
            Button dismissCalibrationButton = (Button) dialog.findViewById(R.id.dialogRequestCalibrationCancelButton);

            requestCalibrationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //send request in background

                }
            });

            dismissCalibrationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }


    }





}
