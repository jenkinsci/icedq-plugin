package com.icedq.ci.plugin.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icedq.ci.plugin.icedq.JsonBatchParameterConversionVO;
import com.icedq.ci.plugin.icedq.JsonRuleParmeterConversionVO;
import com.icedq.ci.plugin.icedq.JsonUserObjParmeterConversionVO;
import com.icedq.ci.plugin.icedq.RestConstantUrl;
import com.icedq.ci.plugin.utils.LoggerUtils;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Amit Bhoyar
 */
public class ExecuteRules {

    public static final String FROM_NAME = "JENKINS";

    public static JSONArray execute(String objectJson, AbstractBuild abstractBuild, BuildListener listener) {
       // System.out.println("Check For Lisener");
        JSONArray exitResultArrayList = new JSONArray();
        PrintStream logger = listener.getLogger();
        
        String runResult = null;
        JSONArray jsonArray = null;
        JSONParser parser = new JSONParser();

        //System.out.println("******jenkinsJobNBuildName:::" + abstractBuild.getProject().getName() + ":" + abstractBuild.getNumber());  //return job name
        String jenkinsJobNBuildName = abstractBuild.getProject().getName() + ":" + abstractBuild.getNumber();
        
        
        try {
            if (parser.parse(objectJson) != null) {
                jsonArray = (JSONArray) parser.parse(objectJson);
            } else {
                //abstractBuild.setResult(Result.FAILURE);
                return null;
            }
            // System.out.println(">>>>>>>>>>arrayLength=====" + jsonArray.size());
            String continueFlag = "fail";
            int counter = 0;
            ExecuteRules.showLogInfo(logger, "EXECUTING..");
           
            //ExecuteRules.showLogInfo(logger, "MARK BUILD FAILED ON ERROR::" + markFailOnError.toUpperCase());
            for (int i = 0; i < jsonArray.size(); i++) {
                //  listener.getLogger().println("Check Listener ::");
               // System.out.println(""+abstractBuild.getExecutor().getState());
               // System.out.println(""+ abstractBuild.getExecutor().isInterrupted());
                int exitCode = -1;
                counter++;
                 if(abstractBuild.getExecutor().isInterrupted())
                    {
                        break;
                    }
                //System.out.println("json.get)====" + jsonArray.get(i));
                JSONObject obj = (JSONObject) jsonArray.get(i);
                // System.out.println("json.get password====" + obj.get("password").toString());
//              
//                String username = obj.get("uname").toString();
//                String repository =obj.get("repoName").toString();
                String token = RestConstantUrl.tokencon + obj.get("password").toString();

//             urlString = url + "/WSClient?METHOD_NAME=runRuleByCode&UNAME=" + userName + "&REPONAME=" + repoName + "&PNAME=" 
//                     + ProjectName + "&FNAME=" + folderName + "&RCODE=" + ruleCode + "&LENAME=" + legalEntityName 
//                     + "&FROM=" + FROM_NAME + "&JOB_NAME=" + jenkinsJobNBuildName + "&PARAMKEYVALUES=" + paramKeyValue;     
                if ("rule".equalsIgnoreCase((String) obj.get("type"))) {
                    String json = null;
                    JsonUserObjParmeterConversionVO userobj = new JsonUserObjParmeterConversionVO(
                            obj.get("repoName").toString(), obj.get("uname").toString());

                    JsonRuleParmeterConversionVO jsonRuleParmeterConversionVO = new JsonRuleParmeterConversionVO(
                            userobj, FROM_NAME, (String) obj.get("parameter").toString(),
                            (String) obj.get("projectName").toString(), (String) obj.get("code").toString(),
                            abstractBuild.getProject().getName() + ":" + abstractBuild.getNumber(), (String) obj.get("folderName").toString());

                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        json = mapper.writeValueAsString(jsonRuleParmeterConversionVO);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //System.out.println("Json String" + json);

                    runResult = runRules((String) obj.get("url"), json, token,logger);
                    
                    if(!runResult.contains("There was an error connecting to"))
                    {
                    exitCode = getExitCode(runResult);
                    }
                    else{ 
                 exitResultArrayList.add("{\"rule\":{\"REASON_CODE\":\"\",\"name\":\"" + (String) obj.get("name").toString() + "\",\"id\":\"\",\"exitcode\":\"\",\"srccount\":\"\",\"trgcount\":\"\",\"status\":\"There was an error connecting to url Or Server is Stop\"}}");
                    }
                } else if ("Batch".equalsIgnoreCase((String) obj.get("type"))) {
                    String json = null;
                    JsonUserObjParmeterConversionVO userobj = new JsonUserObjParmeterConversionVO(
                            obj.get("repoName").toString(), obj.get("uname").toString());

                    JsonBatchParameterConversionVO jsonBatchParameterConversionVO = new JsonBatchParameterConversionVO(
                            userobj, FROM_NAME, (String) obj.get("parameter").toString(),
                            (String) obj.get("projectName").toString(), (String) obj.get("code").toString(),
                            abstractBuild.getProject().getName() + ":" + abstractBuild.getNumber(), (String) obj.get("folderName").toString());

                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        json = mapper.writeValueAsString(jsonBatchParameterConversionVO);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    runResult = runBatch((String) obj.get("url"), json, token,logger);
//                    runResult = runBatch((String) obj.get("url"), (String) obj.get("uname"), (String) obj.get("repoName"), (String) obj.get("projectName"),
//                            (String) obj.get("folderName"), (String) obj.get("code"), (String) obj.get("legalEntityId"), (String) obj.get("parameter"), jenkinsJobNBuildName);
                      if(!runResult.contains("There was an error connecting to"))
                    {
                    exitCode = getExitCode(runResult);
                    }
                    else{
                        exitResultArrayList.add("{\"batch\":{\"REASON_CODE\":\"\",\"name\":\"" + (String) obj.get("name").toString() + "\",\"id\":\"\",\"exitcode\":\"\",\"srccount\":\"\",\"trgcount\":\"\",\"status\":\"There was an error connecting to url Or Server is Stop\"}}");
                    }
                } else if ("Testset".equalsIgnoreCase((String) obj.get("type"))) {

                    runResult = runTestSet((String) obj.get("url"), (String) obj.get("uname"), (String) obj.get("repoName"), (String) obj.get("projectName"),
                            (String) obj.get("folderName"), (String) obj.get("code"), (String) obj.get("legalEntityId"), (String) obj.get("parameter"), jenkinsJobNBuildName);

                } else if ("Testsetfolder".equalsIgnoreCase((String) obj.get("type"))) {
                    runResult = runTestSetFolderByCode((String) obj.get("url"), (String) obj.get("uname"), (String) obj.get("repoName"), (String) obj.get("projectName"),
                            (String) obj.get("code"), (String) obj.get("legalEntityId"), (String) obj.get("parameter"), jenkinsJobNBuildName);
                }
                
                 if(!runResult.contains("There was an error connecting "))
                 {
                      ExecuteRules.showLogInfo(logger, runResult);
                      exitResultArrayList.add(runResult);
                    
                 }
                
                 //System.out.println("ExitCode>: " + exitCode);
               if(!runResult.contains("There was an error connecting to"))
                    {
                if (exitCode == 0) {
                    continueFlag = (String) obj.get("OnSuccess");
                    //   System.out.println("IN OnSuccess if=" + continueFlag);
                } else if (exitCode > 0) {
                    continueFlag = (String) obj.get("onFailure");
                    //   System.out.println("IN onFailure if=" + continueFlag);
                } else {
                    continueFlag = (String) obj.get("onError");
                    //  System.out.println("IN onError if=" + continueFlag);
                }
                    }
                //System.err.println("Continue Flag ::"+continueFlag);
                 if(!runResult.contains("There was an error connecting to"))
                    {
                if ("FAIL".equalsIgnoreCase(continueFlag)) {
                    abstractBuild.setResult(Result.FAILURE);
                    JSONObject jSONObject = (JSONObject) jsonArray.get(i);
                    LoggerUtils.formatError(logger, "iCEDQ execution stopped.");
                    if (jSONObject.get("OnSuccess").toString().equalsIgnoreCase("FAIL")) {
                        LoggerUtils.formatError(logger, jSONObject.get("type") + ": '" + jSONObject.get("name") + "' is marked as Fail OnSuccess");
                    }
                    if (jSONObject.get("onFailure").toString().equalsIgnoreCase("FAIL")) {
                        LoggerUtils.formatError(logger, jSONObject.get("type") + ":'" + jSONObject.get("name") + "' is marked as Fail onFailure");
                    }
                    if (jSONObject.get("onError").toString().equalsIgnoreCase("FAIL")) {
                        LoggerUtils.formatError(logger, jSONObject.get("type") + ": '" + jSONObject.get("name") + "' is marked as Fail onError");
                    }
                    break;
                }
                    }
            }
            if (counter != jsonArray.size()) {
                //System.out.println("inside ::");
                String nonExecutedRules;
                for (int i = counter; i < jsonArray.size(); i++) {
                    JSONObject obj2 = (JSONObject) jsonArray.get(i);
                 //System.out.println("JSONOBJ :: "+obj2);
                    String objType = (String) obj2.get("type");
                    String objCode = (String) obj2.get("code");
                    String objName = (String) obj2.get("name");
                    nonExecutedRules = "{\"" + objType.toLowerCase() + "\":{\"id\":\"-\",\"status\":\"No Run\",\"name\":\"" + objName + "\",\"exitcode\":\"-\",\"code\":\"" + objCode + "\"}}";
                    //    System.out.println("nonExecutedRules:: "+nonExecutedRules );
                    exitResultArrayList.add(nonExecutedRules);
                }
            }
        } catch (ParseException ex) {
            //  Logger.getLogger(ConfigPluginDeploymentIml.class.getName()).log(Level.SEVERE, null, ex);
             //System.out.println("Check Exp3::***************************"); 
            ex.printStackTrace();
        }
          // System.out.println(exitResultArrayList);
        return exitResultArrayList;
    }

