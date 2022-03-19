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
    ImageView downloadIcon;

    Python py;
    PyObject instaGraphPyObject;
    PyObject plot_image;
    Bitmap bmp;

    ParameterParcel userParameters;

    Downloader downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.predict_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        plot_window = findViewById(R.id.plot_window);
        downloadIcon = findViewById(R.id.download_icon);

        next.setOnClickListener(this);
        back.setOnClickListener(this);
        downloadIcon.setOnClickListener(this);

        // Get user choices from previous activities
        // URL, graph choice, columns, etc.
        Intent fromSelectColumns = getIntent();
        userParameters = fromSelectColumns.getParcelableExtra("userParameters");

        // Log the provided URL
        Log.i("InstaGraph", userParameters.getUrl());

        // Initialise Python (using Chaquopy)
        if(!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        // Get an instance of Python to run scripts
        py = Python.getInstance();

        // Run the script associated with the parameter
        instaGraphPyObject = py.getModule("instagraph");

        String modelDataPath = instaGraphPyObject.callAttr(
                "read_model_data",
                userParameters.getDatasetPath(),
                userParameters.getCol1(),
                userParameters.getCol2()
        ).toString();

        userParameters.setModelDataPath(modelDataPath);

        // Plot the graph depending on user choice
        plot_image = instaGraphPyObject.callAttr(
                "graph_plot",
                userParameters.getModelDataPath(),
                userParameters.getGraphType(),
                userParameters.getCol1(),
                userParameters.getCol2(),
                userParameters.getTitle()
        );

        // Log the contents of the PyObject (should be a byte array)
        Log.i("InstaGraph", plot_image.toString());

        // Decode the byte array and display the visualisation bitmap
        try {
            byte[] plot = plot_image.toJava(byte[].class);
            bmp = BitmapFactory.decodeByteArray(plot,0, plot.length);

            plot_window.setImageBitmap(bmp);
        }
        catch (NullPointerException npe) {
            Toast.makeText(
                    PredictActivity.this,
                    npe.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToResult = new Intent(PredictActivity.this, ResultActivity.class);
                goToResult.putExtra("userParameters", userParameters);
                startActivity(goToResult);
                break;

            case(R.id.back):
                finish();
                break;

            case(R.id.download_icon):
                if(downloader == null) {
                    downloader = new Downloader(bmp, PredictActivity.this, this);
                }
                else {
                    downloader.setPlot(bmp);
                }
                downloader.savePlot();

                // Inform user
                Toast.makeText(this, "Plot saved to Download folder", Toast.LENGTH_SHORT).show();
        }
    }
}
