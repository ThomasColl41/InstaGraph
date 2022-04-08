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
    EditText URLInput;
    Button infoButton;
    Button submit;

    Python py;
    PyObject instaGraphPyObject;

    ParameterParcel userParameters;

    RelativeLayout mainLayout;

    Popup infoPopup;
    Popup waitPopup;
    Popup errorPopup;

    PopupWindow infoWindow;
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

        URLInput = findViewById(R.id.url_input);
        infoButton = findViewById(R.id.info_button);
        submit = findViewById(R.id.submit);

        infoButton.setOnClickListener(this);
        submit.setOnClickListener(this);

        infoPopup = new Popup(GetURLActivity.this, mainLayout);
        waitPopup = new Popup(GetURLActivity.this, mainLayout);
        errorPopup = new Popup(GetURLActivity.this, mainLayout);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case (R.id.submit):
                // Check that the URL input field is not empty (blank)
                if(URLInput.getText().toString().equals("")) {
                    errorWindow = errorPopup.showPopup(getString(R.string.url_blank), false);
                    return;
                }

                waitWindow = waitPopup.showPopup(getString(R.string.please_wait), true);

                // Read in the dataset from the URL, returning the path to a local copy of the dataset
                String datasetPath;
                try {
                    // Run the specified function in the script and get the return value
                    datasetPath = instaGraphPyObject.callAttr("read_dataset", URLInput.getText().toString()).toString();
                }
                catch (Exception e) {
                    waitWindow.dismiss();
                    errorWindow = errorPopup.showPopup(e.getMessage(), false);
                    return;
                }

                // Set the URL and datasetPath parameters
                userParameters.setUrl(URLInput.getText().toString());
                userParameters.setDatasetPath(datasetPath);

                // Get the number of rows in the dataset
                int datasetRows = Integer.parseInt(instaGraphPyObject.callAttr("model_rows", userParameters.getDatasetPath()).toString());

                // If there are too many rows, inform the user and return
                if(datasetRows > 3000) {
                    waitWindow.dismiss();
                    errorWindow = errorPopup.showPopup(getString(R.string.too_many_rows), false);
                    return;
                }

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
                    datasetSummary = instaGraphPyObject.callAttr("dataset_summary", URLInput.getText().toString());
                }
                catch (Exception e) {
                    waitWindow.dismiss();
                    errorWindow = errorPopup.showPopup( e.getMessage(), false);
                    return;
                }

                // Return to GetDataActivity
                Intent returnToGetData = new Intent(GetURLActivity.this, GetDataActivity.class);

                // Convert the dataPreview PyObject into a byte array
                try {
                    assert dataPreview != null;
                    byte[] data = dataPreview.toJava(byte[].class);
                    // Return it to GetDataActivity
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
                    // Return it to GetDataActivity
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
                    return;
                }
                catch (Exception e) {
                    waitWindow.dismiss();
                    errorWindow = errorPopup.showPopup(e.getMessage(), false);
                    return;
                }

                // Return the ParameterParcel
                returnToGetData.putExtra("userParameters", userParameters);
                setResult(RESULT_OK, returnToGetData);
                finish();
                break;

            case (R.id.info_button):
                // Display information about the application's limitations
                infoWindow = infoPopup.showPopup(getString(R.string.row_limit_info), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(infoWindow != null && infoWindow.isShowing()) {
            infoWindow.dismiss();
        }
        if(waitWindow != null && waitWindow.isShowing()) {
            waitWindow.dismiss();
        }
        if(errorWindow != null && errorWindow.isShowing()) {
            errorWindow.dismiss();
        }
    }
}
