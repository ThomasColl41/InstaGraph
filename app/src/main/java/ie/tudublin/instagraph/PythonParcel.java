package ie.tudublin.instagraph;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class PythonParcel implements Parcelable {
    // Attributes
    // NullPointerException occurs because the PyObjects have no values
    PyObject dataset;
    PyObject model_data;
    int number;

    // Constructor
    public PythonParcel() {
        this.number = 69;
    }

    // Getters
    public PyObject getDataset() {
        return dataset;
    }

    public PyObject getModel_data() {
        return model_data;
    }

    public int getNumber() {
        return number;
    }

    // Setters
    public void setDataset(PyObject dataset) {
        this.dataset = dataset;
    }

    public void setModel_data(PyObject model_data) {
        this.model_data = model_data;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    // toString
    @NonNull
    @Override
    public String toString() {
        return "PythonParcel{" +
                "dataset=" + dataset +
                ", model_data=" + model_data +
                ", number=" + number +
                '}';
    }

    // Parcelling methods (required)
    protected PythonParcel(Parcel in) {
        if(this.dataset != null) {
            this.dataset = in.readParcelable(dataset.getClass().getClassLoader());
        }
        if(this.model_data != null) {
            this.model_data = in.readParcelable(model_data.getClass().getClassLoader());
        }
        this.number = in.readInt();
    }

    public static final Creator<PythonParcel> CREATOR = new Creator<PythonParcel>() {
        @Override
        public PythonParcel createFromParcel(Parcel in) {
            return new PythonParcel(in);
        }

        @Override
        public PythonParcel[] newArray(int size) {
            return new PythonParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if(this.dataset != null) {
            parcel.writeParcelable((Parcelable) this.dataset, i);
        }
        if(this.model_data != null) {
            parcel.writeParcelable((Parcelable) this.model_data, i);
        }
        parcel.writeInt(this.number);
    }
}
