package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button start;
    Button info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        info = findViewById(R.id.info);

        start.setOnClickListener(this);
        info.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.start):
                Intent goToGetData = new Intent(MainActivity.this, GetDataActivity.class);
                startActivity(goToGetData);
                break;

            case(R.id.info):
                // Display info window
                break;
        }
    }
}