    private static void showLogInfo(PrintStream logger, String list) {
        LoggerUtils.formatHR(logger);
        LoggerUtils.formatInfo(logger, list);
    }

    public static String runRules(String url, String userName, String repoName, String ProjectName, String folderName, String ruleCode, String legalEntityName, String paramKeyValue, String jenkinsJobNBuildName) {
        HttpClient client = new HttpClient();
        String urlString = null;
        String result = null;
        if (url != null && userName != null && repoName != null && ProjectName != null && folderName != null && ruleCode != null && legalEntityName != null) {
            urlString = url + "/WSClient?METHOD_NAME=runRuleByCode&UNAME=" + userName + "&REPONAME=" + repoName + "&PNAME=" + ProjectName + "&FNAME=" + folderName + "&RCODE=" + ruleCode + "&LENAME=" + legalEntityName + "&FROM=" + FROM_NAME + "&JOB_NAME=" + jenkinsJobNBuildName + "&PARAMKEYVALUES=" + paramKeyValue;
        }
        URI uri = null;
        try {
            uri = new HttpURL(urlString);
        } catch (URIException ex) {
            ex.printStackTrace();
            // Logger.getLogger(ExecuteRules.class.getName()).log(Level.SEVERE, null, ex);
        }
        // System.out.println("@@@@@@@@@@@@@@URI RULE=>>>" + uri);
        GetMethod method = new GetMethod(uri.toString());

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                // System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream inputStream = method.getResponseBodyAsStream();
            result = IOUtils.toString(inputStream, "UTF-8");

        } catch (HttpException e) {
            //  System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            //  System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        // System.out.println("RESULT++=================" + result);
        result = ExecuteRules.getJsonFromXML(result);
//         System.out.println("RESULT++=================" + result);
        return result;
    }

