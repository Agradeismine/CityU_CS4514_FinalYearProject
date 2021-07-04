package utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class FileStorage {
    public static String generateFileName(String currentLabel, long currentTimeStamp) {
        return currentLabel + "_" + currentTimeStamp + ".log";
    }


    public static String saveMotionData(Context mContext, String receivedString)  throws IOException{  //new File(, )
        String fileName = "";
        String filePath = "";
        StringBuilder motionData = new StringBuilder("");
        
        Scanner scanner = new Scanner(receivedString);
        if(scanner.hasNextLine()){
            fileName = scanner.nextLine();   //The first line is file name
            filePath = mContext.getExternalFilesDir(null)+"/"+fileName;
        }

        while (scanner.hasNextLine()) {
            motionData.append(scanner.nextLine()).append("\n");
        }
        
        // csv format:
        // timestamp, x, y, z
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {    //mContext.getExternalFilesDir(null).getAbsolutePath()
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                //String header = String.format("index,x,y,z\n");
                osw.write(String.valueOf(motionData));
                scanner.close();
                osw.close();
                Log.d("mContext.getExternalFilesDir(null).getAbsolutePath(),", filePath);
            }
        return filePath;
    }
}
