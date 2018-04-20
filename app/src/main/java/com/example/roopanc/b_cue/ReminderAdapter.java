package com.example.roopanc.b_cue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by RadhikaRanganathan on 01/03/2018.
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.MyViewHolder> {

    private List<EventPOJO> eventPOJOList;
    Context context;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    String EVENT_URL = "https://b-cue-658ce.firebaseio.com/Events";
    String Event_ID;

    public ReminderAdapter(List<EventPOJO> eventPOJOList, Context context)
    {
        this.eventPOJOList = eventPOJOList;
        this.context = context;

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReferenceFromUrl(EVENT_URL);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminderlistholder, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        EventPOJO eventPOJO = eventPOJOList.get(position);
        holder.nametv.setText(eventPOJO.getEventName());
        holder.typetv.setText(eventPOJO.getEventType());
        holder.infotv.setText(eventPOJO.getAddInfo());

        Event_ID = eventPOJO.getEventID();

        holder.priority.setVisibility(View.GONE);

        if (Integer.valueOf(eventPOJO.getPriority()) == 1)
        {
               holder.priority.setImageResource(R.drawable.colostar);
               holder.priority.setVisibility(View.VISIBLE);
        }

        holder.holderLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // code to pop up (Edit or Delete) Option

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Choose the action to perform");
                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //show edit fragment --- passing the event id
                        Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //passing the event id... delete the entry
                        Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                        databaseReference.child(Event_ID).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(context, "Event Deleted", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventPOJOList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView nametv, typetv, infotv;
        public ImageView priority, delete;
        public LinearLayout holderLinear;

        public MyViewHolder(View itemView) {
            super(itemView);

            nametv = itemView.findViewById(R.id.eventnametv);
            typetv = itemView.findViewById(R.id.eventtypetv);
            infotv = itemView.findViewById(R.id.eventinfotv);
            holderLinear = itemView.findViewById(R.id.holderlinear);

            priority = itemView.findViewById(R.id.priorityev);

        }
    }
}
