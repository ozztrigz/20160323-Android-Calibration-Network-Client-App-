package ca.on.tradeport.calibrationnetwork;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExistingUserRegistration extends AppCompatActivity {

    private static String MY_LOGIN_PREFS = "credentials";
    private final String validateCustomerRecordsURL = "http://com.tradeport.on.ca/find_single_calibration_record.php";

    TextView recordName, recordUsername, recordPassword;
    LinearLayout record;
    EditText certificate, serial;
    Button validateButton, dismissButton, ExistingUserLoginButton;
    CoordinatorLayout coordinatorLayout;
    RequestBody formBody;
    String error_message, name, username, password, certificateInput, serialInput;
    int success = 0;
    Snackbar snackbar;
    SessionManager spinnerManager;
    SharedPreferences rememberPreferences;
    ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;
    SessionManager.LoginBackground lgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_user_registration);

        rememberPreferences = getSharedPreferences(MY_LOGIN_PREFS, Context.MODE_PRIVATE);

        record = (LinearLayout) findViewById(R.id.record);
        recordName = (TextView) findViewById(R.id.recordName);
        recordUsername = (TextView) findViewById(R.id.recordUsername);
        recordPassword = (TextView) findViewById(R.id.recordPassword);
        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        spinnerManager = new SessionManager(this, spinnerContainer, coordinatorLayout);

        pwOne = (ProgressWheel) findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();
        certificate = (EditText) findViewById(R.id.validate_cal_certificate);
        serial = (EditText) findViewById(R.id.validate_cal_serial);
        serial.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            GetSingleTask newTask = new GetSingleTask();
                            newTask.execute();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.loginCoordinatorLayout);
        validateButton = (Button) findViewById(R.id.validateRecordButton);

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetSingleTask task = new GetSingleTask();
                task.execute();

            }
        });

        dismissButton = (Button) findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setVisibility(View.GONE);
                serial.setText("");
                certificate.setText("");
                recordName.setText("");
                recordUsername.setText("");
                recordPassword.setText("");
                name = "";
                username = "";
                password = "";
            }
        });

        ExistingUserLoginButton = (Button) findViewById(R.id.existingUserLoginButton);
        ExistingUserLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();

            }
        });

    }

    public class GetSingleTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideSoftKeyboard(ExistingUserRegistration.this);
            error_message = "";
            spinnerContainer.setAlpha(1);
            serialInput = serial.getText().toString();
            certificateInput = certificate.getText().toString();
            Log.i("testing_taskinfo", serialInput+" "+certificateInput);

        }

        @Override
        protected String doInBackground(String... params) {


            OkHttpClient client = new OkHttpClient();

            formBody = new FormBody.Builder()

                    .add("tag", "validate_calibration_record")
                    .add("serial", serialInput)
                    .add("certificate", certificateInput)
                    .build();

            Request request = new Request.Builder().url(validateCustomerRecordsURL).post(formBody).build();

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
                success = jsonObject.getInt("success");

                if (success == 1){

                    JSONArray taskInfoArray = jsonObject.getJSONArray("record");
                    JSONObject object3 = taskInfoArray.getJSONObject(0);

                    name = object3.getString("name");
                    username = object3.getString("username");
                    password = object3.getString("password");

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

            if(success == 1) {

                recordName.setText(name);
                recordPassword.setText(password);
                recordUsername.setText(username);
                record.setVisibility(View.VISIBLE);

            } else {

                snackbar = Snackbar.make(coordinatorLayout,
                        "No record found with the certificate number and serial number entered.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.setActionTextColor(getResources().getColor(R.color.cn_green));
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(Color.DKGRAY);
                TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();

            }

        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    public void login(){

        //if rememberme is checked
        SharedPreferences.Editor editor = rememberPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putInt("rememberme", 1);
        editor.commit();

        if(username.matches("") || password.matches("")) {
            Toast.makeText(getApplicationContext(), "To login, both Username and Password are required.", Toast.LENGTH_LONG).show();

        } else {
                lgTask = new SessionManager(this, spinnerContainer, coordinatorLayout).new LoginBackground();
                lgTask.execute(username, password);
        }


    }

}
