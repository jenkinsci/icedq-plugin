package com.icedq.ci.plugin.icedq;

import com.icedq.ci.plugin.constants.Constants;
import com.icedq.ci.plugin.store.StoreResultServiceImpl;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.kohsuke.stapler.bind.JavaScriptMethod;
/**
 *
 * @author Amit Bhoyar
 */
public class IceProjectBuildAction implements Action {

    @SuppressWarnings("FieldMayBeFinal")
    private AbstractBuild<?, ?> build;

    @Override
    public String getIconFileName() {
        return Constants.ICON_FILE_URL;
    }

    @Override
    public String getDisplayName() {
        return Constants.PROJECT_BUILD_ACTION_DISPLAY_NAME;
    }

    @Override
    public String getUrlName() {
        return "icedqBA";
    }

    public int getBuildNumber() {
        return this.build.number;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    IceProjectBuildAction(final String message, final AbstractBuild<?, ?> build) {
        //  System.out.println("com.icedq.ci.plugin.action.IceProjectAction.<init>()=="+message);
        //  System.out.println("com.icedq.ci.plugin.action.IceProjectAction.<init>()=="+build.getDisplayName());
        this.build = build;
    }

    @JavaScriptMethod
    public String getResult() {
        String resultString = null;
        try {
            // System.out.println(">>>>>>>>>>>>>>>>>>>>Build number=" + getBuildNumber());
            //  System.out.println(">>>>>>>>>>>>>>>>project name=" + build.getProject());
            resultString = "" + getBuildNumber();
            resultString = StoreResultServiceImpl.readSubmitedResult(build.getProject(), getBuildNumber());

        } catch (Exception ex) {
          //  Logger.getLogger(IceProjectBuildAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        //  System.out.println("IceProjectAction.getResult()???>>>>>>>>>>>>>>>>>>>>"+resultString);
        return resultString;
    }
}
