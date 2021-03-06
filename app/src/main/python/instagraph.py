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
# When an Error Exception is raised, the message will appear as
# "Error: <message>" to the user
# Instead of raise a particular Exception, raise an Error
# with a message specific to that exception
class Error(Exception):
    pass


# Function to read in a dataset at a specified url and save it locally
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

    # Handle exceptions when reading the dataset
    try:
        if has_header:
            # If there is a header, it will be inferred
            dataset = pd.read_csv(url, skip_blank_lines=True, encoding='utf_8')
        else:
            # If there is no header, prefix 'Column ' to each column heading
            dataset = pd.read_csv(url, header=None, prefix='Column ', skip_blank_lines=True,
                                  encoding='utf_8')
    except Exception:
        raise Error('An unknown error occurred when reading from the URL. '
                    'Please ensure the URL is correct and try again.')
    finally:
        try:
            # Assert that a dataset was retrieved
            assert dataset is not None
            assert isinstance(dataset, pd.DataFrame)
            assert not dataset.empty
        except AssertionError:
            raise Error('The file located at the URL provided is either not a .csv, or is empty. '
                        'Please try again with a different URL.')

    # ***Code referenced from
    # ***https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    # Get the local file directory for the application
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    # Join 'dataset.csv' to the directory path to form the full file path
    file_name = join(dirname(files_dir), 'dataset.csv')
    # ***End reference

    # Save the dataset to the directory as 'dataset.csv'
    dataset.to_csv(file_name, index=False)

    # Return the path to the file
    return file_name


# Function to generate a preview of a dataset in a matplotlib table and
# return the plot as a byte array
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

    nrows = 5  # Number of rows to display
    ncols = len(dataset.columns)  # Number of columns in the dataset

    # Display the first few rows as a preview
    ds_head = dataset.head(nrows)

    # Create the plot, figure size is (number of columns * 3.5 wide, number of rows high)
    fig, ax = plt.subplots(figsize=(ncols * 3.5, nrows))
    # Plot the table
    ds_table = ax.table(
        cellText=ds_head.values,  # The contents of each cell
        rowLabels=ds_head.index,  # The label for each row (index)
        colLabels=ds_head.columns,  # The label for each column (header)
        loc='center'  # The location of the table in the figure
    )

    fontsize = 40  # Font size of the table cells
    ds_table.set_fontsize(fontsize)

    # Scale the table to increase row height (column width handled below)
    ds_table.scale(1, 4)

    # Set auto column width and font size to reduce white space
    ds_table.auto_set_column_width(np.arange(len(ds_head.columns)))
    ds_table.auto_set_font_size(True)

    # Disable original plot axes
    ax.set_axis_off()

    # Create an IO buffer
    io_buff = io.BytesIO()

    # Save the plot to the buffer in png format
    plt.savefig(io_buff, format="png")

    # Return the contents of the buffer
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
        # Check for a 404 error, otherwise return a general message
        if httpe.code == 404:
            raise Error('A file could not be found at the specified URL. '
                        'Please ensure the URL is correct and try again')
        else:
            raise Error('A HTTP ' + str(httpe.code) + ' error occurred.')
    except UnicodeDecodeError:
        raise Error('Invalid file type. If the file is encoded, it should be encoded with utf-8. '
                    'Compressed files, such as a .zip file, are not accepted.')

    # Open the local file
    try:
        with open(temp_filename) as file:
            # Read the first 1024 characters to determine whether the first row is consistent with
            # subsequent rows (i.e. string vs. integer)
            try:
                header = csv.Sniffer().has_header(file.read(2048))
            except OSError:
                raise Error('A header could not be determined. '
                            'Please try again with a different dataset.')
    except OSError:
        raise Error('An error occurred when attempting to discover a header in the dataset. '
                    'Please try again with a different URL')
    # Return a boolean value which represents whether the dataset has a header or not
    return header


# Function to generate a summary of the dataset
def dataset_summary(file_name):
    try:
        # Read the local copy of the dataset
        dataset = pd.read_csv(file_name, skip_blank_lines=True)
    except FileNotFoundError:
        raise Error('Local copy of dataset could not be read.')

    preview_rows = 5            # Number of rows to preview
    nrows = dataset.shape[0]    # Number of rows in the dataset
    ncols = dataset.shape[1]    # Number of columns in the dataset

    # Create and return summary String
    summary = ('The dataset has ' + str(nrows) + ' rows and ' +
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

    # Return the column names as a list
    return list(dataset.columns.values)


# Function to generate a DataFrame using just user specified columns and save it as a file
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
        # Create the model_data DataFrame with all the rows
        model_data = pd.DataFrame(dataset[ylabel])
        model_data.index = dataset[xlabel]

    # ***Code referenced from
    # ***https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    # Get the local file directory for the application
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())

    # Join 'model_data.csv' to the directory path to form the full file path
    file_name = join(dirname(files_dir), 'model_data.csv')
    # ***End reference

    # Save the dataset to the directory as 'dataset.csv'
    model_data.to_csv(file_name, index=True)

    # Return the path to the file
    return file_name


