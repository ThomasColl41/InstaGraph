package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button start;
    Button infoButton;

    RelativeLayout mainLayout;

    Popup infoPopup;
    PopupWindow infoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        infoButton = findViewById(R.id.info_button);
        mainLayout = findViewById(R.id.main_layout);

        infoPopup = new Popup(MainActivity.this, mainLayout);

        start.setOnClickListener(this);
        infoButton.setOnClickListener(this);
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
                infoWindow = infoPopup.showPopup(getString(R.string.instagraph_information), false);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(infoWindow != null && infoWindow.isShowing()) {
            infoWindow.dismiss();
        }

    }

}