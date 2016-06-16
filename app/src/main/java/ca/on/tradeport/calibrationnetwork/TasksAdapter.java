package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by webmaster on 16-03-03.
 */
public class TasksAdapter extends ArrayAdapter<Task> {

    public TasksAdapter(Context context, ArrayList<Task> tasks) {
        super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Task task = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, parent, false);
        }

        TextView tvCertificate = (TextView) convertView.findViewById(R.id.cert_text);
        TextView tvModel = (TextView) convertView.findViewById(R.id.model_text);

        if (tvCertificate != null){
            tvCertificate.setText(task.certificate);
        }
        if (tvModel != null) {
            tvModel.setText(task.model);
        }

        return convertView;

    }

}
