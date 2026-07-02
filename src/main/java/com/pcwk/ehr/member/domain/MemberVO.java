package com.pcwk.ehr.member.domain;

import com.pcwk.ehr.cmn.DTO;

/**
 * 회원 VO (member 테이블 매핑).
 *
 * M1 회원·인증 모듈용.
 * - cmn.DTO 상속: 페이징(pageNo,pageSize,totalCnt,no)·검색(searchDiv,searchWord) 필드 공용
 *
 * 컬럼 매핑(mapUnderscoreToCamelCase=true) — 최종 DDL 기준:
 *   MEMBER_ID   NUMBER(10)     → memberId   (PK, seq_member)
 *   PASSWORD    VARCHAR2(255)  → password   (BCrypt 암호화 저장)
 *   IS_ADMIN    CHAR(1)        → isAdmin    (Y/N)
 *   EMAIL       VARCHAR2(255)  → email      (UNIQUE)
 *   NICKNAME    NVARCHAR2(10)  → nickname
 *   USER_INTRO  NVARCHAR2(100) → userIntro
 *   REG_DT      DATE           → regDt
 *   MOD_DT      DATE           → modDt
 */
public class MemberVO extends DTO {

	private int memberId;
	private String password;
	private String isAdmin;
	private String email;
	private String nickname;
	private String userIntro;
	private String regDt;
	private String modDt;

	/** 가입·비밀번호 변경 시 확인용 (DB 컬럼 아님) */
	private String confirmPassword;

	/** 마이페이지 selectMyPage 집계용 (DB 컬럼 아님) */
	private int artworkCnt;
	private int likeCnt;

	public MemberVO() {
		super();
	}

	public MemberVO(int memberId, String password, String isAdmin, String email, String nickname,
			String userIntro, String regDt, String modDt) {
		super();
		this.memberId = memberId;
		this.password = password;
		this.isAdmin = isAdmin;
		this.email = email;
		this.nickname = nickname;
		this.userIntro = userIntro;
		this.regDt = regDt;
		this.modDt = modDt;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(String isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUserIntro() {
		return userIntro;
	}

	public void setUserIntro(String userIntro) {
		this.userIntro = userIntro;
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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public int getArtworkCnt() {
		return artworkCnt;
	}

	public void setArtworkCnt(int artworkCnt) {
		this.artworkCnt = artworkCnt;
	}

	public int getLikeCnt() {
		return likeCnt;
	}

	public void setLikeCnt(int likeCnt) {
		this.likeCnt = likeCnt;
	}

	@Override
	public String toString() {
		return "MemberVO [memberId=" + memberId + ", isAdmin=" + isAdmin + ", email=" + email
				+ ", nickname=" + nickname + ", userIntro=" + userIntro + ", regDt=" + regDt
				+ ", modDt=" + modDt + ", artworkCnt=" + artworkCnt + ", likeCnt=" + likeCnt + "]";
	}

}
