package net.navagraha.hunter.pojo;

public class Advert implements java.io.Serializable {

	// Fields

	private Integer advId;
	private String advImg;
	private String advContent;
	private String advUrl;
	private Integer advHotlevel;

	// Property accessors

	public Integer getAdvId() {
		return this.advId;
	}

	public void setAdvId(Integer advId) {
		this.advId = advId;
	}

	public String getAdvImg() {
		return this.advImg;
	}

	public void setAdvImg(String advImg) {
		this.advImg = advImg;
	}

	public String getAdvContent() {
		return this.advContent;
	}

	public void setAdvContent(String advContent) {
		this.advContent = advContent;
	}

	public String getAdvUrl() {
		return this.advUrl;
	}

	public void setAdvUrl(String advUrl) {
		this.advUrl = advUrl;
	}

	public Integer getAdvHotlevel() {
		return this.advHotlevel;
	}

	public void setAdvHotlevel(Integer advHotlevel) {
		this.advHotlevel = advHotlevel;
	}

}