# Function to return a dataset file as a DataFrame
def get_dataframe(file_name, xlabel='x-axis', ylabel='y-label'):
    # Read the file
    dataset = pd.read_csv(file_name, skip_blank_lines=True, header=0)

    # Limit the columns to those set by the user
    dataset_df = pd.DataFrame(dataset[ylabel])
    dataset_df.index = dataset[xlabel]

    # Return the DataFrame
    return dataset_df


# Function to plot a Dataframe
def dataframe_plot(model_data, graph_choice, xlabel='x-axis', ylabel='y-axis',
                   title='Title of Line Graph'):
    # Get the number of rows in the DataFrame
    nrows = model_data.shape[0]

    # List of places to display the labels for pie charts
    # Display a label every 10% of the dataset's length
    pie_labels = [
        int(nrows * 0.1),
        int(nrows * 0.2),
        int(nrows * 0.3),
        int(nrows * 0.4),
        int(nrows * 0.5),
        int(nrows * 0.6),
        int(nrows * 0.7),
        int(nrows * 0.8),
        int(nrows * 0.9),
        int(nrows),
    ]

    # Set the font size for every future plot
    plt.rcParams['font.size'] = '20'

    # Set the plot size to be big enough to see
    fig, ax = plt.subplots(figsize=(11, 16))

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    if graph_choice != 'Horizontal Bar Chart':
        # Limit the number of x-ticks displayed to 10
        # ***Code referenced from
        # ***https://www.delftstack.com/howto/matplotlib/matplotlib-set-number-of-ticks/
        ax.xaxis.set_major_locator(MaxNLocator(10))
        # ***End reference

        # Rotate the x-axis values by 45 degrees
        plt.xticks(rotation=45)
    else:
        # Limit the number of y-ticks displayed to 10
        # ***Code referenced from
        # ***https://www.delftstack.com/howto/matplotlib/matplotlib-set-number-of-ticks/
        ax.yaxis.set_major_locator(MaxNLocator(10))
        # ***End reference

    # Catch incorrect data type exceptions when trying to plot data
    try:
        if graph_choice == 'Line Graph':
            ax.plot(model_data, linewidth=2)
        elif graph_choice == 'Bar Chart':
            ax.bar(model_data.index.values, model_data[ylabel])
        elif graph_choice == 'Pie Chart':
            # ***Code referenced from
            # ***https://matplotlib.org/stable/api/_as_gen/matplotlib.axes.Axes.pie.html
            # ***https://matplotlib.org/stable/api/text_api.html#matplotlib.text.Text
            patches, texts = ax.pie(model_data[ylabel], labels=model_data.index.values,
                                    startangle=90, counterclock=False)
            for i, t in enumerate(texts):
                if i in pie_labels:
                    t._visible = True
                else:
                    t._visible = False
            texts[0]._visible = True
            # ***End reference

            # No labels for pie chart
            ax.set_xlabel('')
            ax.set_ylabel('')
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(model_data.index.values, model_data[ylabel])

            # Opposite labels for horizontal bar chart
            ax.set_xlabel(ylabel)
            ax.set_ylabel(xlabel)
    except TypeError:
        raise Error('Invalid column choices. The plot could not be displayed')

    # Display plot
    plt.show()

    # Create an IO buffer
    io_buff = io.BytesIO()

    # Save the plot to the buffer in png format
    plt.savefig(io_buff, format="png")

    # Return the contents of the buffer
    buffer = io_buff.getvalue()

    return buffer


# Function to return the number of rows in the a dataset
def model_rows(file_name):
    # Read the file
    model_data = pd.read_csv(file_name, skip_blank_lines=True, header=0)

    # Return the number of rows as a String
    nrows = model_data.shape[0]
    return str(nrows)


# Function to test whether graphing the selected columns is appropriate
def test_plot(file_name, graph_choice, xlabel='x-axis', ylabel='y-axis'):
    # Read in the dataset
    dataset = pd.read_csv(file_name, skip_blank_lines=True, header=0)

    # Set up the DataFrame according to user preference
    test_data = pd.DataFrame(dataset[ylabel])
    test_data.index = dataset[xlabel]

    # Plot in a small window (will not be seen)
    fig, ax = plt.subplots(figsize=(1, 1))

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
        raise Error(
            'An unknown error has occurred when attempting to plot the data. Please try again.')

    plt.show()


