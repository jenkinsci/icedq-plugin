package com.icedq.ci.plugin.store;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.remoting.VirtualChannel;
import java.io.BufferedReader;
import org.jenkinsci.remoting.RoleChecker;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 *
 * @author Amit Bhoyar
 */
public class StoreResultServiceImpl {

    private static final String RESULT_FOLDER = "ice_results";

    private static final String RESULT_FILE = "submittedBuild";
    private static final String All_BUILD_RESULTS = "allBuildResults";

    private static final String RESULT_FILE_EXT = ".result";

    public Boolean store(AbstractProject project, final SubmittedResult result)
            throws Exception {
        FilePath resultFolder = getResultFolder(project);
        try {
            resultFolder.mkdirs();
        } catch (IOException | InterruptedException e) {
            throw new Exception(String.format("Cannot make result folder:%s, %s", resultFolder.getName(), e.getMessage()));
        }
        FilePath resultFile = getResultFile(resultFolder, result.getBuildNumber());
        //  System.out.println("result folder====store================="+resultFolder);
//      System.out.println("result.getBuildNumber()================="+result.getBuildNumber());

        try {
            resultFile.act(new FilePath.FileCallable<String>() {
                @Override
                public String invoke(File file, VirtualChannel virtualChannel)
                        throws IOException, InterruptedException {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(file.getPath(), true));
                        writer.write(result.getBuildResult());
                        writer.newLine();
                        return null;
                    } finally {
                        if (null != writer) {
                            writer.close();
                        }
                    }
                }

                @Override
                public void checkRoles(RoleChecker checker) throws SecurityException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        } catch (Exception e) {
           e.printStackTrace();
        }
        return true;
    }

    public static String readSubmitedResult(AbstractProject project, int buildNumber) throws Exception {
        String result = null;
        FilePath resultFolder = getResultFolder(project);

        FilePath resultFile = getResultFile(resultFolder, buildNumber);

        // System.out.println("result folder====read=================" + resultFolder);
        //  System.out.println("result.getBuildNumber()=================" + buildNumber);
        try {
            //  System.out.println("result File==>>>>>>" + resultFile);
            BufferedReader reader = null;
            try {
                //FilePath file = resultFile;
                reader = new BufferedReader(new FileReader(resultFile.toString()));
//                reader.readLine();
                // System.out.println("result===>>>" + reader.readLine());
                result = reader.readLine();
            } finally {
                if (null != reader) {
                    reader.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+result);
        //   formatResult(result);
        return result;
    }

    private static FilePath getResultFolder(AbstractProject project) {
        FilePath projectFolder = new FilePath(project.getConfigFile().getFile()).getParent();
        return new FilePath(projectFolder, RESULT_FOLDER);
    }

    private static FilePath getResultFile(FilePath resultFolder, int fileOrder) {
        return new FilePath(resultFolder, String.format("%s_%s%s", RESULT_FILE, fileOrder, RESULT_FILE_EXT));
    }

    private static FilePath getAllBuildResultFile(FilePath resultFolder) {
        return new FilePath(resultFolder, String.format("%s%s", All_BUILD_RESULTS, RESULT_FILE_EXT));
    }

    public static void storeAllBuildResult(AbstractProject project, final JSONObject respJSONObject) throws Exception {
        FilePath resultFolder = getResultFolder(project);
        String OldFileData = null;
        JSONArray oldFileJSONArray = new JSONArray();
        try {
            resultFolder.mkdirs();
        } catch (Exception e) {
         //  e.printStackTrace();
        }
        FilePath allBuildResultFile = getAllBuildResultFile(resultFolder);
        //read the file first  
        try {
            OldFileData = readAllBuildResult(project);
            
            JSONParser jSONParser = new JSONParser();
            
            oldFileJSONArray = (JSONArray) jSONParser.parse(OldFileData);
           
            oldFileJSONArray.add(respJSONObject);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        //re write file 
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(allBuildResultFile.toString());
            bw = new BufferedWriter(fw);
            bw.write(oldFileJSONArray.toString());
            //System.out.println("FILE WRITING DONE");
        } catch (IOException e) {
           // e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
             //   ex.printStackTrace();
            }
        }
    }

    //read the file 
    public static String readAllBuildResult(AbstractProject project) throws Exception {
        String result = null;
        FilePath resultFolder = getResultFolder(project);
        FilePath allBuildResultFile = getAllBuildResultFile(resultFolder);
        //read the file first  
        try {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(allBuildResultFile.toString()));

                result = reader.readLine();
            } finally {
                if (null != reader) {
                    reader.close();
                }
            }
        } catch (Exception e) {
           // e.printStackTrace();
        }
        return result;
    }
}
