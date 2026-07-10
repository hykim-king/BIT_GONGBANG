<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ADM-01 관리자-회원 관리 — 검색(이메일/닉네임 OR)+페이징, 행별 작품수/수정/삭제.
  수정: 닉네임/관리자여부 편집 모달(POST /admin/member_update.do)
  삭제: 연쇄 삭제 확정(POST /admin/member_delete.do)
--%>
<c:set var="pageTitle" value="회원 관리 · 빚다 관리자"/>
<c:set var="adminActiveMenu" value="member"/>
<%@ include file="/WEB-INF/views/cmn/admin_header.jsp" %>

<div class="section-head"><h2>회원 관리</h2><p>가입한 전체 회원을 검색하고 관리합니다.</p></div>

<form class="board-toolbar" method="get" action="${ctx}/admin/member_list.do">
	<input type="text" class="text-input search-word" name="searchWord" value="<c:out value='${param.searchWord}'/>" placeholder="이메일 또는 닉네임 검색">
	<button type="submit" class="btn ghost">검색</button>
</form>

<div class="table-scroll">
	<table>
		<thead><tr><th style="width:50px;">No</th><th>이메일</th><th>닉네임</th><th style="width:70px;">관리자</th><th style="width:70px;">작품수</th><th>가입일</th><th style="width:130px;">관리</th></tr></thead>
		<tbody>
			<c:forEach var="mVo" items="${list}" varStatus="st">
			<tr data-member-id="${mVo.memberId}" data-nickname="<c:out value='${mVo.nickname}'/>" data-is-admin="${mVo.isAdmin}">
				<td>${(page.pageNo - 1) * page.pageSize + st.count}</td>
				<td><c:out value="${mVo.email}"/></td>
				<td class="row-nickname"><c:out value="${mVo.nickname}"/></td>
				<td><c:choose>
					<c:when test="${mVo.isAdmin eq 'Y'}"><span class="pill admin">Y</span></c:when>
					<c:otherwise>N</c:otherwise>
				</c:choose></td>
				<td style="text-align:right;">${mVo.artworkCnt}</td>
				<td>${mVo.regDt}</td>
				<td><div style="display:flex;gap:6px;">
					<button type="button" class="btn ghost small mem-edit">수정</button>
					<button type="button" class="btn danger small mem-del">삭제</button>
				</div></td>
			</tr>
			</c:forEach>
			<c:if test="${empty list}">
			<tr><td colspan="7" class="empty-state">조건에 맞는 회원이 없습니다.</td></tr>
			</c:if>
		</tbody>
	</table>
</div>

<c:if test="${page.totalPage > 1}">
<div class="pager">
	<c:if test="${page.prev}"><a href="${pagingUrl}&pageNo=${page.startPage - 1}">&laquo;</a></c:if>
	<c:forEach var="p" begin="${page.startPage}" end="${page.endPage}">
		<c:choose>
			<c:when test="${p == page.pageNo}"><span class="cur">${p}</span></c:when>
			<c:otherwise><a href="${pagingUrl}&pageNo=${p}">${p}</a></c:otherwise>
		</c:choose>
	</c:forEach>
	<c:if test="${page.next}"><a href="${pagingUrl}&pageNo=${page.endPage + 1}">&raquo;</a></c:if>
</div>
</c:if>

<%-- 회원 수정 모달 --%>
<div class="overlay" id="memberEditModal">
	<div class="modal">
		<div class="modal-head">
			<h3>회원 수정</h3>
			<button type="button" class="modal-close" aria-label="닫기">&times;</button>
		</div>
		<form id="memberEditForm" method="post">
			<input type="hidden" id="editMemberId" name="memberId">
			<div class="field">
				<label for="editNickname">닉네임 (10자 이내)</label>
				<input type="text" class="text-input" id="editNickname" name="nickname" maxlength="10" required>
			</div>
			<div class="field">
				<label for="editIsAdmin">관리자 여부</label>
				<select class="text-input" id="editIsAdmin" name="isAdmin">
					<option value="N">N (일반)</option>
					<option value="Y">Y (관리자)</option>
				</select>
			</div>
			<button type="submit" class="btn block">저장</button>
		</form>
	</div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
	var ctx = document.body.dataset.ctx || '';

	document.querySelectorAll('.mem-edit').forEach(function(btn) {
		btn.addEventListener('click', function() {
			var tr = btn.closest('tr');
			document.getElementById('editMemberId').value = tr.dataset.memberId;
			document.getElementById('editNickname').value = tr.dataset.nickname;
			document.getElementById('editIsAdmin').value = tr.dataset.isAdmin;
			document.getElementById('memberEditModal').classList.add('open');
		});
	});

	var editForm = document.getElementById('memberEditForm');
	if (editForm) {
		editForm.addEventListener('submit', function(e) {
			e.preventDefault();
			$.post(ctx + '/admin/member_update.do', window.bitda.serializeForm(editForm), function(res) {
				if (res.code === '200') {
					alert(res.message);
					location.reload();
				} else {
					alert(res.message || '수정에 실패했습니다.');
				}
			}, 'json').fail(function() { alert('요청 처리 중 오류가 발생했습니다.'); });
		});
	}

	document.querySelectorAll('.mem-del').forEach(function(btn) {
		btn.addEventListener('click', function() {
			var tr = btn.closest('tr');
			if (!confirm('"' + tr.dataset.nickname + '" 회원을 삭제하시겠습니까?\n작성한 작품·작업일지·댓글·좋아요가 모두 함께 삭제됩니다.')) { return; }
			$.post(ctx + '/admin/member_delete.do', { memberId: tr.dataset.memberId }, function(res) {
				if (res.code === '200') {
					alert(res.message);
					location.reload();
				} else {
					alert(res.message || '삭제에 실패했습니다.');
				}
			}, 'json').fail(function() { alert('요청 처리 중 오류가 발생했습니다.'); });
		});
	});
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
