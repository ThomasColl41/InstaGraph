package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.net.URL;

public class GetURLActivity extends AppCompatActivity implements View.OnClickListener {
    EditText URL_input;
    Button submit;

    Python py;
    PyObject pyobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_url_activity);

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        py = Python.getInstance();

        pyobj = py.getModule("step_one");

        URL_input = findViewById(R.id.url_input);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.submit):
                PyObject obj = pyobj.callAttr("main", URL_input.getText().toString());
                Toast.makeText(this, obj.toString(), Toast.LENGTH_LONG).show();
//                Toast.makeText(this, URL_input.getText().toString(), Toast.LENGTH_LONG).show();
//                Intent returnToGetData = new Intent(GetURLActivity.this, GetDataActivity.class);
//                returnToGetData.putExtra("URL", URL_input.getText().toString());
//                startActivity(returnToGetData);
                break;
        }
    }
}
