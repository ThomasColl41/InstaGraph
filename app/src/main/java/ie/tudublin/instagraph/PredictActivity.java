package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PredictActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;

    ImageView plot_window;

    Python py;
    PyObject instaGraphPyObject;

    String url;
    String graphType;
    String model;
    String col1;
    String col2;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.predict_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        plot_window = findViewById(R.id.plot_window);

        next.setOnClickListener(this);
        back.setOnClickListener(this);

        Intent fromSelectColumns = getIntent();
        url = fromSelectColumns.getStringExtra("URL");
        graphType = fromSelectColumns.getStringExtra("graphType");
        model = fromSelectColumns.getStringExtra("model");
        col1 = fromSelectColumns.getStringExtra("col1");
        col2 = fromSelectColumns.getStringExtra("col2");
        title = col1 + " over " + col2;

        Log.i("InstaGraph", url);

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        // Run the get_column_names function
        PyObject plot_image = instaGraphPyObject.callAttr("line_graph_plot", url, col1, col2, title, url);

        Log.i("InstaGraph", plot_image.toString());

        try {
            byte[] plot = plot_image.toJava(byte[].class);
            Bitmap bmp = BitmapFactory.decodeByteArray(plot,0, plot.length);

            plot_window.setImageBitmap(bmp);
        }
        catch (NullPointerException npe) {
            Toast.makeText(PredictActivity.this, npe.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToResult = new Intent(PredictActivity.this, ResultActivity.class);
                startActivity(goToResult);
                break;

            case(R.id.back):
                finish();
                break;
        }
    }
}
