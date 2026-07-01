package com.pcwk.ehr.like.domain;

import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.TargetType;


public class LikeVO extends DTO{

	//  UNIQUE(member_id, target_type, target_id) 중복 방지 
	private int likeId;
	private int memberId;
	private TargetType targetType;
	private int targetId;

	public LikeVO() {}
	
	public LikeVO(int likeId, int memberId, TargetType targetType, int targetId) {
		super();
		this.likeId = likeId;
		this.memberId = memberId;
		this.targetType = targetType;
		this.targetId = targetId;
	}

	public int getLikeId() {
		return likeId;
	}

	public void setLikeId(int likeId) {
		this.likeId = likeId;
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

	@Override
	public String toString() {
		return "LikeVO [likeId=" + likeId + ", memberId=" + memberId + ", targetType=" + targetType + ", targetId="
				+ targetId + "]";
	}
}
