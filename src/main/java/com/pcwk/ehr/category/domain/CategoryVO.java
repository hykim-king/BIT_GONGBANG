package com.pcwk.ehr.category.domain;

import com.pcwk.ehr.cmn.DTO;

/**
 * 카테고리 VO (category 테이블 매핑).
 *
 * 공예 분야(도예·가죽·목공 등) 분류 정보.
 * - cmn.DTO 상속: 페이징(pageNo,pageSize,totalCnt,no)·검색(searchDiv,searchWord) 필드 공용
 * - 삭제는 물리삭제 대신 useYn='N'(soft delete) 권장 (업무규칙)
 *
 * 컬럼 매핑(mapUnderscoreToCamelCase=true):
 *   CATEGORY_ID  NUMBER(5)    → categoryId  (PK, SEQUENCE)
 *   CATEGORY_NM  VARCHAR2(50) → categoryNm  (UK)
 *   SORT_NO      NUMBER(3)    → sortNo      (DEFAULT 0)
 *   USE_YN       CHAR(1)      → useYn       (DEFAULT 'Y')
 *   REG_DT       DATE         → regDt       (DEFAULT SYSDATE, 조회 시 TO_CHAR)
 *   MOD_DT       DATE         → modDt
 */
public class CategoryVO extends DTO {

	/** 카테고리ID (PK / SEQUENCE) */
	private int categoryId;

	/** 카테고리명 (UNIQUE) */
	private String categoryNm;

	/** 정렬순서 (DEFAULT 0) */
	private int sortNo;

	/** 사용여부 Y/N (DEFAULT 'Y') */
	private String useYn;

	/** 등록일 (DEFAULT SYSDATE) */
	private String regDt;

	/** 수정일 */
	private String modDt;

	public CategoryVO() {
	}

	public CategoryVO(int categoryId, String categoryNm, int sortNo, String useYn, String regDt, String modDt) {
		this.categoryId = categoryId;
		this.categoryNm = categoryNm;
		this.sortNo = sortNo;
		this.useYn = useYn;
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

	public int getSortNo() {
		return sortNo;
	}

	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
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
		return "CategoryVO [categoryId=" + categoryId + ", categoryNm=" + categoryNm + ", sortNo=" + sortNo
				+ ", useYn=" + useYn + ", regDt=" + regDt + ", modDt=" + modDt + "]";
	}

}
