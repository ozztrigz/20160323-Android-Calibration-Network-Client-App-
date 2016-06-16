package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class InstrumentAdapter extends ArrayAdapter<Instrument> {


        public InstrumentAdapter(Context context, ArrayList<Instrument> instruments) {
            super(context, 0, instruments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Instrument instrument = getItem(position);

            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.instrument_item, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.instrument_item_name);
            TextView tvSerial = (TextView) convertView.findViewById(R.id.instrument_item_serial);
            TextView tvAsset = (TextView) convertView.findViewById(R.id.instrument_item_asset);
            TextView tvInstrumentID = (TextView) convertView.findViewById(R.id.instrument_itemID);

            if (tvName != null){
                tvInstrumentID.setText(instrument.instrumentID);
            }

            if (tvName != null){
                tvName.setText(instrument.name);
            }
            if (tvSerial != null) {
                tvSerial.setText(instrument.serial);
            }

            if (tvAsset != null) {
                tvAsset.setText(instrument.asset);
            }

            return convertView;

        }





}
