package com.msgshop.common.utils;

import java.time.LocalDate;

// 페이징기능, 검색기능을 위한 클래스
public class SearchCriteria extends Criteria {
	
	private String searchType; // 검색종류(제목, 내용, 작성자 선택)
	private String keyword;    // 검색어
	private LocalDate startDate; // 검색 시작일
	private LocalDate endDate;   // 검색 종료일
	
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	
	private String rev_content;

    public String getRev_content() { return rev_content; }
    public void setRev_content(String rev_content) { this.rev_content = rev_content; }

	// MyBatis에서 필요한 startRow 계산 getter 추가
	public int getStartRow() {
		return (getPage() - 1) * getPerPageNum();
	}

	public int getPerPageNum() {
		return super.getPerPageNum(); // Criteria에서 가져오기
	}
	
	@Override
	public String toString() {
		return "SearchCriteria [searchType=" + searchType + ", keyword=" + keyword
				+ ", startDate=" + startDate + ", endDate=" + endDate
				+ ", getPage()=" + getPage() + ", getPerPageNum()=" + getPerPageNum() 
				+ ", getStartRow()=" + getStartRow() + "]";
	}
}
