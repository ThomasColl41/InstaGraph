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

public class PredictActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;

    ImageView plot_window;
    ImageView downloadIcon;

    Python py;
    PyObject instaGraphPyObject;
    PyObject plot_image;
    Bitmap bmp;

    ParameterParcel userParameters;

    Downloader downloader;

    RelativeLayout mainLayout;

    Popup pop;
    Popup waitPopup;
    Popup errorPopup;

    PopupWindow popWindow;
    PopupWindow waitWindow;
    PopupWindow errorWindow;

    int modelRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.predict_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        plot_window = findViewById(R.id.plot);
        downloadIcon = findViewById(R.id.download_icon);
        mainLayout = findViewById(R.id.main_layout);

        next.setOnClickListener(this);
        back.setOnClickListener(this);
        downloadIcon.setOnClickListener(this);

        pop = new Popup(PredictActivity.this, mainLayout);
        waitPopup = new Popup(PredictActivity.this, mainLayout);
        errorPopup = new Popup(PredictActivity.this, mainLayout);

        // Get user choices from previous activities
        // URL, graph choice, columns, etc.
        Intent fromSelectColumns = getIntent();
        userParameters = fromSelectColumns.getParcelableExtra("userParameters");

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
            String modelDataPath = instaGraphPyObject.callAttr(
                    "read_model_data",
                    userParameters.getDatasetPath(),
                    userParameters.getCol1(),
                    userParameters.getCol2(),
                    userParameters.getFirstLast(),
                    userParameters.getRowLimit()
            ).toString();

            userParameters.setModelDataPath(modelDataPath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Plot the graph depending on user choice
        try {
            plot_image = instaGraphPyObject.callAttr(
                    "graph_plot",
                    userParameters.getModelDataPath(),
                    userParameters.getGraphType(),
                    userParameters.getCol1(),
                    userParameters.getCol2(),
                    userParameters.getTitle()
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            modelRows = Integer.parseInt(instaGraphPyObject.callAttr("model_rows", userParameters.getModelDataPath()).toString());
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                // Check that ARIMA is suitable with the dataset
                try {
                    instaGraphPyObject.callAttr(
                            "arima_check",
                            userParameters.getModelDataPath(),
                            userParameters.getCol1(),
                            userParameters.getCol2(),
                            userParameters.getModel()
                    );
                }
                catch (Exception e) {
                    errorWindow = errorPopup.showPopup(e.getMessage(), false);
                    return;
                }

                if(modelRows < minimumPredictionRows(userParameters.getModel())) {
                    errorWindow = errorPopup.showPopup(getString(R.string.not_enough_rows_to_predict), false);
                    return;
                }
                Intent goToResult = new Intent(PredictActivity.this, ResultActivity.class);
                goToResult.putExtra("userParameters", userParameters);
                waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);
                startActivity(goToResult);
                break;

            case(R.id.back):
                finish();
                break;

            case(R.id.download_icon):
                if(downloader == null) {
                    downloader = new Downloader(PredictActivity.this, this);
                }
                downloader.savePlot(bmp);

                // Inform user
                popWindow = pop.showPopup(getString(R.string.graph_saved), false);

        }
    }

    // Method to get the minimum number of rows required for prediction
    public int minimumPredictionRows(String modelChoice) {
        switch(modelChoice) {
            case "AR":
                if(userParameters.getPara1().equals("")) {
                    // The default AR model required at least 26 rows for predictions
                    return 26;
                }
                else {
                    return (1 + Integer.parseInt(userParameters.getPara1()) * 2) + 1;
                }
            case "ARIMA":
                // If any of the custom parameters are not set, set them to be the highest possible default
                // Minimum rows for default model is 26
                if(userParameters.getPara1().equals("")) {
                    userParameters.setPara1("12");
                }
                if(userParameters.getPara2().equals("")) {
                    userParameters.setPara2("2");
                }
                if(userParameters.getPara3().equals("")) {
                    userParameters.setPara3("1");
                }
                return ((1 +Integer.parseInt(userParameters.getPara1()) +
                        Integer.parseInt(userParameters.getPara2()) +
                        Integer.parseInt(userParameters.getPara3())) * 2) + 1;

            case "SES":
                // No additional rows required for prediction in SES
                return 2;

            case "HWES":
                if(userParameters.getPara3().equals("")) {
                    userParameters.setPara3("12");
                }
                // At least 13 rows required for default seasonal period of 12
                return Integer.parseInt(userParameters.getPara3()) + 1;
        }

        // By default, return the highest default requirement
        return 26;
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
