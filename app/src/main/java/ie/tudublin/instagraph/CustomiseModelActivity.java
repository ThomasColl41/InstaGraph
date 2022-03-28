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

    Spinner modelSpinner;
    Spinner col1Spinner;
    Spinner col2Spinner;

    TextView col1Text;
    TextView col2Text;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customise_model);

        modelSpinner = findViewById(R.id.model_spinner);
        col1Spinner = findViewById(R.id.column_one_spinner);
        col2Spinner = findViewById(R.id.column_two_spinner);
        col1Text = findViewById(R.id.column_one_text);
        col2Text = findViewById(R.id.column_two_text);
        mainLayout = findViewById(R.id.main_layout);

        ar = findViewById(R.id.ar);
        arima = findViewById(R.id.arima);
        ses = findViewById(R.id.ses);
        hwes = findViewById(R.id.hwes);
        submit = findViewById(R.id.submit);
        modelName = findViewById(R.id.model_name);

        submit.setOnClickListener(this);


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

        spinnerSetup(names);

        // Display the appropriate parameters depending on user choice
        displayModelParameters(userParameters.getModel());
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.submit) {
            finish();
        }
    }

    public void spinnerSetup(String[] colNames) {
        // Get the models string array from strings.xml
        String[] models = getResources().getStringArray(R.array.models);

        // Load the models into the ArrayAdapter
        ArrayAdapter<String> modelArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                models);

        // Set the appearance of dropdown items
        modelArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        // Fill the spinner with the values in modelArray
        modelSpinner.setAdapter(modelArray);

        // Refresh the view if the data changes
        modelArray.notifyDataSetChanged();

        // Add all the dataset's columns into the columns String list
        columns.addAll(Arrays.asList(colNames));

        // Create an ArrayAdapter to handle spinners for picking columns
        // Set the context, appearance and data
        ArrayAdapter<String> columnArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                columns);

        // Set the appearance of dropdown items
        columnArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        // Fill the spinners with the values in columnArray
        col1Spinner.setAdapter(columnArray);
        col2Spinner.setAdapter(columnArray);

        // Refresh the view if the data changes
        columnArray.notifyDataSetChanged();
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

        // Set the appearance of dropdown items
        trendArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        hwesTrendArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        boolArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        // Refresh the view if the data changes
        trendArray.notifyDataSetChanged();
        hwesTrendArray.notifyDataSetChanged();
        boolArray.notifyDataSetChanged();

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
