package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class SelectGraphActivity extends AppCompatActivity implements View.OnClickListener {
    Button next;
    Button back;

    ImageView lineGraph;
    ImageView barChart;
    ImageView pieChart;
    ImageView horizontalBarChart;

    int highlighted = 0;
    String graphType;

    ParameterParcel userParameters;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_graph_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);

        lineGraph = findViewById(R.id.line_graph_window);
        barChart = findViewById(R.id.bar_chart_window);
        pieChart = findViewById(R.id.pie_chart_window);
        horizontalBarChart = findViewById(R.id.horizontal_bar_chart_window);

        next.setOnClickListener(this);
        back.setOnClickListener(this);

        lineGraph.setOnClickListener(this);
        barChart.setOnClickListener(this);
        pieChart.setOnClickListener(this);
        horizontalBarChart.setOnClickListener(this);

        // Set the line graph as default
        graphType = selectGraph(R.id.line_graph_border);

        Intent fromGetData = getIntent();
        userParameters = fromGetData.getParcelableExtra("userParameters");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                userParameters.setGraphType(graphType);
                Intent goToSelectColumns = new Intent(SelectGraphActivity.this, SelectColumnsActivity.class);
                goToSelectColumns.putExtra("userParameters", userParameters);
                startActivity(goToSelectColumns);
                break;

            case(R.id.back):
                finish();
                break;

            case(R.id.line_graph_window):
                graphType = selectGraph(R.id.line_graph_border);
                break;

            case(R.id.bar_chart_window):
                graphType = selectGraph(R.id.bar_chart_border);
                break;

            case(R.id.pie_chart_window):
                graphType = selectGraph(R.id.pie_chart_border);
                break;

            case(R.id.horizontal_bar_chart_window):
                graphType = selectGraph(R.id.horizontal_bar_chart_border);
                break;
        }
    }

    public String selectGraph(int id) {
        if (highlighted == 0) {
            findViewById(id).setVisibility(View.VISIBLE);
            highlighted = id;
            return getGraphType(id);
        }
        else if (id != highlighted) {
            try {
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(id).setVisibility(View.VISIBLE);
                highlighted = id;
                return getGraphType(id);
            }
            catch (NullPointerException npe) {
                Log.i("InstaGraph", npe.getMessage());
                // Line Graph is the default
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(R.id.line_graph_border).setVisibility(View.VISIBLE);
                highlighted = R.id.line_graph_border;
                Toast.makeText(
                        this,
                        npe.getMessage() + " occurred, Please try again.",
                        Toast.LENGTH_LONG).show();
                return "Line Graph";
            }
        }
        return getGraphType(id);
    }

    public String getGraphType(int id) {
        // Return name of selected graph
        switch(id) {
            case(R.id.line_graph_border):
                return "Line Graph";

            case(R.id.bar_chart_border):
                return "Bar Chart";

            case(R.id.pie_chart_border):
                return "Pie Chart";

            case(R.id.horizontal_bar_chart_border):
                return "Horizontal Bar Chart";

            default:
                return "Line Graph";
        }
    }
}
