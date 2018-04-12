package com.icedq.ci.plugin.http;

import com.icedq.ci.plugin.icedq.RestConstantUrl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Amit Bhoyar
 */
public class HttpClientCalls {

    static HttpGet getRuleObjHttpGlobalInst;
    static CloseableHttpResponse getRuleClosableResponse;
    static CloseableHttpClient getRuleObjHttpClient;
    
    static HttpPost getRestRuleObjHttpGlobalInst;
   
    public static String getRepoList(String url) {
        HttpClient client = new HttpClient();
        String result = null;
        String urlString = url + RestConstantUrl.repoList;
        URI uri = null;
        try {
            uri = new HttpURL(urlString);
            //System.out.println("Url ::" + uri);
        } catch (URIException ex) {
            ex.printStackTrace();
            // Logger.getLogger(ExecuteRules.class.getName()).log(Level.SEVERE, null, ex);
        }
        GetMethod method = new GetMethod(uri.toString());
        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
               // System.err.println("Method failed: " + method.getStatusLine());
                statusCode = HttpStatus.SC_BAD_GATEWAY;
            }
            InputStream inputStream = method.getResponseBodyAsStream();
            result = IOUtils.toString(inputStream, "UTF-8");
          //  System.out.println("******************************");
           // System.out.println(result);
        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
//            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
//            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return result;
    }

   
    public static String autenticatRestUser(String url,String inputJson,String token) throws ParseException, IOException
    {
        String tempurl = url + RestConstantUrl.RestauthenticationUrl;
        JSONObject j = new JSONObject();
        JSONObject requestjson = new JSONObject();
        JSONParser parser = new JSONParser();
        j = (JSONObject) parser.parse(inputJson);
        requestjson.put("userObj", j);

        // System.out.println("Rahul check uri ::"+tempurl);
        //System.out.println("Rahul check uri ::" + requestjson.toString());
        getRuleObjHttpClient = new DefaultHttpClient();
        String result = null;
        getRestRuleObjHttpGlobalInst = new HttpPost(tempurl);
        getRestRuleObjHttpGlobalInst.setHeader("Authorization", RestConstantUrl.tokencon + token);

        StringEntity input;
        try {
            input = new StringEntity(requestjson.toString());
            //System.out.println("Rahul check uri ::"+tempurl);

            input.setContentType("application/json");
            getRestRuleObjHttpGlobalInst.setEntity(input);
            getRuleClosableResponse = getRuleObjHttpClient.execute(getRestRuleObjHttpGlobalInst);
            if (getRuleClosableResponse.getStatusLine().getStatusCode() != 200) {
                //System.out.println(" if check ::"+response.getStatusLine().getStatusCode());
                switch (getRuleClosableResponse.getStatusLine().getStatusCode()) {
                    case 401:
                        result = "Authorization Token Not Present Or Expired";
                        break;
                    case 403:
                        result = "Forbbiden";
                        break;
                    case 404:
                        result = "Request-URL Not Matching";
                        break;
                    default:
                        result = "EXCEPTION";
                        break;
                }
            } else {
                //System.out.println("else check ::"+response.getStatusLine().getStatusCode());
                BufferedReader rd = new BufferedReader(new InputStreamReader(getRuleClosableResponse.getEntity().getContent()));
                result = rd.readLine();
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            getRuleObjHttpClient.close();
            getRestRuleObjHttpGlobalInst.releaseConnection();
            getRuleClosableResponse.close();

        }

        return result;
    }
    
    public  static String getIceRestObject(String jsonInput,String url,String token ) throws ParseException, IOException
    {
        String result = HttpClientCalls.autenticatRestUser(url, jsonInput, token);

        //System.err.println("CHeck Re ::" + result);
//         result ="{ \"userObj\": { \"repository\": \"MSSQL_angular_app_Dev\", \"userId\": 7, \"userName\": \"admin\" } }";
        JSONParser parser = new JSONParser();
        JSONObject requestJSONObject = null;
        JSONObject obj = null;
        try {
            requestJSONObject = (JSONObject) parser.parse(result.toString());
            obj = (JSONObject) parser.parse(jsonInput.toString());
            requestJSONObject.put("icelist", obj);
            // System.err.println("check"+requestJSONObject.toJSONString());

        } catch (ParseException ex) {
            Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
        }

        String tempurl = url + RestConstantUrl.RestRuleUrl;
        getRuleObjHttpClient = new DefaultHttpClient();
        getRestRuleObjHttpGlobalInst = new HttpPost(tempurl);
        getRestRuleObjHttpGlobalInst.setHeader("Authorization", RestConstantUrl.tokencon + token);

        StringEntity input;
        try {
            input = new StringEntity(requestJSONObject.toString());
            input.setContentType("application/json");
            getRestRuleObjHttpGlobalInst.setEntity(input);
             if (getRestRuleObjHttpGlobalInst.isAborted() != true) {
            getRuleClosableResponse = getRuleObjHttpClient.execute(getRestRuleObjHttpGlobalInst);
             }
            if (getRuleClosableResponse.getStatusLine().getStatusCode() != 200) {
              switch (getRuleClosableResponse.getStatusLine().getStatusCode()) {
                    case 401:
                        result = "Authorization Token Not Present Or Expired";
                        break;
                    case 403:
                        result = "Forbbiden";
                        break;
                    case 404:
                        result = "Request-URL Not Matching";
                        break;
                    default:
                        result = "EXCEPTION";
                        break;
                }
                //System.out.println("check ::" + getRuleClosableResponse.getStatusLine().getStatusCode());
            } else {
                //System.out.println("check ::" + getRuleClosableResponse.getStatusLine().getStatusCode());
                BufferedReader rd = new BufferedReader(new InputStreamReader(getRuleClosableResponse.getEntity().getContent()));
                result = rd.readLine();
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            getRuleObjHttpClient.close();
            getRestRuleObjHttpGlobalInst.releaseConnection();
            getRuleClosableResponse.close();

        }

        return result;
    }
    
//    public static String getIceObjects(String inurl, String repoName, String userName, String password, String objType, String objCode, String objName) {
//
//        getRuleObjHttpClient = HttpClients.createDefault();
//        String url = null;
//        long legalEntityId = 1;
//        
//        if (userName != null && password != null && repoName != null && legalEntityId != 0 && userName.length() > 0 && password.length() > 0 && repoName.length() > 0) {
//           // Utils de = new Utils(userName);
//           // String userPwd = de.encode(password);
//            url = inurl + "/WSClient?METHOD_NAME=getIceObjects&UNAME=" + userName
//                    + "&REPONAME=" + repoName + "&PASSWORD=" + userPwd + "&LENTITY_ID=" + legalEntityId
//                    + "&OBJ_TYPE=" + objType + "&OBJ_CODE=" + objCode + "&OBJ_NAME=" + objName;
//        }
//        String result = "";
//        URI uri = null;
//        System.out.println("Rahul Check Url "+url);
//        try {
//            uri = new HttpURL(url);
//            // System.out.println("URI GET OBJ:: "+uri);
//            getRuleObjHttpGlobalInst = new HttpGet(uri.toString());
//        } catch (Exception ex) {
//        }
//        try {
//            // Execute the method.
//            if (getRuleObjHttpGlobalInst.isAborted() != true) {
//                getRuleClosableResponse = getRuleObjHttpClient.execute(getRuleObjHttpGlobalInst);
//            }
//
//            if (getRuleClosableResponse.getStatusLine().getStatusCode() == 200) {
////                System.out.println("RESP:: " + getRuleClosableResponse);
//                HttpEntity entity = getRuleClosableResponse.getEntity();
//                result = EntityUtils.toString(entity);
////                System.out.println(result);
//            }
//        } catch (HttpException e) {
//
//            e.printStackTrace();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        } finally {
//            try {
//                // Release the connection.
//                getRuleClosableResponse.close();
//                getRuleObjHttpGlobalInst.releaseConnection();
//                getRuleObjHttpClient.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        return result;
//    }

    public static String abortGetRuleObjHttpGlobalInst() {
        try {
            //System.out.println("com.icedq.ci.plugin.http.HttpClientCalls.abortGetRuleObjHttpGlobalInst():: " + getRuleObjHttpGlobalInst);
            getRuleObjHttpGlobalInst.abort();
            //System.out.println("com.icedq.ci.plugin.http.HttpClientCalls.abortGetRuleObjHttpGlobalInst():: " + getRuleClosableResponse);
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            try {
                getRuleClosableResponse.close();
                getRuleObjHttpGlobalInst.releaseConnection();
                getRuleObjHttpClient.close();
            } catch (Exception ex) {
                //Logger.getLogger(HttpClientCalls.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}//  class end
