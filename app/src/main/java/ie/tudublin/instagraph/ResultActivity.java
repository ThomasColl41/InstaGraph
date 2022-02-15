package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {
    Button finish;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);

        finish = findViewById(R.id.finish);
        back = findViewById(R.id.back);

        finish.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.finish):
                Intent goToMain = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(goToMain);
                break;

            case(R.id.back):
                finish();
                break;
        }
    }
}
