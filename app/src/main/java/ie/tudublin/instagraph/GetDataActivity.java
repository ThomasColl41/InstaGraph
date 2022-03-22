package ie.tudublin.instagraph;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GetDataActivity extends AppCompatActivity implements View.OnClickListener {
    Button chooseFile;
    Button next;
    ImageView dataPreview;
    TextView dataSummary;
    ImageView downloadIcon;

    Bitmap bmp;

    ParameterParcel userParameters;

    Downloader downloader;

    Popup waitPopup;

    RelativeLayout mainLayout;

    PopupWindow popWindow;


    // Inspired from https://www.youtube.com/watch?v=-y5eF0u1bZQ and https://www.youtube.com/watch?v=Ke9PaRdMcgc
    // StartActivityForResult alternative implementation
    ActivityResultLauncher<Intent> getUrlLauncher =
        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    // When GetURLActivity returns a value, this method is called
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Get the result code and data
                        int resCode = result.getResultCode();
                        Intent data = result.getData();

                        // if things went ok (the user pressed submit)
                        if(resCode == RESULT_OK && data != null) {
                            try {
                                byte[] image = data.getByteArrayExtra("dataPreview");
                                bmp = BitmapFactory.decodeByteArray(image,0,image.length);
                                userParameters = data.getParcelableExtra("userParameters");
                                Log.i("InstaGraph", "Returned dataset path: " + userParameters.getDatasetPath());

                                dataPreview.setImageBitmap(bmp);

                                String summary = data.getStringExtra("summary");
                                dataSummary.setText(summary);
                                dataSummary.setVisibility(View.VISIBLE);
                            }
                            catch (NullPointerException npe) {
                                Toast.makeText(GetDataActivity.this, npe.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            // Otherwise, something went wrong
                            Toast.makeText(GetDataActivity.this, "Something went wrong " + resCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_data_activity);

        chooseFile = findViewById(R.id.choose_file);
        next = findViewById(R.id.next);
        dataPreview = findViewById(R.id.data_preview);
        dataSummary = findViewById(R.id.data_summary);
        downloadIcon = findViewById(R.id.download_icon);
        mainLayout = findViewById(R.id.main_layout);

        chooseFile.setOnClickListener(this);
        next.setOnClickListener(this);
        downloadIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.choose_file):
                Intent goToChooseFile = new Intent(GetDataActivity.this, GetURLActivity.class);
                waitPopup = new Popup(GetDataActivity.this, mainLayout);
                popWindow = waitPopup.showPopup(getResources().getString(R.string.please_wait), true);
                getUrlLauncher.launch(goToChooseFile);
                break;

            case(R.id.next):
                Intent goToSelectGraph = new Intent(GetDataActivity.this, SelectGraphActivity.class);
                goToSelectGraph.putExtra("userParameters", userParameters);
                startActivity(goToSelectGraph);
                break;

            case(R.id.download_icon):
                if(bmp == null) {
                    Toast.makeText(this, "No preview to save (Choose File...)", Toast.LENGTH_SHORT).show();
                    break;
                }
                if(downloader == null) {
                    downloader = new Downloader(GetDataActivity.this, this);
                }
                downloader.savePlot(bmp);

                // Inform user
                Toast.makeText(this, "Preview saved to Download folder", Toast.LENGTH_SHORT).show();
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
