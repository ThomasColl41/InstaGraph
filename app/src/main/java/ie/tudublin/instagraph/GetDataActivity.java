package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class GetDataActivity extends AppCompatActivity implements View.OnClickListener {
    Button chooseFile;
    Button next;
    ImageView dataPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_data_activity);

        chooseFile = findViewById(R.id.choose_file);
        next = findViewById(R.id.next);
        dataPreview = findViewById(R.id.data_preview);

        chooseFile.setOnClickListener(this);
        next.setOnClickListener(this);

        Intent intent = getIntent();

        try {
            byte[] image = intent.getByteArrayExtra("dataPreview");

            Bitmap bmp = BitmapFactory.decodeByteArray(image,0,image.length);

            dataPreview.setImageBitmap(bmp);
        }
        catch (Exception e) {
            Log.i("InstaGraph", e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.choose_file):
                Intent goToChooseFile = new Intent(GetDataActivity.this, GetURLActivity.class);
                startActivity(goToChooseFile);
                break;

            case(R.id.next):
                Intent goToSelectGraph = new Intent(GetDataActivity.this, SelectGraphActivity.class);
                startActivity(goToSelectGraph);
                break;
        }
    }
}
