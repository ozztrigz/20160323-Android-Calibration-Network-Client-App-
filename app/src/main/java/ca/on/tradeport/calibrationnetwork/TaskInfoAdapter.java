package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class TaskInfoAdapter extends ArrayAdapter<Settings> {

    public TaskInfoAdapter(Context context, ArrayList<Settings> settings) {
        super(context, 0 , settings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Settings settings = getItem(position);


        if(settings.label == "label") {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_info_label, parent, false);

            TextView tvInfoLabel = (TextView) convertView.findViewById(R.id.task_info_label);

            if (tvInfoLabel != null) {
                tvInfoLabel.setText(settings.value);
            }

        } else if(settings.label == "calculation file" || settings.label == "certificate") {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_info_button, parent, false);

            TextView tvButtonText = (TextView) convertView.findViewById(R.id.task_button_label);

            if (tvButtonText != null) {
                tvButtonText.setText(settings.label);
            }
        } else {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_info_item, parent, false);

            TextView tvLabel = (TextView) convertView.findViewById(R.id.task_info_item_label);
            TextView tvText = (TextView) convertView.findViewById(R.id.task_info_item_text);

            if (tvLabel != null) {
                tvLabel.setText(settings.label);
            }

            if (tvText != null) {
                tvText.setText(settings.value);
            }
        }


        return convertView;


    }
}
