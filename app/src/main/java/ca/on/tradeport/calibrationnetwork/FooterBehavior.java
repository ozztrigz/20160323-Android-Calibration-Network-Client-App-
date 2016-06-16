package ca.on.tradeport.calibrationnetwork;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

public class FooterBehavior extends AppBarLayout.ScrollingViewBehavior {

    private View layout;

    public FooterBehavior() {}

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        boolean result = super.onDependentViewChanged(parent, child, dependency);
        if (layout != null) {
            layout.setPadding(layout.getPaddingLeft(), layout.getPaddingTop(), layout
                    .getPaddingRight(), layout.getTop());
        }
        return result;
    }

    public void setLayout(View layout) {
        this.layout = layout;
    }

}
