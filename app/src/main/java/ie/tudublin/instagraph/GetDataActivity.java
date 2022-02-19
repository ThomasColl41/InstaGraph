package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GetDataActivity extends AppCompatActivity implements View.OnClickListener {
    Button chooseFile;
    Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_data_activity);

        chooseFile = findViewById(R.id.choose_file);
        next = findViewById(R.id.next);

        chooseFile.setOnClickListener(this);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.choose_file):
                Intent goToChooseFile = new Intent(GetDataActivity.this, GetURLActivity.class);
                startActivity(goToChooseFile);
//                Toast.makeText(this, "YOOOOOOO", Toast.LENGTH_SHORT).show();
                break;

            case(R.id.next):
                Intent goToSelectGraph = new Intent(GetDataActivity.this, SelectGraphActivity.class);
                startActivity(goToSelectGraph);
                break;
        }
    }
}