# Function to replace the index of a dataset with a suitable pandas Index
def replace_index(data, index_name='Index'):
    # If the index is made up of integers, convert to a RangeIndex
    if data.index.dtype == 'int64':
        # Assuming the data is not missing any steps
        index_step = data.index[1] - data.index[0]
        data.index = pd.RangeIndex(start=data.index[0], stop=(len(data.index) - 1) + index_step,
                                   step=index_step)
    else:
        try:
            # Assuming a non-integer index contains dates, attempt to convert to DatetimeIndex
            data.index = pd.DatetimeIndex(data=data.index.values, freq='infer')

            # If frequency of index could not be inferred, resort to RangeIndex
            if data.index.freq is None:
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


# Function to test different differences to achieve stationarity (uses pandas' diff method)
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
def arima_check(file_name, xlabel='x-axis', ylabel='y-axis', model_choice='AR', ):
    # Read in the datast
    dataset = pd.read_csv(file_name, skip_blank_lines=True)

    # Set up the DataFrame according to user preference
    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    # Call replace_index to prepare the dataset for predictions
    model_data = replace_index(model_data, model_data.index.name)

    # If the dataset has a RangeIndex, ARIMA will not predict values
    if isinstance(model_data.index, pd.RangeIndex) and model_choice == 'ARIMA':
        raise Error(
            'The dataset is not suitable for prediction with ARIMA. Please choose a different '
            'model.')


