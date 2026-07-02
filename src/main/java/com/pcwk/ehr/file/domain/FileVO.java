package com.pcwk.ehr.file.domain;

import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.TargetType;

public class FileVO extends DTO {

	// target_type(ARTWORK/ARTWORK_ENTRY) + target_id 로 대상 참조
	private int fileId;
	private int memberId;
	private TargetType targetType;
	private int targetId;
	private String orgFileNm;
	private String saveFileNm;
	private String filePath;
	private String mimeType;
	private String isRep;
	private int sortNo;
	private String regDt;

	public FileVO() {}
	
	public FileVO(int fileId, int memberId, TargetType targetType, int targetId, String orgFileNm, String saveFileNm,
			String filePath, String mimeType, String isRep, int sortNo, String regDt) {
		super();
		this.fileId = fileId;
		this.memberId = memberId;
		this.targetType = targetType;
		this.targetId = targetId;
		this.orgFileNm = orgFileNm;
		this.saveFileNm = saveFileNm;
		this.filePath = filePath;
		this.mimeType = mimeType;
		this.isRep = isRep;
		this.sortNo = sortNo;
		this.regDt = regDt;
	}



	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public TargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(TargetType targetType) {
		this.targetType = targetType;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public String getOrgFileNm() {
		return orgFileNm;
	}

	public void setOrgFileNm(String orgFileNm) {
		this.orgFileNm = orgFileNm;
	}

	public String getSaveFileNm() {
		return saveFileNm;
	}

	public void setSaveFileNm(String saveFileNm) {
		this.saveFileNm = saveFileNm;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getIsRep() {
		return isRep;
	}

	public void setIsRep(String isRep) {
		this.isRep = isRep;
	}

	public int getSortNo() {
		return sortNo;
	}

	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}

	public String getRegDt() {
		return regDt;
	}

	public void setRegDt(String regDt) {
		this.regDt = regDt;
	}

	@Override
	public String toString() {
		return "AttachFileVO [fileId=" + fileId + ", memberId=" + memberId + ", targetType=" + targetType
				+ ", targetId=" + targetId + ", orgFileNm=" + orgFileNm + ", saveFileNm=" + saveFileNm + ", filePath="
				+ filePath + ", mimeType=" + mimeType + ", isRep=" + isRep + ", sortNo=" + sortNo + ", regDt=" + regDt
				+ "]";
	}
}
