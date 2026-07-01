package com.pcwk.ehr.comment.domain;

import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.TargetType;

public class CommentVO extends DTO {

	// target_type(ARTWORK/ARTWORK_ENTRY) + target_id 로 대상 참조.
	private int commentId;
	private int memberId;
	private TargetType targetType;
	private int targetId;
	private String content;
	private String regDt;
	private String modDt;

	public CommentVO() {}
	
	public CommentVO(int commentId, int memberId, TargetType targetType, int targetId, String content, String regDt,
			String modDt) {
		super();
		this.commentId = commentId;
		this.memberId = memberId;
		this.targetType = targetType;
		this.targetId = targetId;
		this.content = content;
		this.regDt = regDt;
		this.modDt = modDt;
	}

	public int getCommentId() {
		return commentId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
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

	@Override
	public String toString() {
		return "CommentVO [commentId=" + commentId + ", memberId=" + memberId + ", targetType=" + targetType
				+ ", targetId=" + targetId + ", content=" + content + ", regDt=" + regDt + ", modDt=" + modDt + "]";
	}
}
