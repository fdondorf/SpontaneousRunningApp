package org.spontaneous.activities.view;

import org.spontaneous.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class CustomViewFlipper extends ViewFlipper {

	private Paint paint = new Paint();

	public CustomViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		int width = getWidth();
	
		float margin = 5;
		float radius = 10;
		float cx = (width / 2) - ((radius + margin) * 2 * getChildCount() / 2);
		float cy = getHeight() - 20;
	
		canvas.save();
	
		for (int i = 0; i < getChildCount(); i++) {
			if (i == getDisplayedChild()) {
				int colorBlue = getResources().getColor(R.color.ColorPrimary);
				paint.setColor(colorBlue);
				canvas.drawCircle(cx, cy, radius, paint);
			} else {
				int colorGrey = getResources().getColor(R.color.ColorGrey);
				paint.setColor(colorGrey);
				canvas.drawCircle(cx, cy, radius, paint);
			}
			cx += 2 * (radius + margin);
		}
		canvas.restore();
	}

}
