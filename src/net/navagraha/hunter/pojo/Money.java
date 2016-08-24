package net.navagraha.hunter.pojo;

public class Money implements java.io.Serializable {

	// Fields

	private Integer monId;
	private String monNo;
	private String monAlipay;
	private String monName;
	private Double monPay;
	private String monComment;
	private Integer monState;
	private String monType;
	private String monTime;

	// Property accessors

	public Integer getMonId() {
		return this.monId;
	}

	public void setMonId(Integer monId) {
		this.monId = monId;
	}

	public String getMonNo() {
		return this.monNo;
	}

	public void setMonNo(String monNo) {
		this.monNo = monNo;
	}

	public String getMonAlipay() {
		return this.monAlipay;
	}

	public void setMonAlipay(String monAlipay) {
		this.monAlipay = monAlipay;
	}

	public String getMonName() {
		return this.monName;
	}

	public void setMonName(String monName) {
		this.monName = monName;
	}

	public Double getMonPay() {
		return this.monPay;
	}

	public void setMonPay(Double monPay) {
		this.monPay = monPay;
	}

	public String getMonComment() {
		return this.monComment;
	}

	public void setMonComment(String monComment) {
		this.monComment = monComment;
	}

	public Integer getMonState() {
		return this.monState;
	}

	public void setMonState(Integer monState) {
		this.monState = monState;
	}

	public void setMonTime(String monTime) {
		this.monTime = monTime;
	}

	public String getMonTime() {
		return monTime;
	}

	public void setMonType(String monType) {
		this.monType = monType;
	}

	public String getMonType() {
		return monType;
	}

}