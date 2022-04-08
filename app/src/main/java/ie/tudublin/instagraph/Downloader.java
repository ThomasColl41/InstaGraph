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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;

public class Downloader extends AppCompatActivity {
    public static final int writePermission = 1;
    Context context;
    Activity activity;
    boolean permission;

    public Downloader(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.permission = false;
    }

    // Method to save the bitmap image of the visualisation
    // as a JPEG to the Download folder
    public void savePlot(Bitmap plot) {
        // Check if the application has the appropriate permission to write files
        checkPermission();

        if(!isPermission()) {
            // Permission not granted
            Toast.makeText(
                    getContext(),
                    "Permission to write files denied",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Get the Download directory
        String downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString();

        // File path to save the visualisation
        String filePath = downloadsDir + generateFileName() + ".jpg";

        // Create a file for the image
        File imageFile = new File(filePath);

        // Overwrite a file if it already exists
        if (imageFile.exists()) {
            imageFile.delete();
        }
        try {
            // Create a FileOutputStream to save the file
            FileOutputStream out = new FileOutputStream(imageFile);
            // Compress the Bitmap using the output stream
            plot.compress(Bitmap.CompressFormat.JPEG, 90, out);
            // Flush and close the stream
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to save a file as a csv to the Download folder
    public void saveFile(String fileToSavePath) throws IOException {
        // Check if the application has the appropriate permission to write files
        checkPermission();

        if(!isPermission()) {
            // Permission not granted
            Toast.makeText(getContext(), "Permission to write files denied", Toast.LENGTH_LONG).show();
            return;
        }

        // Get the Download directory
        String downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString();

        // File path to save the dataset
        String destFilePath = downloadsDir + generateFileName() + ".csv";

        // Create Path variables based on the paths of the source and destination
        Path srcPath = new File(fileToSavePath).toPath();
        Path destPath = new File(destFilePath).toPath();

        // Use the Files.copy method to copy the file from source to destination
        // StandardCopyOption.REPLACE_EXISTING will replace the old file if
        // There is a file with the same name already in the Download folder
        Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
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
            // Permission is already granted
            setPermission(true);
        }
    }

    // Method that uses the Calendar class to get the current date and time as a String
    // And return a String containing InstaGraph_date_and_time for use as a file name
    public String generateFileName() {
        // Get an instance of Calendar, and use it to get the current date and time
        Calendar cal = Calendar.getInstance();
        String dateTime =
                cal.get(Calendar.DAY_OF_MONTH) + "_" +
                        (cal.get(Calendar.MONTH) + 1) + "_" +
                        cal.get(Calendar.YEAR) + "_" +
                        cal.get(Calendar.HOUR_OF_DAY) + "_" +
                        cal.get(Calendar.MINUTE) + "_" +
                        cal.get(Calendar.SECOND);
        return "/InstaGraph_" + dateTime;
    }

    // Override onRequestPermissionsResult to check whether the file writing permission was granted.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == writePermission) {
            // If the request is cancelled, the result array will be empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted
                Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_LONG).show();
                setPermission(true);
            }
            else {
                // Permission was denied
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_LONG).show();
                setPermission(false);
            }
        }
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
                ", context=" + context +
                ", activity=" + activity +
                ", permission=" + permission +
                '}';
    }
}
