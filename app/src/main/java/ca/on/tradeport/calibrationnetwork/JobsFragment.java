package ca.on.tradeport.calibrationnetwork;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.LinearLayout;
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




public class JobsFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String MY_PREFS_NAME = "session";

    String PO = "", SO = "";

    ListView jobsListView;
    ArrayList<Job> jobArray = new ArrayList<>();
    JobsAdapter adapter;
    String error_message, searchString;
    Boolean isSearch = false;
    RequestBody formBody;
    SQLiteDatabase sessionDB;
    EditText SOET, POET;
    getJobs getJobs;
    Boolean isFragmentOnline = false;
    LinearLayout footerSearchButton;

    SessionManager sessionManager;

    Dialog myDialog;

    RelativeLayout jobSearchButton;
    TextView searchButtonText;

    private final String getJobsURL = "http://com.tradeport.on.ca/view_client_jobs.php";
    private final String getJobsSearchURL = "http://com.tradeport.on.ca/search_client_jobs.php";

    ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public JobsFragment() {
        // Required empty public constructor
    }

    public static JobsFragment newInstance(String param1, String param2) {
        JobsFragment fragment = new JobsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jobs, container, false);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sessionManager = new SessionManager(getContext());
        isFragmentOnline = sessionManager.fragmentIsOnline();

        jobSearchButton = (RelativeLayout) getView().findViewById(R.id.jobSearchButton);

        spinnerContainer = (RelativeLayout) getView().findViewById(R.id.spinnerContainer);
        pwOne = (ProgressWheel) getView().findViewById(R.id.progressBarTwo);
        pwOne.startSpinning();

        myDialog = new Dialog(getActivity());
        messageDialog();

        SOET = (EditText) myDialog.findViewById(R.id.so_edittext);
        POET = (EditText) myDialog.findViewById(R.id.po_edittext);

        searchButtonText = (TextView) getView().findViewById(R.id.searchButtonText);

        adapter = new JobsAdapter(getContext(), jobArray);

        jobsListView = (ListView) getView().findViewById(R.id.jobListView);
        jobsListView.setAdapter(adapter);
        jobsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Job tempJob = jobArray.get(position);
                String jobID = tempJob.jobID;

                Intent intent = new Intent(getContext(), JobDetails.class);
                intent.putExtra("jobID", jobID);
                startActivity(intent);

            }
        });

        getJobs = new  getJobs();
        getJobs.execute();

        jobSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isSearch) {
                    isSearch = true;
                    myDialog.show();
                }else{
                    isSearch = false;
                    getJobs getNewJobs = new getJobs();
                    getNewJobs.execute();

                }

            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public class getJobs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isFragmentOnline = sessionManager.fragmentIsOnline();

            if(!isFragmentOnline){
                this.cancel(true);
            } else {
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

            if(isSearch){

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("po", params[0])
                        .add("so", params[1])
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "search_client_jobs")
                        .build();

                request = new Request.Builder().url(getJobsSearchURL).post(formBody).build();

            } else {

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "view_client_jobs")
                        .build();

                request = new Request.Builder().url(getJobsURL).post(formBody).build();

            }


            Response response = null;
            String result = null;

            jobArray.clear();

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

                    JSONArray taskInfoArray = jsonObject.getJSONArray("jobs");

                    JSONArray object2 = taskInfoArray.getJSONArray(0);

                    for (int i = 0; i < object2.length(); i++) {

                        JSONObject object3 = object2.getJSONObject(i);

                        String jobID  = object3.getString("jobID");
                        String description = object3.getString("description");
                        String status = object3.getString("status");
                        String PO = object3.getString("PO");
                        String SO = object3.getString("SO");
                        String customer = object3.getString("customer");
                        String dateCreated = object3.getString("dateCreated");
                        String notes = object3.getString("notes");

                        jobArray.add(new Job(jobID, description, status, PO, SO, customer, dateCreated, notes));

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

            if(isCancelled()){
                return;
            }

            spinnerContainer.setAlpha(0);

            if(isSearch){

                myDialog.dismiss();
                SO = "";
                PO = "";


                jobSearchButton.setBackground(getResources().getDrawable(R.drawable.cancel_button_shape));
                searchButtonText.setText("Cancel");

            } else {

                SOET.setText("");
                POET.setText("");

                searchButtonText.setText("Search");
                jobSearchButton.setBackground(getResources().getDrawable(R.drawable.green_button));
            }

            adapter.notifyDataSetChanged();


        }
    }


    public void messageDialog() {

        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.job_search_dialog);
        myDialog.setCancelable(false);

        TextView text = (TextView) myDialog.findViewById(R.id.dialog_message);


        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        text.setText("Fill out either of these fields to find a specific job");

        Button search = (Button) myDialog.findViewById(R.id.dialogJobSearchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                //check if any text was filled out
                SO = SOET.getText().toString();
                PO = POET.getText().toString();

                if (SO.matches("") && PO.matches("")) {

                    Toast.makeText(getContext(), "Please enter a purchase order number or a sales order number before hitting the search button", Toast.LENGTH_SHORT).show();

                } else {

                    getJobs searchJobs = new getJobs();
                    searchJobs.execute(PO,SO);

                }


            }
        });

        Button createAccount= (Button) myDialog.findViewById(R.id.dialogJobSearchCancelButton);
        createAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                myDialog.dismiss();


            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if(getJobs != null && getJobs.getStatus() == AsyncTask.Status.RUNNING) {
            getJobs.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getJobs != null && getJobs.getStatus() == AsyncTask.Status.RUNNING) {
            getJobs.cancel(true);
        }
    }
}
