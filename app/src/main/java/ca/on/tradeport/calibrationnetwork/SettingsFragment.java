package ca.on.tradeport.calibrationnetwork;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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


public class SettingsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String MY_PREFS_NAME = "session";

    ListView settingsListView;
    ArrayList<Settings> settingsArray = new ArrayList<>();
    SettingsAdapter adapter;
    String error_message;
    RequestBody formBody;
    getUserSettings getUserSettings;
    Boolean isFragmentOnline = false;

    String updateLabel = "", updateValue = "";

    SessionManager sessionManager;

    ProgressWheel pwOne;
    public RelativeLayout spinnerContainer, sendFeedbackButton;

    private final String getSettingsURL = "http://com.tradeport.on.ca/view_single_user.php";
    private final String updateUserSettingsURL = "http://com.tradeport.on.ca/update_single_user_field.php";


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sessionManager = new SessionManager(getContext());
        isFragmentOnline = sessionManager.fragmentIsOnline();

        adapter = new SettingsAdapter(getContext(), settingsArray);

        settingsListView = (ListView) getView().findViewById(R.id.settingsListView);
        settingsListView.setAdapter(adapter);

        spinnerContainer = (RelativeLayout) getView().findViewById(R.id.spinnerContainer);
        pwOne = (ProgressWheel) getView().findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();

        sendFeedbackButton = (RelativeLayout) getView().findViewById(R.id.sendFeedbackButton);
        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getContext(), Feedback.class);
                startActivity(in);
            }
        });

        getUserSettings = new getUserSettings();
        getUserSettings.execute();

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Settings tempSetting = settingsArray.get(position);

                if (tempSetting.label == "password") {

                    messageDialog(view.getContext(), tempSetting.value, tempSetting.label, position);

                } else {
                    messageDialog(view.getContext(), tempSetting.value, tempSetting.label, position);
                }


            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    public class getUserSettings extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isFragmentOnline = sessionManager.fragmentIsOnline();
            if(!isFragmentOnline){
                this.cancel(true);
            } else{
                error_message = "";
                spinnerContainer.setAlpha(1);
            }

        }

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
            String userID = prefs.getString("userID", null);
            String key = prefs.getString("session_key", null);
            String username = prefs.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;


            formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("uid", userID)
                    .add("key", key)
                    .add("tag", "view_user_information")
                    .build();

            request = new Request.Builder().url(getSettingsURL).post(formBody).build();


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

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);

                    settingsArray.clear();

                    JSONArray userInfoArray = jsonObject.getJSONArray("user");

                    JSONObject userInfo = userInfoArray.getJSONObject(0);

                    String suserID = userInfo.getString("userID");
                    String sname = userInfo.getString("name");
                    String suserType = userInfo.getString("userType");
                    String saddress1 = userInfo.getString("address1");
                    String saddress2 = userInfo.getString("address2");
                    String sprovince = userInfo.getString("province");
                    String scity = userInfo.getString("city");
                    String scountry = userInfo.getString("country");
                    String spostalCode = userInfo.getString("postalcode");
                    String semail = userInfo.getString("email");
                    String sphone = userInfo.getString("phone");
                    String susername = userInfo.getString("username");


                    User user = new User(suserID, sname, suserType, saddress1, saddress2, sprovince,
                            scity, scountry, spostalCode, semail, sphone, susername);


                    settingsArray.add(new Settings("name", user.name));
                    settingsArray.add(new Settings("address 1", user.address1));
                    settingsArray.add(new Settings("address 2", user.address2));
                    settingsArray.add(new Settings("province", user.province));
                    settingsArray.add(new Settings("city", user.city));
                    settingsArray.add(new Settings("country", user.country));
                    if(user.country.equals("United States") || user.country.equals("US") || user.country.equals("united states") || user.country.equals("us")) {
                        settingsArray.add(new Settings("zip code", user.postalCode));
                    } else {
                        settingsArray.add(new Settings("postal code", user.postalCode));
                    }
                    settingsArray.add(new Settings("email", user.email));
                    settingsArray.add(new Settings("username", user.username));
                    settingsArray.add(new Settings("password", "••••••••••••••"));

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

            adapter.notifyDataSetChanged();
            spinnerContainer.setAlpha(0);


        }
    }

    public class updateUserSetting extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isFragmentOnline = sessionManager.fragmentIsOnline();
            if(!isFragmentOnline){
                this.cancel(true);
            } else{
                error_message = "";
            }

        }

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
            String userID = prefs.getString("userID", null);
            String key = prefs.getString("session_key", null);
            String username = prefs.getString("username", null);
            OkHttpClient client = new OkHttpClient();
            Request request;

            if(params[2] != "password") {

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "update_single_user_field")
                        .add("field", params[0])
                        .add("value", params[1])
                        .build();
            } else {

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "update_single_user_password")
                        .add("p1", params[0])
                        .add("p2", params[1])
                        .build();

            }

            request = new Request.Builder().url(updateUserSettingsURL).post(formBody).build();


            Response response = null;
            String result = null;

            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                result = response.body().string();
                Log.i("testing_password_update", result);

                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){

                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);
                    SettingsFragment.getUserSettings newFetchTask = new getUserSettings();

                    if(params[2] != "password") {

                        for (Settings setting: settingsArray) {
                            if(setting.label == params[2]){
                                setting.setValue(params[1]);
                            }

                        }
                    }


                } else {

                    error_message = jsonObject.getString("message");

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

            if(error_message == "") {
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Your settings were updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), error_message, Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getUserSettings != null && getUserSettings.getStatus() == AsyncTask.Status.RUNNING){
            getUserSettings.cancel(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(getUserSettings != null && getUserSettings.getStatus() == AsyncTask.Status.RUNNING){
            getUserSettings.cancel(true);
        }
    }


    public void messageDialog(Context c, String oldValue, final String label, final int position) {

        final Dialog dialog = new Dialog(c);

        Log.i("testing_clicked_inside", String.format("value = %d", position));

        if(position < 9) {

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.settings_dialog);
            dialog.setCancelable(false);

            TextView updateLabel = (TextView) dialog.findViewById(R.id.settings_change_label);
            final EditText updateText = (EditText) dialog.findViewById(R.id.settings_change_text);


            updateLabel.setText("previous value : " + oldValue);

            Button update = (Button) dialog.findViewById(R.id.dialogSettingsUpdateButton);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //check if any text was filled out

                    String sUpdateText = String.valueOf(updateText.getText());


                    if (sUpdateText.matches("")) {

                        Toast.makeText(getContext(), "Please enter a new value before hitting the update button", Toast.LENGTH_SHORT).show();

                    } else {

                        updateUserSetting updateTask = new updateUserSetting();
                        updateTask.execute(label, sUpdateText, label);
                        dialog.dismiss();

                    }


                }
            });

            Button cancel = (Button) dialog.findViewById(R.id.dialogSettingsCancelButton);
            cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    dialog.dismiss();


                }
            });

        } else {

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.settings_password_dialog);
            dialog.setCancelable(false);

            final EditText p1 = (EditText) dialog.findViewById(R.id.settings_p1);
            final EditText p2 = (EditText) dialog.findViewById(R.id.settings_p2);
            final EditText p3 = (EditText) dialog.findViewById(R.id.settings_p3);

            Button update = (Button) dialog.findViewById(R.id.dialogSettingsUpdateButton);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //check if any text was filled out

                    String sP1 = String.valueOf(p1.getText());
                    String sP2 = String.valueOf(p2.getText());
                    String sP3 = String.valueOf(p3.getText());


                    if (sP3.matches("") || sP2.matches("") || sP3.matches("")) {

                        Toast.makeText(getContext(), "Please fill out all of the fields to change your password", Toast.LENGTH_SHORT).show();

                    } else {

                        if(!sP3.matches(sP2)){
                            Toast.makeText(getContext(), "Your new password does not match in both fields. Check your spelling and try again", Toast.LENGTH_SHORT).show();
                        } else {

                            updateUserSetting updateTask = new updateUserSetting();
                            updateTask.execute(sP1, sP2, label);
                            dialog.dismiss();
                        }

                    }


                }
            });

            Button cancel = (Button) dialog.findViewById(R.id.dialogSettingsCancelButton);
            cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    dialog.dismiss();


                }
            });
        }


        dialog.show();
    }
}
