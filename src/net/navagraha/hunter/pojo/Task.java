package net.navagraha.hunter.pojo;

import java.util.HashSet;
import java.util.Set;

public class Task implements java.io.Serializable {

	// Fields

	private Integer tasId;
	private Users tasUser;
	private String tasTitle;
	private String tasImg;
	private String tasContact;
	private String tasContent;
	private String tasTime;
	private Integer tasPrice;
	private String tasType;
	private Integer tasState;
	private String tasTimeout;
	private String tasFinishtime;
	private Integer tasRulenum;
	private Integer tasReceivenum;
	private Integer tasFinishnum;
	private String tasEvaluate;
	private Set<Apply> tasApplies = new HashSet<Apply>(0);

	// Property accessors

	public Integer getTasId() {
		return this.tasId;
	}

	public void setTasId(Integer tasId) {
		this.tasId = tasId;
	}

	public String getTasTitle() {
		return this.tasTitle;
	}

	public void setTasTitle(String tasTitle) {
		this.tasTitle = tasTitle;
	}

	public String getTasImg() {
		return this.tasImg;
	}

	public void setTasImg(String tasImg) {
		this.tasImg = tasImg;
	}

	public String getTasContact() {
		return this.tasContact;
	}

	public void setTasContact(String tasContact) {
		this.tasContact = tasContact;
	}

	public String getTasContent() {
		return this.tasContent;
	}

	public void setTasContent(String tasContent) {
		this.tasContent = tasContent;
	}

	public String getTasTime() {
		return this.tasTime;
	}

	public void setTasTime(String tasTime) {
		this.tasTime = tasTime;
	}

	public Integer getTasPrice() {
		return this.tasPrice;
	}

	public void setTasPrice(Integer tasPrice) {
		this.tasPrice = tasPrice;
	}

	public String getTasType() {
		return this.tasType;
	}

	public void setTasType(String tasType) {
		this.tasType = tasType;
	}

	public Integer getTasState() {
		return this.tasState;
	}

	public void setTasState(Integer tasState) {
		this.tasState = tasState;
	}

	public String getTasTimeout() {
		return this.tasTimeout;
	}

	public void setTasTimeout(String tasTimeout) {
		this.tasTimeout = tasTimeout;
	}

	public String getTasFinishtime() {
		return this.tasFinishtime;
	}

	public void setTasFinishtime(String tasFinishtime) {
		this.tasFinishtime = tasFinishtime;
	}

	public Integer getTasRulenum() {
		return this.tasRulenum;
	}

	public void setTasRulenum(Integer tasRulenum) {
		this.tasRulenum = tasRulenum;
	}

	public Integer getTasReceivenum() {
		return this.tasReceivenum;
	}

	public void setTasReceivenum(Integer tasReceivenum) {
		this.tasReceivenum = tasReceivenum;
	}

	public Integer getTasFinishnum() {
		return this.tasFinishnum;
	}

	public void setTasFinishnum(Integer tasFinishnum) {
		this.tasFinishnum = tasFinishnum;
	}

	public String getTasEvaluate() {
		return this.tasEvaluate;
	}

	public void setTasEvaluate(String tasEvaluate) {
		this.tasEvaluate = tasEvaluate;
	}

	public Set<Apply> getTasApplies() {
		return this.tasApplies;
	}

	public void setTasApplies(Set<Apply> tasApplies) {
		this.tasApplies = tasApplies;
	}

	public void setTasUser(Users tasUser) {
		this.tasUser = tasUser;
	}

	public Users getTasUser() {
		return tasUser;
	}

}