package ie.tudublin.instagraph;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class Downloader extends AppCompatActivity {
    Bitmap plot;
    public static final int writePermission = 1;
    Context context;
    Activity activity;
    boolean permission;

    public Downloader(Bitmap plot, Context context, Activity activity) {
        this.plot = plot;
        this.context = context;
        this.activity = activity;
        this.permission = false;
    }

    // Method to save the bitmap image of the visualisation
    // as a JPEG to the Download folder
    public void savePlot() {
        // Check if the application has the appropriate permission to write files
        checkPermission();

        if(!isPermission()) {
            // Permission not granted
            Toast.makeText(getContext(), "Permission to write files denied", Toast.LENGTH_LONG).show();
            return;
        }

        // Get the Download directory
        String downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

        // Get an instance of Calendar, and use it to get the current date and time
        Calendar cal = Calendar.getInstance();
        String dateTime =
                cal.get(Calendar.DAY_OF_MONTH) + "_" +
                        (cal.get(Calendar.MONTH) + 1) + "_" +
                        cal.get(Calendar.YEAR) + "_" +
                        cal.get(Calendar.HOUR_OF_DAY) + "_" +
                        cal.get(Calendar.MINUTE) + "_" +
                        cal.get(Calendar.SECOND);

        // File path to save the visualisation
        String fileName = "/InstaGraph_" + dateTime + ".jpg";

        // Save the image
        // Inspired from https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage/7887114#7887114
        File file = new File (downloadsDir, fileName);
        if (file.exists()) {
            if(!file.delete()) {
                Log.i("InstaGraph", "Duplicate file failed to delete");
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            getPlot().compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.i("InstaGraph", e.getMessage());
        }

        // Log values
        Log.i("InstaGraph", "Downloads: " + downloadsDir);
        Log.i("InstaGraph", "Current DateTime: " + dateTime);
        Log.i("InstaGraph", "Save to: " + downloadsDir + fileName);
    }

    // Method to check application has permission to write to external storage
    public void checkPermission() {
        // If file writing permission is granted, request it from the user
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Display the pre-defined permissions dialog box to request it
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, writePermission);
        }
        else {
            // Permission is already granted;
            setPermission(true);
        }
    }

    // Override onRequestPermissionsResult to check whether the file writing permission was granted.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == writePermission) {
            // If request is cancelled, the result array will be empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_LONG).show();
                setPermission(true);
            }
            else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_LONG).show();
                setPermission(false);
            }
        }
    }

    public Bitmap getPlot() {
        return plot;
    }

    public void setPlot(Bitmap plot) {
        this.plot = plot;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    @NonNull
    @Override
    public String toString() {
        return "Downloader{" +
                "plot=" + plot +
                ", context=" + context +
                ", activity=" + activity +
                ", permission=" + permission +
                '}';
    }
}
