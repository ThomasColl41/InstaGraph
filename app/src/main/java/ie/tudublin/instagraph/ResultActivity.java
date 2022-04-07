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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.IOException;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {
    Button finish;
    Button back;

    ImageView plot_window;
    ImageView jpgIcon;
    ImageView csvIcon;

    Python py;
    PyObject instaGraphPyObject;
    PyObject plot_image;
    Bitmap bmp;

    ParameterParcel userParameters;

    Downloader downloader;

    RelativeLayout mainLayout;

    Popup pop;

    PopupWindow popWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);

        finish = findViewById(R.id.finish);
        back = findViewById(R.id.back);
        plot_window = findViewById(R.id.plot);
        jpgIcon = findViewById(R.id.jpg_icon);
        csvIcon = findViewById(R.id.csv_icon);
        mainLayout = findViewById(R.id.main_layout);

        finish.setOnClickListener(this);
        back.setOnClickListener(this);
        jpgIcon.setOnClickListener(this);
        csvIcon.setOnClickListener(this);

        pop = new Popup(ResultActivity.this, mainLayout);

        // Get user choices from previous activities
        // URL, graph choice, columns, etc.
        Intent fromPredict = getIntent();
        userParameters = fromPredict.getParcelableExtra("userParameters");

        // Log the provided URL
        Log.i("InstaGraph", userParameters.getUrl());

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        try {
            // Run the predict function
            plot_image = instaGraphPyObject.callAttr(
                    "predict",
                    userParameters.getModelDataPath(),
                    userParameters.getCol1(),
                    userParameters.getCol2(),
                    userParameters.getTitle(),
                    userParameters.getGraphType(),
                    userParameters.getModel(),
                    userParameters.getPara1(),
                    userParameters.getPara2(),
                    userParameters.getPara3(),
                    userParameters.getPara4(),
                    userParameters.getNumPredictions()
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Get the directory where the predictions have been saved
            String predPath = instaGraphPyObject.callAttr("read_predictions").toString();
            userParameters.setPredictionsPath(predPath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Decode the byte array and display the visualisation bitmap
        try {
            byte[] plot = plot_image.toJava(byte[].class);
            bmp = BitmapFactory.decodeByteArray(plot,0, plot.length);

            plot_window.setImageBitmap(bmp);
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.finish):
                Intent goToMain = new Intent(ResultActivity.this, MainActivity.class);
                goToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(goToMain);
                break;

            case(R.id.back):
                finish();
                break;

            case(R.id.jpg_icon):
                if(downloader == null) {
                    downloader = new Downloader(ResultActivity.this, this);
                }
                downloader.savePlot(bmp);

                // Inform user
                popWindow = pop.showPopup(getString(R.string.graph_saved), false);
                break;

            case(R.id.csv_icon):
                if(downloader == null) {
                    downloader = new Downloader(ResultActivity.this, this);
                }
                try {
                    downloader.saveFile(userParameters.getPredictionsPath());
                } catch (IOException e) {
                    Log.i("InstaGraph", "Failed to save predictions " + e.getMessage());
                }

                // Inform user
                popWindow = pop.showPopup(getString(R.string.predictions_saved), false);
                break;
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
