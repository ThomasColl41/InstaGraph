# Imports
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import io
import csv
import urllib.request
from os.path import dirname, join
from statsmodels.tsa.ar_model import AutoReg, ar_select_order
from com.chaquo.python import Python

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

    # Inspired from https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir),'dataset.csv')
    dataset.to_csv(file_name, index=False)
    return file_name

def step_one(file_name):
    dataset = pd.read_csv(file_name, skip_blank_lines=True)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    nrows = 5   # Number of rows to display
    ncols = len(dataset.columns)    # Number of columns in the dataset

    # Display the first few rows as a preview
    ds_head = dataset.head(nrows)

    # Create the plot, figure size is (number of columns * 3.5 wide, number of rows high)
    fig, ax = plt.subplots(figsize=(ncols * 3.5,nrows))
    # Plot the table
    ds_table = ax.table(
        cellText=ds_head.values,    # The contents of each cell
        rowLabels=ds_head.index,    # The label for each row (index)
        colLabels=ds_head.columns,  # The label for each column (header)
        loc='center'                # The location of the table in the figure
    )

    fontsize = 40   # Font size of the table cells
    ds_table.set_fontsize(fontsize)

    # Scale the table to increase row height (column width handled below)
    ds_table.scale(1,4)

    # Set auto column width and font size to reduce white space
    ds_table.auto_set_column_width(np.arange(len(ds_head.columns)))
    ds_table.auto_set_font_size(True)

    # Disable original plot window (axes)
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

# Function to generate a summary of the dataset
def dataset_summary(file_name):
    dataset = pd.read_csv(file_name, skip_blank_lines=True)
    preview_rows = 5
    nrows = dataset.shape[0]
    ncols = dataset.shape[1]
    summary =   ('The dataset has ' + str(nrows) + ' rows and ' +
                 str(ncols) + ' columns. ' +
                 'Above is a preview of the first ' + str(preview_rows) + ' rows.')
    return summary

# Function to return the names of a dataset's columns
def get_column_names(file_name):
    dataset = pd.read_csv(file_name, skip_blank_lines=True)
    return list(dataset.columns.values)

# Function to generate the model_data DataFrame and save it
def read_model_data(file_name, xlabel='x-axis', ylabel='y-axis'):
    dataset = pd.read_csv(file_name, skip_blank_lines=True)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    # Inspired from https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir),'model_data.csv')
    model_data.to_csv(file_name, index=False)
    return file_name

# Function to plot a graph based on user choice
def graph_plot(file_name, graph_choice, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph'):
    model_data = pd.read_csv(file_name, skip_blank_lines=True, header=0)

    model_data = replace_index(model_data, model_data.index.name)

    fig, ax = plt.subplots()

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    # Catch incorrect data type exceptions when trying to plot data
    try:
        if graph_choice == 'Line Graph':
            ax.plot(model_data)
        elif graph_choice == 'Bar Chart':
            ax.bar(model_data.index.values, model_data[model_data.columns[0]])
        elif graph_choice == 'Pie Chart':
            ax.pie(model_data)
            # No labels for pie chart
            ax.set_xlabel('')
            ax.set_ylabel('')
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(model_data.index.values, model_data[model_data.columns[0]])
            # Opposite labels for horizontal bar chart
            ax.set_xlabel(ylabel)
            ax.set_ylabel(xlabel)
    except TypeError as te:
        return str(te + ' has occurred, check column choices')

    # Display plot
    plt.show()

    # Return plot as buffer
    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer

# Function to replace the index of a dataset with a suitable pandas Index
def replace_index(data, index_name='Index'):
    # If the index is made up of integers, convert to a RangeIndex
    if data.index.dtype == 'int64':
        # Assuming the data is not missing any steps
        index_step = data.index[1] - data.index[0]
        data.index = pd.RangeIndex(start=data.index[0], stop=(len(data.index) - 1) + index_step, step=index_step)
    else:
        # Assuming a non-integer index contains dates, attempt to convert to DatetimeIndex
        try:
            data.index = pd.DatetimeIndex(data=data.index.values, freq='infer')
            # If frequency of index could not be inferred, resort to RangeIndex
            if data.index.freq == None:
                data.index = pd.RangeIndex(start=1, stop=len(data.index) + 1, step=1)
        except Exception as te:
            # If all else fails, create a basic RangeIndex of 1,2,3...
            print(te, 'has occurred, swapping to integer index')
            data.index = pd.RangeIndex(start=1, stop=len(data.index) + 1, step=1)

    # Assign a name to the new index
    data.index.name = index_name
    return pd.DataFrame(data)

# Function to predict future values
def predict(file_name, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph', graph_choice='Line Graph', model_choice='AR'):
    model_data = pd.read_csv(file_name, skip_blank_lines=True)

    model_data = replace_index(model_data, model_data.index.name)

    # Limit number of lags to check to 12 to reduce execution time
    max_lag = 12
    num_predictions = 12

    # Determine appropriate lag structure
    model_lags = ar_select_order(model_data, maxlag=max_lag, ic='aic').ar_lags

    # Try and create a model with the appropriate lag structure
    model = AutoReg(model_data, lags = model_lags).fit()

    # predictions = model.forecast(num_predictions)
    # full_data = model_data[model_data.columns[0]].append(predictions)

    predictions = AutoReg(model_data, lags=model_lags).fit().predict(model_data.index[-1], model_data.index[-1] + num_predictions)
    full_data = model_data[model_data.columns[0]].append(predictions).dropna()

    fig, ax = plt.subplots()

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    # Catch incorrect data type execeptions when trying to plot data
    try:
        if graph_choice == 'Line Graph':
            ax.plot(full_data[:-num_predictions + 1])
            ax.plot(full_data[-num_predictions:], color='red')
        elif graph_choice == 'Bar Chart':
            ax.bar(full_data[:-num_predictions + 1].index.values, full_data[:-num_predictions + 1])
            ax.bar(full_data[-num_predictions:].index.values, full_data[-num_predictions:], color='red')
        elif graph_choice == 'Pie Chart':
            # ax.pie(full_data2.index.values)
            ax.pie(full_data)
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(full_data[:-num_predictions + 1].index.values, full_data[:-num_predictions + 1])
            ax.barh(full_data[-num_predictions:].index.values, full_data[-num_predictions:], color='red')
            ax.set_xlabel(ylabel)
            ax.set_ylabel(xlabel)
    except TypeError as te:
        return str(str(te) + ' has occurred, check column choices')

    # Display plot
    plt.show()

    # Return plot as buffer
    io_buff = io.BytesIO()
    plt.savefig(io_buff, format="png")
    buffer = io_buff.getvalue()

    return buffer