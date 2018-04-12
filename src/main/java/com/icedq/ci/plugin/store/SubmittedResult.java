package com.icedq.ci.plugin.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 *
 * @author Amit Bhoyar
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmittedResult {

    private String url;
    private long projectId;
    private int buildNumber;
    private String buildResult;

    public String getBuildResult() {
        return buildResult;
    }

    public void setBuildResult(String buildResult) {
        this.buildResult = buildResult;
    }

    public String getUrl() {
        return url;
    }

    public SubmittedResult setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getProjectId() {
        return projectId;
    }

    public SubmittedResult setProjectId(long projectId) {
        this.projectId = projectId;
        return this;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public SubmittedResult setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
        return this;
    }

}
