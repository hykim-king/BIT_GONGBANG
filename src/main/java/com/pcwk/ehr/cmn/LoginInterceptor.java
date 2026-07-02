package com.pcwk.ehr.cmn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 로그인 필요 URL 접근 제어.
 */
public class LoginInterceptor implements HandlerInterceptor {

	private static final Logger log = LogManager.getLogger(LoginInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession(false);
		MemberVO loginMember = session == null ? null
				: (MemberVO) session.getAttribute(SessionConst.LOGIN_MEMBER);

		if (loginMember != null) {
			return true;
		}

		log.debug("비로그인 접근 차단: {}", request.getRequestURI());
		response.sendRedirect(request.getContextPath() + "/member/login.do");
		return false;
	}

}
