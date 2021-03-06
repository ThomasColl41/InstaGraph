package ie.tudublin.instagraph;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ParameterParcel implements Parcelable {
    // Attributes
    String url;
    String datasetPath;
    String modelDataPath;
    String predictionsPath;
    String graphType;
    String model;
    String col1;
    String col2;
    String title;

    // Four possible parameters for a custom model
    String para1;
    String para2;
    String para3;
    String para4;

    // The custom row limit on the data
    String firstLast;
    String rowLimit;

    // The number of steps to predict ahead
    String numPredictions;

    // Constructor
    public ParameterParcel() {
        this.url = "";
        this.datasetPath = "";
        this.modelDataPath = "";
        this.predictionsPath = "";
        this.graphType = "";
        this.model = "";
        this.col1 = "";
        this.col2 = "";
        this.title = "";
        this.para1 = "";
        this.para2 = "";
        this.para3 = "";
        this.para4 = "";
        this.firstLast = "";
        this.rowLimit = "";
        this.numPredictions = "";
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public void setDatasetPath(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    public String getModelDataPath() {
        return modelDataPath;
    }

    public void setModelDataPath(String modelDataPath) {
        this.modelDataPath = modelDataPath;
    }

    public String getPredictionsPath() {
        return predictionsPath;
    }

    public void setPredictionsPath(String predictionsPath) {
        this.predictionsPath = predictionsPath;
    }

    public String getGraphType() {
        return graphType;
    }

    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPara1() {
        return para1;
    }

    public void setPara1(String para1) {
        this.para1 = para1;
    }

    public String getPara2() {
        return para2;
    }

    public void setPara2(String para2) {
        this.para2 = para2;
    }

    public String getPara3() {
        return para3;
    }

    public void setPara3(String para3) {
        this.para3 = para3;
    }

    public String getPara4() {
        return para4;
    }

    public void setPara4(String para4) {
        this.para4 = para4;
    }

    public String getFirstLast() {
        return firstLast;
    }

    public void setFirstLast(String firstLast) {
        this.firstLast = firstLast;
    }

    public String getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(String rowLimit) {
        this.rowLimit = rowLimit;
    }

    public String getNumPredictions() {
        return numPredictions;
    }

    public void setNumPredictions(String numPredictions) {
        this.numPredictions = numPredictions;
    }

    // toString
    @NonNull
    @Override
    public String toString() {
        return "ParameterParcel{" +
                "url='" + url + '\'' +
                ", datasetPath='" + datasetPath + '\'' +
                ", modelDataPath='" + modelDataPath + '\'' +
                ", predictionsPath='" + predictionsPath + '\'' +
                ", graphType='" + graphType + '\'' +
                ", model='" + model + '\'' +
                ", col1='" + col1 + '\'' +
                ", col2='" + col2 + '\'' +
                ", title='" + title + '\'' +
                ", para1='" + para1 + '\'' +
                ", para2='" + para2 + '\'' +
                ", para3='" + para3 + '\'' +
                ", para4='" + para4 + '\'' +
                ", firstLast='" + firstLast + '\'' +
                ", rowLimit='" + rowLimit + '\'' +
                ", numPredictions='" + numPredictions + '\'' +
                '}';
    }

    // Parcelling methods (required)
    protected ParameterParcel(Parcel in) {
        this.url = in.readString();
        this.datasetPath = in.readString();
        this.modelDataPath = in.readString();
        this.predictionsPath = in.readString();
        this.graphType = in.readString();
        this.model = in.readString();
        this.col1 = in.readString();
        this.col2 = in.readString();
        this.title = in.readString();
        this.para1 = in.readString();
        this.para2 = in.readString();
        this.para3 = in.readString();
        this.para4 = in.readString();
        this.firstLast = in.readString();
        this.rowLimit = in.readString();
        this.numPredictions = in.readString();
    }

    public static final Creator<ParameterParcel> CREATOR = new Creator<ParameterParcel>() {
        @Override
        public ParameterParcel createFromParcel(Parcel in) {
            return new ParameterParcel(in);
        }

        @Override
        public ParameterParcel[] newArray(int size) {
            return new ParameterParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.url);
        parcel.writeString(this.datasetPath);
        parcel.writeString(this.modelDataPath);
        parcel.writeString(this.predictionsPath);
        parcel.writeString(this.graphType);
        parcel.writeString(this.model);
        parcel.writeString(this.col1);
        parcel.writeString(this.col2);
        parcel.writeString(this.title);
        parcel.writeString(this.para1);
        parcel.writeString(this.para2);
        parcel.writeString(this.para3);
        parcel.writeString(this.para4);
        parcel.writeString(this.firstLast);
        parcel.writeString(this.rowLimit);
        parcel.writeString(this.numPredictions);
    }
}
