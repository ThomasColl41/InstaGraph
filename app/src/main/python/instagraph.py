# Imports
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator
import numpy as np
import io
import csv
import urllib.request
from urllib.error import HTTPError
from os.path import dirname, join
from statsmodels.tsa.ar_model import AutoReg, ar_select_order
from statsmodels.tsa.arima.model import ARIMA
from statsmodels.tsa.holtwinters import SimpleExpSmoothing, ExponentialSmoothing
from statsmodels.tsa.stattools import adfuller
from com.chaquo.python import Python

# A custom Exception to raise and return to the app
class Error(Exception):
    pass

# Function to read in a dataset at a specified url
def read_dataset(url):
    # Variable to store the dataset at the specified URL
    dataset = None

    # Check if file at url has a header
    try:
        has_header = check_for_header(url)
    except UnicodeDecodeError:
        raise Error('Invalid file type. If the file is encoded, it should be encoded with utf-8. '
                    'Compressed files, such as a .zip file, are not accepted.')
    except Exception:
        raise Error('An unknown error occurred when attempting to read the dataset from the URL. '
                    'Please try again, or use a different URL.')

    # has_header could be an error message
    # If so, return it without proceeding
    if isinstance(has_header, str):
        return has_header

    # Handle exceptions
    try:
        if has_header:
            dataset = pd.read_csv(url, skip_blank_lines=True, encoding='utf_8')
        else:
            dataset = pd.read_csv(url, header=None, prefix='Column ', skip_blank_lines=True, encoding='utf_8')
    except Exception:
        raise Error('An unknown error occurred when reading from the URL. '
                    'Please ensure the URL is correct and try again.')
    finally:
        try:
            assert dataset is not None
            assert isinstance(dataset, pd.DataFrame)
            assert not dataset.empty
        except AssertionError:
            raise Error('The file located at the URL provided is either not a .csv, or is empty. '
                        'Please try again with a different URL.')

    # Inspired from https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir),'dataset.csv')
    dataset.to_csv(file_name, index=False)
    return file_name

def step_one(file_name):
    try:
        # Read the local copy of the dataset
        dataset = pd.read_csv(file_name, skip_blank_lines=True)
    except FileNotFoundError:
        raise Error('Local copy of dataset could not be read.')

    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        raise Error('Local copy of file is not a dataset. '
                    'Please try again with a different URL.')

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
    except HTTPError as httpe:
        if httpe.code == 404:
            raise Error('A file could not be found at the specified URL. '
                        'Please ensure the URL is correct and try again')
        else:
            raise Error('A HTTP ' + str(httpe.code) + ' error occurred.')
    except UnicodeDecodeError:
        raise Error('Invalid file type. If the file is encoded, it should be encoded with utf-8. '
                    'Compressed files, such as a .zip file, are not accepted.')

    # Open the file
    try:
        with open(temp_filename) as file:
            # Read the first 1024 characters to determine whether the first row is consistent with
            # subsequent rows (i.e. string vs. integer)
            try:
                header = csv.Sniffer().has_header(file.read(1024))
            except OSError:
                raise Error('A header could not be determined. '
                            'Please try again with a different dataset.')
    except OSError:
        raise Error('An error occurred when attempting to discover a header in the dataset. '
                    'Please try again with a different URL')
    return header

# Function to generate a summary of the dataset
def dataset_summary(file_name):
    try:
        # Read the local copy of the dataset
        dataset = pd.read_csv(file_name, skip_blank_lines=True)
    except FileNotFoundError:
        raise Error('Local copy of dataset could not be read.')

    preview_rows = 5
    nrows = dataset.shape[0]
    ncols = dataset.shape[1]
    summary =   ('The dataset has ' + str(nrows) + ' rows and ' +
                 str(ncols) + ' columns. ' +
                 'Above is a preview of the first ' + str(preview_rows) + ' rows.')
    return summary

# Function to return the names of a dataset's columns
def get_column_names(file_name):
    try:
        # Read the local copy of the dataset
        dataset = pd.read_csv(file_name, skip_blank_lines=True)
    except FileNotFoundError:
        raise Error('Local copy of dataset could not be read.')
    return list(dataset.columns.values)

