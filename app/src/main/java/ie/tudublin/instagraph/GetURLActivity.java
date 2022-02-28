package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class GetURLActivity extends AppCompatActivity implements View.OnClickListener {
    EditText URL_input;
    Button submit;

    Python py;
    PyObject instaGraphPyObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_url_activity);

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
            // Run the specified function in the script and get the return value
            PyObject dataPreview = instaGraphPyObject.callAttr("step_one", URL_input.getText().toString());

//                Inspired from https://github.com/Robbi-Blechdose/FT-TXT/blob/8751afe7fd0f74efb05f1635bb5b9a28d107013e/app/src/main/java/de/rbgs/ft_txt_app/Main.java#L264
            try {
                byte[] data = dataPreview.toJava(byte[].class);
                Intent returnToGetData = new Intent(GetURLActivity.this, GetDataActivity.class);
                returnToGetData.putExtra("dataPreview", data);
                setResult(RESULT_OK, returnToGetData);
                finish();
            }
            catch (java.lang.ClassCastException cce) {
                Toast.makeText(this, cce.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("InstaGraph", cce.getMessage());
            }
        }
    }
}
