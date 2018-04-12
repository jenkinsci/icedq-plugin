package com.icedq.ci.plugin.icedq;

import com.icedq.ci.plugin.constants.Constants;
import com.icedq.ci.plugin.store.StoreConnections;
import com.icedq.ci.plugin.store.StoreResultServiceImpl;
import com.icedq.ci.plugin.store.SubmittedResult;
import com.icedq.ci.plugin.http.ExecuteRules;
import com.icedq.ci.plugin.http.HttpClientCalls;
import com.icedq.ci.plugin.utils.AesUtil;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import java.io.IOException;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import hudson.model.AbstractProject;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import jenkins.model.Jenkins;
import org.json.simple.JSONArray;

/**
 *
 * @author Amit Bhoyar
 */
public class ConfigPluginDeploymentIml extends Publisher {

    private static final Logger LOG = Logger.getLogger(ConfigPluginDeploymentIml.class.getName());
    private static final int KEY_SIZE = 128;
    private static final int ITERATION_COUNT = 1000;
    private static final String PASSKEY = "abcdefghxyzpqr@132!";
    private static final String IV_STORE = "335525cff4b2815c580714d322113c7f";
    private static final String SALT = "0be37276e7fd37fa1af65287bfbe2524";

    public static ConfigPluginDeploymentIml newInstance() {
        return new ConfigPluginDeploymentIml();
    }

