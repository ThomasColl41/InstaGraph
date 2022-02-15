package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PredictActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.predict_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);

        next.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToResult = new Intent(PredictActivity.this, ResultActivity.class);
                startActivity(goToResult);
                break;

            case(R.id.back):
                finish();
                break;
        }
    }
}
