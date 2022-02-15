package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectGraphActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_graph_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);

        next.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToSelectColumns = new Intent(SelectGraphActivity.this, SelectColumnsActivity.class);
                startActivity(goToSelectColumns);
                break;

            case(R.id.back):
                finish();
                break;
        }
    }
}
