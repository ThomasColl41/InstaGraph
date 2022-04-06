package ie.tudublin.instagraph;

import android.content.Intent;
import android.os.Bundle;
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

public class CustomiseModelActivity extends AppCompatActivity implements View.OnClickListener {

    Python py;
    PyObject instaGraphPyObject;

    ParameterParcel userParameters;

    RelativeLayout mainLayout;

    Popup errorPopup;
    Popup infoPopup;

    PopupWindow errorWindow;
    PopupWindow infoWindow;

    RelativeLayout ar;
    RelativeLayout arima;
    RelativeLayout ses;
    RelativeLayout hwes;

    Button submit;
    Button cancel;
    Button modelInfo;

    TextView modelName;

    EditText arOrderInput;
    Spinner arTrendSpinner;

    EditText arimaAROrderInput;
    EditText arimaDiffOrderInput;
    EditText arimaMAOrderInput;
    Spinner arimaTrendSpinner;

    Spinner hwesTrendSpinner;
    Spinner hwesSeasonalSpinner;
    EditText hwesSeasonalPeriodInput;

    Spinner rowLimitFirstLastSpinner;
    EditText rowLimitInput;

    EditText numPredictionsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customise_model);

        mainLayout = findViewById(R.id.main_layout);

        submit = findViewById(R.id.submit);
        cancel = findViewById(R.id.cancel);
        modelInfo = findViewById(R.id.model_info);
        ar = findViewById(R.id.ar);
        arima = findViewById(R.id.arima);
        ses = findViewById(R.id.ses);
        hwes = findViewById(R.id.hwes);
        modelName = findViewById(R.id.model_name);
        rowLimitFirstLastSpinner = findViewById(R.id.row_limit_first_last_spinner);
        rowLimitInput = findViewById(R.id.row_limit_input);
        numPredictionsInput = findViewById(R.id.num_predictions_input);

        submit.setOnClickListener(this);
        cancel.setOnClickListener(this);
        modelInfo.setOnClickListener(this);

        errorPopup = new Popup(CustomiseModelActivity.this, mainLayout);
        infoPopup = new Popup(CustomiseModelActivity.this, mainLayout);

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

        // Display the appropriate parameters depending on user choice
        displayModelParameters(userParameters.getModel());
    }

    @Override
    public void onClick(View view) {
        // The the user limits the number of rows, it should be at least 2
        if(!rowLimitInput.getText().toString().equals("")) {
            if(Integer.parseInt(rowLimitInput.getText().toString()) < 2) {
                errorWindow = errorPopup.showPopup(getString(R.string.not_enough_rows), false);
                return;
            }
        }

        // Check that the user has not entered a seasonal period less than 2 for HWES
        if(userParameters.getModel().equals("HWES") && !hwesSeasonalPeriodInput.getText().toString().equals("")) {
            if(Integer.parseInt(hwesSeasonalPeriodInput.getText().toString()) < 2) {
                errorWindow = errorPopup.showPopup(getString(R.string.not_enough_seasonal_periods), false);
                return;
            }
        }
        switch(view.getId()) {
            case(R.id.submit):
                // Set the custom parameters in the userParameters object
                setParameters(userParameters.getModel());

                // Return to SelectColumnsActivity with the newly updated userParameters
                Intent returnToSelectColumns = new Intent(CustomiseModelActivity.this, SelectColumnsActivity.class);
                returnToSelectColumns.putExtra("userParameters", userParameters);
                setResult(RESULT_OK, returnToSelectColumns);
                finish();
                break;

            case(R.id.cancel):
                // Cancel changes, no update to userParameters needed
                finish();
                break;

            case(R.id.model_info):
                // Display information appropriate to the chosen model
                switch(userParameters.getModel()) {
                    case "AR":
                        infoWindow = infoPopup.showPopup(getString(R.string.ar_model_info), false);
                        break;

                    case "ARIMA":
                        infoWindow = infoPopup.showPopup(getString(R.string.arima_model_info), false);
                        break;

                    case "SES":
                        infoWindow = infoPopup.showPopup(getString(R.string.ses_model_info), false);
                        break;

                    case "HWES":
                        infoWindow = infoPopup.showPopup(getString(R.string.hwes_model_info), false);
                        break;
                }
                break;
        }
    }

    // Method to display options for the chosen forecasting model
    public void displayModelParameters(String modelChoice) {
        // Set the content of the modelName TextView to that of the chosen model
        modelName.setText(userParameters.getModel());
        switch(modelChoice) {
            case "AR":
                ar.setVisibility(View.VISIBLE);
                arOrderInput = findViewById(R.id.ar_order_input);
                arTrendSpinner = findViewById(R.id.ar_trend_spinner);
                break;

            case "ARIMA":
                arima.setVisibility(View.VISIBLE);
                arimaAROrderInput = findViewById(R.id.arima_ar_order_input);
                arimaDiffOrderInput = findViewById(R.id.arima_diff_order_input);
                arimaMAOrderInput = findViewById(R.id.arima_ma_order_input);
                arimaTrendSpinner = findViewById(R.id.arima_trend_spinner);
                break;

            case "SES":
                ses.setVisibility(View.VISIBLE);
                break;

            case "HWES":
                hwes.setVisibility(View.VISIBLE);
                hwesTrendSpinner = findViewById(R.id.hwes_trend_spinner);
                hwesSeasonalSpinner = findViewById(R.id.hwes_seasonal_spinner);
                hwesSeasonalPeriodInput = findViewById(R.id.hwes_seasonal_period_input);
                break;
        }
        prepareModelParameters(modelChoice);
    }

    // Method to perpare model parameters for user input
    public void prepareModelParameters(String modelChoice) {
        // Get the string array resources from strings.xml
        String[] trends = getResources().getStringArray(R.array.trend);
        String[] hwesTrends = getResources().getStringArray(R.array.hwes_trend);
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

        ArrayAdapter<String> firstLastArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                firstLast);

        // Set the appearance of dropdown items
        trendArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        hwesTrendArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        firstLastArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        // Refresh the view if the data changes
        trendArray.notifyDataSetChanged();
        hwesTrendArray.notifyDataSetChanged();
        firstLastArray.notifyDataSetChanged();

        switch(modelChoice) {
            case "AR":
                arTrendSpinner.setAdapter(trendArray);
                break;

            case "ARIMA":
                arimaTrendSpinner.setAdapter(trendArray);
                break;

            // Nothing to prepare for SES

            case"HWES":
                hwesTrendSpinner.setAdapter(hwesTrendArray);
                hwesSeasonalSpinner.setAdapter(hwesTrendArray);
                break;
        }

        rowLimitFirstLastSpinner.setAdapter(firstLastArray);
    }

    // Method to set the custom parameters depending on the choice of model
    public void setParameters(String modelChoice) {
        // Submit changes
        switch(modelChoice) {
            case "AR":
                userParameters.setPara1(arOrderInput.getText().toString());
                userParameters.setPara2(determineTrend(arTrendSpinner.getSelectedItem().toString()));
                break;

            case "ARIMA":
                userParameters.setPara1(arimaAROrderInput.getText().toString());
                userParameters.setPara2(arimaDiffOrderInput.getText().toString());
                userParameters.setPara3(arimaMAOrderInput.getText().toString());
                userParameters.setPara4(determineTrend(arimaTrendSpinner.getSelectedItem().toString()));
                break;

            // No parameters for SES

            case "HWES":
                userParameters.setPara1(hwesTrendSpinner.getSelectedItem().toString().toLowerCase());
                userParameters.setPara2(hwesSeasonalSpinner.getSelectedItem().toString().toLowerCase());
                userParameters.setPara3(hwesSeasonalPeriodInput.getText().toString());
                break;
        }

        // Set the parameters for the row limit
        userParameters.setFirstLast(rowLimitFirstLastSpinner.getSelectedItem().toString());
        userParameters.setRowLimit(rowLimitInput.getText().toString());

        // Set the number of predictions parameter
        userParameters.setNumPredictions(numPredictionsInput.getText().toString());
    }

    // Method to covert the chosen value for trend into one that will be accepted by the models
    public String determineTrend(String trendValue) {
        switch(trendValue) {
            case "No trend":
                return "n";
            case "Constant only":
                return "c";
            case "Time trend only":
                return "t";
            case "Constant and time trend":
                return "ct";

        }

        // Return "c" by default
        return "c";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(errorWindow != null && errorWindow.isShowing()) {
            errorWindow.dismiss();
        }
        if(infoWindow != null && infoWindow.isShowing()) {
            infoWindow.dismiss();
        }
    }
}
