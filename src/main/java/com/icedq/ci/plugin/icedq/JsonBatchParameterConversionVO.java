package com.icedq.ci.plugin.icedq;

import org.apache.commons.lang.StringUtils;

public class JsonBatchParameterConversionVO {
	
private JsonUserObjParmeterConversionVO userObj ;
	
	private String execMedium =StringUtils.EMPTY;
	private String paramKeyValues =StringUtils.EMPTY;
	private String projectName =StringUtils.EMPTY;
	private String batchCode = StringUtils.EMPTY;
	private String srcConName=StringUtils.EMPTY;
	private String trgConName= StringUtils.EMPTY;
	private String calledProgramName = StringUtils.EMPTY;
	private String folderName = StringUtils.EMPTY;
	
	
	public JsonBatchParameterConversionVO(
			JsonUserObjParmeterConversionVO userObj, String execMedium,
			String paramKeyValues, String projectName, String batchCode,
			 String calledProgramName,
			String folderName) {
		super();
		this.userObj = userObj;
		this.execMedium = execMedium;
		this.paramKeyValues = paramKeyValues;
		this.projectName = projectName;
		this.batchCode = batchCode;
		this.calledProgramName = calledProgramName;
		this.folderName = folderName;
	}
	public JsonUserObjParmeterConversionVO getUserObj() {
		return userObj;
	}
	public void setUserObj(JsonUserObjParmeterConversionVO userObj) {
		this.userObj = userObj;
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
	public String getBatchCode() {
		return batchCode;
	}
	public void setBatchCode(String batchCode) {
		this.batchCode = batchCode;
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
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	

}