# Function to predict future values
def predict(file_name, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph',
            graph_choice='Line Graph', model_choice='AR', para1='', para2='', para3='', para4='',
            num_predictions=''):
    # Read in the dataset
    dataset = pd.read_csv(file_name, skip_blank_lines=True)

    # Set up the DataFrame according to user column selection
    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    # Set the correct pandas Index in place to improve prediction results
    # With a proper Index, the index values will be extended to match the predictions
    model_data = replace_index(model_data, model_data.index.name)

    # Limit the number of lags to check to 12 to reduce execution time
    max_lag = 12

    # Check if the user has specified the number of predictions to make
    # The default is 12 (the user has not entered a value)
    if num_predictions == '' or num_predictions is None:
        num_predictions = 12
    else:
        num_predictions = abs(int(num_predictions))

    # Determine appropriate lag structure for AR and ARIMA models
    # If the AR order is specified, use it when deciding model lags
    if model_choice == 'AR' or model_choice == 'ARIMA':
        if para1 == '':
            model_lags = ar_select_order(model_data, maxlag=max_lag, ic='aic').ar_lags
        else:
            model_lags = ar_select_order(model_data, maxlag=int(para1), ic='aic').ar_lags
    else:
        # For other models, there is no model_lags
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

        # Fit the model
        model = AutoReg(model_data, lags=para1, trend=para2).fit()

    elif model_choice == 'ARIMA':
        # ARIMA
        # Determine the order of differencing
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

        # Fit the model
        model = ARIMA(model_data, order=(int(para1), int(para2), int(para3)), trend=para4).fit()
    elif model_choice == 'SES':
        # SES
        # No custom parameters in SES
        # Fit the model
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

        # Fit the model
        model = ExponentialSmoothing(model_data, trend=para1, seasonal=para2,
                                     seasonal_periods=int(para3)).fit()
    else:
        raise Error('The choice of model could not be determined')

    # Since dates cannot be added to like integers, if the dataset uses a DateTimeIndex,
    # use the pandas Timedelta class to generate the forecast datetimes
    if isinstance(model_data.index, pd.DatetimeIndex):
        # If SES is used, adjust the smoothing parameter to be optimised
        if model_choice == 'SES':
            # ***Code referenced from
            # ***https://www.statsmodels.org/devel/examples/notebooks/generated/exponential_smoothing.html#Holt%E2%80%99s-Winters-Seasonal
            predictions = model.predict(model_data.index[-1],
                                        model_data.index[-1] + pd.Timedelta(num_predictions - 1,
                                        unit=str(model_data.index.freqstr))).rename(
                                        r"$\alpha=%s$" % model.model.params["smoothing_level"])
            # ***End reference
        else:
            predictions = model.predict(model_data.index[-1],
                                        model_data.index[-1] + pd.Timedelta(num_predictions - 1,
                                        unit=str(model_data.index.freqstr)))
    # Otherwise, add on to the RangeIndex
    else:
        # If SES is used, adjust the smoothing parameter to be optimised
        if model_choice == 'SES':
            # ***Code referenced from
            # ***https://www.statsmodels.org/devel/examples/notebooks/generated/exponential_smoothing.html#Holt%E2%80%99s-Winters-Seasonal
            predictions = model.predict(model_data.index[-1],
                                        model_data.index[-1] + num_predictions).rename(
                                        r"$\alpha=%s$" % model.model.params["smoothing_level"])
            # ***End reference
        else:
            predictions = model.predict(model_data.index[-1],
                                        model_data.index[-1] + num_predictions)

    # prediction is a pandas Series (a single column)
    # name sets the header for the Series
    predictions.name = ylabel

    # Concatenate the original data with the predictions
    full_data = pd.concat([model_data[ylabel], predictions])

    # Set the data column's header
    full_data.name = ylabel

    # Set the index's name
    full_data.index.name = xlabel

    # Save this new DataFrame as a file
    save_predictions(full_data)

    # Get the number of rows
    nrows = full_data.shape[0]

    # List of places to display the labels for pie charts
    # Display a label every 10% of the dataset's length
    pie_labels = [
        int(nrows * 0.1),
        int(nrows * 0.2),
        int(nrows * 0.3),
        int(nrows * 0.4),
        int(nrows * 0.5),
        int(nrows * 0.6),
        int(nrows * 0.7),
        int(nrows * 0.8),
        int(nrows * 0.9),
        int(nrows),
    ]

    # Set the plot size to be big enough to see
    fig, ax = plt.subplots(figsize=(11, 16))

    # Set axis labels and title
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    plt.title(title)

    if graph_choice != 'Horizontal Bar Chart':
        # Rotate the x-axis values by 45 degrees
        plt.xticks(rotation=45)

    # Depending on the Index used, step back some values when slicing.
    # This allows for the data and predictions to be displayed consistently
    # i.e. in a line graph there will be one continuous line
    if isinstance(full_data.index, pd.DatetimeIndex):
        step_back = 1
    else:
        step_back = 2

    # Catch incorrect data type exceptions when trying to plot data
    try:
        # Plot the original data in the default colour (blue)
        # Plot the predictions in red
        # num_predictions represents where to slice the dta
        if graph_choice == 'Line Graph':
            ax.plot(full_data[:-num_predictions], linewidth=2)
            ax.plot(full_data[-num_predictions - step_back:], color='red', linewidth=2)
        elif graph_choice == 'Bar Chart':
            ax.bar(full_data[:-num_predictions].index.values, full_data[:-num_predictions])
            ax.bar(full_data[-num_predictions - step_back:].index.values,
                   full_data[-num_predictions - step_back:], color='red')
        elif graph_choice == 'Pie Chart':
            # ***Code referenced from
            # ***https://matplotlib.org/stable/api/_as_gen/matplotlib.axes.Axes.pie.html
            # ***https://matplotlib.org/stable/api/text_api.html#matplotlib.text.Text
            patches, texts = ax.pie(full_data.dropna(), labels=full_data.dropna().index.values,
                                    startangle=90, counterclock=False)
            for i, t in enumerate(texts):
                if i in pie_labels:
                    t._visible = True
                else:
                    t._visible = False
            texts[0]._visible = True
            # ***End reference

            # No labels for pie chart
            ax.set_xlabel('')
            ax.set_ylabel('')
        elif graph_choice == 'Horizontal Bar Chart':
            ax.barh(full_data[:-num_predictions].index.values, full_data[:-num_predictions])
            ax.barh(full_data[-num_predictions - step_back:].index.values,
                    full_data[-num_predictions - step_back:], color='red')

            # Opposite labels for horizontal bar chart
            ax.set_xlabel(ylabel)
            ax.set_ylabel(xlabel)
    except TypeError:
        raise Error('Invalid column choices. The plot could not be displayed')

    # Display plot
    plt.show()

    # Create an IO buffer
    io_buff = io.BytesIO()

    # Save the plot to the buffer in png format
    plt.savefig(io_buff, format="png")

    # Return the contents of the buffer
    buffer = io_buff.getvalue()
    return buffer


# Function to save the predictions dataframe as a file
def save_predictions(dataset):
    # ***Code referenced from
    # ***https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    # Get the local file directory for the application
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())

    # Join 'predictions.csv' to the directory path to form the full file path
    file_name = join(dirname(files_dir), 'predictions.csv')
    # ***End reference

    # Save the dataset as a file
    dataset.dropna().to_csv(file_name, index=True)


# Function to return the file directory where the prediction dataset is first saved
def read_predictions():
    # ***Code referenced from
    # ***https://www.youtube.com/watch?v=sm02Q91ujfs&list=PLeOtHc_su2eXZuiqCH4pBgV6vamBbP88K&index=7
    # Get the local file directory for the application
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())

    # Join 'predictions.csv' to the directory path to form the full file path
    file_name = join(dirname(files_dir), 'predictions.csv')
    # ***End reference

    # Return the path to the file
    return file_name
