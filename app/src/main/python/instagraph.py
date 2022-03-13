# Imports
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import io
import csv
import urllib.request
from os.path import dirname, join
# import pmdarima as pm
from statsmodels.tsa.ar_model import AutoReg, ar_select_order

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

    # Function to calculate model aicc
def model_aicc(data, seasonal=False, periods=1):
    model = None

    # aicc is initially infinity (the most unsuitable)
    aicc = np.inf

    # Limit number of lags to check to 12 to reduce execution time
    max_lag = 12

    # Create model differently if there is seasonality
    if seasonal:
        # Determine appropriate lag structure
        model_lags = ar_select_order(data, maxlag = max_lag, ic='aic', seasonal = True, period = periods).ar_lags

        # Create model and calculate AICc
        try:
            model = AutoReg(data, lags = model_lags, seasonal = True, period = periods).fit()
            aicc = model.aicc
        except ZeroDivisionError as zde:
            print(zde, 'has occurred in period ' + str(periods) + ' model')
    else:
        # Determine appropriate lag structure
        model_lags = ar_select_order(data, maxlag = max_lag, ic='aic').ar_lags

        # Create model and calculate AICc
        try:
            model = AutoReg(data, lags = model_lags).fit()
            aicc = model.aicc
        except ZeroDivisionError as zde:
            print(zde, 'has occurred in period ' + str(periods) + ' model')

    return aicc

# Function to estimate seasonality
def predict_seasonality(data):
    # Determine the aicc of three models:
    # 1. A simple model with no seasonality
    # 2. A model with a seasonality of 7 periods
    # 3. A model with a seasonality of 12 periods
    order_aicc = model_aicc(data)
    s7_order_aicc = model_aicc(data, True, 6)
    s12_order_aicc = model_aicc(data, True, 11)

    # Find the lowest aicc and thus, the most appropriate model
    min_aicc = min([order_aicc, s7_order_aicc, s12_order_aicc])

    # Return the period of the best model, rather than the model itself
    if s7_order_aicc == min_aicc:
        return 7
    elif s12_order_aicc == min_aicc:
        return 12
    else:
        return 1


# Function to create a model and return the original data plus predictions
def model_predict(data, model_type = 'AR'):
    # Model will predict up to 10% of the data's length ahead
    num_predictions = round(len(data) * 0.1)

    # Minimum and maximum predictaions are 12 and 100 respectively
    if num_predictions < 12:
        num_predictions = 12
    elif num_predictions > 100:
        num_predictions = 100

    # Create the appropriate model depending on user choice
    if model_type == 'AR':
        # Create model
        full_data = ar_model_predict(data, num_predictions)


    ### ARIMA, SES and HWES models not implemented ###
    elif model_type == 'ARIMA':
        # full_data = arima_model_predict(data, num_predictions)
        print("ARIMA")
    elif model_type == 'SES':
        # full_data = ses_model_predict(data, num_predictions)
        print('SES')
    elif model_type == 'HWES':
        # full_data = hwes_model_predict(data, num_predictions)
        print('HWES')
    # Default to AR model
    else:
        full_data = ar_model_predict(data, num_predictions)

    return full_data, num_predictions

# Using an autoregressive model, predict future values
# Since data is assumed to be stationary at this point, there are no accommodations for seasonality
def ar_model_predict(data, num_predictions=12):
    # Limit number of lags to check to 12 to reduce execution time
    max_lag = 12

    # Determine appropriate lag structure
    model_lags = ar_select_order(data, maxlag = max_lag, ic='aic').ar_lags
    try:
        # Try and create a model with the appropriate lag structure
        model = AutoReg(data, lags = model_lags).fit()
    except ZeroDivisionError as zde:
        print(zde, 'has occurred in prediction model')

        # Default to most basic AR model
        model = AutoReg(data, lags = 1).fit()
    finally:
        # Predict data and append it to original dataset
        predictions = model.forecast(num_predictions)
        full_data = data[data.columns[0]].append(predictions)

    return full_data



# Function to difference data
def difference(data, order=1, period=1):
    if order > 2:
        print('Order of differencing higher than 2 not implemented')
        return data
    else:
        # Difference the data
        differenced_data = data.diff(periods=period)

        # Replace NaNs with the original data so the differenced data can be undifferenced
        differenced_data.iloc[:period] = data.iloc[:period]

        if order == 2:
            # Difference again
            differenced_data2 = differenced_data.diff(periods=period)

            # Replace NaNs with the original data so the differenced data can be undifferenced
            differenced_data2.iloc[:period] = differenced_data.iloc[:period]
            return differenced_data2
        else:
            return differenced_data

