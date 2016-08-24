package net.navagraha.hunter.pojo;

public class Apply implements java.io.Serializable {

	// Fields

	private Integer appId;
	private Users appBeUser;
	private Task appTask;
	private String appReason;
	private Integer appState;

	// Property accessors

	public Integer getAppId() {
		return this.appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getAppReason() {
		return this.appReason;
	}

	public void setAppReason(String appReason) {
		this.appReason = appReason;
	}

	public Integer getAppState() {
		return this.appState;
	}

	public void setAppState(Integer appState) {
		this.appState = appState;
	}

	public void setAppBeUser(Users appBeUser) {
		this.appBeUser = appBeUser;
	}

	public Users getAppBeUser() {
		return appBeUser;
	}

	public void setAppTask(Task appTask) {
		this.appTask = appTask;
	}

	public Task getAppTask() {
		return appTask;
	}

}