package de.lmu.settlebattle.catanclient.grid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


public class ConstructionsLayer extends RelativeLayout {

  private boolean mWithholdTouchEventsFromChildren;

  public ConstructionsLayer(Context context) {
    this(context, null);
  }

  public ConstructionsLayer(Context context, AttributeSet attrs) {
    super(context, attrs, 0);
  }

  @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mWithholdTouchEventsFromChildren || super.onInterceptTouchEvent(ev);
    }

    public void setWithholdTouchEventsFromChildren(boolean withholdTouchEventsFromChildren) {
        mWithholdTouchEventsFromChildren = withholdTouchEventsFromChildren;
    }

}
