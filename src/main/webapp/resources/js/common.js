/* ============================================================================
 * 빚다(BITDA) 공통 JS — CC-CMN-01 (로그인/회원가입 모달, CC-USR-01/02)
 * 전제: jQuery 로드 후 실행, body[data-ctx]에 contextPath 세팅(header.jsp).
 * 응답 규약: MessageVO {code:'200'|'400'|'401', message, data}
 *  - checkEmail/checkNickname은 항상 code '200', 판정은 data(Boolean)로만.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';

	/* HTML 이스케이프 헬퍼(이후 단계의 동적 렌더링 공용) */
	window.bitda = window.bitda || {};
	window.bitda.esc = function (s) {
		return String(s == null ? '' : s)
			.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
	};

	/* 폼 -> urlencoded 문자열 (AJAX의 data 인자로 넘길 값)
	 * FormData는 HTML 표준에 따라 textarea의 줄바꿈을 CRLF(\r\n)로 바꿔버린다.
	 * 줄바꿈이 LF(\n) 그대로 서버에 전달되도록 textarea 값만 다시 넣어준다. */
	window.bitda.serializeForm = function (form) {
		var params = new URLSearchParams(new FormData(form));
		var areas = form.querySelectorAll('textarea[name]');
		for (var i = 0; i < areas.length; i++) {
			params.set(areas[i].name, areas[i].value);
		}
		return params.toString();
	};

	/* ---------- 모달 열기/닫기 ---------- */
	function openModal(id) {
		document.querySelectorAll('.overlay').forEach(function (el) { el.classList.remove('open'); });
		var modal = document.querySelector(id);
		if (modal) { modal.classList.add('open'); }
	}
	function closeModals() {
		document.querySelectorAll('.overlay').forEach(function (el) { el.classList.remove('open'); });
	}

	document.addEventListener('click', function (e) {
		var t = e.target.closest('#btnOpenLogin');
		if (!t) return;
		openModal('#loginModal');
	});
	document.addEventListener('click', function (e) {
		var t = e.target.closest('#btnOpenJoin');
		if (!t) return;
		openModal('#joinModal');
	});
	document.addEventListener('click', function (e) {
		var t = e.target.closest('.modal-close');
		if (!t) return;
		closeModals();
	});
	document.addEventListener('click', function (e) {
		if (e.target.classList.contains('overlay')) { closeModals(); }
	});
	document.addEventListener('keydown', function (e) {
		if (e.key === 'Escape') { closeModals(); }
	});
	/* 모달 전환(로그인 <-> 회원가입) */
	document.addEventListener('click', function (e) {
		var t = e.target.closest('#linkToJoin');
		if (!t) return;
		openModal('#joinModal');
	});
	document.addEventListener('click', function (e) {
		var t = e.target.closest('#linkToLogin');
		if (!t) return;
		openModal('#loginModal');
	});

	/* ---------- 로그인 (CC-USR-01): POST /member/doLoginAjax.do ---------- */
	var loginForm = document.getElementById('loginForm');
	if (loginForm) {
		loginForm.addEventListener('submit', function (e) {
			e.preventDefault();
			var email = document.getElementById('loginEmail').value.trim();
			var password = document.getElementById('loginPassword').value;
			if (!email || !password) {
				var loginMsg = document.getElementById('loginMsg');
				loginMsg.textContent = '이메일과 비밀번호를 입력하세요.';
				loginMsg.classList.remove('ok');
				loginMsg.classList.add('fail');
				return;
			}
			$.post(ctx + '/member/doLoginAjax.do', { email: email, password: password }, function (res) {
				if (res.code === '200') {
					location.reload();
				} else {
					var loginMsg = document.getElementById('loginMsg');
					loginMsg.textContent = '이메일 또는 비밀번호가 올바르지 않습니다.';
					loginMsg.classList.remove('ok');
					loginMsg.classList.add('fail');
				}
			}, 'json').fail(function () {
				var loginMsg = document.getElementById('loginMsg');
				loginMsg.textContent = '요청 처리 중 오류가 발생했습니다.';
				loginMsg.classList.remove('ok');
				loginMsg.classList.add('fail');
			});
		});
	}

	/* ---------- 회원가입 (CC-USR-02): checkEmail/checkNickname + doSave ---------- */
	var joinEmailChecked = false;
	var joinNickChecked = false;

	var btnJoinCheckEmail = document.getElementById('btnJoinCheckEmail');
	if (btnJoinCheckEmail) {
		btnJoinCheckEmail.addEventListener('click', function () {
			var email = document.getElementById('joinEmail').value.trim();
			if (!email) { alert('이메일을 입력하세요.'); return; }
			$.post(ctx + '/member/checkEmail.do', { email: email }, function (res) {
				var joinEmailMsg = document.getElementById('joinEmailMsg');
				if (res.code === '200' && res.data === true) {
					joinEmailMsg.textContent = '사용 가능합니다.';
					joinEmailMsg.classList.remove('fail');
					joinEmailMsg.classList.add('ok');
					joinEmailChecked = true;
				} else {
					joinEmailMsg.textContent = '이미 사용 중입니다.';
					joinEmailMsg.classList.remove('ok');
					joinEmailMsg.classList.add('fail');
					joinEmailChecked = false;
				}
			}, 'json').fail(function () {
				var joinEmailMsg = document.getElementById('joinEmailMsg');
				joinEmailMsg.textContent = '확인 중 오류가 발생했습니다.';
				joinEmailMsg.classList.remove('ok');
				joinEmailMsg.classList.add('fail');
				joinEmailChecked = false;
			});
		});
	}
	var joinEmail = document.getElementById('joinEmail');
	if (joinEmail) {
		joinEmail.addEventListener('input', function () {
			joinEmailChecked = false;
			document.getElementById('joinEmailMsg').textContent = '';
		});
	}

	var btnJoinCheckNick = document.getElementById('btnJoinCheckNick');
	if (btnJoinCheckNick) {
		btnJoinCheckNick.addEventListener('click', function () {
			var nickname = document.getElementById('joinNickname').value.trim();
			if (!nickname) { alert('닉네임을 입력하세요.'); return; }
			$.post(ctx + '/member/checkNickname.do', { nickname: nickname }, function (res) {
				var joinNickMsg = document.getElementById('joinNickMsg');
				if (res.code === '200' && res.data === true) {
					joinNickMsg.textContent = '사용 가능합니다.';
					joinNickMsg.classList.remove('fail');
					joinNickMsg.classList.add('ok');
					joinNickChecked = true;
				} else {
					joinNickMsg.textContent = '이미 사용 중입니다.';
					joinNickMsg.classList.remove('ok');
					joinNickMsg.classList.add('fail');
					joinNickChecked = false;
				}
			}, 'json').fail(function () {
				var joinNickMsg = document.getElementById('joinNickMsg');
				joinNickMsg.textContent = '확인 중 오류가 발생했습니다.';
				joinNickMsg.classList.remove('ok');
				joinNickMsg.classList.add('fail');
				joinNickChecked = false;
			});
		});
	}
	var joinNickname = document.getElementById('joinNickname');
	if (joinNickname) {
		joinNickname.addEventListener('input', function () {
			joinNickChecked = false;
			document.getElementById('joinNickMsg').textContent = '';
		});
	}

	var joinForm = document.getElementById('joinForm');
	if (joinForm) {
		joinForm.addEventListener('submit', function (e) {
			e.preventDefault();
			var pw = document.getElementById('joinPassword').value;
			var cpw = document.getElementById('joinConfirmPassword').value;
			var intro = document.getElementById('joinUserIntro').value;
			if (!joinEmailChecked) { alert('이메일 중복확인을 해주세요.'); return; }
			if (pw.length < 8) { alert('비밀번호는 8자 이상이어야 합니다.'); return; }
			if (pw !== cpw) { alert('비밀번호가 일치하지 않습니다.'); return; }
			if (!joinNickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }
			if (intro.length > 100) { alert('자기소개는 100자 이내로 입력하세요.'); return; }

			$.post(ctx + '/member/doSave.do', window.bitda.serializeForm(joinForm), function (res) {
				if (res.code === '200') {
					alert('회원가입이 완료되었습니다. 로그인해 주세요.');
					joinForm.reset();
					joinEmailChecked = false;
					joinNickChecked = false;
					document.querySelectorAll('#joinEmailMsg, #joinNickMsg').forEach(function (el) { el.textContent = ''; });
					openModal('#loginModal');
				} else {
					alert(res.message || '회원가입에 실패했습니다. 입력값을 확인하세요.');
				}
			}, 'json').fail(function () {
				alert('요청 처리 중 오류가 발생했습니다.');
			});
		});
	}
});
