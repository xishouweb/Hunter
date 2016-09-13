package net.navagraha.hunter.pojo;

public class About implements java.io.Serializable {

	// Fields

	private Integer aboId;
	private String aboContent;
	private String aboVersion;

	// Property accessors

	public Integer getAboId() {
		return this.aboId;
	}

	public void setAboId(Integer aboId) {
		this.aboId = aboId;
	}

	public String getAboContent() {
		return this.aboContent;
	}

	public void setAboContent(String aboContent) {
		this.aboContent = aboContent;
	}

	public String getAboVersion() {
		return aboVersion;
	}

	public void setAboVersion(String aboVersion) {
		this.aboVersion = aboVersion;
	}

}