package com.icedq.ci.plugin.icedq;

import com.icedq.ci.plugin.constants.Constants;
import com.icedq.ci.plugin.store.StoreResultServiceImpl;
import hudson.model.AbstractProject;
import hudson.model.Action;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.kohsuke.stapler.bind.JavaScriptMethod;
/**
 *
 * @author Amit Bhoyar
 */
public class ProjectAction implements Action {

    private AbstractProject<?, ?> project;

    @Override
    public String getIconFileName() {
        return Constants.ICON_FILE_URL;
    }

    @Override
    public String getDisplayName() {
        return Constants.PROJECT_ACTION_DISPLAY_NAME;
    }

    @Override
    public String getUrlName() {
        return "icedqPA";
    }

    public AbstractProject<?, ?> getProject() {
        return this.project;
    }

    public String getProjectName() {
        return this.project.getName();
    }

    ProjectAction(final AbstractProject<?, ?> project) {
        this.project = project;
    }

    @JavaScriptMethod
    public JSONArray getProjectResultJSON() {
        JSONArray resultArray = new JSONArray();
        try {
            String respString = StoreResultServiceImpl.readAllBuildResult(project);
           // System.out.println("respString " + respString);
            JSONParser jSONParser = new JSONParser();
            resultArray = (JSONArray) jSONParser.parse(respString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultArray;
    }
}//class end
