package net.navagraha.hunter.pojo;

public class Census implements java.io.Serializable {

	// Fields

	private Integer cenId;
	private String cenMonth;
	private Integer cenDay;
	private Integer cenActivenum;
	private Integer cenActivetotal;
	private Integer cenLoginnum;
	private Integer cenOnlinenum;

	// Property accessors

	public Integer getCenId() {
		return this.cenId;
	}

	public void setCenId(Integer cenId) {
		this.cenId = cenId;
	}

	public String getCenMonth() {
		return this.cenMonth;
	}

	public void setCenMonth(String cenMonth) {
		this.cenMonth = cenMonth;
	}

	public Integer getCenDay() {
		return this.cenDay;
	}

	public void setCenDay(Integer cenDay) {
		this.cenDay = cenDay;
	}

	public Integer getCenActivenum() {
		return this.cenActivenum;
	}

	public void setCenActivenum(Integer cenActivenum) {
		this.cenActivenum = cenActivenum;
	}

	public Integer getCenLoginnum() {
		return this.cenLoginnum;
	}

	public void setCenLoginnum(Integer cenLoginnum) {
		this.cenLoginnum = cenLoginnum;
	}

	public Integer getCenOnlinenum() {
		return this.cenOnlinenum;
	}

	public void setCenOnlinenum(Integer cenOnlinenum) {
		this.cenOnlinenum = cenOnlinenum;
	}

	public void setCenActivetotal(Integer cenActivetotal) {
		this.cenActivetotal = cenActivetotal;
	}

	public Integer getCenActivetotal() {
		return cenActivetotal;
	}

}