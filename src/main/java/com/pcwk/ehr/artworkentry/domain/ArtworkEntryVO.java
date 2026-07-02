package com.pcwk.ehr.artworkentry.domain;

import com.pcwk.ehr.cmn.DTO;

public class ArtworkEntryVO extends DTO {
	
	//컬럼명 변수 선언 (카멜케이스)
	private int artworkEntry; //작품일지 ID
	private int artworkId;    //작품 ID
	private String content;   //내용
	private String regDt;     //등록일
	private String modDt;     //수정일
	
	public ArtworkEntryVO() {
		super();
	}
	//변수 필드생성
	public ArtworkEntryVO(int artworkEntry, int artworkId, String content, String regDt, String modDt) {
		super();
		this.artworkEntry = artworkEntry;
		this.artworkId = artworkId;
		this.content = content;
		this.regDt = regDt;
		this.modDt = modDt;
	}
	//getter / setter 생성
	public int getArtworkEntry() {
		return artworkEntry;
	}

	public void setArtworkEntry(int artworkEntry) {
		this.artworkEntry = artworkEntry;
	}

	public int getArtworkId() {
		return artworkId;
	}

	public void setArtworkId(int artworkId) {
		this.artworkId = artworkId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRegDt() {
		return regDt;
	}

	public void setRegDt(String regDt) {
		this.regDt = regDt;
	}

	public String getModDt() {
		return modDt;
	}

	public void setModDt(String modDt) {
		this.modDt = modDt;
	}
	//toString 생성
	@Override
	public String toString() {
		return "ArtworkEntryVO [artworkEntry=" + artworkEntry + ", artworkId=" + artworkId + ", content=" + content
				+ ", regDt=" + regDt + ", modDt=" + modDt + ", toString()=" + super.toString() + "]";
	}
	

}
