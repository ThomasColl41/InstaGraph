# Imports
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import io
import csv
import urllib.request
from os.path import dirname, join

# Function to read in a dataset at a specified url
def read_dataset(url):
    # Check if file at url has a header
    has_header = check_for_header(url)

    # Handle exceptions
    dataset = pd.DataFrame()
    if has_header:
        try:
            dataset = pd.read_csv(url, skip_blank_lines=True)
        except Exception as e:
            return e
        finally:
            try:
                assert not dataset.empty
            except AssertionError:
                return "Dataset is empty"
    else:
        try:
            dataset = pd.read_csv(url, header=None, prefix='Column ', skip_blank_lines=True)
        except Exception as e:
            return e
        finally:
            try:
                assert not dataset.empty
            except AssertionError:
                return "Dataset is empty"
    return dataset

def step_one(url):
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

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
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    return list(dataset.columns.values)

# Function to plot a line graph
def line_graph_plot(url, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph', other_data=pd.DataFrame()):
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    fig, ax = plt.subplots()

    # Catch incorrect data type execeptions when trying to plot data
    try:
        ax.plot(model_data)
        # if not other_data.empty:
        #     ax.plot(other_data, color='red')
    except TypeError as te:
        return str(te + ' has occurred, check column choices')

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    # Display plot
    plt.show()

    # Return plot as buffer
    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer

# Function to plot a bar chart
def bar_chart_plot(url, xlabel='x-axis', ylabel='y-axis', title='Title of Bar Chart', other_data=pd.DataFrame()):
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    model_data = pd.DataFrame(dataset[xlabel])
    model_data.index = dataset[ylabel]

    fig, ax = plt.subplots()

    # Catch incorrect data type execeptions when trying to plot data
    try:
        ax.bar(model_data[model_data.columns[0]], model_data.index.values)
        # if not other_data.empty:
        #     ax.plot(other_data, color='red')
    except TypeError as te:
        return str(te + ' has occurred, check column choices')

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    # Display plot
    plt.show()

    # Return plot as buffer
    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer

# Function to plot a bar chart
def pie_chart_plot(url, xlabel='x-axis', ylabel='y-axis', title='Title of Pie Chart', other_data=pd.DataFrame()):
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    fig, ax = plt.subplots()

    # Catch incorrect data type execeptions when trying to plot data
    try:
        ax.pie(model_data)
        # if not other_data.empty:
        #     ax.plot(other_data, color='red')
    except TypeError as te:
        return str(te + ' has occurred, check column choices')

    # Set axis labels and title
    # ax.set_xlabel(xlabel)
    # ax.set_ylabel(ylabel)
    plt.title(title)

    # Display plot
    plt.show()

    # Return plot as buffer
    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer

# Function to plot a bar chart
def horizontal_bar_chart_plot(url, xlabel='x-axis', ylabel='y-axis', title='Title of Horizontal Bar Chart', other_data=pd.DataFrame()):
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    model_data = pd.DataFrame(dataset[xlabel])
    model_data.index = dataset[ylabel]

    fig, ax = plt.subplots()

    # Catch incorrect data type execeptions when trying to plot data
    try:
        ax.barh(model_data[model_data.columns[0]], model_data.index.values)
        # if not other_data.empty:
        #     ax.plot(other_data, color='red')
    except TypeError as te:
        return str(te + ' has occurred, check column choices')

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    # Display plot
    plt.show()

    # Return plot as buffer
    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer