package com.pcwk.ehr.cmn;

/**
 * 페이징 블록 계산 유틸 (수업 26장 패턴).
 * totalCnt / pageNo / pageSize 로 전체 페이지 수와 블록(이전·다음) 범위를 계산한다.
 */
public class PageUtil {

	private final int pageNo;
	private final int pageSize;
	private final int totalCnt;
	private final int blockSize;

	private int totalPage;
	private int startPage;
	private int endPage;
	private boolean prev;
	private boolean next;

	public PageUtil(int pageNo, int pageSize, int totalCnt) {
		this(pageNo, pageSize, totalCnt, 5);
	}

	public PageUtil(int pageNo, int pageSize, int totalCnt, int blockSize) {
		this.pageNo = Math.max(pageNo, 1);
		this.pageSize = pageSize < 1 ? 10 : pageSize;
		this.totalCnt = Math.max(totalCnt, 0);
		this.blockSize = blockSize < 1 ? 5 : blockSize;
		calc();
	}

	private void calc() {
		totalPage = (int) Math.ceil((double) totalCnt / pageSize);
		if (totalPage < 1) {
			totalPage = 1;
		}
		int curBlock = (int) Math.ceil((double) pageNo / blockSize);
		startPage = (curBlock - 1) * blockSize + 1;
		endPage = Math.min(startPage + blockSize - 1, totalPage);
		prev = startPage > 1;
		next = endPage < totalPage;
	}

	public int getPageNo() {
		return pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getTotalCnt() {
		return totalCnt;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getStartPage() {
		return startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public boolean isPrev() {
		return prev;
	}

	public boolean isNext() {
		return next;
	}

	@Override
	public String toString() {
		return "PageUtil [pageNo=" + pageNo + ", pageSize=" + pageSize + ", totalCnt=" + totalCnt + ", totalPage="
				+ totalPage + ", startPage=" + startPage + ", endPage=" + endPage + ", prev=" + prev + ", next=" + next
				+ "]";
	}
}
