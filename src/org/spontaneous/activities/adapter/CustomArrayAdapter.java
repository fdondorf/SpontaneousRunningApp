package org.spontaneous.activities.adapter;

import java.util.List;

import org.spontaneous.R;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.DateUtil;
import org.spontaneous.activities.util.StringUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomArrayAdapter extends ArrayAdapter<TrackModel> {
	  private final Context context;
	  private final List<TrackModel> values;

	  public CustomArrayAdapter(Context context, List<TrackModel> values) {
	    super(context, R.layout.row_layout, values);
	    this.context = context;
	    this.values = values;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		  LayoutInflater inflater = (LayoutInflater) context
				  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  
		  View rowView = inflater.inflate(R.layout.row_layout, parent, false);
		  TextView firstLineLeft = (TextView) rowView.findViewById(R.id.firstLine_left);
		  TextView firstLineRight = (TextView) rowView.findViewById(R.id.firstLine_right);
		  TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
		  TextView secondLineRight = (TextView) rowView.findViewById(R.id.secondLine_right);

		  ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		  
		  int pos = position + 1;
		  firstLineLeft.setText("Aktivität " + pos);
		  firstLineRight.setText(StringUtil.getDistanceString(values.get(position).getTotalDistance()));
		  //secondLine.setText(DateUtil.millisToShortDHMS(values.get(position).getTotalDuration()));
		  secondLine.setText(getSecondLineLeftText(position));
		  secondLineRight.setText(DateUtil.printDate(values.get(position).getCreationDate()));
	      imageView.setImageResource(R.drawable.ic_activity_dark);

		  return rowView;
	  }
	  
	  private String getSecondLineLeftText(int position) {
		  StringBuilder builder = new StringBuilder();
		  builder.append(DateUtil.printDate(values.get(position).getCreationDate()));
		  builder.append(" - ");
		  builder.append(values.get(position).getUserId());
		  return builder.toString();
	  }
}
