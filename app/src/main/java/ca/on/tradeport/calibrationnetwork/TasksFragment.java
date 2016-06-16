package ca.on.tradeport.calibrationnetwork;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TasksFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String MY_PREFS_NAME = "session";

    private View rootView;

    ListView taskListView;
    ArrayList<Task> taskArray = new ArrayList<>();
    public TasksAdapter adapter;
    String error_message;
    Boolean isSearch = false;
    RequestBody formBody;
    public SessionManager sessionManager;
    getTasks getTasks;
    Boolean isFragmentOnline = false;

    MasterActivity master;

    SharedPreferences sharedPreferences;
    public ProgressWheel pwOne;
    public RelativeLayout spinnerContainer;

    String Cert = "", Serial = "", DR = "", PO = "";
    LinearLayout footerTaskSearchButton;
    RelativeLayout taskSearchButton;
    TextView taskSearchButtonText;

    final String getTasksURL = "http://com.tradeport.on.ca/view_client_tasks.php";
    final String getTasksSearchURL = "http://com.tradeport.on.ca/view_client_tasks_search.php";

    private List<String> myData;

    View parentView;

    private Boolean isCalledFromMaster;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TasksFragment() {
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

    public TasksFragment newInstance(Boolean param1, String param2) {
        TasksFragment fragment = new TasksFragment();
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
        rootView = inflater.inflate(R.layout.fragment_tasks, container, false);

        Log.i("testing_methods", "onCreateView");



        sessionManager = new SessionManager(getActivity());
        isFragmentOnline = sessionManager.fragmentIsOnline();
        sharedPreferences = rootView.getContext().getApplicationContext().getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);


        spinnerContainer = (RelativeLayout) rootView.findViewById(R.id.spinnerContainer);
        taskSearchButton = (RelativeLayout) rootView.findViewById(R.id.taskSearchButton);
        taskSearchButtonText = (TextView) rootView.findViewById(R.id.taskSearchButtonText);
        taskListView = (ListView) rootView.findViewById(R.id.taskListView);
        pwOne = (ProgressWheel) rootView.findViewById(R.id.progressBarTwo);

        pwOne.startSpinning();
        adapter = new TasksAdapter(rootView.getContext(), taskArray);
        taskListView.setAdapter(adapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Task tempTask = taskArray.get(position);

                String taskID = tempTask.taskID;
                String cert = tempTask.certificate;

                Intent intent = new Intent(rootView.getContext(), TaskDetails.class);
                intent.putExtra("taskID", taskID);
                intent.putExtra("cert", cert);
                startActivity(intent);
            }
        });





        getTasks = new  getTasks();
        getTasks.execute();



        taskSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isSearch) {
                    isSearch = true;
                    messageDialog(v.getContext());
                }else{
                    isSearch = false;
                    getTasks getNewTasks = new getTasks();
                    getNewTasks.execute();

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


    public class getTasks extends AsyncTask<String, String, String> {

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
                        .add("po", params[2])
                        .add("dr", params[3])
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "search_client_tasks")
                        .build();

                request = new Request.Builder().url(getTasksSearchURL).post(formBody).build();

            } else {

                formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("uid", userID)
                        .add("key", key)
                        .add("tag", "view_client_tasks")
                        .build();

                request = new Request.Builder().url(getTasksURL).post(formBody).build();

            }

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

                JSONObject jsonObject = new JSONObject(result);

                //check if success is equal to 1
                int success = jsonObject.getInt("success");

                if (success == 1){


                    String newExpiryDate = jsonObject.getString("session_expire");
                    sessionManager.updateExpireDate(newExpiryDate);


                    JSONArray taskInfoArray = jsonObject.getJSONArray("tasks");

                    JSONArray object2 = taskInfoArray.getJSONArray(0);

                    for (int i = 0; i < object2.length(); i++) {

                        JSONObject object3 = object2.getJSONObject(i);

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
                DR = "";
                PO = "";

                if (taskSearchButton != null && taskSearchButtonText != null) {
                    taskSearchButton.setBackground(getResources().getDrawable(R.drawable.cancel_button_shape));
                    taskSearchButtonText.setText("Cancel");
                }

            } else {

                if (taskSearchButton != null && taskSearchButtonText != null) {
                    taskSearchButtonText.setText("Search");
                    taskSearchButton.setBackground(getResources().getDrawable(R.drawable.green_button));
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
        taskSearchButton = null;
        taskSearchButtonText = null;
        taskListView = null;

        if(getTasks != null && getTasks.getStatus() == AsyncTask.Status.RUNNING){
            getTasks.cancel(true);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("testing_methods", "onPause");

        setRetainInstance(true);

        if(getTasks != null && getTasks.getStatus() == AsyncTask.Status.RUNNING){
            getTasks.cancel(true);
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
        dialog.setContentView(R.layout.task_search_dialog);
        dialog.setCancelable(false);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_message);
        final EditText certificate = (EditText) dialog.findViewById(R.id.TaskSearchDialogCertificateEditText);
        final EditText serial = (EditText) dialog.findViewById(R.id.TaskSearchDialogSerialEditText);
        final EditText purchaseOrderNumber = (EditText) dialog.findViewById(R.id.TaskSearchDialogPurchaseOrderEditText);

        final TextView dateFrom = (TextView) dialog.findViewById(R.id.date_dialog_from);
        final TextView dateTo = (TextView) dialog.findViewById(R.id.date_dialog_to);

        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar mcurrentdate = Calendar.getInstance();
                int mYear = mcurrentdate.get(Calendar.YEAR);
                int mMonth = mcurrentdate.get(Calendar.MONTH);
                int mDay = mcurrentdate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        String newFromDate = String.valueOf(year)+"-"+String.valueOf(monthOfYear)+"-"+String.valueOf(dayOfMonth);

                        dateFrom.setText(newFromDate);

                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select \"From\" Date");
                mDatePicker.show();
            }
        });

        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar mcurrentdate = Calendar.getInstance();
                int mYear = mcurrentdate.get(Calendar.YEAR);
                int mMonth = mcurrentdate.get(Calendar.MONTH);
                int mDay = mcurrentdate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        String newToDate = String.valueOf(year)+"-"+String.valueOf(monthOfYear)+"-"+String.valueOf(dayOfMonth);

                        dateTo.setText(newToDate);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select \"To\" Date");
                mDatePicker.show();

            }
        });

        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        text.setText("Fill out either of these fields to find a specific task");

        Button search = (Button) dialog.findViewById(R.id.dialogJobSearchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if any text was filled out

                Cert = certificate.getText().toString();
                Serial = serial.getText().toString();
                PO = purchaseOrderNumber.getText().toString();

                String dateFromText = dateFrom.getText().toString();
                String dateToText = dateTo.getText().toString();

                DR = dateFromText+"-"+dateToText;


                if (Cert.matches("") && Serial.matches("") && PO.matches("") && DR.matches("-----")) {

                    Toast.makeText(getContext(), "Please enter a criteria before hitting the search button", Toast.LENGTH_SHORT).show();

                } else {

                    TasksFragment.getTasks searchTasks = new TasksFragment.getTasks();
                    searchTasks.execute(Cert, Serial, PO, DR);
                    dialog.dismiss();

                }


            }
        });

        Button createAccount= (Button) dialog.findViewById(R.id.dialogJobSearchCancelButton);
        createAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dialog.dismiss();
                isSearch = false;


            }
        });


        dialog.show();
    }






}
