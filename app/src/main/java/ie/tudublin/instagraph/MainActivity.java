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
                Popup p = new Popup(MainActivity.this, mainLayout);
                p.showPopup(R.string.instagraph_information, false);
                break;
        }
    }
}