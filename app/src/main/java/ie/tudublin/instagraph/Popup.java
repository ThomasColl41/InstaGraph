package ie.tudublin.instagraph;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class Popup extends PopupWindow {
    // Attributes
    Context context;
    RelativeLayout parentLayout;

    public Popup(Context context, RelativeLayout parentLayout) {
        this.context = context;
        this.parentLayout = parentLayout;
    }

    public PopupWindow showPopup(String text, boolean hasProgressBar) {
        View popupView;

        // Create inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            // Inflate the popup (resource (xml), root, attachToRoot)
            // attachToRoot is false because only the LayoutParams of mainLayout are required
            popupView = inflater.inflate(R.layout.popup, parentLayout, false);
        }
        catch (InflateException ie) {
            Log.i("InstaGraph", ie.getMessage());
            Toast.makeText(
                    context,
                    "Error displaying information (" + ie.getMessage() + ")",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        // Create PopupWindow (view, width, height, focusable)
        // focusable is true so the popup can be dismissed by tapping anywhere
        PopupWindow popWindow = new PopupWindow(
                popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Identify the TextView in the popup xml and set its text dynamically
        TextView popupText = popupView.findViewById(R.id.popup_textview);
        popupText.setText(text);

        // Identify the ProgressBar in the popup xml and set its visibility if needed
        if(hasProgressBar) {
            ProgressBar pBar = popupView.findViewById(R.id.progress_bar);
            pBar.setVisibility(View.VISIBLE);
        }

        // Display the popup in the center of the screen (layout)
        popWindow.showAtLocation(parentLayout, Gravity.CENTER, 0,0);

        // Set up an onTouchListener to react to the user tapping the screen
        // A lambda method is used for the onTouch method
        popupView.setOnTouchListener((view, motionEvent) -> {
            // Hide the popup
            popWindow.dismiss();

            // Perform a click to call the OnClickListener and register the clicking event
            // for sound preferences and accessibility features
            view.performClick();
            return true;
        });
        return popWindow;
    }

    // Getters and Setters
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public RelativeLayout getParentLayout() {
        return parentLayout;
    }

    public void setParentLayout(RelativeLayout parentLayout) {
        this.parentLayout = parentLayout;
    }


    //toString
    @NonNull
    @Override
    public String toString() {
        return "Popup{" +
                "context=" + context +
                ", parentLayout=" + parentLayout +
                '}';
    }
}
