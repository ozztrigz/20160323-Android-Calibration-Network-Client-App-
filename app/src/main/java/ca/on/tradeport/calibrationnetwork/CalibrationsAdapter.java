package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CalibrationsAdapter extends ArrayAdapter<ca.on.tradeport.calibrationnetwork.Calibration> {

    public CalibrationsAdapter(Context context, ArrayList<ca.on.tradeport.calibrationnetwork.Calibration> calibrations) {
        super(context, 0, calibrations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ca.on.tradeport.calibrationnetwork.Calibration calibration = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.calibration_item, parent, false);
        }

        TextView tvCertificate = (TextView) convertView.findViewById(R.id.cal_certificate);

        if (tvCertificate != null){
            tvCertificate.setText(calibration.certificate);
        }


        return convertView;

    }
}
