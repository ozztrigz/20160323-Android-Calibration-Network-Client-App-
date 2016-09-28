package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CalDueAdapter extends ArrayAdapter<Instrument> {


    public CalDueAdapter(Context context, ArrayList<Instrument> instruments) {
        super(context, 0, instruments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Instrument instrument = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.calibration_due_item, parent, false);
        }

        TextView tvInstrumentID = (TextView) convertView.findViewById(R.id.instrument_itemID);
        TextView tvInstrumentName = (TextView) convertView.findViewById(R.id.instrument_due_item_name);
        TextView tvInstrumentSerial = (TextView) convertView.findViewById(R.id.instrument_due_item_serial);
        TextView tvInstrumentAsset = (TextView) convertView.findViewById(R.id.instrument_due_item_asset);
        TextView tvInstrumentCaltype = (TextView) convertView.findViewById(R.id.instrument_due_item_caltype);

        if(tvInstrumentID != null){
            tvInstrumentID.setText(instrument.instrumentID);
        }

        if(tvInstrumentAsset != null) {
            tvInstrumentAsset.setText(instrument.asset);
        }

        if (tvInstrumentName != null){
            tvInstrumentName.setText(instrument.name);
        }

        if (tvInstrumentSerial != null){
            tvInstrumentSerial.setText(instrument.serial);
        }

        if(tvInstrumentCaltype != null) {

            if(instrument.calType == 1) {
                tvInstrumentCaltype.setText("traceable");
            }else{
                tvInstrumentCaltype.setText("ISO 17025");
            }
        }


        return convertView;

    }

}
