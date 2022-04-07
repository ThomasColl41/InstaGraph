package ie.tudublin.instagraph;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectColumnsActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;
    Button customiseModel;

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
    Popup errorPopup;

    PopupWindow waitWindow;
    PopupWindow errorWindow;

    // Inspired from https://www.youtube.com/watch?v=-y5eF0u1bZQ and https://www.youtube.com/watch?v=Ke9PaRdMcgc
    // StartActivityForResult alternative implementation
    ActivityResultLauncher<Intent> customiseModelLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        // When CustomiseModelActivity returns, this method is called
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            // Get the result code and data
                            int resCode = result.getResultCode();
                            Intent data = result.getData();

                            // if things went ok (the user pressed submit)
                            if(resCode == RESULT_OK && data != null) {
                                try {
                                    userParameters = data.getParcelableExtra("userParameters");
                                }
                                catch (NullPointerException npe) {
                                    errorWindow = errorPopup.showPopup(getString(R.string.custom_parameters_not_found), false);
                                }
                            }
                            else if (resCode != RESULT_CANCELED) {
                                // Otherwise, something went wrong
                                errorWindow = errorPopup.showPopup(getString(R.string.custom_parameters_unknown_error), false);
                            }
                        }
                    }

            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_columns_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        customiseModel = findViewById(R.id.customise_model);
        modelSpinner = findViewById(R.id.model_spinner);
        col1Spinner = findViewById(R.id.column_one_spinner);
        col2Spinner = findViewById(R.id.column_two_spinner);
        col1Text = findViewById(R.id.column_one_text);
        col2Text = findViewById(R.id.column_two_text);
        mainLayout = findViewById(R.id.main_layout);

        next.setOnClickListener(this);
        back.setOnClickListener(this);
        customiseModel.setOnClickListener(this);

        waitPopup = new Popup(SelectColumnsActivity.this, mainLayout);
        errorPopup = new Popup(SelectColumnsActivity.this, mainLayout);

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

        try {
            // Run the get_column_names function
            PyObject columnNames = instaGraphPyObject.callAttr("get_column_names", userParameters.getDatasetPath());

            // Store the returned names in a String array
            String[] names = columnNames.toJava(String[].class);

            spinnerSetup(names);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        columnTextSetup(userParameters.getGraphType());
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                String title = col1Spinner.getSelectedItem().toString() + " over " + col2Spinner.getSelectedItem().toString();
                userParameters.setModel(modelSpinner.getSelectedItem().toString());
                userParameters.setCol1(col1Spinner.getSelectedItem().toString());
                userParameters.setCol2(col2Spinner.getSelectedItem().toString());
                userParameters.setTitle(title);
                if(col1Spinner.getSelectedItem().toString().equals(col2Spinner.getSelectedItem().toString())) {
                    errorWindow = errorPopup.showPopup(getString(R.string.identical_columns), false);
                    return;
                }
                try {
                    instaGraphPyObject.callAttr(
                            "test_plot",
                            userParameters.getDatasetPath(),
                            userParameters.getGraphType(),
                            userParameters.getCol1(),
                            userParameters.getCol2()
                    );
                }
                catch (Exception e) {
                    errorWindow = errorPopup.showPopup(e.getMessage(), false);
                    return;
                }
                if(userParameters.getModel().equals("AR")) {
                    userParameters.setPara2(determineTrend(userParameters.getPara2()));
                }
                else if(userParameters.getModel().equals("ARIMA")) {
                    userParameters.setPara4(determineTrend(userParameters.getPara4()));
                }
                Intent goToPredict = new Intent(SelectColumnsActivity.this, PredictActivity.class);
                goToPredict.putExtra("userParameters", userParameters);
                waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);
                startActivity(goToPredict);
                break;

            case(R.id.customise_model):
                Intent goToCustomise = new Intent(SelectColumnsActivity.this, CustomiseModelActivity.class);
                goToCustomise.putExtra("userParameters", userParameters);
                goToCustomise.putExtra("model", modelSpinner.getSelectedItem().toString());
                waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);
                customiseModelLauncher.launch(goToCustomise);
                break;

            case(R.id.back):
                finish();
                break;
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

    // Method that sets the text for the column Spinners depending on the graph choice
    public void columnTextSetup(String graphType) {
        switch (graphType) {
            case "Line Graph":
                col1Text.setText(R.string.line_graph_col1);
                col2Text.setText(R.string.line_graph_col2);
                break;

            case "Bar Chart":
                col1Text.setText(R.string.bar_chart_col1);
                col2Text.setText(R.string.bar_chart_col2);
                break;

            case "Pie Chart":
                col1Text.setText(R.string.pie_chart_col1);
                col2Text.setText(R.string.pie_chart_col2);
                break;

            case "Horizontal Bar Chart":
                col1Text.setText(R.string.horizontal_bar_chart_col1);
                col2Text.setText(R.string.horizontal_bar_chart_col2);
                break;
        }
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
        if(waitWindow != null && waitWindow.isShowing()) {
            waitWindow.dismiss();
        }
        if(errorWindow != null && errorWindow.isShowing()) {
            errorWindow.dismiss();
        }
    }
}
