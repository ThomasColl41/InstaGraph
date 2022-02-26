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
    String graphName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_graph_activity);

        next = findViewById(R.id.next);
        back = findViewById(R.id.back);

        lineGraph = findViewById(R.id.line_graph);
        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        horizontalBarChart = findViewById(R.id.horizontal_bar_chart);

        next.setOnClickListener(this);
        back.setOnClickListener(this);

        lineGraph.setOnClickListener(this);
        barChart.setOnClickListener(this);
        pieChart.setOnClickListener(this);
        horizontalBarChart.setOnClickListener(this);

        // Set the line graph as default
        graphName = selectGraph(R.id.line_graph_border);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                Intent goToSelectColumns = new Intent(SelectGraphActivity.this, SelectColumnsActivity.class);
                goToSelectColumns.putExtra("graphType", graphName);
                startActivity(goToSelectColumns);
                break;

            case(R.id.back):
                finish();
                break;

            case(R.id.line_graph):
                graphName = selectGraph(R.id.line_graph_border);
                break;

            case(R.id.bar_chart):
                graphName = selectGraph(R.id.bar_chart_border);
                break;

            case(R.id.pie_chart):
                graphName = selectGraph(R.id.pie_chart_border);
                break;

            case(R.id.horizontal_bar_chart):
                graphName = selectGraph(R.id.horizontal_bar_chart_border);
                break;
        }
    }

    public String selectGraph(int id) {
        if (highlighted == 0) {
            findViewById(id).setVisibility(View.VISIBLE);
            highlighted = id;
            return getGraphName(id);
        }
        else if (id != highlighted) {
            try {
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(id).setVisibility(View.VISIBLE);
                highlighted = id;
                return getGraphName(id);
            }
            catch (NullPointerException npe) {
                Log.i("InstaGraph", npe.getMessage());
                // Line Graph is the default
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(R.id.line_graph_border).setVisibility(View.VISIBLE);
                highlighted = R.id.line_graph_border;
                Toast.makeText(this, npe + " occurred, Please try again.", Toast.LENGTH_LONG).show();
                return "Line Graph";
            }
        }
        return getGraphName(id);
    }

    public String getGraphName(int id) {
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
