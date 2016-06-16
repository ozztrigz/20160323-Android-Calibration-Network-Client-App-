package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class JobsAdapter extends ArrayAdapter<Job> {

    public JobsAdapter(Context context, ArrayList<Job> jobs) {
        super(context, 0, jobs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){


        Job job = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.job_item, parent, false);
        }

        TextView tvJobPO = (TextView) convertView.findViewById(R.id.po_text);
        TextView tvJobSO = (TextView) convertView.findViewById(R.id.so_text);
        TextView tvJobDate = (TextView) convertView.findViewById(R.id.date_text);

        if(tvJobPO != null){
            tvJobPO.setText(job.PO);
        }

        if(tvJobSO != null){
            tvJobSO.setText(job.SO);
        }

        if(tvJobDate != null){
            tvJobDate.setText(job.dateCreated);
        }


        return convertView;

    }

}
