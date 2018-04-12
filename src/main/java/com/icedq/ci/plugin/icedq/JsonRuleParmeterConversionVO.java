package com.icedq.ci.plugin.icedq;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class JsonRuleParmeterConversionVO {
	
	
	private JsonUserObjParmeterConversionVO userObj ;
	
	private String execMedium =StringUtils.EMPTY;
	private String paramKeyValues =StringUtils.EMPTY;
	private String projectName =StringUtils.EMPTY;
	private String ruleCode = StringUtils.EMPTY;
	private String srcConName=StringUtils.EMPTY;
	private String trgConName= StringUtils.EMPTY;
	private String calledProgramName = StringUtils.EMPTY;
	private String folderName = StringUtils.EMPTY;
	
	
	
	public JsonRuleParmeterConversionVO(
			JsonUserObjParmeterConversionVO userObj, String execMedium,
			String paramKeyValues, String projectName, String ruleCode,
			 String calledProgramName ,String folderName) {
		super();
		this.userObj = userObj;
		this.execMedium = execMedium;
		this.paramKeyValues = paramKeyValues;
		this.projectName = projectName;
		this.ruleCode = ruleCode;
		this.calledProgramName = calledProgramName;
		this.folderName = folderName; 
	}
	public String getExecMedium() {
		return execMedium;
	}
	public void setExecMedium(String execMedium) {
		this.execMedium = execMedium;
	}
	public String getParamKeyValues() {
		return paramKeyValues;
	}
	public void setParamKeyValues(String paramKeyValues) {
		this.paramKeyValues = paramKeyValues;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getRuleCode() {
		return ruleCode;
	}
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}
	public String getSrcConName() {
		return srcConName;
	}
	public void setSrcConName(String srcConName) {
		this.srcConName = srcConName;
	}
	public String getTrgConName() {
		return trgConName;
	}
	public void setTrgConName(String trgConName) {
		this.trgConName = trgConName;
	}
	public String getCalledProgramName() {
		return calledProgramName;
	}
	public void setCalledProgramName(String calledProgramName) {
		this.calledProgramName = calledProgramName;
	}
	public JsonUserObjParmeterConversionVO getUserObj() {
		return userObj;
	}
	public void setUserObj(JsonUserObjParmeterConversionVO userObj) {
		this.userObj = userObj;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	


}
