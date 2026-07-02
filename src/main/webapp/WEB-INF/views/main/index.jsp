<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>공예커뮤니티 - 메인</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
<header class="site-header">
    <div class="header-inner">
        <h1 class="logo"><a href="${pageContext.request.contextPath}/main/index">공예커뮤니티</a></h1>
        <form class="search-form" action="${pageContext.request.contextPath}/main/search" method="get">
            <select name="searchDiv">
                <option value="">전체</option>
                <option value="title">제목</option>
                <option value="content">내용</option>
                <option value="nickname">닉네임</option>
                <option value="category">카테고리</option>
            </select>
            <input type="text" name="searchWord" placeholder="작품·작가·카테고리 검색">
            <button type="submit">검색</button>
        </form>
        <nav class="auth-nav">
            <a href="#">로그인</a>
            <a href="#">회원가입</a>
        </nav>
    </div>
</header>

<main class="main-content">
    <section class="hero">
        <h2>손끝에서 피어나는 작품들</h2>
        <p>공예 작가들의 완성작과 작업 과정을 만나보세요</p>
    </section>

    <section class="artwork-section">
        <div class="section-header">
            <h3>추천 작품</h3>
            <span class="section-desc">좋아요와 조회수를 반영한 추천</span>
        </div>
        <div class="artwork-grid">
            <c:forEach var="item" items="${recommendList}">
                <article class="artwork-card">
                    <a href="${pageContext.request.contextPath}/artwork/detail?artworkId=${item.artworkId}">
                        <div class="thumb">
                            <c:choose>
                                <c:when test="${not empty item.thumbUrl}">
                                    <img src="${item.thumbUrl}" alt="${item.title}">
                                </c:when>
                                <c:otherwise>
                                    <div class="thumb-placeholder">No Image</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="card-body">
                            <span class="category">${item.categoryName}</span>
                            <h4>${item.title}</h4>
                            <p class="meta">${item.nickname} · ♥ ${item.likeCount} · 👁 ${item.viewCount}</p>
                        </div>
                    </a>
                </article>
            </c:forEach>
            <c:if test="${empty recommendList}">
                <p class="empty-msg">추천 작품이 없습니다.</p>
            </c:if>
        </div>
    </section>

    <section class="artwork-section">
        <div class="section-header">
            <h3>인기 작품</h3>
            <span class="section-desc">최근 7일간 많은 사랑을 받은 작품</span>
        </div>
        <div class="artwork-grid">
            <c:forEach var="item" items="${popularList}">
                <article class="artwork-card">
                    <a href="${pageContext.request.contextPath}/artwork/detail?artworkId=${item.artworkId}">
                        <div class="thumb">
                            <c:choose>
                                <c:when test="${not empty item.thumbUrl}">
                                    <img src="${item.thumbUrl}" alt="${item.title}">
                                </c:when>
                                <c:otherwise>
                                    <div class="thumb-placeholder">No Image</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="card-body">
                            <span class="category">${item.categoryName}</span>
                            <h4>${item.title}</h4>
                            <p class="meta">${item.nickname} · 최근 ♥ ${item.recentLikeCount}</p>
                        </div>
                    </a>
                </article>
            </c:forEach>
            <c:if test="${empty popularList}">
                <p class="empty-msg">인기 작품이 없습니다.</p>
            </c:if>
        </div>
    </section>

    <section class="artwork-section">
        <div class="section-header">
            <h3>최신 작품</h3>
            <span class="section-desc">방금 올라온 완성작</span>
        </div>
        <div class="artwork-grid">
            <c:forEach var="item" items="${latestList}">
                <article class="artwork-card">
                    <a href="${pageContext.request.contextPath}/artwork/detail?artworkId=${item.artworkId}">
                        <div class="thumb">
                            <c:choose>
                                <c:when test="${not empty item.thumbUrl}">
                                    <img src="${item.thumbUrl}" alt="${item.title}">
                                </c:when>
                                <c:otherwise>
                                    <div class="thumb-placeholder">No Image</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="card-body">
                            <span class="category">${item.categoryName}</span>
                            <h4>${item.title}</h4>
                            <p class="meta">${item.nickname}
                                · <fmt:formatDate value="${item.regDt}" pattern="yyyy.MM.dd"/></p>
                        </div>
                    </a>
                </article>
            </c:forEach>
            <c:if test="${empty latestList}">
                <p class="empty-msg">최신 작품이 없습니다.</p>
            </c:if>
        </div>
    </section>
</main>

<footer class="site-footer">
    <p>&copy; 2026 Craft Community</p>
</footer>
</body>
</html>
