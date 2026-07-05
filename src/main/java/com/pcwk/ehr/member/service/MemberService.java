package com.pcwk.ehr.member.service;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.WorkDiv;
import com.pcwk.ehr.mapper.MemberMapper;
import com.pcwk.ehr.member.domain.MemberVO;

@Service
public class MemberService implements WorkDiv<MemberVO> {

	private static final Logger log = LogManager.getLogger(MemberService.class);
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final int MAX_NICKNAME_LENGTH = 10;
	private static final int MAX_USER_INTRO_LENGTH = 100;

	@Autowired
	private MemberMapper memberMapper;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public MemberService() {
		log.debug("MemberService");
	}

	@Override
	public List<MemberVO> doRetrieve(DTO param) {
		return memberMapper.doRetrieve(param);
	}

	@Override
	public MemberVO doSelectOne(MemberVO param) throws EmptyResultDataAccessException {
		return memberMapper.doSelectOne(param);
	}

	@Override
	@Transactional
	public int doSave(MemberVO param) {
		if (!isJoinFormOk(param)) {
			return 0;
		}
		if (existsEmail(param)) {
			return 0;
		}
		if (existsNickname(param)) {
			return 0;
		}

		param.setPassword(passwordEncoder.encode(param.getPassword()));
		param.setIsAdmin("N");
		return memberMapper.doSave(param);
	}

	@Override
	@Transactional
	public int doUpdate(MemberVO param) {
		if (param == null || param.getMemberId() == 0) {
			return 0;
		}
		if (param.getNickname() != null) {
			String nickname = param.getNickname().trim();
			if (nickname.isEmpty() || nickname.length() > MAX_NICKNAME_LENGTH) {
				return 0;
			}
			param.setNickname(nickname);
			if (existsNickname(param)) {
				return 0;
			}
		}
		if (param.getUserIntro() != null && param.getUserIntro().length() > MAX_USER_INTRO_LENGTH) {
			return 0;
		}
		if (param.getPassword() != null && !param.getPassword().isEmpty()) {
			if (!isPasswordPairOk(param.getPassword(), param.getConfirmPassword())) {
				return 0;
			}
			param.setPassword(passwordEncoder.encode(param.getPassword()));
		} else {
			param.setPassword(null);
		}
		return memberMapper.doUpdate(param);
	}

	@Override
	@Transactional
	public int doDelete(MemberVO param) {
		if (param == null || param.getMemberId() == 0) {
			return 0;
		}
		return memberMapper.doDelete(param);
	}

	/** 로그인 — email/password 검증 후 세션용 VO 반환 (password 제외) */
	public MemberVO login(MemberVO param) {
		if (param == null || isBlank(param.getEmail()) || isBlank(param.getPassword())) {
			return null;
		}

		MemberVO dbMember = memberMapper.selectByEmail(param);
		if (dbMember == null) {
			return null;
		}
		if (!passwordEncoder.matches(param.getPassword(), dbMember.getPassword())) {
			return null;
		}

		dbMember.setPassword(null);
		return dbMember;
	}

	public MemberVO getMyPage(int memberId) {
		if (memberId == 0) {
			return null;
		}
		MemberVO param = new MemberVO();
		param.setMemberId(memberId);
		return memberMapper.selectMyPage(param);
	}

	public boolean existsEmail(MemberVO param) {
		if (param == null || isBlank(param.getEmail())) {
			return false;
		}
		return memberMapper.countByEmail(param) > 0;
	}

	public boolean existsNickname(MemberVO param) {
		if (param == null || isBlank(param.getNickname())) {
			return false;
		}
		return memberMapper.countByNickname(param) > 0;
	}

	private boolean isJoinFormOk(MemberVO param) {
		if (param == null) {
			return false;
		}
		if (isBlank(param.getEmail()) || !EMAIL_PATTERN.matcher(param.getEmail().trim()).matches()) {
			return false;
		}
		if (!isPasswordPairOk(param.getPassword(), param.getConfirmPassword())) {
			return false;
		}
		String nickname = param.getNickname() == null ? "" : param.getNickname().trim();
		if (nickname.isEmpty() || nickname.length() > MAX_NICKNAME_LENGTH) {
			return false;
		}
		param.setEmail(param.getEmail().trim());
		param.setNickname(nickname);
		if (param.getUserIntro() != null) {
			param.setUserIntro(param.getUserIntro().trim());
			if (param.getUserIntro().length() > MAX_USER_INTRO_LENGTH) {
				return false;
			}
		}
		return true;
	}

	private boolean isPasswordPairOk(String password, String confirmPassword) {
		if (password == null || confirmPassword == null) {
			return false;
		}
		if (password.length() < MIN_PASSWORD_LENGTH) {
			return false;
		}
		return password.equals(confirmPassword);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
