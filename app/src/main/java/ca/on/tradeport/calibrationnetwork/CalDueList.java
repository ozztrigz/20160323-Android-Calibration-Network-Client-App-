package ca.on.tradeport.calibrationnetwork;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalDueList extends AppCompatActivity {


    private RecyclerView recyclerView;
    public static String MY_PREFS_NAME = "session";
    private static String grabInstrumentsInfoURL = "http://com.tradeport.on.ca/view_instruments_due_for_calibration.php";
    private static String calibrationRequestForAllURL = "http://com.tradeport.on.ca/send_calibration_request_for_all_instruments.php";
    private static String calibrationRequestForSingleURL = "http://com.tradeport.on.ca/send_calibration_request_for_single_instrument.php";
    private static String disableNotificationForSingleURL = "http://com.tradeport.on.ca/disable_notification_for_single_instrument.php";
    private static String ignoreNotificationForSingleURL = "http://com.tradeport.on.ca/ignore_notification_for_single_instrument.php";
    public SessionManager sessionManager;
    public CalibrationDueAdapter adapter;
    public ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;
    public String error_message;
    public String singleCalInstrumentID = "";
    public String singleCalType = "";
    private CalDueAdapter calDueAdapter;

    ArrayList<Instrument> instruments = new ArrayList<>();
    RequestBody formBody;
    SharedPreferences sharedPreferences;
    RelativeLayout requestCalibrationButton;
    ListView calDueLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_due_list);



        sessionManager = new SessionManager(this);

        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        sharedPreferences = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);

        pwOne = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();

        /*
        calDueLV = (ListView) findViewById(R.id.calDueListView);
        requestCalibrationButton = (RelativeLayout) findViewById(R.id.requestAllCalibrationButton);

        requestCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendListCalibrationRequest sendListCalibrationRequest = new SendListCalibrationRequest();
                sendListCalibrationRequest.execute();

            }
        });

        adapter = new CalibrationDueAdapter(this, instruments);
        calDueLV.setAdapter(adapter);

        registerForContextMenu(calDueLV);

        calDueLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView instrumentID = (TextView) view.findViewById(R.id.instrument_itemID);
                singleCalInstrumentID = instrumentID.getText().toString();
                chooseCalibrationAction();

            }

        });

        */



        recyclerView = (RecyclerView) findViewById(R.id.newList);
        calDueAdapter = new CalDueAdapter(this);
        recyclerView.setAdapter(calDueAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        GetInstruments getInstruments = new GetInstruments();
        getInstruments.execute();



    }

    public void chooseCalibrationAction() {

        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("--");
        spinnerArray.add("ISO 17025");
        spinnerArray.add("Traceable");

        final Dialog dialog = new Dialog(CalDueList.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.single_cal_due_instrument_dialog);
        dialog.setCancelable(false);

        final Spinner calType = (Spinner) dialog.findViewById(R.id.calType);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calType.setAdapter(adapter);

        Button requestCalibrationButton = (Button) dialog.findViewById(R.id.dialogRequestCalibrationButton);
        final Button ignoreCalibrationButton = (Button) dialog.findViewById(R.id.dialogRequestCalibrationIgnoreButton);
        Button disableCalibrationButton = (Button) dialog.findViewById(R.id.dialogRequestCalibrationDisableButton);
        Button dismissCalibrationButton = (Button) dialog.findViewById(R.id.dialogRequestCalibrationCancelButton);

            requestCalibrationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //check if spinner was set
                    if(calType.getSelectedItemPosition() != 0) {

                        if(calType.getSelectedItemPosition() ==1) {
                            singleCalType = "ISO 17025";
                        } else {
                            singleCalType = "Traceable";
                        }

                        SendSingleCalibrationRequest sendSingleCalibrationRequest = new SendSingleCalibrationRequest();
                        sendSingleCalibrationRequest.execute();

                        dialog.dismiss();

                    }
                }
            });

        disableCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DisableNotificationForSingleInstrument disableNotificationForSingleInstrument = new DisableNotificationForSingleInstrument();
                disableNotificationForSingleInstrument.execute();
                dialog.dismiss();

            }
        });

        ignoreCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IgnoreNotificationForSingleInstrument ignoreNotificationForSingleInstrument = new IgnoreNotificationForSingleInstrument();
                ignoreNotificationForSingleInstrument.execute();
                dialog.dismiss();
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

    public class GetInstruments extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            error_message = "";
            spinnerContainer.setAlpha(1);

        }

        @Override
        protected String doInBackground(String... params) {


            String userID = sharedPreferences.getString("userID", null);
            String key = sharedPreferences.getString("session_key", null);
            String username = sharedPreferences.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;
            FormBody.Builder formBodyBuilder = new FormBody.Builder();



            SQLiteDatabase instrumentsdb = CalDueList.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
            Cursor c = instrumentsdb.rawQuery("SELECT * FROM instrumentsDueForCal", null);

            int iID = c.getColumnIndex("instrumentID");

            if(c != null && c.getCount() > 0) {
                if(c.moveToFirst()){

                    do{

                        formBodyBuilder.add("params[]", c.getString(iID));

                    } while (c.moveToNext());

                }
            }

            instrumentsdb.close();

            formBodyBuilder.add("username", username);
            formBodyBuilder.add("uid", userID);
            formBodyBuilder.add("key", key);

            formBodyBuilder.add("tag", "view_cal_due_instruments");
            RequestBody body = formBodyBuilder.build();

            request = new Request.Builder().url(grabInstrumentsInfoURL).post(body).build();

            instruments.clear();
            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("instrumentsInfo", result);
                JSONObject jsonObject = new JSONObject(result);


                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);


                    JSONArray instrumentsInfoArray = jsonObject.getJSONArray("instruments");

                    Log.i("instrumentsInfo", instrumentsInfoArray.toString());

                    for (int i = 0; i < instrumentsInfoArray.length(); i++) {

                        JSONObject object3 = instrumentsInfoArray.getJSONObject(i);

                        String instrumentID = object3.getString("instrumentID");
                        String asset = object3.getString("assetID");
                        String name = object3.getString("name");
                        String category = object3.getString("category");
                        String description = object3.getString("description");
                        String serial = object3.getString("serial");
                        String manufacturer = object3.getString("manufacturer");
                        String model= object3.getString("model");
                        int calType = object3.getInt("calType");

                        instruments.add(new Instrument(instrumentID, asset, serial, name, description, model, category, manufacturer, calType));

                    }



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


            if(spinnerContainer != null) {
                spinnerContainer.setAlpha(0);
            }

            if(isCancelled()){
                return;
            }

           // calDueAdapter.notifyDataSetChanged();
            calDueAdapter.setInstrumentList(instruments);

        }
    }

    public class SendListCalibrationRequest extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            error_message = "";
            spinnerContainer.setAlpha(1);

        }

        @Override
        protected String doInBackground(String... params) {


            String userID = sharedPreferences.getString("userID", null);
            String key = sharedPreferences.getString("session_key", null);
            String username = sharedPreferences.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;
            FormBody.Builder formBodyBuilder = new FormBody.Builder();



            SQLiteDatabase instrumentsdb = CalDueList.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
            Cursor c = instrumentsdb.rawQuery("SELECT * FROM instrumentsDueForCal", null);

            int iID = c.getColumnIndex("instrumentID");

            if(c != null && c.getCount() > 0) {
                if(c.moveToFirst()){
                    do{
                        formBodyBuilder.add("params[]", c.getString(iID));
                    } while (c.moveToNext());
                }
            }

            for(Instrument instrument : instruments) {
                formBodyBuilder.add("calTypes[]", String.valueOf(instrument.calType));
            }


            formBodyBuilder.add("username", username);
            formBodyBuilder.add("uid", userID);
            formBodyBuilder.add("key", key);

            formBodyBuilder.add("tag", "send_calibration_request_for_all");
            RequestBody body = formBodyBuilder.build();

            request = new Request.Builder().url(calibrationRequestForAllURL).post(body).build();

            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {

                result = response.body().string();
                JSONObject jsonObject = new JSONObject(result);
                Log.i("testing_cal", result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);

                    //once calibration requests have been sent, there's no need to keep those in the
                    //database anymore, just delete them and once all this is done return user to MasterActivity
                    instruments.clear();
                    instrumentsdb.delete("instrumentsDueForCal",null,null);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            instrumentsdb.close();


            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(error_message.equals("")) {
                Intent intent = new Intent(CalDueList.this, MasterActivity.class);
                CalDueList.this.startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();
            }


        }
    }

    public class SendSingleCalibrationRequest extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            error_message = "";
            spinnerContainer.setAlpha(1);

        }

        @Override
        protected String doInBackground(String... params) {


            String userID = sharedPreferences.getString("userID", null);
            String key = sharedPreferences.getString("session_key", null);
            String username = sharedPreferences.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;

            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            formBodyBuilder.add("username", username);
            formBodyBuilder.add("uid", userID);
            formBodyBuilder.add("key", key);
            formBodyBuilder.add("instrumentID", String.valueOf(singleCalInstrumentID));
            formBodyBuilder.add("calType", singleCalType);
            formBodyBuilder.add("tag", "send_calibration_request_for_single_instrument");

            RequestBody body = formBodyBuilder.build();

            request = new Request.Builder().url(calibrationRequestForSingleURL).post(body).build();

            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("instrumentsInfo", result);
                JSONObject jsonObject = new JSONObject(result);


                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);

                    for (int i = 0; i < instruments.size(); i++) {

                        if(instruments.get(i).instrumentID == singleCalInstrumentID){
                            instruments.remove(i);
                        }
                    }

                    SQLiteDatabase instrumentsdb = CalDueList.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
                    instrumentsdb.execSQL("DELETE FROM instrumentsDueForCal WHERE instrumentID = "+singleCalInstrumentID);
                    instrumentsdb.close();

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


            if(spinnerContainer != null) {
                spinnerContainer.setAlpha(0);
            }

            if(isCancelled()){
                return;
            }

            adapter.notifyDataSetChanged();


        }
    }

    public class DisableNotificationForSingleInstrument extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            error_message = "";
            spinnerContainer.setAlpha(1);

        }

        @Override
        protected String doInBackground(String... params) {


            String userID = sharedPreferences.getString("userID", null);
            String key = sharedPreferences.getString("session_key", null);
            String username = sharedPreferences.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;
            FormBody.Builder formBodyBuilder = new FormBody.Builder();

            formBodyBuilder.add("username", username);
            formBodyBuilder.add("uid", userID);
            formBodyBuilder.add("key", key);
            formBodyBuilder.add("instrumentID", singleCalInstrumentID);

            formBodyBuilder.add("tag", "disable_notifications_for_single_instrument");
            RequestBody body = formBodyBuilder.build();

            request = new Request.Builder().url(disableNotificationForSingleURL).post(body).build();

            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("instrumentsInfo", result);
                JSONObject jsonObject = new JSONObject(result);


                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);

                    for (int i = 0; i < instruments.size(); i++) {

                        if(instruments.get(i).instrumentID == singleCalInstrumentID){
                            instruments.remove(i);
                        }
                    }

                    SQLiteDatabase instrumentsDB = CalDueList.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
                    instrumentsDB.delete("instrumentsDueForCal", "instrumentID = " + singleCalInstrumentID, null);
                    instrumentsDB.close();

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

            if(!error_message.equals("")) {
                Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();
            }

            if(spinnerContainer != null) {
                spinnerContainer.setAlpha(0);
            }

            if(isCancelled()){
                return;
            }

            adapter.notifyDataSetChanged();
        }
    }


    public class IgnoreNotificationForSingleInstrument extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            error_message = "";
            spinnerContainer.setAlpha(1);

        }

        @Override
        protected String doInBackground(String... params) {


            String userID = sharedPreferences.getString("userID", null);
            String key = sharedPreferences.getString("session_key", null);
            String username = sharedPreferences.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;
            FormBody.Builder formBodyBuilder = new FormBody.Builder();

            formBodyBuilder.add("username", username);
            formBodyBuilder.add("uid", userID);
            formBodyBuilder.add("key", key);
            formBodyBuilder.add("instrumentID", singleCalInstrumentID);

            formBodyBuilder.add("tag", "ignore_notifications_single_instrument");
            RequestBody body = formBodyBuilder.build();

            request = new Request.Builder().url(ignoreNotificationForSingleURL).post(body).build();

            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("instrumentsInfo", result);
                JSONObject jsonObject = new JSONObject(result);


                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);

                    for (int i = 0; i < instruments.size(); i++) {

                        if(instruments.get(i).instrumentID == singleCalInstrumentID){
                            instruments.remove(i);
                        }
                    }

                    SQLiteDatabase instrumentsDB = CalDueList.this.openOrCreateDatabase("Instruments", Context.MODE_PRIVATE, null);
                    instrumentsDB.delete("instrumentsDueForCal", "instrumentID = " + singleCalInstrumentID, null);
                    instrumentsDB.close();

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

            if(!error_message.equals("")) {
                Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();
            }

            if(spinnerContainer != null) {
                spinnerContainer.setAlpha(0);
            }

            if(isCancelled()){
                return;
            }

            adapter.notifyDataSetChanged();


        }
    }

}