    public static String runRules(String Url, String json, String token,PrintStream logger) {
        String result = null;
        String tempurl = Url + RestConstantUrl.runRule;
         //System.out.println("url ::"+tempurl);
       // System.out.println("json ::"+json);
        //System.out.println("token ::"+token);
        CloseableHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(tempurl);
        post.setHeader("Authorization", token);

        StringEntity input;
        try {
            input = new StringEntity(json);
            input.setContentType("application/json");
            post.setEntity(input);
            HttpResponse response;
            response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
               
                switch (response.getStatusLine().getStatusCode()) {
                    case 401:
                        Logger.getLogger("Authorization token not present or expired"+" For "+ "rule" +" Exit code : -10 " + " Reason code : -1 ");
                        break;
//                System.out.println("check ::" + response.getStatusLine().getStatusCode());
                    case 403:
                        Logger.getLogger("Forbbiden"+" For "+ "rule" +" Exit code : -16 " + " Reason code : -16 ");
                        break;
                    case 404:
                        Logger.getLogger("Request-URI Not Matching"+" For "+ "rule" +" Exit code :  -8 " + " Reason code :  -8 ");
                        break;
                    default:
                        Logger.getLogger("Request-URI Not Matching"+" For "+ "rule" +" Exit code : 253 " + " Reason code :  253 ");
                        break;
                }
            } else {
               
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                result = rd.readLine();
            }
        } catch (UnsupportedEncodingException ex) {
             //System.out.println("Not Reached Rule1");
               result="There was an error connecting to "+tempurl; 
              Logger.getLogger(result);
               return result;
          
           
        } catch (IOException ex) {
           // System.out.println("Not Reached Rule2");
            
              result="There was an error connecting to "+tempurl; 
              ExecuteRules.showLogInfo(logger, result);
              
               return result;
          
         
        } finally {

            client.getConnectionManager().shutdown();

        }