# Function to generate the model_data DataFrame and save it
def read_model_data(file_name, xlabel='x-axis', ylabel='y-axis', first_last='', row_limit=''):
    try:
        # Read the local copy of the dataset
        dataset = pd.read_csv(file_name, skip_blank_lines=True)
    except FileNotFoundError:
        raise Error('Local copy of dataset could not be read.')

    # Check whether the user wishes to limit the number of rows
    if first_last != '' and row_limit != '' and row_limit is not None:
        rl = abs(int(row_limit))

        # Depending on whether First or Last was chosen, slice the dataset accordingly
        if first_last == 'First':
            limited_dataset = dataset[:rl]
        else:
            limited_dataset = dataset[-rl:]

        # Create the model_data DataFrame with the limited number of rows
        model_data = pd.DataFrame(limited_dataset[ylabel])
        model_data.index = limited_dataset[xlabel]
    else:
        model_data = pd.DataFrame(dataset[ylabel])
        model_data.index = dataset[xlabel]

    # Inspired from https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir),'model_data.csv')
    model_data.to_csv(file_name, index=True)
    return file_name

# Function to return the number of rows in the model_data dataset
def model_rows(file_name):
    model_data = pd.read_csv(file_name, skip_blank_lines=True, header=0)
    nrows = model_data.shape[0]
    return str(nrows)

# Function to test whether graphing the selected columns is appropriate
# Function to plot a graph based on user choice
def test_plot(file_name, graph_choice, xlabel='x-axis', ylabel='y-axis'):
    dataset = pd.read_csv(file_name, skip_blank_lines=True, header=0)

    test_data = pd.DataFrame(dataset[ylabel])
    test_data.index = dataset[xlabel]

    # Plot in a small window (will not be seen)
    fig, ax = plt.subplots(figsize=(1,1))

    # Catch incorrect data type exceptions when trying to plot data
    try:
        if graph_choice == 'Line Graph':
            ax.plot(test_data)
        elif graph_choice == 'Bar Chart':
            ax.bar(test_data.index.values, test_data[ylabel])
        elif graph_choice == 'Pie Chart':
            ax.pie(test_data[ylabel])
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(test_data.index.values, test_data[ylabel])
    except (TypeError, ValueError):
        raise Error('Unsuitable columns have been selected for graphing. Please try again.')
    except Exception:
        raise Error('An unknown error has occurred when attempting to plot the data. Please try again.')

    plt.show()

