package com.pcwk.ehr.cmn;

import java.util.HashMap;
import java.util.Map;

/**
 * 모든 VO의 공통 부모. 페이징·검색 필드 제공 (v2: SearchVO 대신 DTO 상속).
 */
public class DTO {

	private int pageNo;
	private int pageSize;
	private int no;
	private int totalCnt;

	private String searchDiv;
	private String searchWord;

	private Map<String, String> searchMap = new HashMap<>();

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getTotalCnt() {
		return totalCnt;
	}

	public void setTotalCnt(int totalCnt) {
		this.totalCnt = totalCnt;
	}

	public String getSearchDiv() {
		return searchDiv;
	}

	public void setSearchDiv(String searchDiv) {
		this.searchDiv = searchDiv;
	}

	public String getSearchWord() {
		return searchWord;
	}

	public void setSearchWord(String searchWord) {
		this.searchWord = searchWord;
	}

	public Map<String, String> getSearchMap() {
		return searchMap;
	}

	public void setSearchMap(Map<String, String> searchMap) {
		this.searchMap = searchMap;
	}

}
