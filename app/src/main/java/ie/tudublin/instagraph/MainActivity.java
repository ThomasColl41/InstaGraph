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
    Button info_button;

    RelativeLayout mainLayout;

    Popup waitPopup;
    PopupWindow popWindow;

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
                waitPopup = new Popup(MainActivity.this, mainLayout);
                popWindow = waitPopup.showPopup(getResources().getString(R.string.please_wait), true);
                startActivity(goToGetData);
                break;

            case(R.id.info_button):
                // Display info pop-up
                Popup infoPopup = new Popup(MainActivity.this, mainLayout);
                infoPopup.showPopup(getResources().getString(R.string.instagraph_information), false);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(popWindow != null && popWindow.isShowing()) {
            popWindow.dismiss();
        }

    }

}