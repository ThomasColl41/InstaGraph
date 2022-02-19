package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button start;
    Button info_button;
    TextView info_window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        info_button = findViewById(R.id.info_button);
        info_window = findViewById(R.id.info_window);

        start.setOnClickListener(this);
        info_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.start):
                // Make information window invisible
                info_window.setVisibility(View.INVISIBLE);

                Intent goToGetData = new Intent(MainActivity.this, GetDataActivity.class);
                startActivity(goToGetData);
                break;

            case(R.id.info_button):
                // Display info window
                if(info_window.getVisibility() == View.INVISIBLE) {
                    info_window.setVisibility(View.VISIBLE);
                }
                else {
                    info_window.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }
}