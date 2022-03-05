# Imports
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import io
import csv
import urllib.request
from os.path import dirname, join

def step_one(url):

    # Check if file at url has a header
    has_header = check_for_header(url)

    # Handle exceptions
    dataset = pd.DataFrame()
    if has_header:
        dataset = pd.read_csv(url)
        try:
            dataset = pd.read_csv(url)
        except Exception as e:
            return e
        finally:
            try:
                assert not dataset.empty
            except AssertionError:
                return "Dataset is empty"
    else:
        try:
            dataset = pd.read_csv(url, header=None, prefix='Column ')
        except Exception as e:
            return e
        finally:
            try:
                assert not dataset.empty
            except AssertionError:
                return "Dataset is empty"



    # Get the first five rows to display as a preview
    ds_head = dataset.head()

    # Create the subplot, figure size is (the number of columns/1.5) wide, 1.2 high
    fig, ax = plt.subplots(figsize=(len(ds_head.columns)/1.5,1.2))
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

# Function to determine if a csv file has a header
def check_for_header(url):
    # Write contents of url to a file (temp.csv)
    temp_filename = join(dirname(__file__), 'temp.csv')

    # Use urlretrieve to write the contents of the url to a file (temp.csv)
    try:
        urllib.request.urlretrieve(url, temp_filename)
    except Exception as e:
        print(e)

    # Open the file
    with open(temp_filename) as file:
        # Read the first 1024 characters to determine whether the first row is consistent with
        # subsequent rows (i.e. string vs. integer)
        header = csv.Sniffer().has_header(file.read(1024))
    return header

# Function to return the names of a dataset's columns
def get_column_names(url):
    # Check if file at url has a header
    has_header = check_for_header(url)

    # Handle exceptions
    dataset = pd.DataFrame()
    if has_header:
        dataset = pd.read_csv(url)
        try:
            dataset = pd.read_csv(url)
        except Exception as e:
            return e
        finally:
            try:
                assert not dataset.empty
            except AssertionError:
                return "Dataset is empty"
    else:
        try:
            dataset = pd.read_csv(url, header=None, prefix='Column ')
        except Exception as e:
            return e
        finally:
            try:
                assert not dataset.empty
            except AssertionError:
                return "Dataset is empty"

    return list(dataset.columns.values)