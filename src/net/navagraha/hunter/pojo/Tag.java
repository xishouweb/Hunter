package net.navagraha.hunter.pojo;

public class Tag implements java.io.Serializable {

	// Fields

	private Integer tagId;
	private Users tagUser;
	private String tagLogtime;
	private Integer tagTimeout;
	private Integer tagDistance;
	private String tagSex;
	private String tagTasktype;

	// Property accessors

	public Integer getTagId() {
		return this.tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	public String getTagLogtime() {
		return this.tagLogtime;
	}

	public void setTagLogtime(String tagLogtime) {
		this.tagLogtime = tagLogtime;
	}

	public Integer getTagTimeout() {
		return this.tagTimeout;
	}

	public void setTagTimeout(Integer tagTimeout) {
		this.tagTimeout = tagTimeout;
	}

	public Integer getTagDistance() {
		return this.tagDistance;
	}

	public void setTagDistance(Integer tagDistance) {
		this.tagDistance = tagDistance;
	}

	public String getTagSex() {
		return this.tagSex;
	}

	public void setTagSex(String tagSex) {
		this.tagSex = tagSex;
	}

	public String getTagTasktype() {
		return this.tagTasktype;
	}

	public void setTagTasktype(String tagTasktype) {
		this.tagTasktype = tagTasktype;
	}

	public void setTagUser(Users tagUser) {
		this.tagUser = tagUser;
	}

	public Users getTagUser() {
		return tagUser;
	}

}