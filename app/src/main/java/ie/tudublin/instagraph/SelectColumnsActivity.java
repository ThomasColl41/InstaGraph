package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class SelectColumnsActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;

    Python py;
    PyObject instaGraphPyObject;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_columns_activity);

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

        Intent fromSelectGraph = getIntent();
        url = fromSelectGraph.getStringExtra("URL");

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);

        next.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToPredict = new Intent(SelectColumnsActivity.this, PredictActivity.class);
                startActivity(goToPredict);
                break;

            case(R.id.back):
                finish();
                break;
        }
    }
}
