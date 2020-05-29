package com.giosis.util.qdrive.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSpinner;

public class NDSpinner extends AppCompatSpinner {

    public NDSpinner(Context context) {
        super(context);
    }

    public NDSpinner(Context context, int mode) {
        super(context, mode);
    }

    public NDSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NDSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NDSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public NDSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }


    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);

        if (sameSelected) {

            // spinner does not call the OnItemSelectedListener if the same item is selected,
            // so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position) {
        if (position == 7) {

            boolean sameSelected = position == getSelectedItemPosition();
            super.setSelection(position);

            if (sameSelected) {

                // spinner does not call the OnItemSelectedListener if the same item is selected,
                // so do it manually now
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            }
        }
         else {
            super.setSelection(position);
        }
    }
}