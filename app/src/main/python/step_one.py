def main(URL):
    # Imports
    import pandas as pd
    import matplotlib.pyplot as plt
    import numpy as np

    # dataset = pd.read_csv("https://raw.githubusercontent.com/jbrownlee/Datasets/master/shampoo.csv")
    # dataset = pd.read_csv(URL)

    # Just return the URL for now
    return URL

    # # Get the first five rows to display as a preview
    # ds_head = dataset.head()

    # # Create the subplot, figure size is (the number of columns/2) wide, 1 high 
    # fig, ax = plt.subplots(figsize=(len(ds_head.columns)/2,1))

    # # Plot the table
    # ds_table = ax.table(
    #     cellText=ds_head.values,    # The contents of each cell
    #     rowLabels=ds_head.index,    # The label for each row (index)
    #     colLabels=ds_head.columns,  # The label for each column (header) 
    #     loc='center'                # The location of the table in the figure
    # )

    # # Set auto column width and font size to reduce white space
    # ds_table.auto_set_column_width(np.arange(len(ds_head.columns)))
    # ds_table.auto_set_font_size(True)

    # # Disable original plot window
    # ax.set_axis_off()

    # # plt.show()

    # # plt.savefig('mytable.png')