/* ============================================================================
 * 빚다(BITDA) 공통 JS — CC-CMN-01 (로그인/회원가입 모달, CC-USR-01/02)
 * 전제: jQuery 로드 후 실행, body[data-ctx]에 contextPath 세팅(header.jsp).
 * 응답 규약: MessageVO {code:'200'|'400'|'401', message, data}
 *  - checkEmail/checkNickname은 항상 code '200', 판정은 data(Boolean)로만.
 *
 * 이 파일은 두 부분으로 나뉜다.
 *  1) window.bitda 공용 헬퍼 — 스크립트 로드 시점에 즉시 정의한다.
 *     (DOMContentLoaded 안에서 정의하면 다른 파일이 먼저 읽을 위험이 있다)
 *  2) 헤더 모달(로그인/회원가입) 동작 — DOMContentLoaded 이후.
 * ==========================================================================*/

/* ============================ 1) 공용 헬퍼 ============================ */
(function () {
	'use strict';

	window.bitda = window.bitda || {};

	/* ---------- 문자열/폼 ---------- */

	/* HTML 이스케이프(동적 렌더링 공용) */
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

	/* ---------- 입력값 유효성 검사 ---------- */

	/* 비었으면 alert 후 true 를 돌려준다. 사용: if (isEmpty(input, '...')) return false; */
	window.bitda.isEmpty = function (input, message) {
		if (input.value.trim() === '') {
			alert(message);
			input.focus();
			return true;
		}
		return false;
	};

	/* 숫자면 true. 사용: if (!isNumber(input, '...')) return false; */
	window.bitda.isNumber = function (input, message) {
		if (Number.isNaN(Number(input.value.trim()))) {
			alert(message);
			input.focus();
			return false;
		}
		return true;
	};

	/* 이메일 형식이면 true */
	window.bitda.isValidEmail = function (input, message) {
		var emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailPattern.test(input.value.trim())) {
			alert(message || '올바른 이메일 형식이 아닙니다.');
			input.focus();
			return false;
		}
		return true;
	};

	/* ---------- AJAX 공통 요청 ----------
	 * @param {string}   url          요청 URL (contextPath 포함)
	 * @param {string}   type         HTTP 방식 (기본 POST)
	 * @param {Object}   data         전송 데이터
	 * @param {Function} resFunction  성공 응답 처리 함수
	 * @param {Function} failFunction 실패 처리 함수 (없으면 failMessage 로 alert)
	 * @param {string}   failMessage  실패 시 기본 알림 문구
	 * @returns {jqXHR}  .always() 등을 이어 붙일 수 있다.
	 */
	window.bitda.requestAjax = function (options) {
		var failMessage = options.failMessage || '요청 처리 중 오류가 발생했습니다.';
		return $.ajax({
			url: options.url,
			type: options.type || 'POST',
			data: options.data || {},
			dataType: 'json'
		}).done(function (res) {
			if (typeof options.resFunction === 'function') {
				options.resFunction(res);
			}
		}).fail(function () {
			if (typeof options.failFunction === 'function') {
				options.failFunction();
			} else {
				alert(failMessage);
			}
		});
	};

	/* ---------- 화면 공용 부품 ---------- */

	/* 카테고리 셀렉트 채우기 (완성/공개 목록·글쓰기·수정 5개 화면 공용)
	 * select[data-selected] 값과 같은 옵션을 미리 선택한다. */
	window.bitda.fillCategorySelect = function (sel) {
		if (!sel) { return; }
		var ctx = document.body.dataset.ctx || '';
		var selected = String(sel.dataset.selected || '');
		window.bitda.requestAjax({
			url: ctx + '/category/doRetrieve.do',
			type: 'GET',
			resFunction: function (res) {
				if (res.code !== '200') { return; }
				(res.data || []).forEach(function (cItem) {
					var opt = document.createElement('option');
					opt.value = cItem.categoryId;
					opt.textContent = cItem.categoryNm;
					if (selected !== '' && String(cItem.categoryId) === selected) { opt.selected = true; }
					sel.appendChild(opt);
				});
			}
		});
	};

	/* 이메일/닉네임 중복확인 (모달·회원가입·정보수정 3개 화면 공용)
	 * 서버는 항상 code '200' 을 주고 data(Boolean) 로만 사용가능 여부를 알린다.
	 * @param {Function} onResult 사용 가능하면 true 를 받는다. */
	window.bitda.checkDuplicate = function (url, data, msgEl, onResult) {
		function show(text, cls) {
			msgEl.textContent = text;
			msgEl.classList.remove('ok', 'fail');
			msgEl.classList.add(cls);
		}
		window.bitda.requestAjax({
			url: url,
			data: data,
			resFunction: function (res) {
				if (res.code === '200' && res.data === true) {
					show('사용 가능합니다.', 'ok');
					onResult(true);
				} else {
					show('이미 사용 중입니다.', 'fail');
					onResult(false);
				}
			},
			failFunction: function () {
				show('확인 중 오류가 발생했습니다.', 'fail');
				onResult(false);
			}
		});
	};
}());

/* ==================== 2) 헤더 모달(로그인/회원가입) ==================== */
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';

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
	/* 오버레이 자기 자신을 클릭했을 때만 닫는다(모달 안쪽 클릭은 통과) */
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

	/* 헤더 모달은 비로그인 상태에서만 렌더된다(header.jsp의 c:if).
	   로그인 상태에서는 아래 요소들이 없으므로 반드시 null 가드가 필요하다. */

	/* ---------- 로그인 (CC-USR-01): POST /member/doLoginAjax.do ---------- */
	var loginForm = document.getElementById('loginForm');
	if (loginForm) {
		var showLoginMsg = function (text) {
			var loginMsg = document.getElementById('loginMsg');
			loginMsg.textContent = text;
			loginMsg.classList.remove('ok');
			loginMsg.classList.add('fail');
		};
		loginForm.addEventListener('submit', function (e) {
			e.preventDefault();
			// 1. 입력값 읽기
			var email = document.getElementById('loginEmail').value.trim();
			var password = document.getElementById('loginPassword').value;

			// 2. 유효성 검사
			if (!email || !password) {
				showLoginMsg('이메일과 비밀번호를 입력하세요.');
				return;
			}

			// 3. ajax()
			window.bitda.requestAjax({
				url: ctx + '/member/doLoginAjax.do',
				data: { email: email, password: password },
				// 4. 응답 처리
				resFunction: function (res) {
					if (res.code === '200') {
						location.reload();
					} else {
						showLoginMsg('이메일 또는 비밀번호가 올바르지 않습니다.');
					}
				},
				failFunction: function () {
					showLoginMsg('요청 처리 중 오류가 발생했습니다.');
				}
			});
		});
	}

	/* ---------- 회원가입 (CC-USR-02): checkEmail/checkNickname + doSave ---------- */
	var joinEmailChecked = false;
	var joinNickChecked = false;

	var btnJoinCheckEmail = document.getElementById('btnJoinCheckEmail');
	if (btnJoinCheckEmail) {
		btnJoinCheckEmail.addEventListener('click', function () {
			var emailInput = document.getElementById('joinEmail');
			if (window.bitda.isEmpty(emailInput, '이메일을 입력하세요.')) { return; }
			window.bitda.checkDuplicate(
				ctx + '/member/checkEmail.do',
				{ email: emailInput.value.trim() },
				document.getElementById('joinEmailMsg'),
				function (ok) { joinEmailChecked = ok; }
			);
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
			var nicknameInput = document.getElementById('joinNickname');
			if (window.bitda.isEmpty(nicknameInput, '닉네임을 입력하세요.')) { return; }
			window.bitda.checkDuplicate(
				ctx + '/member/checkNickname.do',
				{ nickname: nicknameInput.value.trim() },
				document.getElementById('joinNickMsg'),
				function (ok) { joinNickChecked = ok; }
			);
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
			// 1. 입력값 읽기
			var pw = document.getElementById('joinPassword').value;
			var cpw = document.getElementById('joinConfirmPassword').value;
			var intro = document.getElementById('joinUserIntro').value;

			// 2. 유효성 검사
			if (!joinEmailChecked) { alert('이메일 중복확인을 해주세요.'); return; }
			if (pw.length < 8) { alert('비밀번호는 8자 이상이어야 합니다.'); return; }
			if (pw !== cpw) { alert('비밀번호가 일치하지 않습니다.'); return; }
			if (!joinNickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }
			if (intro.length > 100) { alert('자기소개는 100자 이내로 입력하세요.'); return; }

			// 3. ajax()
			window.bitda.requestAjax({
				url: ctx + '/member/doSave.do',
				data: window.bitda.serializeForm(joinForm),
				// 4. 응답 처리
				resFunction: function (res) {
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
				}
			});
		});
	}
});
