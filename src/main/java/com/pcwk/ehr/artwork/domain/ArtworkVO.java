package com.pcwk.ehr.artwork.domain;

import com.pcwk.ehr.cmn.DTO;

public class ArtworkVO extends DTO {
	
	//핵심 컬럼
	private int    artworkId;   // 작품 고유번호 (PK)
	private int    memberId;    // 작성자 (FK)
	private int    categoryId;  // 카테고리 (FK)
	private String isStatus;    // 완성여부 'Y'/'N'
	private String title;       // 제목
	private String content;     // 본문(CLOB)  
	private int    viewCount;   // 조회수
	private String regDt;       // 등록일      
	private String modDt;       // 수정일      
	private String compDt;      // 완성일 (공개작업은 null)
	
	public ArtworkVO() {
		super();
	}

	public ArtworkVO(int artworkId, int memberId, int categoryId, String isStatus, String title, String content,
			int viewCount, String regDt, String modDt, String compDt) {
		super();
		this.artworkId = artworkId;
		this.memberId = memberId;
		this.categoryId = categoryId;
		this.isStatus = isStatus;
		this.title = title;
		this.content = content;
		this.viewCount = viewCount;
		this.regDt = regDt;
		this.modDt = modDt;
		this.compDt = compDt;
	}

	public int getArtworkId() {
		return artworkId;
	}

	public void setArtworkId(int artworkId) {
		this.artworkId = artworkId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getIsStatus() {
		return isStatus;
	}

	public void setIsStatus(String isStatus) {
		this.isStatus = isStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
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

	public String getCompDt() {
		return compDt;
	}

	public void setCompDt(String compDt) {
		this.compDt = compDt;
	}

	@Override
	public String toString() {
		return "ArtworkVO [artworkId=" + artworkId + ", memberId=" + memberId + ", categoryId=" + categoryId
				+ ", isStatus=" + isStatus + ", title=" + title + ", content=" + content + ", viewCount=" + viewCount
				+ ", regDt=" + regDt + ", modDt=" + modDt + ", compDt=" + compDt + ", toString()=" + super.toString()
				+ "]";
	}
	
}