package org.spontaneous.activities.adapter;

import java.util.List;

import org.spontaneous.R;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.DateUtil;
import org.spontaneous.activities.util.StringUtil;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ActivityViewHolder>{
	 
	
	private List<TrackModel> tracks;
	 
	public RVAdapter(List<TrackModel> tracks) {
	    this.tracks = tracks;
	}
	
    public static class ActivityViewHolder extends RecyclerView.ViewHolder {      
        CardView cv;
        TextView activityName;
        TextView activityDate;
        TextView activityDuration;
        ImageView activityIcon;
 
        ActivityViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            activityName = (TextView) itemView.findViewById(R.id.activityName);
            activityDate = (TextView) itemView.findViewById(R.id.activityDate);
            activityIcon = (ImageView) itemView.findViewById(R.id.activityIcon);
            activityDuration = (TextView) itemView.findViewById(R.id.activityDuration);
        }
    }

	@Override
	public int getItemCount() {
		return this.tracks.size();
	}

	@Override
	public void onBindViewHolder(ActivityViewHolder personViewHolder, int i) {
	    personViewHolder.activityName.setText(tracks.get(i).getName());
	    personViewHolder.activityDate.setText(DateUtil.printDate(tracks.get(i).getCreationDate()));
	    personViewHolder.activityDuration.setText(StringUtil.getDistanceString(tracks.get(i).getTotalDistance()));
	    personViewHolder.activityIcon.setImageResource(R.drawable.ic_activity);
	}

	@Override
	public ActivityViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comp_activity_entry, viewGroup, false);
	    ActivityViewHolder pvh = new ActivityViewHolder(v);
	    return pvh;
	}
	
	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
	    super.onAttachedToRecyclerView(recyclerView);
	}

}
