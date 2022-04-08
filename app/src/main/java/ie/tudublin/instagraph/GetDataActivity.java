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

public class GetDataActivity extends AppCompatActivity implements View.OnClickListener {
    Button chooseFile;
    Button next;
    ImageView dataPreview;
    TextView dataSummary;
    ImageView downloadIcon;

    Bitmap bmp;

    ParameterParcel userParameters;

    Downloader downloader;

    Popup pop;
    Popup waitPopup;
    Popup errorPopup;

    RelativeLayout mainLayout;

    PopupWindow popWindow;
    PopupWindow waitWindow;
    PopupWindow errorWindow;


    // *** Code referenced from https://www.youtube.com/watch?v=-y5eF0u1bZQ and https://www.youtube.com/watch?v=Ke9PaRdMcgc
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

                                dataPreview.setImageBitmap(bmp);

                                String summary = data.getStringExtra("summary");
                                dataSummary.setText(summary);
                                dataSummary.setVisibility(View.VISIBLE);
                                next.setAlpha(1);
                            }
                            catch (NullPointerException npe) {
                                errorWindow = errorPopup.showPopup(getString(R.string.preview_not_found), false);
                            }
                        }
                        else if (resCode != RESULT_CANCELED) {
                            // Otherwise, something went wrong
                            errorWindow = errorPopup.showPopup(getString(R.string.preview_unknown_error), false);
                        }
                    }
                }

        );
    // *** End code reference

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

        pop = new Popup(GetDataActivity.this, mainLayout);
        waitPopup = new Popup(GetDataActivity.this, mainLayout);
        errorPopup = new Popup(GetDataActivity.this, mainLayout);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.choose_file):
                // Go to GetURLActivity
                Intent goToChooseFile = new Intent(GetDataActivity.this, GetURLActivity.class);
                waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);
                getUrlLauncher.launch(goToChooseFile);
                break;

            case(R.id.next):
                // Check that a dataset has been selected
                if(userParameters == null || userParameters.getDatasetPath() == null) {
                    errorWindow = errorPopup.showPopup(getString(R.string.no_dataset),false);
                    return;
                }
                // Go to SelectGraphActivity
                Intent goToSelectGraph = new Intent(GetDataActivity.this, SelectGraphActivity.class);
                goToSelectGraph.putExtra("userParameters", userParameters);
                startActivity(goToSelectGraph);
                break;

            case(R.id.download_icon):
                // Download the preview
                if(bmp == null) {
                    errorWindow = errorPopup.showPopup(getString(R.string.no_preview), false);
                    break;
                }
                if(downloader == null) {
                    downloader = new Downloader(GetDataActivity.this, this);
                }
                downloader.savePlot(bmp);

                // Inform user of success
                popWindow = pop.showPopup(getString(R.string.preview_saved), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(popWindow != null && popWindow.isShowing()) {
            popWindow.dismiss();
        }
        if(waitWindow != null && waitWindow.isShowing()) {
            waitWindow.dismiss();
        }
        if(errorWindow != null && errorWindow.isShowing()) {
            errorWindow.dismiss();
        }
    }
}
