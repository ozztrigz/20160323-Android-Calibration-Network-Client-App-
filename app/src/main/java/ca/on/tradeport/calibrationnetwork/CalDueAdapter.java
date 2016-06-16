package ca.on.tradeport.calibrationnetwork;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import java.util.ArrayList;


public class CalDueAdapter extends RecyclerView.Adapter<CalDueAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    ArrayList<Instrument> data = new ArrayList<>();
    private Context context;

    public CalDueAdapter(Context context) {

        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setInstrumentList(ArrayList<Instrument> instruments) {

        this.data = instruments;
        notifyItemRangeChanged(0, instruments.size());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.calibration_due_item, parent, false);
        MyViewHolder MyViewholder = new MyViewHolder(view);

        return MyViewholder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Instrument instrument = data.get(position);
        holder.TVID.setText(instrument.instrumentID);
        holder.TVName.setText(instrument.name);
        holder.TVSerial.setText(instrument.serial);
        holder.TVAsset.setText(instrument.asset);

        if(instrument.calType == 1) {
            holder.TVCalibration.setText("ISO 17025 Accredited");
        } else {
            holder.TVCalibration.setText("Traceable");
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView TVName;
        TextView TVID;
        TextView TVAsset;
        TextView TVSerial;
        TextView TVCalibration;
        CardView cardView;



        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            TVID = (TextView) itemView.findViewById(R.id.instrument_itemID);
            TVName = (TextView) itemView.findViewById(R.id.instrument_due_item_name);
            TVAsset = (TextView) itemView.findViewById(R.id.instrument_due_item_asset);
            TVSerial = (TextView) itemView.findViewById(R.id.instrument_due_item_serial);
            TVCalibration = (TextView) itemView.findViewById(R.id.instrument_due_item_caltype);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {

            cardView.setAnimation(new AlphaAnimation(1,0));

        }


    }


}
