package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class GetURLActivity extends AppCompatActivity implements View.OnClickListener {
    EditText URL_input;
    Button submit;

    Python py;
    PyObject instaGraphPyObject;

    ParameterParcel userParameters;

    RelativeLayout mainLayout;

    Popup waitPopup;
    Popup errorPopup;

    PopupWindow waitWindow;
    PopupWindow errorWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_url_activity);

        mainLayout = findViewById(R.id.main_layout);

        userParameters = new ParameterParcel();

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        URL_input = findViewById(R.id.url_input);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(this);

        waitPopup = new Popup(GetURLActivity.this, mainLayout);
        errorPopup = new Popup(GetURLActivity.this, mainLayout);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.submit) {
            if(URL_input.getText().toString().equals("")) {
                errorWindow = errorPopup.showPopup(getString(R.string.url_blank), false);
                return;
            }

            waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);

            // Read in the dataset from the URL, returning the path to a local copy of the dataset
            String datasetPath;
            try {
                // Run the specified function in the script and get the return value
                datasetPath = instaGraphPyObject.callAttr("read_dataset", URL_input.getText().toString()).toString();
            }
            catch (Exception e) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(e.getMessage(), false);
                return;
            }

            Log.i("InstaGraph", "file path to dataset: " + datasetPath);

            // Set the URL and datasetPath parameters
            userParameters.setUrl(URL_input.getText().toString());
            userParameters.setDatasetPath(datasetPath);

            // The plot of the data preview
            PyObject dataPreview;

            // A string summarising the dataset
            PyObject datasetSummary;

            // Run the step_one function to generate a preview of the data
            try {
                dataPreview = instaGraphPyObject.callAttr("step_one", datasetPath);
            }
            catch (Exception e) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(e.getMessage(), false);
                return;
            }

            // Run the dataset_summary function to get a summary of the dataset
            try {
                datasetSummary = instaGraphPyObject.callAttr("dataset_summary", URL_input.getText().toString());
            }
            catch (Exception e) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup( e.getMessage(), false);
                return;
            }

            Intent returnToGetData = new Intent(GetURLActivity.this, GetDataActivity.class);

            // Convert the dataPreview PyObject into a byte array
            try {
                assert dataPreview != null;
                byte[] data = dataPreview.toJava(byte[].class);
                returnToGetData.putExtra("dataPreview", data);
            }
            catch (AssertionError ae) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(getString(R.string.preview_not_found), false);
                return;
            }
            catch (java.lang.ClassCastException cce) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(getString(R.string.preview_class_cast_error), false);
                Log.i("InstaGraph", cce.getMessage());
                return;
            }
            catch (Exception e) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(e.getMessage(), false);
                return;
            }

            // Convert the datasetSummary PyObject to a String
            try {
                assert datasetSummary != null;
                String summary = datasetSummary.toJava(String.class);
                returnToGetData.putExtra("summary", summary);
            }
            catch (AssertionError ae) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(getString(R.string.summary_not_found), false);
                return;
            }
            catch (java.lang.ClassCastException cce) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(getString(R.string.summary_class_cast_error), false);
                Log.i("InstaGraph", cce.getMessage());
                return;
            }
            catch (Exception e) {
                waitWindow.dismiss();
                errorWindow = errorPopup.showPopup(e.getMessage(), false);
                return;
            }

            returnToGetData.putExtra("userParameters", userParameters);
            setResult(RESULT_OK, returnToGetData);
            finish();
        }
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
