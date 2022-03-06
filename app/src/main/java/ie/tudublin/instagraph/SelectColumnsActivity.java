package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

    Python py;
    PyObject instaGraphPyObject;

    private final List<String> models = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();

    String url;
    String graphType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_columns_activity);

        Intent fromSelectGraph = getIntent();
        url = fromSelectGraph.getStringExtra("URL");
        graphType = fromSelectGraph.getStringExtra("graphType");

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        // Run the get_column_names function
        PyObject columnNames = instaGraphPyObject.callAttr("get_column_names", url);

        String[] names = columnNames.toJava(String[].class);

        for (String name : names) {
            Log.i("InstaGraph", name);
        }

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        modelSpinner = findViewById(R.id.model_spinner);
        col1Spinner = findViewById(R.id.column_one_spinner);
        col2Spinner = findViewById(R.id.column_two_spinner);

        next.setOnClickListener(this);
        back.setOnClickListener(this);

        spinnerSetup(names);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToPredict = new Intent(SelectColumnsActivity.this, PredictActivity.class);
                goToPredict.putExtra("graphType", graphType);
                goToPredict.putExtra("model", modelSpinner.getSelectedItem().toString());
                goToPredict.putExtra("col1", col1Spinner.getSelectedItem().toString());
                goToPredict.putExtra("col2", col2Spinner.getSelectedItem().toString());
                goToPredict.putExtra("URL", url);
                startActivity(goToPredict);
                break;

            case(R.id.back):
                finish();
                break;
        }
    }

    public void spinnerSetup(String[] colNames) {
        models.add("AR");
        models.add("ARIMA");
        models.add("SES");
        models.add("HWES");
        ArrayAdapter<String> modelArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                models);
        modelArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        modelSpinner.setAdapter(modelArray);
        modelArray.notifyDataSetChanged();

        columns.addAll(Arrays.asList(colNames));

        ArrayAdapter<String> columnArray = new ArrayAdapter<>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                columns);
        columnArray.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        col1Spinner.setAdapter(columnArray);
        col2Spinner.setAdapter(columnArray);
        columnArray.notifyDataSetChanged();
    }
}
