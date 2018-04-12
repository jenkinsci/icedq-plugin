package com.icedq.ci.plugin.store;

import hudson.FilePath;
import hudson.XmlFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileReader;
import com.icedq.ci.plugin.constants.Constants;

/**
 *
 * @author Amit Bhoyar
 */
public class StoreConnections {

    public static String createFolderFile(File projectPath, String FileName) {
        String filePath = null;
        try {
            FilePath projectFolder = new FilePath(projectPath);
            FilePath resultFolder = new FilePath(projectFolder, Constants.ICE_FOLDER);
            resultFolder.mkdirs();

            //System.out.println("projectFolder+>>"+projectFolder);
              //System.out.println("resultFolder>>>>"+resultFolder);
            try {
                File file = new File(projectPath + File.separator + Constants.ICE_FOLDER, FileName + Constants.FILE_EXT);
                // System.out.println("FILE+++"+file);
                if (file.createNewFile()) {
                      System.out.println("file create Success!\n FILEPATH=" + file.getPath());
                } else {
                    // System.out.println(" file already exists..\n  FILEPATH=" + file.getPath());
                }
                filePath = file.getPath();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public void writeFile(File projectPath, String FileNameString, String data) {
        try {
             //System.err.println("FILE DATA==" + data);
            //System.err.println("FILE NAME==" + FileNameString);
            try {
                File file = new File(createFolderFile(projectPath, FileNameString));
                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(data);
                bw.close();
                //System.out.println("writing Done");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(File conFile, String FileNameString) {
        String str = "";
        //  DataEncrypter  dataEncrypter = new DataEncrypter(ENCRIPTER_STRING);
        try (BufferedReader br = new BufferedReader(new FileReader(createFolderFile(conFile, FileNameString)))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                str = str + sCurrentLine;
            }
        } catch (IOException e) {
            System.out.println("" + e.getMessage());
        }
        return str;
    }

}
