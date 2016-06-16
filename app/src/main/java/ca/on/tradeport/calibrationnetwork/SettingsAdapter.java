package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class SettingsAdapter extends ArrayAdapter<Settings> {

    public SettingsAdapter(Context context, ArrayList<Settings> settings) {
        super(context, 0 , settings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Settings settings = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item, parent, false);
        }

        TextView tvLabel = (TextView) convertView.findViewById(R.id.settings_label);
        TextView tvText = (TextView) convertView.findViewById(R.id.settings_text);

        if (tvLabel != null) {
            tvLabel.setText(settings.label);
        }

        if(tvText != null) {
            tvText.setText(settings.value);
        }


        return convertView;
    }
}
