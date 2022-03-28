package ie.tudublin.instagraph;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomiseModelActivity extends AppCompatActivity implements View.OnClickListener {

    Python py;
    PyObject instaGraphPyObject;

    private final List<String> columns = new ArrayList<>();

    ParameterParcel userParameters;

    RelativeLayout mainLayout;

    Popup waitPopup;

    PopupWindow popWindow;

    RelativeLayout ar;
    RelativeLayout arima;
    RelativeLayout ses;
    RelativeLayout hwes;

    Button submit;
    Button cancel;

    TextView modelName;

    EditText arOrderInput;
    Spinner arTrendSpinner;

    EditText arimaAROrderInput;
    EditText arimaDiffOrderInput;
    EditText arimaMAOrderInput;
    Spinner arimaTrendSpinner;

    Spinner hwesTrendSpinner;
    Spinner hwesDampedTrendSpinner;
    Spinner hwesSeasonalSpinner;
    EditText hwesSeasonalPeriodInput;

    Spinner rowLimitFirstLastSpinner;
    EditText rowLimitInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customise_model);

        mainLayout = findViewById(R.id.main_layout);

        submit = findViewById(R.id.submit);
        cancel = findViewById(R.id.cancel);
        ar = findViewById(R.id.ar);
        arima = findViewById(R.id.arima);
        ses = findViewById(R.id.ses);
        hwes = findViewById(R.id.hwes);
        modelName = findViewById(R.id.model_name);
        rowLimitFirstLastSpinner = findViewById(R.id.row_limit_first_last_spinner);
        rowLimitInput = findViewById(R.id.row_limit_input);

        submit.setOnClickListener(this);
        cancel.setOnClickListener(this);


        Intent fromSelectGraph = getIntent();
        userParameters = fromSelectGraph.getParcelableExtra("userParameters");

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        // Run the get_column_names function
        PyObject columnNames = instaGraphPyObject.callAttr("get_column_names", userParameters.getDatasetPath());

        // Store the returned names in a String array
        String[] names = columnNames.toJava(String[].class);

        // Log the column names
        for (String name : names) {
            Log.i("InstaGraph", name);
        }

        // Display the appropriate parameters depending on user choice
        displayModelParameters(userParameters.getModel());
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.submit):
                // Submit changes
                finish();
            case(R.id.cancel):
                // Cancel changes
                finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(popWindow != null && popWindow.isShowing()) {
            popWindow.dismiss();
        }
    }

    // Method to display options for the chosen forecasting model
    public void displayModelParameters(String model_choice) {
        // Set the content of the modelName TextView to that of the chosen model
        modelName.setText(userParameters.getModel());
        switch(model_choice) {
            case "AR":
                ar.setVisibility(View.VISIBLE);
                arOrderInput = findViewById(R.id.ar_order_input);
                arTrendSpinner = findViewById(R.id.ar_trend_spinner);
                prepareModelParameters(model_choice);
                break;

            case "ARIMA":
                arima.setVisibility(View.VISIBLE);
                arimaAROrderInput = findViewById(R.id.arima_ar_order_input);
                arimaDiffOrderInput = findViewById(R.id.arima_diff_order_input);
                arimaMAOrderInput = findViewById(R.id.arima_ma_order_input);
                arimaTrendSpinner = findViewById(R.id.arima_trend_spinner);
                prepareModelParameters(model_choice);
                break;

            case "SES":
                ses.setVisibility(View.VISIBLE);
                prepareModelParameters(model_choice);
                break;

            case "HWES":
                hwes.setVisibility(View.VISIBLE);
                hwesTrendSpinner = findViewById(R.id.hwes_trend_spinner);
                hwesDampedTrendSpinner = findViewById(R.id.hwes_damped_trend_spinner);
                hwesSeasonalSpinner = findViewById(R.id.hwes_seasonal_spinner);
                hwesSeasonalPeriodInput = findViewById(R.id.hwes_seasonal_period_input);
                prepareModelParameters(model_choice);
                break;
        }
    }

    // Method to perpare model parameters for user input
    public void prepareModelParameters(String model_choice) {
        // Get the string array resources from strings.xml
        String[] trends = getResources().getStringArray(R.array.trend);
        String[] hwesTrends = getResources().getStringArray(R.array.hwes_trend);
        String[] boolValues = getResources().getStringArray(R.array.bool_spinner);
        String[] firstLast = getResources().getStringArray(R.array.first_last);

        // Load the string array values into the ArrayAdapters
        ArrayAdapter<String> trendArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                trends);

        ArrayAdapter<String> hwesTrendArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                hwesTrends);

        ArrayAdapter<String> boolArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                boolValues);

        ArrayAdapter<String> firstLastArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                firstLast);

        // Set the appearance of dropdown items
        trendArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        hwesTrendArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        boolArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        firstLastArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        // Refresh the view if the data changes
        trendArray.notifyDataSetChanged();
        hwesTrendArray.notifyDataSetChanged();
        boolArray.notifyDataSetChanged();
        firstLastArray.notifyDataSetChanged();

        switch(model_choice) {
            case "AR":
                arTrendSpinner.setAdapter(trendArray);
                break;

            case "ARIMA":
                arimaTrendSpinner.setAdapter(trendArray);
                break;

            // Nothing to prepare for SES

            case"HWES":
                hwesTrendSpinner.setAdapter(hwesTrendArray);
                hwesDampedTrendSpinner.setAdapter(boolArray);
                hwesSeasonalSpinner.setAdapter(hwesTrendArray);
                break;
        }

        rowLimitFirstLastSpinner.setAdapter(firstLastArray);
    }

    // Method to retrieve AR parameters from use input
    public void getARParameters() {

    }

    // Method to retrieve ARIMA parameters from use input
    public void getARIMAParameters() {

    }

    // Method to retrieve SES parameters from use input
    public void getSESParameters() {

    }

    // Method to retrieve HWES parameters from use input
    public void getHWESParameters() {

    }
}
