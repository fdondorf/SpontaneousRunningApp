package org.spontaneous.activities.adapter;

import java.util.List;

import org.spontaneous.R;
import org.spontaneous.activities.model.SplitTimeModel;
import org.spontaneous.activities.util.DateUtil;
import org.spontaneous.activities.util.StringUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SplitTimeArrayAdapter extends ArrayAdapter<SplitTimeModel> {
	
	  private final Context context;
	  private final List<SplitTimeModel> values;

	  public SplitTimeArrayAdapter(Context context, List<SplitTimeModel> values) {
	    super(context, R.layout.row_layout_splittimes, values);
	    this.context = context;
	    this.values = values;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		  LayoutInflater inflater = (LayoutInflater) context
				  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  
		  View rowView = inflater.inflate(R.layout.row_layout_splittimes, parent, false);
		  TextView firstLineLeft = (TextView) rowView.findViewById(R.id.firstLine_left);
		  TextView firstLineRight = (TextView) rowView.findViewById(R.id.firstLine_right);
		  ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		  int pos = position + 1;
		  firstLineLeft.setText("Kilometer " + pos);
		  firstLineRight.setText(DateUtil.millisToShortDHMS(values.get(position).getTime()));
	      imageView.setImageResource(R.drawable.ic_splittime);

		  return rowView;
	  }
}
