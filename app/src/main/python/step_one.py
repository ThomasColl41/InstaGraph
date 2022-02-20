# Imports
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import io

def main(url):
    # Handle exceptions
    dataset = pd.DataFrame()
    try:
        dataset = pd.read_csv(url)
    except Exception as e:
        return e
    finally:
        try:
            assert not dataset.empty
        except AssertionError:
            return "Dataset is empty"

    # Get the first five rows to display as a preview
    ds_head = dataset.head()

    # Create the subplot, figure size is (the number of columns/2) wide, 1 high
    fig, ax = plt.subplots(figsize=(len(ds_head.columns)/2,1))

    # Plot the table
    ds_table = ax.table(
        cellText=ds_head.values,    # The contents of each cell
        rowLabels=ds_head.index,    # The label for each row (index)
        colLabels=ds_head.columns,  # The label for each column (header)
        loc='center'                # The location of the table in the figure
    )

    # Set auto column width and font size to reduce white space
    ds_table.auto_set_column_width(np.arange(len(ds_head.columns)))
    ds_table.auto_set_font_size(True)

    # Disable original plot window
    ax.set_axis_off()

    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer