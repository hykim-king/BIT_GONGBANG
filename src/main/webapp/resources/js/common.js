/* ============================================================================
 * 빚다(BITDA) 공통 JS — CC-CMN-01 (로그인/회원가입 모달, CC-USR-01/02)
 * 전제: jQuery 로드 후 실행, body[data-ctx]에 contextPath 세팅(header.jsp).
 * 응답 규약: MessageVO {code:'200'|'400'|'401', message, data}
 *  - checkEmail/checkNickname은 항상 code '200', 판정은 data(Boolean)로만.
 * ==========================================================================*/
$(function () {
	'use strict';

	var ctx = $('body').data('ctx') || '';

	/* HTML 이스케이프 헬퍼(이후 단계의 동적 렌더링 공용) */
	window.bitda = window.bitda || {};
	window.bitda.esc = function (s) {
		return String(s == null ? '' : s)
			.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
	};

	/* ---------- 모달 열기/닫기 ---------- */
	function openModal(id) {
		$('.overlay').removeClass('open');
		$(id).addClass('open');
	}
	function closeModals() {
		$('.overlay').removeClass('open');
	}

	$(document).on('click', '#btnOpenLogin', function () { openModal('#loginModal'); });
	$(document).on('click', '#btnOpenJoin', function () { openModal('#joinModal'); });
	$(document).on('click', '.modal-close', closeModals);
	$(document).on('click', '.overlay', function (e) {
		if (e.target === this) { closeModals(); }
	});
	$(document).on('keydown', function (e) {
		if (e.key === 'Escape') { closeModals(); }
	});
	/* 모달 전환(로그인 <-> 회원가입) */
	$(document).on('click', '#linkToJoin', function () { openModal('#joinModal'); });
	$(document).on('click', '#linkToLogin', function () { openModal('#loginModal'); });

	/* ---------- 로그인 (CC-USR-01): POST /member/doLoginAjax.do ---------- */
	$('#loginForm').on('submit', function (e) {
		e.preventDefault();
		var email = $('#loginEmail').val().trim();
		var password = $('#loginPassword').val();
		if (!email || !password) {
			$('#loginMsg').text('이메일과 비밀번호를 입력하세요.').removeClass('ok').addClass('fail');
			return;
		}
		$.post(ctx + '/member/doLoginAjax.do', { email: email, password: password }, function (res) {
			if (res.code === '200') {
				location.reload();
			} else {
				$('#loginMsg').text('이메일 또는 비밀번호가 올바르지 않습니다.').removeClass('ok').addClass('fail');
			}
		}, 'json').fail(function () {
			$('#loginMsg').text('요청 처리 중 오류가 발생했습니다.').removeClass('ok').addClass('fail');
		});
	});

	/* ---------- 회원가입 (CC-USR-02): checkEmail/checkNickname + doSave ---------- */
	var joinEmailChecked = false;
	var joinNickChecked = false;

	$('#btnJoinCheckEmail').on('click', function () {
		var email = $('#joinEmail').val().trim();
		if (!email) { alert('이메일을 입력하세요.'); return; }
		$.post(ctx + '/member/checkEmail.do', { email: email }, function (res) {
			if (res.code === '200' && res.data === true) {
				$('#joinEmailMsg').text('사용 가능합니다.').removeClass('fail').addClass('ok');
				joinEmailChecked = true;
			} else {
				$('#joinEmailMsg').text('이미 사용 중입니다.').removeClass('ok').addClass('fail');
				joinEmailChecked = false;
			}
		}, 'json').fail(function () {
			$('#joinEmailMsg').text('확인 중 오류가 발생했습니다.').removeClass('ok').addClass('fail');
			joinEmailChecked = false;
		});
	});
	$('#joinEmail').on('input', function () {
		joinEmailChecked = false;
		$('#joinEmailMsg').text('');
	});

	$('#btnJoinCheckNick').on('click', function () {
		var nickname = $('#joinNickname').val().trim();
		if (!nickname) { alert('닉네임을 입력하세요.'); return; }
		$.post(ctx + '/member/checkNickname.do', { nickname: nickname }, function (res) {
			if (res.code === '200' && res.data === true) {
				$('#joinNickMsg').text('사용 가능합니다.').removeClass('fail').addClass('ok');
				joinNickChecked = true;
			} else {
				$('#joinNickMsg').text('이미 사용 중입니다.').removeClass('ok').addClass('fail');
				joinNickChecked = false;
			}
		}, 'json').fail(function () {
			$('#joinNickMsg').text('확인 중 오류가 발생했습니다.').removeClass('ok').addClass('fail');
			joinNickChecked = false;
		});
	});
	$('#joinNickname').on('input', function () {
		joinNickChecked = false;
		$('#joinNickMsg').text('');
	});

	$('#joinForm').on('submit', function (e) {
		e.preventDefault();
		var pw = $('#joinPassword').val();
		var cpw = $('#joinConfirmPassword').val();
		var intro = $('#joinUserIntro').val();
		if (!joinEmailChecked) { alert('이메일 중복확인을 해주세요.'); return; }
		if (pw.length < 8) { alert('비밀번호는 8자 이상이어야 합니다.'); return; }
		if (pw !== cpw) { alert('비밀번호가 일치하지 않습니다.'); return; }
		if (!joinNickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }
		if (intro.length > 100) { alert('자기소개는 100자 이내로 입력하세요.'); return; }

		$.post(ctx + '/member/doSave.do', $(this).serialize(), function (res) {
			if (res.code === '200') {
				alert('회원가입이 완료되었습니다. 로그인해 주세요.');
				$('#joinForm')[0].reset();
				joinEmailChecked = false;
				joinNickChecked = false;
				$('#joinEmailMsg, #joinNickMsg').text('');
				openModal('#loginModal');
			} else {
				alert(res.message || '회원가입에 실패했습니다. 입력값을 확인하세요.');
			}
		}, 'json').fail(function () {
			alert('요청 처리 중 오류가 발생했습니다.');
		});
	});
});
