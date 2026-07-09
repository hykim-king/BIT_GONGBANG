package com.pcwk.ehr.cmn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 관리자 전용 URL(/admin/**) 접근 제어.
 * 팀 세션 모델 기준: 세션 loginMember(MemberVO)의 is_admin='Y' 만 통과한다.
 * (m1 로그인은 세션에 loginMember 만 저장하고 별도 "isAdmin" 속성은 두지 않으므로 VO 필드로 판정)
 */
public class AdminInterceptor implements HandlerInterceptor {

	private static final Logger log = LogManager.getLogger(AdminInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession(false);
		MemberVO loginMember = session == null ? null
				: (MemberVO) session.getAttribute(SessionConst.LOGIN_MEMBER);

		if (loginMember != null && "Y".equals(loginMember.getIsAdmin())) {
			return true;
		}

		log.debug("관리자 권한 없음(차단): {}", request.getRequestURI());
		response.sendRedirect(request.getContextPath() + "/member/login.do");
		return false;
	}

}
