package net.navagraha.hunter.pojo;

public class Pay implements java.io.Serializable {

	// Fields

	private Integer payId;
	private Users payUser;
	private String payTime;
	private Double payIn;
	private Double payOut;

	// Property accessors

	public Integer getPayId() {
		return this.payId;
	}

	public void setPayId(Integer payId) {
		this.payId = payId;
	}

	public String getPayTime() {
		return this.payTime;
	}

	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}

	public Double getPayIn() {
		return this.payIn;
	}

	public void setPayIn(Double payIn) {
		this.payIn = payIn;
	}

	public Double getPayOut() {
		return this.payOut;
	}

	public void setPayOut(Double payOut) {
		this.payOut = payOut;
	}

	public void setPayUser(Users payUser) {
		this.payUser = payUser;
	}

	public Users getPayUser() {
		return payUser;
	}

}