package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PredictActivity extends AppCompatActivity implements View.OnClickListener {
    Button changeGraphButton;
    Button hide;
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

    PyObject dataFrame;

    ImageButton lineGraph;
    ImageButton barChart;
    ImageButton pieChart;
    ImageButton horizontalBarChart;

    int modelRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.predict_activity);

        changeGraphButton = findViewById(R.id.change_graph);
        hide = findViewById(R.id.hide);
        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        plot_window = findViewById(R.id.plot);
        downloadIcon = findViewById(R.id.download_icon);
        mainLayout = findViewById(R.id.main_layout);
        lineGraph = findViewById(R.id.line_graph);
        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        horizontalBarChart = findViewById(R.id.horizontal_bar_chart);

        changeGraphButton.setOnClickListener(this);
        hide.setOnClickListener(this);
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        downloadIcon.setOnClickListener(this);
        lineGraph.setOnClickListener(this);
        barChart.setOnClickListener(this);
        pieChart.setOnClickListener(this);
        horizontalBarChart.setOnClickListener(this);

        pop = new Popup(PredictActivity.this, mainLayout);
        waitPopup = new Popup(PredictActivity.this, mainLayout);
        errorPopup = new Popup(PredictActivity.this, mainLayout);

        // Get user choices from previous activities
        // URL, graph choice, columns, etc.
        Intent fromSelectColumns = getIntent();
        userParameters = fromSelectColumns.getParcelableExtra("userParameters");

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        try {
            // Using the columns specified by the user,
            // Save a csv file of the data and return the path
            String modelDataPath = instaGraphPyObject.callAttr(
                    "read_model_data",
                    userParameters.getDatasetPath(),
                    userParameters.getCol1(),
                    userParameters.getCol2(),
                    userParameters.getFirstLast(),
                    userParameters.getRowLimit()
            ).toString();

            // Add the path of the model dataset to userParameters
            userParameters.setModelDataPath(modelDataPath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Rather than reading the csv file everytime a plot is needed,
            // return a PyObject which contains the pandas DataFrame of
            // the data
            dataFrame = instaGraphPyObject.callAttr(
                    "get_dataframe",
                    userParameters.getModelDataPath(),
                    userParameters.getCol1(),
                    userParameters.getCol2()
            );

            // Use the DataFrame to plot the data
            plot_image = instaGraphPyObject.callAttr(
                    "dataframe_plot",
                    dataFrame,
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
            // Get the number of rows in the dataset
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
            // Display the graph options window
            case(R.id.change_graph):
                findViewById(R.id.change_graph_window).setVisibility(View.VISIBLE);
                break;

            // Hide the graph options window
            case(R.id.hide):
                findViewById(R.id.change_graph_window).setVisibility(View.GONE);
                break;

            // Re-plot the data based on the new graph choice
            case(R.id.line_graph):
                changeGraph(getString(R.string.line_graph));
                break;

            // Re-plot the data based on the new graph choice
            case(R.id.bar_chart):
                changeGraph(getString(R.string.bar_chart));
                break;

            // Re-plot the data based on the new graph choice
            case(R.id.pie_chart):
                changeGraph(getString(R.string.pie_chart));
                break;

            // Re-plot the data based on the new graph choice
            case(R.id.horizontal_bar_chart):
                changeGraph(getString(R.string.horizontal_bar_chart));
                break;

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

                // If there are not enough rows in the model dataset for predictions, inform the user
                if(modelRows < minimumPredictionRows(userParameters.getModel())) {
                    errorWindow = errorPopup.showPopup(getString(R.string.not_enough_rows_to_predict), false);
                    return;
                }

                // Go to ResultActivity
                Intent goToResult = new Intent(PredictActivity.this, ResultActivity.class);
                goToResult.putExtra("userParameters", userParameters);
                waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);
                startActivity(goToResult);
                break;

            case(R.id.back):
                finish();
                break;

            case(R.id.download_icon):
                // Download the visualisation
                if(downloader == null) {
                    downloader = new Downloader(PredictActivity.this, this);
                }
                downloader.savePlot(bmp);

                // Inform user of success
                popWindow = pop.showPopup(getString(R.string.graph_saved), false);

        }
    }

    // Method to get the minimum number of rows required for prediction
    public int minimumPredictionRows(String modelChoice) {
        switch(modelChoice) {
            case "AR":
                if(userParameters.getPara1().equals("")) {
                    // The default AR model requires at least 26 rows for predictions
                    return 26;
                }
                else {
                    // Otherwise, there should be twice as many rows as there is lags,
                    // plus one for the trend, and another one for safety
                    return (1 + Integer.parseInt(userParameters.getPara1()) * 2) + 1;
                }
            case "ARIMA":
                // If any of the custom parameters are not set, set them to be the highest possible default
                // Minimum rows for default model is 26
                if(userParameters.getPara1().equals("")) {
                    userParameters.setPara1("12");
                }
                if(userParameters.getPara2().equals("")) {
                    userParameters.setPara2("1");
                }
                if(userParameters.getPara3().equals("")) {
                    userParameters.setPara3("1");
                }
                // There should be twice as many rows as orders plus one
                return ((1 +Integer.parseInt(userParameters.getPara1()) +
                        Integer.parseInt(userParameters.getPara2()) +
                        Integer.parseInt(userParameters.getPara3())) * 2) + 1;

            case "SES":
                // No additional rows required for prediction in SES
                return 2;

            case "HWES":
                // If no seasonal period has been set, the default is 12
                if(userParameters.getPara3().equals("")) {
                    userParameters.setPara3("12");
                }
                // At least 13 rows required for default seasonal period of 12
                return Integer.parseInt(userParameters.getPara3()) + 1;
        }

        // By default, return the highest default requirement
        return 26;
    }

    // Method to change the graph plot
    public void changeGraph(String graphType) {
        waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);

        // Re-plot the data using the Dataframe and the new graph choice
        try {
            plot_image = instaGraphPyObject.callAttr(
                    "dataframe_plot",
                    dataFrame,
                    graphType,
                    userParameters.getCol1(),
                    userParameters.getCol2(),
                    userParameters.getTitle()
            );

            byte[] plot = plot_image.toJava(byte[].class);
            bmp = BitmapFactory.decodeByteArray(plot,0, plot.length);
            plot_window.setImageBitmap(bmp);
        }
        catch (Exception e) {
            waitWindow.dismiss();
            errorWindow = errorPopup.showPopup(getString(R.string.unknown_error_changing_graph), false);
        }
        finally {
            if(waitWindow.isShowing()) {
                waitWindow.dismiss();
            }
        }

        // Update userParameters with the new graph choice
        userParameters.setGraphType(graphType);
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
