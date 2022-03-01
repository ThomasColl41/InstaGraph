package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button start;
    Button info_button;

    RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        info_button = findViewById(R.id.info_button);
        mainLayout = findViewById(R.id.main_layout);

        start.setOnClickListener(this);
        info_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.start):
                Intent goToGetData = new Intent(MainActivity.this, GetDataActivity.class);
                startActivity(goToGetData);
                break;

            case(R.id.info_button):
                // Display info pop-up
                showInfoPopup();
                break;
        }
    }

    public void showInfoPopup() {
        View infoView;

        // Create inflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        try {
            // Inflate the popup (resource (xml), root, attachToRoot)
            // attachToRoot is false because only the LayoutParams of mainLayout are required
            infoView = inflater.inflate(R.layout.popup, mainLayout, false);
        }
        catch (InflateException ie) {
            Log.i("InstaGraph", ie.getMessage());
            Toast.makeText(
                    this,
                    "Error displaying information (" + ie.getMessage() + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create PopupWindow (view, width, height, focusable)
        // focusable is true so the popup can be dismissed by tapping anywhere
        PopupWindow infoPopup = new PopupWindow(
                infoView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Identify the TexView in the popup xml and set its text dynamically
        TextView popupText = infoView.findViewById(R.id.popup_textview);
        popupText.setText(R.string.instagraph_information);

        // Display the popup in the center of the screen (layout)
        infoPopup.showAtLocation(mainLayout, Gravity.CENTER, 0,0);

        // Set up an onTouchListener to react to the user tapping the screen
        // A lambda method is used for the onTouch method
        infoView.setOnTouchListener((view, motionEvent) -> {
            // Hide the popup
            infoPopup.dismiss();

            // Perform a click to call the OnClickListener and register the clicking event
            // for sound preferences and accessibility features
            view.performClick();
            return true;
        });
    }
}