        return result;
    }

    public static String runBatch(String Url, String json, String token,PrintStream logger) {
        String result = null;
        String tempurl = Url + RestConstantUrl.runBatch;
        // System.out.println("url ::"+tempurl);
        //System.out.println("json ::"+json);
        //System.out.println("token ::"+token);
        CloseableHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(tempurl);
        post.setHeader("Authorization", token);

        StringEntity input;
        try {
            input = new StringEntity(json);
            input.setContentType("application/json");
            post.setEntity(input);
            HttpResponse response;
            response = client.execute(post);
           
            if (response.getStatusLine().getStatusCode() != 200) {
                
                switch (response.getStatusLine().getStatusCode()) {
                     
                    case 401:
                        Logger.getLogger("Authorization token not present or expired"+" For "+ "batch" +" Exit code : -10 " + " Reason code : -1 ");
                        break;
//                System.out.println("check ::" + response.getStatusLine().getStatusCode());
                    case 403:
                        Logger.getLogger("Forbbiden"+" For "+ "batch" +" Exit code : -16 " + " Reason code : -16 ");
                        break;
                    case 404:
                        Logger.getLogger("Request-URI Not Matching"+" For "+ "batch" +" Exit code :  -8 " + " Reason code :  -8 ");
                        break;
                    default:
                        Logger.getLogger("Request-URI Not Matching"+" For "+ "batch" +" Exit code : 253 " + " Reason code :  253 ");
                        break;
                }
            } else {
               // System.out.println("check ::" + response.getStatusLine().getStatusCode());
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                result = rd.readLine();
            }
        } catch (UnsupportedEncodingException ex) {
           // System.err.println("InEXCEPtion1");
           // System.out.println("Not Reached Batch1");
            result="There was an error connecting to "+tempurl; 
              ExecuteRules.showLogInfo(logger, result);
          return result;
        } catch (IOException ex) {
          // Logger.getLogger("There was an error connecting to"); 
          // System.out.println("Not Reached Batch2");
              result="There was an error connecting to "+tempurl; 
              ExecuteRules.showLogInfo(logger, result);
               return result;
         
           //Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
           //result ="There was an error connecting ";
            
        } finally {

            client.getConnectionManager().shutdown();

        }

        return result;

    }

    public static String runBatch(String url, String userName, String repoName, String ProjectName, String folderName, String batchCode, String legalEntityName, String paramKeyValue, String jenkinsJobNBuildName) {
        HttpClient client = new HttpClient();
        String urlString = null;
        String result = null;
        if (url != null && userName != null && repoName != null && ProjectName != null && folderName != null && batchCode != null && legalEntityName != null) {
            urlString = url + "/WSClient?METHOD_NAME=runBatchByCode&UNAME=" + userName + "&REPONAME=" + repoName + "&PNAME=" + ProjectName + "&FNAME=" + folderName + "&BCODE=" + batchCode + "&LENAME=" + legalEntityName + "&FROM=" + FROM_NAME + "&JOB_NAME=" + jenkinsJobNBuildName + "&PARAMKEYVALUES=" + paramKeyValue;
        }
        URI uri = null;
        try {
            uri = new HttpURL(urlString);
        } catch (URIException ex) {
        }
        //   System.out.println("@@@@@@@@@@@@@@URi BATCH=>>>>" + uri);
        GetMethod method = new GetMethod(uri.toString());
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                //    System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream inputStream = method.getResponseBodyAsStream();
            result = IOUtils.toString(inputStream, "UTF-8");
        } catch (HttpException e) {
            //   System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.getLogger(e.getMessage());
            //  System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        // System.out.println("RESULT++=================" + result);
        result = ExecuteRules.getJsonFromXML(result);
        //  System.out.println("RESULT formated++=================" + result);
        return result;
    }

    public static String runTestSet(String url, String userName, String repoName, String ProjectName, String folderName, String batchCode, String legalEntityName, String paramKeyValue, String jenkinsJobNBuildName) {
        HttpClient client = new HttpClient();
        String urlString = null;
        String result = null;
        if (url != null && userName != null && repoName != null && ProjectName != null && folderName != null && batchCode != null && legalEntityName != null) {
            urlString = url + "/WSClient?METHOD_NAME=runTSByCode&UNAME=" + userName + "&REPONAME=" + repoName + "&PNAME=" + ProjectName + "&FNAME=" + folderName + "&BCODE=" + batchCode + "&LENAME=" + legalEntityName + "&FROM=" + FROM_NAME + "&JOB_NAME=" + jenkinsJobNBuildName + "&PARAMKEYVALUES=" + paramKeyValue;
        }
        URI uri = null;
        try {
            uri = new HttpURL(urlString);
        } catch (URIException ex) {
        }
        //  System.out.println("@@@@@@@@@@@@@@URi TESTSET=>>>>" + uri);
        GetMethod method = new GetMethod(uri.toString());
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                // System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream inputStream = method.getResponseBodyAsStream();
            result = IOUtils.toString(inputStream, "UTF-8");
        } catch (HttpException e) {
            //   System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.getLogger(e.getMessage());
            //  System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        // System.out.println("RESULT++=================" + result);
        result = ExecuteRules.getJsonFromXML(result);
        //  System.out.println("RESULT formated++=================" + result);
        return result;
    }

    public static String runTestSetFolderByCode(String url, String userName, String repoName, String ProjectName, String batchCode, String legalEntityName, String paramKeyValue, String jenkinsJobNBuildName) {
        HttpClient client = new HttpClient();
        String urlString = null;
        String result = null;
        if (url != null && userName != null && repoName != null && ProjectName != null && batchCode != null && legalEntityName != null) {
            urlString = url + "/WSClient?METHOD_NAME=runTSFByCode&UNAME=" + userName + "&REPONAME=" + repoName + "&PNAME=" + ProjectName + "&BCODE=" + batchCode + "&LENAME=" + legalEntityName + "&FROM=" + FROM_NAME + "&JOB_NAME=" + jenkinsJobNBuildName + "&PARAMKEYVALUES=" + paramKeyValue;
        }
        URI uri = null;
        try {
            uri = new HttpURL(urlString);
        } catch (URIException ex) {
        }
        //  System.out.println("@@@@@@@@@@@@@@URi TEST_SET_FOLDER =>>>>" + uri);
        GetMethod method = new GetMethod(uri.toString());
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                //  System.err.println("Method failed: " + method.getStatusLine());
            }
            InputStream inputStream = method.getResponseBodyAsStream();
            result = IOUtils.toString(inputStream, "UTF-8");
        } catch (HttpException e) {
            //   System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.getLogger(e.getMessage());
            //  System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        // System.out.println("RESULT++=================" + result);
        result = ExecuteRules.getJsonFromXML(result);
        //  System.out.println("RESULT formated++=================" + result);
        return result;
    }

    public static String getJsonFromXML(String xml) {
        String jsonPrettyPrintString = null;
        try {
            // System.out.println("String XML :: "+xml);
            org.json.JSONObject xmlJSONObj = org.json.XML.toJSONObject(xml);
            jsonPrettyPrintString = xmlJSONObj.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
            //   Logger.getLogger(ExecuteRules.class.getName()).log(Level.SEVERE, null, ex.getMessage());
        }
        return jsonPrettyPrintString;
    }

    public static int getExitCode(String jsonInput) {
        int exitCode = -1;
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonInput);

            if (jsonObject.get("rule") != null) {
                JSONObject obj = (JSONObject) jsonObject.get("rule");
                exitCode = Integer.parseInt(obj.get("exitcode").toString());
            } else if (jsonObject.get("batch") != null) {
                JSONObject obj = (JSONObject) jsonObject.get("batch");
                String failCode = obj.get("exitcode").toString();
                //System.out.println("FAIL===>>"+failCode +">>>SIZE="+failCode.length());
                if (failCode.length() == 0 || failCode.equalsIgnoreCase("")) {
                    exitCode = Integer.parseInt(obj.get("REASON_CODE").toString());
                } else {
                    exitCode = Integer.parseInt(obj.get("exitcode").toString());
                }
            }
            // System.out.println("EXITCODE===>>>" + exitCode);
        } catch (ParseException ex) {
            ex.printStackTrace();
            Logger.getLogger(ex.getMessage());
        }
        return exitCode;
    }
}
