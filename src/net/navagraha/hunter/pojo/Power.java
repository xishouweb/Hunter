package net.navagraha.hunter.pojo;

public class Power implements java.io.Serializable {

	// Fields

	private Integer powId;
	private Users powUser;
	private Integer powFast;
	private Integer powCredit;

	// Property accessors

	public Integer getPowId() {
		return this.powId;
	}

	public void setPowId(Integer powId) {
		this.powId = powId;
	}

	public Integer getPowFast() {
		return this.powFast;
	}

	public void setPowFast(Integer powFast) {
		this.powFast = powFast;
	}

	public Integer getPowCredit() {
		return this.powCredit;
	}

	public void setPowCredit(Integer powCredit) {
		this.powCredit = powCredit;
	}

	public void setPowUser(Users powUser) {
		this.powUser = powUser;
	}

	public Users getPowUser() {
		return powUser;
	}

}