package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    Spinner modelSpinner;
    Spinner col1Spinner;
    Spinner col2Spinner;

    TextView col1Text;
    TextView col2Text;

    Python py;
    PyObject instaGraphPyObject;

    private final List<String> columns = new ArrayList<>();

    ParameterParcel userParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_columns_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        modelSpinner = findViewById(R.id.model_spinner);
        col1Spinner = findViewById(R.id.column_one_spinner);
        col2Spinner = findViewById(R.id.column_two_spinner);
        col1Text = findViewById(R.id.column_one_text);
        col2Text = findViewById(R.id.column_two_text);

        next.setOnClickListener(this);
        back.setOnClickListener(this);

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
                Intent goToPredict = new Intent(SelectColumnsActivity.this, PredictActivity.class);
                goToPredict.putExtra("userParameters", userParameters);
                startActivity(goToPredict);
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
}
