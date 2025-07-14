
package com.ssafy.cms.util.page;

import java.io.Serializable;

/**
 * <PRE>
 * 1. ClassName : 페이징 처리를 위한 VO
 * 2. FileName  : pageNavigationVo.java
 * 3. Package  : com.jaso.framework.page.vo
 * </PRE>
 */

public class pageNavigationVo implements Serializable{
	
	private static final long serialVersionUID = 12L;

	/* 총 개수 */
    private int totalCount;
    
    /* 기본페이지 */
    private int firstPage;
    
    /* 페이지당 행 수 */
    private int rowPerPage;
    
    /* 현재페이시 번호 */
    private int currentPage;
    
    /* 네이게이션에 보일 숫자 수 */
    private int naviCount;
    
    /* 마지막 페이지 */
    private int lastPage;
    
    /* 현재 데이터 No */
    private int currDataNo;
    
    /* 페이지 카운트 */
    private int pageCount;
    
    /* current Page input 네임 */
    private String pageInputName;
    
    /* 페이지 변경시 호출될 javascript 함수 */
    private String pageCallFunction;
    
    public String getPageCallFunction() {
		return pageCallFunction;
	}

	public void setPageCallFunction(String pageCallFunction) {
		this.pageCallFunction = pageCallFunction;
	}
	
	

	public String getPageInputName() {
		return pageInputName;
	}

	public void setPageInputName(String pageInputName) {
		this.pageInputName = pageInputName;
	}

	public int getPageCount() {
		return this.pageCount;
	}
	
	public void setPageCount(int pageCount){
		this.pageCount = pageCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(int firstPage) {
		this.firstPage = firstPage;
	}

	public int getRowPerPage() {
		return rowPerPage;
	}

	public void setRowPerPage(int rowPerPage) {
		this.rowPerPage = rowPerPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getNaviCount() {
		return naviCount;
	}

	public void setNaviCount(int naviCount) {
		this.naviCount = naviCount;
	}
	
	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	public int getCurrDataNo() {
		return currDataNo;
	}

	public void setCurrDataNo(int currDataNo) {
		this.currDataNo = currDataNo;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}    
	
}