# Function to reverse the effects of differencing data
def reverse_difference(data, order=1, period=1):
    if order > 2:
        print('Order of undifferencing higher than 2 not implemented')
        return data
    else:
        # Undifference the data
        undifferenced_data = pm.utils.diff_inv(data, lag=period)[period:]

        if order == 2:
            # Undifference again
            undifferenced_data2 = pm.utils.diff_inv(undifferenced_data, lag=period)[period:]
            return undifferenced_data2
        else:
            return undifferenced_data

# Function to difference data if necessary and build appropriate model
def predict_data(data, model_type='AR'):
    # Determine the period of the seasonality (if any)
    # periods = 1 indicates there is no seasonality
    periods = predict_seasonality(data)

    # Initially set orders of differencing to 0 (no differencing neccessary)
    order_of_differencing = 0
    order_of_seasonal_differencing = 0

    # Check for differencing
    order_of_differencing = pm.arima.ndiffs(data, test='adf', max_d=2)

    # If there is seasonality
    if periods > 1:
        # Check for seasonal differencing
        order_of_seasonal_differencing = pm.arima.nsdiffs(data, periods)

    # Perform seasonal differencing first, if applicable
    if order_of_seasonal_differencing > 0:
        # Perform seasonal differencing
        seasonally_differenced_data = difference(data, order_of_seasonal_differencing, periods)

        # Check for further regular differencing
        order_of_differencing = pm.arima.ndiffs(data, test='adf', max_d=2)

        # Difference if applicable, and predict future values
        if order_of_differencing > 0:
            differenced_data = difference(seasonally_differenced_data, order_of_differencing, 1)
            full_data, num_predictions = model_predict(differenced_data, model_type)
        else:
            full_data, num_predictions = model_predict(seasonally_differenced_data, model_type)

        # Undifference data and predictions to revert the dataset to its original values
        seasonally_undifferenced_data = reverse_difference(full_data, order_of_seasonal_differencing, periods)

        # If regular differencing also occurred, perform regular undifferencing
        if order_of_differencing > 0:
            full_data_undifferenced = reverse_difference(seasonally_undifferenced_data, order_of_differencing, 1)
            return full_data_undifferenced, num_predictions
        else:
            return seasonally_undifferenced_data, num_predictions
    elif order_of_differencing > 0:
        # Only non-seasonal differencing is required
        differenced_data = difference(data, order_of_differencing, 1)

        full_data, num_predictions = model_predict(differenced_data, model_type)

        # Undifference the dataset with predictions
        full_data_undifferenced = reverse_difference(full_data, order_of_differencing, 1)
        return full_data_undifferenced, num_predictions
    else:
        # No differencing is required
        full_data, num_predictions = model_predict(data, model_type)
        return full_data, num_predictions

# Function to replace the index of a dataset with a suitable pandas Index
def replace_index(data, index_name='Index'):
    # If the index is made up of integers, convert to a RangeIndex
    if data.index.dtype == 'int64':
        # Assuming the data is not missing any steps
        index_step = data.index[1] - data.index[0]
        data.index = pd.RangeIndex(start=data.index[0], stop=len(data.index) + index_step, step=index_step)
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
def predict(url, xlabel='x-axis', ylabel='y-axis', title='Title of Line Graph', graph_choice='Line Graph', model_choice='AR', other_data=pd.DataFrame()):
    dataset = read_dataset(url)

    # dataset could be an error message,
    # Check if it is a DataFrame
    if not isinstance(dataset, pd.DataFrame):
        return str('Not a dataset ' + dataset)

    model_data = pd.DataFrame(dataset[ylabel])
    model_data.index = dataset[xlabel]

    model_data = replace_index(model_data, model_data.index.name)

    # Limit number of lags to check to 12 to reduce execution time
    max_lag = 12
    num_predictions = 12

    # Determine appropriate lag structure
    model_lags = ar_select_order(model_data, maxlag = max_lag, ic='aic').ar_lags

    # Try and create a model with the appropriate lag structure
    model = AutoReg(model_data, lags = model_lags).fit()

    # predictions = model.forecast(num_predictions)
    # full_data = model_data[model_data.columns[0]].append(predictions)

    predictions = AutoReg(model_data, lags=model_lags).fit().predict(model_data.index[-1], model_data.index[-1] + num_predictions)
    full_data = model_data[model_data.columns[0]].append(predictions).dropna()

    fig, ax = plt.subplots()

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
    except TypeError as te:
        return str(str(te) + ' has occurred, check column choices')

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