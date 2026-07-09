package com.pcwk.ehr.artwork.domain;

import java.util.List;

import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
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
	private List<ArtworkEntryVO> entryList;   // 공개작업 상세: 작업일지 타임라인

	public List<ArtworkEntryVO> getEntryList() { return entryList; }
	public void setEntryList(List<ArtworkEntryVO> entryList) { this.entryList = entryList; }
	
	// ==== 메인페이지 관련 (XML 기본값)==== 
	// 컬럼이 없는 임의의 지정할 값이라 XML에 전달해줄 기본값
	// Service에서 호출때 값을 변경지정하면 호출값으로 덮어씌워짐
	private int days = 30;        // 인기 집계 기간(일), 기본 30
	private int likeWeight = 2;   // 추천 좋아요 가중치, 기본 2
	
	// ===== JOIN / 집계 파생필드 (테이블 X, 화면 렌더용) =====
		// 생성자에는 넣지 않는다. resultType 매핑 시 setter로 채워짐.
		private String nickname;     // member.nickname (작성자)
		private String categoryNm;   // category.category_nm (카테고리명)
		private int    likeCount;    // board_like 집계 (추천/인기 정렬·표시)
		private int    repFileId;    // attach_file 대표(is_rep='Y') file_id — 카드 썸네일용, 없으면 0
	
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getCategoryNm() {
		return categoryNm;
	}

	public void setCategoryNm(String categoryNm) {
		this.categoryNm = categoryNm;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getRepFileId() {
		return repFileId;
	}

	public void setRepFileId(int repFileId) {
		this.repFileId = repFileId;
	}

	// ==== 메인 화면 사용 getter/setter ====
	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}
	
	public int getLikeWeight() {
		return likeWeight;
	}
	public void setLikeWeight(int likeWeight) {
		this.likeWeight = likeWeight;
	}

	@Override
	public String toString() {
		return "ArtworkVO [artworkId=" + artworkId + ", memberId=" + memberId + ", categoryId=" + categoryId
				+ ", isStatus=" + isStatus + ", title=" + title + ", content=" + content + ", viewCount=" + viewCount
				+ ", regDt=" + regDt + ", modDt=" + modDt + ", compDt=" + compDt + ", days=" + days + ", likeWeight="
				+ likeWeight + ", nickname=" + nickname + ", categoryNm=" + categoryNm + ", likeCount=" + likeCount
				+ ", toString()=" + super.toString() + "]";
	}

	

}