# Function to plot a graph based on user choice
def graph_plot(file_name, graph_choice, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph'):
    dataset = pd.read_csv(file_name, skip_blank_lines=True, header=0)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    # model_data = replace_index(model_data, model_data.index.name)

    nrows = model_data.shape[0]

    # List of place to display the axis ticks
    # Display a tick for the 10% mark, 20%, 30%, and so on
    pie_labels = [
        int(nrows*0.1),
        int(nrows*0.2),
        int(nrows*0.3),
        int(nrows*0.4),
        int(nrows*0.5),
        int(nrows*0.6),
        int(nrows*0.7),
        int(nrows*0.8),
        int(nrows*0.9),
        int(nrows),
    ]

    # Set the font size for every future plot
    plt.rcParams['font.size'] = '20'

    fig, ax = plt.subplots(figsize=(11,16))

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    if graph_choice != 'Horizontal Bar Chart':
        # Limit the number of x-ticks displayed to 10
        # Inspired by https://www.delftstack.com/howto/matplotlib/matplotlib-set-number-of-ticks/
        ax.xaxis.set_major_locator(MaxNLocator(10))

        # Rotate the x-axis values by 45 degrees
        plt.xticks(rotation=45)
    else:
        # Limit the number of y-ticks displayed to 10
        ax.yaxis.set_major_locator(MaxNLocator(10))

    # Catch incorrect data type exceptions when trying to plot data
    try:
        if graph_choice == 'Line Graph':
            ax.plot(model_data, linewidth=2)
        elif graph_choice == 'Bar Chart':
            ax.bar(model_data.index.values, model_data[ylabel])
        elif graph_choice == 'Pie Chart':
            patches, texts = ax.pie(model_data[ylabel], labels=model_data.index.values, startangle=90, counterclock=False)
            for i, t in enumerate(texts):
                if i in pie_labels:
                    t._visible = True
                else:
                    t._visible = False
            texts[0]._visible = True
            # No labels for pie chart
            ax.set_xlabel('')
            ax.set_ylabel('')
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(model_data.index.values, model_data[ylabel])
            # Opposite labels for horizontal bar chart
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

# Function to perform an augmented Dickey-Fuller unit root test
def adf_test(data):
    return adfuller(data, autolag='AIC')[1]

# Function to test different differences of data for stationarity (uses pandas' diff method)
def stationarity_test(data, d_label):
    # Take a copy of the model data to check differencing on
    differences = data.copy()

    # Dictionary for holding p-values of ADF test
    p_dict = {'No_Diff': adf_test(differences[d_label])}

    # First order differencing
    differences['First_Diff'] = differences[d_label].diff(1)

    # Second order differencing
    differences['Second_Diff'] = differences['First_Diff'].diff(1)

    p_dict['First_Diff'] = adf_test(differences['First_Diff'].dropna())
    p_dict['Second_Diff'] = adf_test(differences['Second_Diff'].dropna())

    stationarity = min(p_dict, key=p_dict.get)
    return str(stationarity)

# Function to check if ARIMA is being used with a RangeIndex (does not work)
def arima_check(file_name, xlabel='x-axis', ylabel='y-axis', model_choice='AR',):
    dataset = pd.read_csv(file_name, skip_blank_lines=True)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    model_data = replace_index(model_data, model_data.index.name)

    if isinstance(model_data.index, pd.RangeIndex) and model_choice == 'ARIMA':
        raise Error('The dataset is not suitable for prediction with ARIMA. Please choose a different model.')

# Function to predict future values
def predict(file_name, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph', graph_choice='Line Graph', model_choice='AR', para1='', para2='', para3='', para4='', num_predictions=''):
    dataset = pd.read_csv(file_name, skip_blank_lines=True)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    model_data = replace_index(model_data, model_data.index.name)

    # Limit number of lags to check to 12 to reduce execution time
    max_lag = 12
    if num_predictions == '' or num_predictions is None:
        num_predictions = 12
    else:
        num_predictions = abs(int(num_predictions))

    # Determine appropriate lag structure for AR and ARIMA models
    if model_choice == 'AR' or model_choice == 'ARIMA':
        model_lags = ar_select_order(model_data, maxlag=max_lag, ic='aic').ar_lags
    else:
        model_lags = 0

    if model_choice == 'AR':
        # Autoregression
        # If custom parameters are blank, set them with appropriate values
        if para1 == '' or para1 is None:
            para1 = model_lags
        else:
            para1 = abs(int(para1))
        if para2 == '' or para2 is None:
            para2 = 'c'

        model = AutoReg(model_data, lags=para1, trend=para2).fit()

    elif model_choice == 'ARIMA':
        # ARIMA
        differencing = stationarity_test(model_data, ylabel)
        if differencing == 'First_Diff':
            diff_order = 1
        elif differencing == 'Second_Diff':
            diff_order = 2
        else:
            diff_order = 0

        # If custom parameters are blank, set them with appropriate values
        if para1 == '' or para1 is None:
            para1 = model_lags
        if para2 == '' or para2 is None:
            para2 = diff_order
        if para3 == '' or para3 is None:
            para3 = 1
        if para4 == '' or para4 is None:
            para4 = 'c'

        model = ARIMA(model_data, order=(int(para1), int(para2), int(para3)), trend=para4).fit()
    elif model_choice == 'SES':
        # SES
        # No custom parameters in SES
        model = SimpleExpSmoothing(model_data).fit()
    elif model_choice == 'HWES':
        # HWES
        # If custom parameters are blank, set them with appropriate values
        if para1 == '' or para1 is None:
            para1 = 'additive'
        if para2 == '' or para2 is None:
            para2 = 'additive'
        if para3 == '' or para3 is None:
            para3 = 12

        model = ExponentialSmoothing(model_data, trend=para1, seasonal=para2, seasonal_periods=int(para3)).fit()
    else:
        raise Error('The choice of model could not be determined')

    # Since dates cannot be added to with integers, if the dataset uses a DateTimeIndex,
    # use the pandas Timedelta class to generate the forecast datetimes
    if isinstance(model_data.index, pd.DatetimeIndex):
        # If SES is used, adjust the smoothing parameter to be optimised
        # Inspired by https://www.statsmodels.org/devel/examples/notebooks/generated/exponential_smoothing.html#Holt%E2%80%99s-Winters-Seasonal
        if model_choice == 'SES':
            predictions = model.predict(model_data.index[-1], model_data.index[-1] + pd.Timedelta(num_predictions - 1, unit=str(model_data.index.freqstr))).rename(r"$\alpha=%s$" % model.model.params["smoothing_level"])
        else:
            predictions = model.predict(model_data.index[-1], model_data.index[-1] + pd.Timedelta(num_predictions - 1, unit=str(model_data.index.freqstr)))
    # Otherwise, add on to the RangeIndex
    else:
        # If SES is used, adjust the smoothing parameter to be optimised
        # Inspired by https://www.statsmodels.org/devel/examples/notebooks/generated/exponential_smoothing.html#Holt%E2%80%99s-Winters-Seasonal
        if model_choice == 'SES':
            predictions = model.predict(model_data.index[-1], model_data.index[-1] + num_predictions).rename(r"$\alpha=%s$" % model.model.params["smoothing_level"])
        else:
            predictions = model.predict(model_data.index[-1], model_data.index[-1] + num_predictions)

    predictions.name = ylabel

    full_data = pd.concat([model_data[ylabel], predictions])

    # Set the data column's header
    full_data.name = ylabel

    # Set the index's name
    full_data.index.name = xlabel

    save_predictions(full_data)

    nrows = full_data.shape[0]

    # List of place to display the labels for the pie chart
    # Display a tick for the 10% mark, 20%, 30%, and so on
    pie_labels = [
        int(nrows*0.1),
        int(nrows*0.2),
        int(nrows*0.3),
        int(nrows*0.4),
        int(nrows*0.5),
        int(nrows*0.6),
        int(nrows*0.7),
        int(nrows*0.8),
        int(nrows*0.9),
        int(nrows),
    ]

    fig, ax = plt.subplots(figsize=(11,16))

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    if graph_choice != 'Horizontal Bar Chart':
        # Rotate the x-axis values by 45 degrees
        plt.xticks(rotation=45)

    if isinstance(full_data.index, pd.DatetimeIndex):
        step_back = 1
    else:
        step_back = 2

    # Catch incorrect data type exceptions when trying to plot data
    try:
        if graph_choice == 'Line Graph':
            ax.plot(full_data[:-num_predictions], linewidth=2)
            ax.plot(full_data[-num_predictions - step_back:], color='red', linewidth=2)
        elif graph_choice == 'Bar Chart':
            ax.bar(full_data[:-num_predictions].index.values, full_data[:-num_predictions])
            ax.bar(full_data[-num_predictions - step_back:].index.values, full_data[-num_predictions - step_back:], color='red')
        elif graph_choice == 'Pie Chart':
            patches, texts = ax.pie(full_data.dropna(), labels=full_data.dropna().index.values, startangle=90, counterclock=False)
            for i, t in enumerate(texts):
                if i in pie_labels:
                    t._visible = True
                else:
                    t._visible = False
            texts[0]._visible = True
            # No labels for pie chart
            ax.set_xlabel('')
            ax.set_ylabel('')
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(full_data[:-num_predictions].index.values, full_data[:-num_predictions])
            ax.barh(full_data[-num_predictions - step_back:].index.values, full_data[-num_predictions - step_back:], color='red')
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

# Function to save the predictions dataframe
def save_predictions(dataset):
    # Inspired from https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir),'predictions.csv')
    dataset.dropna().to_csv(file_name, index=True)

# Function to return the file directory where the datasets are first saved
def read_predictions():
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir),'predictions.csv')
    return file_name