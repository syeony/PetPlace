
package com.ssafy.cms.util.page;

import com.ssafy.cms.constant.Const;
import com.ssafy.cms.constant.DataMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;

/**
 * <PRE>
 * 1. ClassName : 페이징 처리를 위한 유틸리티
 * 2. FileName  : pageNavigationUtil.java
 * 3. Package  : com.jaso.framework.page.util
 * </PRE>
 */

 public class pageNavigationUtil {

	private static Log log = LogFactory.getLog(pageNavigationUtil.class);

	/**
	 * <PRE>
	 * 1. MethodName : pageNavigationUtil
	 * 2. ClassName  : pageNavigationUtil
	 * </PRE>
	 */
	public pageNavigationUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 네비게이션에 들어갈 정보 생성
	 * @param model
	 * @param dataMap
	 * @return
	 */
	public static DataMap createNavigationInfo(ModelMap model, DataMap dataMap) {

		pageNavigationVo pnv = new pageNavigationVo();
		
		// 넘겨받은 파라미터에 pageInputName이 없는 경우 임의의 currentPage input name을 설정
		pnv.setPageInputName(dataMap.getString("pageInputName", "currentPage"));
		// 넘겨받은 파라미터에 pageCallFunction(페이지 이동 함수)가 없는 경우 fnGoPage로 함수이름 결정
		pnv.setPageCallFunction(dataMap.getString("pageCallFunction", "fnGoPage"));

		// 데이터의 총 갯수
		pnv.setTotalCount(dataMap.getInt("totalCount"));
		// 한 화면에 보여줄 데이터의 갯수
		pnv.setRowPerPage(Integer.parseInt(dataMap.getString("rowPerPage", Const.DEF_ROW_PER_PAGE_20)));
		// 현재 페이지 설정(currentPage의 값으로 결정. 없으면 지정된 값으로 설정)
		pnv.setCurrentPage(Integer.parseInt(dataMap.getString(pnv.getPageInputName(), Const.DEF_CURRENT_PAGE)));
		// 네비게이션바에 보여줄 최대 페이지의 수
		pnv.setNaviCount(Integer.parseInt(dataMap.getString("naviCount", Const.DEF_NAVI_COUNT)));

		// 마지막 페이지 넘버 설정
		int lastPage = pnv.getTotalCount() / pnv.getRowPerPage();
		int dummyPage = 0;
		if (pnv.getTotalCount() % pnv.getRowPerPage() > 0) { //나머지가 존재할경우 1페이지 추가		
			dummyPage = 1;
		}
		pnv.setLastPage(lastPage + dummyPage);
		int plusPage = pnv.getCurrentPage() % pnv.getNaviCount() == 0 ? -1 * pnv.getNaviCount() + 1 : 1;
		pnv.setFirstPage(pnv.getCurrentPage() / pnv.getNaviCount() * pnv.getNaviCount() + plusPage);
		pnv.setCurrDataNo(pnv.getTotalCount() - ((pnv.getCurrentPage() - 1) * pnv.getRowPerPage()));

		model.addAttribute(dataMap.getString("pageNavigationVoName", "pageNavigationVo"), pnv);

		dataMap.put("limitStart", (pnv.getCurrentPage() - 1) * pnv.getRowPerPage());
		dataMap.put("limitCount", pnv.getRowPerPage());

		// mysql 경우 limit 이용시 끝에는 한페이지에 보여줄 게시물수만 있으면 된다.
		dataMap.put("limitEnd", pnv.getRowPerPage());

		String naviBar = createNavigationBar(pnv);
		// 페이지 관련 태그 스트링 등록
		model.addAttribute(dataMap.getString("navigationBarName", "navigationBar"), naviBar);
		dataMap.put("navigationBar", naviBar);

		return dataMap;
	}

	/**
	 * 네비게이션 바 생성
	 * @param pnv
	 * @return
	 */
	public static String createNavigationBar(pageNavigationVo pnv) {

		StringBuffer rtnStr = new StringBuffer();
		int nextPage = 0;

		if (pnv.getTotalCount() > 0) {
			rtnStr.append("<ul class=\"pagination pagination-sm no-margin\">");

			if (pnv.getFirstPage() + pnv.getNaviCount() > pnv.getLastPage()) {
				nextPage = pnv.getLastPage() + 1;
			} else {
				nextPage = pnv.getFirstPage() + pnv.getNaviCount();
			}

			rtnStr.append("<li class=\"pagination_arrow\"><a href=\"#\" title=\"맨앞으로가기\" onclick=\"" + pnv.getPageCallFunction() + "('1'); return false;\">&laquo;</a></li>");

			for (int i = pnv.getFirstPage(); i < nextPage; i++) {
				if (pnv.getCurrentPage() == i) {
					rtnStr.append("<li class=\"active\"><a href=\"#\" onclick=\"" + pnv.getPageCallFunction() + "('" + i + "'); return false;\">" + i + "</a></li>");
				} else {
					rtnStr.append("<li><a href=\"#\" onclick=\"" + pnv.getPageCallFunction() + "('" + i + "'); return false;\">" + i + "</a></li>");
				}
			}

			rtnStr.append("<li class=\"pagination_arrow\"><a href=\"#\" title=\"맨뒤로가기\" onclick=\"" + pnv.getPageCallFunction() + "('" + pnv.getLastPage()
					+ "'); return false;\">&raquo;</a></li>");
			rtnStr.append("</ul>");

			rtnStr.append("<input type=\"hidden\" name=\"" + pnv.getPageInputName() + "\" id=\"" + pnv.getPageInputName() + "\" value=\"" + pnv.getCurrentPage() + "\"/>");
		}
		
		return rtnStr.toString();
	}
}
