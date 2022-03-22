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
import android.widget.Toast;

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

    PopupWindow popWindow;

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
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.submit) {
            waitPopup = new Popup(GetURLActivity.this, mainLayout);
            popWindow = waitPopup.showPopup(getResources().getString(R.string.please_wait), true);
            // Run the specified function in the script and get the return value
            String datasetPath = instaGraphPyObject.callAttr("read_dataset", URL_input.getText().toString()).toString();
            Log.i("InstaGraph", "file path to dataset: " + datasetPath);
            userParameters.setUrl(URL_input.getText().toString());
            userParameters.setDatasetPath(datasetPath);

            PyObject dataPreview = instaGraphPyObject.callAttr("step_one", datasetPath);

            // Run the dataset_summary function
            PyObject datasetSummary = instaGraphPyObject.callAttr("dataset_summary", URL_input.getText().toString());

//                Inspired from https://github.com/Robbi-Blechdose/FT-TXT/blob/8751afe7fd0f74efb05f1635bb5b9a28d107013e/app/src/main/java/de/rbgs/ft_txt_app/Main.java#L264
            try {
                byte[] data = dataPreview.toJava(byte[].class);
                String summary = datasetSummary.toJava(String.class);
                Intent returnToGetData = new Intent(GetURLActivity.this, GetDataActivity.class);
                returnToGetData.putExtra("dataPreview", data);
                returnToGetData.putExtra("summary", summary);
                returnToGetData.putExtra("userParameters", userParameters);
                setResult(RESULT_OK, returnToGetData);
                finish();
            }
            catch (java.lang.ClassCastException cce) {
                Toast.makeText(this, cce.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("InstaGraph", cce.getMessage());
                popWindow.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(popWindow != null && popWindow.isShowing()) {
            popWindow.dismiss();
        }
    }
}
