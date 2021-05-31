package utils;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class FileStorage {
    public static String generateFileName(String currentLabel, long currentTimeStamp) {
        return currentLabel + "_" + currentTimeStamp + ".log";
    }


    public static void saveLineData(File file, LineData lineData, int selectedEntryIndex, int length) throws IOException {
        // csv format:
        // timestamp, x, y, z

        try (FileOutputStream fos = new FileOutputStream(file)){
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            //String header = String.format("index,x,y,z\n");
            //osw.write(header);

            //This is the whole X,Y,Z Line
            ILineDataSet setX = lineData.getDataSetByIndex(0);
            ILineDataSet setY = lineData.getDataSetByIndex(1);
            ILineDataSet setZ = lineData.getDataSetByIndex(2);

            if ((setX.getEntryCount() != setY.getEntryCount()) || (setY.getEntryCount() != setZ.getEntryCount()))
                throw new IllegalStateException("Z, Y, Z data set is different");

            float startX = setX.getEntryForIndex(selectedEntryIndex).getX(); // fix and get start time
            for (int i = selectedEntryIndex; i < selectedEntryIndex + length; i++) {
                String str = String.format(Locale.ROOT, "%f,%f,%f,%f\n",
                        setX.getEntryForIndex(i).getX() - startX,   //current Timestamp of DataSet
                        setX.getEntryForIndex(i).getY(),
                        setY.getEntryForIndex(i).getY(),
                        setZ.getEntryForIndex(i).getY());
                osw.write(str);
            }
            osw.close();
        }
    }
}