    @DataBoundConstructor
    public ConfigPluginDeploymentIml() {

    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {

        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        
        return (DescriptorImpl) super.getDescriptor();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
        {   
       String jsonINString = getObjectFromExeFile(build.getProject());
        if (jsonINString == null || "".equalsIgnoreCase(jsonINString)) {
            // build.setResult(Result.FAILURE);
            return false;
        }
        
     
        JSONArray resString = ExecuteRules.execute(jsonINString, build, listener);
      
        StoreResultServiceImpl resultServiceImpl = new StoreResultServiceImpl();
        try {
            JSONParser jSONParser = new JSONParser();
            JSONObject buildNObject = new JSONObject();
            JSONArray buildRespNArray = new JSONArray();
            // System.out.println("*************************************************************************************************************************");
            if (resString != null) {
                for (Iterator iterator = resString.iterator(); iterator.hasNext();) {
                    JSONObject next = (JSONObject) jSONParser.parse(iterator.next().toString());
                    //rule
                   
                   
                    //  System.out.println("NEXT:: " + next);
                    if (next.containsKey("rule")) {
                        try {
                            JSONObject jIteratorObject = (JSONObject) next.get("rule");
                            JSONObject resNObject = new JSONObject();
                            resNObject.put("type", "Rule");
                            resNObject.put("id", jIteratorObject.get("id"));
                            resNObject.put("name", jIteratorObject.get("name"));
                            resNObject.put("code", jIteratorObject.get("code"));
                            resNObject.put("riid", jIteratorObject.get("riid"));
                            resNObject.put("status", jIteratorObject.get("status"));
                            resNObject.put("exitcode", jIteratorObject.get("exitcode"));
                            resNObject.put("reasonCode", jIteratorObject.get("REASON_CODE"));
                            resNObject.put("srccount", jIteratorObject.get("srccount"));
                            resNObject.put("trgcount", jIteratorObject.get("trgcount"));
                            resNObject.put("successflag", jIteratorObject.get("successflag"));
                            resNObject.put("failureflag", jIteratorObject.get("failureflag"));

                            buildRespNArray.add(resNObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (next.containsKey("batch")) {
                        try {
                            JSONObject jIteratorObject = (JSONObject) next.get("batch");
                            JSONObject resNObject = new JSONObject();
                            JSONArray seqNArray = new JSONArray();
                            if (jIteratorObject.containsKey("seq")) {
                                //  System.out.println(jIteratorObject.get("seq") instanceof org.json.simple.JSONObject);
                                if (jIteratorObject.get("seq") instanceof org.json.simple.JSONObject) {
                                    seqNArray.add((JSONObject) jIteratorObject.get("seq"));
                                } else {
                                    JSONArray seqJSONArray = (JSONArray) jIteratorObject.get("seq");
                                    for (Iterator iterator1 = seqJSONArray.iterator(); iterator1.hasNext();) {
                                        JSONObject seqNObject = (JSONObject) iterator1.next();
                                        seqNArray.add(seqNObject);
                                    }
                                }
                            }
                            resNObject.put("type", "Batch");
                            resNObject.put("id", jIteratorObject.get("id"));
                            resNObject.put("name", jIteratorObject.get("name"));
                            resNObject.put("code", jIteratorObject.get("code"));
                            resNObject.put("status", jIteratorObject.get("status"));
                            resNObject.put("exitcode", jIteratorObject.get("exitcode"));
                            resNObject.put("reasonCode", jIteratorObject.get("REASON_CODE"));
                            resNObject.put("seq", seqNArray);

                            buildRespNArray.add(resNObject);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (next.containsKey("testset")) {
                        JSONObject jIteratorObject = (JSONObject) next.get("testset");
                        JSONObject resNObject = new JSONObject();

                        resNObject.put("type", "Test Set");
                        resNObject.put("id", jIteratorObject.get("id"));
                        resNObject.put("name", jIteratorObject.get("name"));
                        resNObject.put("code", jIteratorObject.get("code"));
                        resNObject.put("status", jIteratorObject.get("status"));
                        resNObject.put("exitcode", jIteratorObject.get("exitcode"));
                        resNObject.put("reasonCode", jIteratorObject.get("REASON_CODE"));

                        buildRespNArray.add(resNObject);

                    } else if (next.containsKey("testsetfolder")) {
                        JSONObject resNObject = new JSONObject();
                        JSONObject jIteratorObject = (JSONObject) next.get("testsetfolder");
                        JSONArray testSetRespArray = new JSONArray();
                        if (jIteratorObject.containsKey("testset")) {
                            //  System.out.println(jIteratorObject.get("seq") instanceof org.json.simple.JSONObject);
                            if (jIteratorObject.get("testset") instanceof org.json.simple.JSONObject) {
                                testSetRespArray.add((JSONObject) jIteratorObject.get("testset"));

                            } else {

                                JSONArray testSetNArray = (JSONArray) jIteratorObject.get("testset");
                                for (Iterator iterator1 = testSetNArray.iterator(); iterator1.hasNext();) {
                                    JSONObject testSetNObject = (JSONObject) iterator1.next();
                                    JSONObject testsetObj = new JSONObject();
                                    JSONArray testRespArray = new JSONArray();
                                    if (testSetNObject.containsKey("test")) {

                                        if (testSetNObject.get("test") instanceof org.json.simple.JSONObject) {
                                            testRespArray.add((JSONObject) testSetNObject.get("test"));
                                        } else {

                                            JSONArray testNArray = (JSONArray) testSetNObject.get("test");
                                            for (Iterator iterator2 = testNArray.iterator(); iterator2.hasNext();) {
                                                JSONObject next1 = (JSONObject) iterator2.next();
                                                testRespArray.add(next1);
                                            }
                                        }
                                    }
                                    testsetObj.put("id", testSetNObject.get("id"));
                                    testsetObj.put("name", testSetNObject.get("name"));
                                    testsetObj.put("status", testSetNObject.get("status"));
                                    testsetObj.put("exitcode", testSetNObject.get("exitcode"));
                                    testsetObj.put("test", testRespArray);

                                    testSetRespArray.add(testsetObj);
                                }
                            }
                        }
                        resNObject.put("type", "Test Set Folder");
                        resNObject.put("id", jIteratorObject.get("id"));
                        resNObject.put("name", jIteratorObject.get("name"));
                        resNObject.put("code", jIteratorObject.get("code"));
                        resNObject.put("status", jIteratorObject.get("status"));
                        resNObject.put("exitcode", jIteratorObject.get("exitcode"));
                        resNObject.put("reasonCode", jIteratorObject.get("REASON_CODE"));
                        resNObject.put("testset", testSetRespArray);

                        buildRespNArray.add(resNObject);
                    }
                }
            }
            
            
            buildNObject.put("buildNum", build.getNumber());
            buildNObject.put("buildStatus", build.getResult().toString());
            buildNObject.put("response", buildRespNArray);
            //write result for all build file 
            StoreResultServiceImpl.storeAllBuildResult(build.getProject(), buildNObject);
            //write result for specific build
            SubmittedResult result = new SubmittedResult();
            result.setBuildResult(buildRespNArray.toString());
            result.setBuildNumber(build.getNumber());
            resultServiceImpl.store(build.getProject(), result);
              //System.out.println("*************************************************************************************************************************");
        }catch(InterruptedException e)
                {
                     listener.getLogger().println("InterruptedException!"+e);
                   
                    
                }
        catch (Exception ex) {
            
                listener.getLogger().println("InterruptedException!"+ex);
              
                     
            // Logger.getLogger(ConfigPluginDeploymentIml.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        @SuppressWarnings("null")
//        File currentBasedDir = new File(build.getWorkspace().toURI());
        IceProjectBuildAction iceProjectAction = new IceProjectBuildAction("SomeStringInput", build);
        build.addAction(iceProjectAction);
        return true;
    }

    // defines a project action link on the left side panel bellow job configuration 
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        // return new ProjectAction(project);
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(ConfigPluginDeploymentIml.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Constants.POST_BUILD_ACTION_DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, net.sf.json.JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        public static File getCurrentProjectPath(String jobName) {
            
            File projectPath = null;
            try {
                jobName = java.net.URLDecoder.decode(jobName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(ConfigPluginDeploymentIml.class.getName()).log(Level.SEVERE, null, ex);
            }
            // get current project name and rootDir of the project 
            AbstractProject abstractProject = AbstractProject.findNearest(jobName);
            projectPath = abstractProject.getRootDir();
//            System.err.println("check abstractProject path ::"+abstractProject.toString());
//            System.err.println("check path ::"+projectPath.toString());
            return projectPath;
        }

        @JavaScriptMethod
        public JSONArray getRepoNameList(String serverUrl, String apiKey) {
//             System.out.println("@@@@@@@@@@@@@@@@getRepoNameList=======" + serverUrl + "@@@@@@@@@@@@@@apiKey======" + apiKey);
            JSONArray json = null;
            String url = serverUrl;

            String jsonDataAsString = HttpClientCalls.getRepoList(url);
//            System.err.println("Check Rahul Res ::"+jsonDataAsString);
            if (jsonDataAsString != null) {
                JSONParser parser = new JSONParser();
                try {
                    json = (JSONArray) parser.parse(jsonDataAsString);
                } catch (ParseException ex) {
                    //   Logger.getLogger(ConfigPluginDeploymentIml.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }
             // System.out.println("REPO NAME LIST JSON===" + json);
            return json;
        }

       
         @JavaScriptMethod
         public String authenticateRestUser(String inputjson) {
            String result = "EXCEPTION:\\n something went wrong";
//             System.out.println("Rahul"+inputjson);
            try {
               
                JSONParser jSONParser = new JSONParser();
                JSONObject jSONObject = (JSONObject) jSONParser.parse(inputjson);
                result = HttpClientCalls.autenticatRestUser(jSONObject.get("connectionUrl").toString(),jSONObject.toString(),jSONObject.get("token").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @JavaScriptMethod
        public String writeServerConnFile(String iv, String salt, String ciphertext, String jobName) {
            String result = null;
            File projectPath = getCurrentProjectPath(jobName);
            try {
                AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
                result = aesUtil.decrypt(salt, iv, PASSKEY, ciphertext);
                // System.out.println("PLAIN TEXT :: " + result);
                result = aesUtil.encrypt(SALT, IV_STORE, PASSKEY, result);

                //System.out.println("CIpher: " + result);
                StoreConnections st = new StoreConnections();
                st.writeFile(projectPath, Constants.SERVER_CONN, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @JavaScriptMethod
        public String readServerConnFile(String jobName) {
            String result = null;
            File projectPath = getCurrentProjectPath(jobName);
            Logger.getLogger("PATH"+projectPath);
         //System.out.println("PROJECT PATH :: " + projectPath);
            try {
                result = StoreConnections.readFile(projectPath, Constants.SERVER_CONN);
            } catch (Exception e) {
                e.printStackTrace();
            }
           // System.out.println("result ::"+result);
            
            return result;
        }

        @JavaScriptMethod
        public String writeExecuteTableDataFile(String iv, String salt, String ciphertext, String jobName) {
            String result = null;
            File projectPath = getCurrentProjectPath(jobName);
            try {
                AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
                result = aesUtil.decrypt(salt, iv, PASSKEY, ciphertext);
//                System.out.println("PLAIN TEXT:: " + result);
                result = aesUtil.encrypt(SALT, IV_STORE, PASSKEY, result);
//                System.out.println("CP TEXT:: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                StoreConnections st = new StoreConnections();
                st.writeFile(projectPath, Constants.EXECUTE_TABLE_DATA, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @JavaScriptMethod
        public String readExecuteTableDataFile(String jobName) {
            String result = null;
            File projectPath = getCurrentProjectPath(jobName);
            try {
                result = StoreConnections.readFile(projectPath, Constants.EXECUTE_TABLE_DATA);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @JavaScriptMethod
        public String getRuleObject(String salt, String iv, String ciphertext) {
            String resString = "EXCEPTION";
            try {
                AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
                String plainText = aesUtil.decrypt(salt, iv, PASSKEY, ciphertext);
                JSONParser jSONParser = new JSONParser();
                JSONObject jSONObject = (JSONObject) jSONParser.parse(plainText);
              
//                String objectType =jSONObject.get("objectType").toString();
//                String objectName =jSONObject.get("objectName").toString();
//              String objectCode =jSONObject.get("objectCode").toString();
//              
//              ArrayList<String> list = new ArrayList<String>();
//              list.add(objectType);
//              list.add(objectName);
//               list.add(objectCode);
//               
//                  System.out.println("list"+list.toString());    
                
               
//                resString = HttpClientCalls.autenticatRestUser(jSONObject.get("connectionUrl").toString(),jSONObject.toString(),RestConstantUrl.tokencon+jSONObject.get("token").toString());
//                
//                resString ="{ \"userVo\": { \"repoName\": \"ICERepo_MSSQL_Linux_Dev\", \"userId\": 7, \"userName\": \"admin\" } }";
//                if(!resString.contains("EXCEPTION"))
//                {
//                       
//                        JSONParser parser = new JSONParser();
//			JSONObject jsonObject = (JSONObject) parser.parse(resString);
//                       
//                       // JSONObject userJSONObject = (JSONObject) jsonObject.get("userObj");
//                      System.out.println("Check obj"+jsonObject.toString());
                        
                    resString =HttpClientCalls.getIceRestObject(jSONObject.toString(),jSONObject.get("connectionUrl").toString(),jSONObject.get("token").toString());
//                }
//                System.err.println("Rahul response::"+resString);
                
//                resString = HttpClientCalls.getIceObjects(jSONObject.get("connectionUrl").toString(), jSONObject.get("repository").toString(), jSONObject.get("userName").toString(),
//                        jSONObject.get("token").toString(), jSONObject.get("objType").toString(), jSONObject.get("objCode").toString(), jSONObject.get("objName").toString());
//                System.out.println(">>>> " + resString);
            } catch (Exception e) {
                System.out.println("ERROR " + e.getMessage());
            }
            return resString;
        }

        @JavaScriptMethod
        public String abortIceObjSearch(String flag) {
          // System.out.println("FLAG "+flag);
            String resString = null;
            try {
                resString = HttpClientCalls.abortGetRuleObjHttpGlobalInst();
            } catch (Exception e) {
                System.out.println("" + e.getMessage());
            }
            return resString;
        }
    }

    ///return the rules stores in a execute table in job 
    public static String getObjectFromExeFile(AbstractProject abstractProject) {
        String result = null;

        File projectPath = abstractProject.getRootDir();
       // System.out.println("PROJECT PATH  :: " + projectPath);
        try {
            AesUtil aesUtil = new AesUtil(KEY_SIZE, ITERATION_COUNT);
            result = StoreConnections.readFile(projectPath, Constants.EXECUTE_TABLE_DATA);
            result = aesUtil.decrypt(SALT, IV_STORE, PASSKEY, result);
           // System.out.println("PLAIN TEXT :: " + result.length());
            //System.out.println("PLAIN TEXT :: " + result);
        } catch (Exception e) {
            System.out.println("" + e.getMessage());
        }
        return result;
    }
}
