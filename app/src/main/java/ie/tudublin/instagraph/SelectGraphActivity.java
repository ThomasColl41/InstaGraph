package ie.tudublin.instagraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

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

    RelativeLayout mainLayout;

    Popup pop;

    PopupWindow popWindow;


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
        mainLayout = findViewById(R.id.main_layout);

        next.setOnClickListener(this);
        back.setOnClickListener(this);

        lineGraph.setOnClickListener(this);
        barChart.setOnClickListener(this);
        pieChart.setOnClickListener(this);
        horizontalBarChart.setOnClickListener(this);

        pop = new Popup(SelectGraphActivity.this, mainLayout);

        // Set the line graph as default
        graphType = selectGraph(R.id.line_graph_border);

        Intent fromGetData = getIntent();
        userParameters = fromGetData.getParcelableExtra("userParameters");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case(R.id.next):
                // Set the graph choice in the ParameterParcel and go to SelectColumnsActivity
                userParameters.setGraphType(graphType);
                Intent goToSelectColumns = new Intent(SelectGraphActivity.this, SelectColumnsActivity.class);
                goToSelectColumns.putExtra("userParameters", userParameters);
                popWindow = pop.showPopup(getString(R.string.please_wait), true);
                startActivity(goToSelectColumns);
                break;

            case(R.id.back):
                finish();
                break;

            // Change the graph selection depending on which image was clicked
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

    // Method to adjust with visualisation option is highlighted
    public String selectGraph(int id) {
        // If no image is highlighted, highlight this one
        if (highlighted == 0) {
            try {
                findViewById(id).setVisibility(View.VISIBLE);
                highlighted = id;
                return getGraphType(id);
            }
            catch (NullPointerException npe) {
                // Line Graph is the default
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(R.id.line_graph_border).setVisibility(View.VISIBLE);
                highlighted = R.id.line_graph_border;
                popWindow = pop.showPopup(getString(R.string.graph_not_highlighted), false);
                return getString(R.string.line_graph);
            }
        }
        // If an element is already highlighted, remove the highlight and
        // apply it to the current element
        else if (id != highlighted) {
            try {
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(id).setVisibility(View.VISIBLE);
                highlighted = id;
                return getGraphType(id);
            }
            catch (NullPointerException npe) {
                // Line Graph is the default
                findViewById(highlighted).setVisibility(View.INVISIBLE);
                findViewById(R.id.line_graph_border).setVisibility(View.VISIBLE);
                highlighted = R.id.line_graph_border;
                popWindow = pop.showPopup(getString(R.string.graph_not_highlighted), false);
                return getString(R.string.line_graph);
            }
        }
        // Return the String that represents the graph
        return getGraphType(id);
    }

    // Method that returns a String used to identify
    // the chosen visualisation
    public String getGraphType(int id) {
        // Return name of selected graph
        switch(id) {
            case(R.id.line_graph_border):
                return getString(R.string.line_graph);

            case(R.id.bar_chart_border):
                return getString(R.string.bar_chart);

            case(R.id.pie_chart_border):
                return getString(R.string.pie_chart);

            case(R.id.horizontal_bar_chart_border):
                return getString(R.string.horizontal_bar_chart);

            default:
                return getString(R.string.line_graph);
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
