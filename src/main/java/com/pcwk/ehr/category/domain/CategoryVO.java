package com.pcwk.ehr.category.domain;

import com.pcwk.ehr.cmn.DTO;

/**
 * 카테고리 VO (category 테이블 매핑).
 *
 * 공예 분야(도예·가죽·목공 등) 분류 정보.
 * - cmn.DTO 상속: 페이징(pageNo,pageSize,totalCnt,no)·검색(searchDiv,searchWord) 필드 공용
 *
 * 컬럼 매핑(mapUnderscoreToCamelCase=true) — 최종 DDL(craft_community_final.sql) 기준:
 *   CATEGORY_ID  NUMBER(5)      → categoryId  (PK, seq_category)
 *   CATEGORY_NM  NVARCHAR2(10)  → categoryNm  (NOT NULL)
 *   REG_DT       DATE           → regDt       (DEFAULT SYSDATE, 조회 시 TO_CHAR)
 *   MOD_DT       DATE           → modDt
 */
public class CategoryVO extends DTO {

	/** 카테고리ID (PK / seq_category) */
	private int categoryId;

	/** 카테고리명 */
	private String categoryNm;

	/** 등록일 (DEFAULT SYSDATE) */
	private String regDt;

	/** 수정일 */
	private String modDt;

	public CategoryVO() {
	}

	public CategoryVO(int categoryId, String categoryNm, String regDt, String modDt) {
		this.categoryId = categoryId;
		this.categoryNm = categoryNm;
		this.regDt = regDt;
		this.modDt = modDt;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryNm() {
		return categoryNm;
	}

	public void setCategoryNm(String categoryNm) {
		this.categoryNm = categoryNm;
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

	@Override
	public String toString() {
		return "CategoryVO [categoryId=" + categoryId + ", categoryNm=" + categoryNm + ", regDt=" + regDt
				+ ", modDt=" + modDt + "]";
	}

}
