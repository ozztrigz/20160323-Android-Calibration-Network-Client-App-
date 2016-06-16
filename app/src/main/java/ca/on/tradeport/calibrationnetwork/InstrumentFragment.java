package ca.on.tradeport.calibrationnetwork;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
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
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class InstrumentFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String MY_PREFS_NAME = "session";

    private View rootView;

    ListView instrumentListView;
    ArrayList<Instrument> instrumentArray = new ArrayList<>();
    public InstrumentAdapter adapter;
    String error_message;
    Boolean isSearch = false;
    RequestBody formBody;
    public SessionManager sessionManager;
    getInstruments getInstruments;
    Boolean isFragmentOnline = false;

    MasterActivity master;

    SharedPreferences sharedPreferences;
    public ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;

    String Cert = "", Serial = "", Asset = "";
    RelativeLayout InstrumentSearchButton;
    TextView InstrumentSearchButtonText;

    final String getInstrumentsURL = "http://com.tradeport.on.ca/view_all_client_instruments.php";
    final String getInstrumentsSearchURL = "http://com.tradeport.on.ca/search_client_instruments.php";

    private List<String> myData;

    View parentView;

    private Boolean isCalledFromMaster;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public InstrumentFragment() {
        // Required empty constructor
        Log.i("testing_methods","empty constructor");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    public InstrumentFragment newInstance(Boolean param1, String param2) {
        InstrumentFragment fragment = new InstrumentFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        Log.i("testing_methods","newInstance constructor");
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("testing_methods", "onCreate");

        setRetainInstance(true);

        if (getArguments() != null) {
            isCalledFromMaster = getArguments().getBoolean(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("testing_methods", "onActivityCreated");



    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i("testing_methods", "onStart");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_instruments, container, false);

        Log.i("testing_methods", "onCreateView");

        sessionManager = new SessionManager(getActivity());
        isFragmentOnline = sessionManager.fragmentIsOnline();
        sharedPreferences = rootView.getContext().getApplicationContext().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);


        spinnerContainer = (RelativeLayout) rootView.findViewById(R.id.spinnerContainer);
        InstrumentSearchButton = (RelativeLayout) rootView.findViewById(R.id.instrumentSearchButton);
        InstrumentSearchButtonText = (TextView) rootView.findViewById(R.id.instrumentSearchButtonText);
        instrumentListView = (ListView) rootView.findViewById(R.id.instrumentListView);
        pwOne = (ProgressWheel) rootView.findViewById(R.id.progressBarTwo);

        pwOne.startSpinning();
        adapter = new InstrumentAdapter(rootView.getContext(), instrumentArray);
        instrumentListView.setAdapter(adapter);

        instrumentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Instrument tempInstrument = instrumentArray.get(position);

                String instrumentID = tempInstrument.instrumentID;


                //change to InstrumentDetails when available
                Intent intent = new Intent(rootView.getContext(), InstrumentDetails.class);
                intent.putExtra("instrumentID", instrumentID);
                startActivity(intent);
            }
        });





        getInstruments = new getInstruments();
        getInstruments.execute();



        InstrumentSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isSearch) {
                    isSearch = true;
                    messageDialog(v.getContext());
                }else{
                    isSearch = false;
                    getInstruments getNewInstruments = new getInstruments();
                    getNewInstruments.execute();

                }

            }
        });


        return rootView;

    }



    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i("testing_methods","onAtach");

        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.i("testing_methods","onDetach");
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }


    public class getInstruments extends AsyncTask<String, String, String> {

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

            if(isSearch){

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("cert", params[0])
                        .add("serial", params[1])
                        .add("assetID", params[2])
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "search_client_instruments")
                        .build();

                request = new Request.Builder().url(getInstrumentsSearchURL).post(formBody).build();

            } else {

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "view_all_client_instruments")
                        .build();

                request = new Request.Builder().url(getInstrumentsURL).post(formBody).build();

            }

            instrumentArray.clear();
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
                Log.i("instrumentsInfo", result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if(isSearch) {

                    String query = jsonObject.getString("query");

                    Log.i("instrumentsInfo", query);

                }

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

                        instrumentArray.add(new Instrument(instrumentID, asset, serial, name, description, model, category, manufacturer));

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
            } else {

                RelativeLayout spinner = (RelativeLayout) getActivity().findViewById(R.id.spinnerContainer);
                spinner.setAlpha(1);
            }

            if(isCancelled()){
                return;
            }

            if (isSearch) {

                Cert = "";
                Serial = "";
                Asset = "";

                if (InstrumentSearchButton != null && InstrumentSearchButtonText != null) {
                    InstrumentSearchButton.setBackground(getResources().getDrawable(R.drawable.cancel_button_shape));
                    InstrumentSearchButtonText.setText("Cancel");
                }

            } else {

                if (InstrumentSearchButton != null && InstrumentSearchButtonText != null) {
                    InstrumentSearchButtonText.setText("Search");
                    InstrumentSearchButton.setBackground(getResources().getDrawable(R.drawable.green_button));
                }
            }



            adapter.notifyDataSetChanged();


        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("testing_methods", "onDestroy");

        //cleanup time
        spinnerContainer = null;
        pwOne = null;
        InstrumentSearchButton = null;
        InstrumentSearchButtonText = null;
        instrumentListView = null;

        if(getInstruments != null && getInstruments.getStatus() == AsyncTask.Status.RUNNING){
            getInstruments.cancel(true);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("testing_methods", "onPause");

        setRetainInstance(true);

        if(getInstruments != null && getInstruments.getStatus() == AsyncTask.Status.RUNNING){
            getInstruments.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getRetainInstance();

        Log.i("testing_methods", "onResume");
    }

    public void messageDialog(Context c) {

        final Dialog dialog = new Dialog(c);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.instrument_search_dialog);
        dialog.setCancelable(false);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_message);
        final EditText certificate = (EditText) dialog.findViewById(R.id.InstrumentSearchDialogCertificateEditText);
        final EditText serial = (EditText) dialog.findViewById(R.id.InstrumentSearchDialogSerialEditText);
        final EditText asset = (EditText) dialog.findViewById(R.id.InstrumentSearchDialogAssetEditText);


        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        text.setText("Fill out either of these fields to find a specific instrument");

        Button search = (Button) dialog.findViewById(R.id.dialogInstrumentSearchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if any text was filled out

                Cert = certificate.getText().toString();
                Serial = serial.getText().toString();
                Asset = asset.getText().toString();

                if (Cert.matches("") && Serial.matches("") && Asset.matches("")) {

                    Toast.makeText(getContext(), "Please enter a criteria before hitting the search button", Toast.LENGTH_SHORT).show();

                } else {

                    Log.i("InstrumentSearch", Cert+" "+Asset+" "+Serial);

                    getInstruments searchInstruments = new getInstruments();
                    searchInstruments.execute(Cert, Serial, Asset);
                    dialog.dismiss();

                }


            }
        });

        Button createAccount= (Button) dialog.findViewById(R.id.dialogInstrumentSearchCancelButton);
        createAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dialog.dismiss();
                isSearch = false;

            }
        });


        dialog.show();
    }






}
