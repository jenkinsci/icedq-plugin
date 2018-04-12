package com.icedq.ci.plugin.icedq;
import org.apache.commons.lang.StringUtils;

public class JsonUserObjParmeterConversionVO {
	
	
	private String repository = StringUtils.EMPTY;
	private String userName = StringUtils.EMPTY ;

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
	
	
	public JsonUserObjParmeterConversionVO(String repository, String username) {
		super();
		this.repository = repository;
		this.userName = username;
	}
	
	
	
	
	
	
	
	

}
