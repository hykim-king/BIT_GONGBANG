<div align="center">

# 🧶 비트공방 · BITDA

### 공예 작품과 _제작 과정_ 을 함께 나누는 크래프트 커뮤니티

<!-- 대표 이미지 추가 예정 -->

<img src="https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
<img src="https://img.shields.io/badge/Spring_MVC-5.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/MyBatis-3.5-C10000?style=for-the-badge">
<img src="https://img.shields.io/badge/Oracle-DB-F80000?style=for-the-badge&logo=oracle&logoColor=white">
<img src="https://img.shields.io/badge/Tomcat-WAR-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black">

</div>

---

## 👨‍🏫 프로젝트 개요

> 완성작만 소비하는 SNS가 아니라, **작품이 만들어지는 과정**까지 담는 커뮤니티.

**비트공방**은 공예 작가와 애호가가 작품·재료·기법·제작 과정을 함께 공유하고 탐색하는 공예 전문 커뮤니티 웹 서비스입니다.
사용된 재료·기법과 날짜별 작업 기록을 **작업일지 타임라인**으로 보여주어, 결과물뿐 아니라 *만드는 여정*을 함께 나눌 수 있게 합니다.

| | |
|:--|:--|
| 🏆 **완성 게시판** | 완성된 작품 공유 |
| 🛠️ **공개작업 게시판** | 제작 중인 작품 + 날짜별 작업일지 타임라인 |
| 💬 **커뮤니티** | 댓글 · 좋아요 · 카테고리 기반 검색/추천 |

---

## 🧑‍🤝‍🧑 팀 구성 및 역할

| 코드 | 이름 | 담당 모듈 / 역할 |
|:--:|:--:|:--|
| `m1` | 장소은 | **회원·인증** — 회원가입 · 로그인/로그아웃 · 마이페이지 · 정보수정 |
| `m2` | 이기준 | **메인·검색·추천** — 메인 피드 · 추천/인기 · 통합검색 |
| `m3` | 강은후 | **게시판·작업일지** — 작품 CRUD · 작업일지 타임라인 |
| `m4` | 김신우 | **공통기능** — 댓글 · 좋아요 · 첨부파일 |
| `m5` | 홍선기 | **카테고리·관리자** — 카테고리 · 회원/게시물 관리 · 통계 |

> 🌿 **브랜치 전략** — 팀원별 `feature/m1` ~ `feature/m5` → Pull Request → `main`

---

## 🗂️ 주요 기능

> 기능(메뉴) 관점으로 그룹핑 — 테이블과 1:1이 아닙니다.

| 모듈 | 주요 기능 |
|:--|:--|
| **M1 · 회원·인증** | 회원가입 · 로그인/로그아웃 · 마이페이지 · 정보수정/탈퇴 · 이메일·닉네임 중복확인 |
| **M2 · 메인·검색·추천** | 메인 피드 · 추천(좋아요·조회수) · 인기 랭킹 · 통합검색(제목·내용·닉네임·카테고리) |
| **M3 · 게시판(작품)** | 완성·공개작업 게시판 CRUD · 조회수 · 작업중→완성 전환 |
| **M3-2 · 작업일지** | 날짜별 제작 로그(타임라인) · 진행 이미지 업로드 |
| **M4 · 공통기능** | 댓글 · 좋아요(토글) · 첨부파일(다중 업로드·대표지정·다운로드) &nbsp;`polymorphic` |
| **M5 · 카테고리** | 공예 분야 분류 · 검색 필터 · 관리자 CRUD |
| **M6 · 관리자** | 회원관리 · 게시물관리 · 통계 대시보드 |

---

## 🏗️ 아키텍처

Spring MVC 레이어드 아키텍처 — 학원 `sw16`의 `cmn` 공통 패턴 기반

```text
┌────────────┐   ┌───────────────────────┐   ┌──────────────────┐   ┌────────┐
│ Controller │ → │ Service (WorkDiv<T>)  │ → │ Mapper(IF) + XML │ → │ Oracle │
└────────────┘   └───────────────────────┘   └──────────────────┘   └────────┘
                            │
                    Domain VO (extends cmn.DTO)
```

- **공통 계층 `cmn`** — `DTO`(페이징·검색) · `WorkDiv<T>`(CRUD 계약 5종: `doRetrieve`·`doSelectOne`·`doSave`·`doUpdate`·`doDelete`) · `MessageVO` · `FileManager` · `Grade`·`TargetType` · `Login/Admin Interceptor`
- **영속성** — MyBatis `@Mapper` 인터페이스 + XML _(별도 DAO 없음)_
- **다형(polymorphic) 연관** — 댓글·좋아요·첨부는 `target_type` / `target_id`로 여러 게시판이 공유

---

## 🗄️ 데이터베이스

`7 tables` — 모든 VO는 `cmn.DTO`를 상속합니다.

| 테이블 | 설명 | Domain VO |
|:--|:--|:--|
| `member` | 회원·인증 _(email UNIQUE)_ | `MemberVO` |
| `category` | 공예 카테고리 | `CategoryVO` |
| `artwork` | 작품 _(완성/공개작업 `is_status` 분기)_ | `ArtworkVO` |
| `artwork_entry` | 작업일지 _(artwork FK)_ | `ArtworkEntryVO` |
| `attach_file` | 첨부 이미지 _(polymorphic)_ | `AttachFileVO` |
| `board_comment` | 댓글 _(polymorphic)_ | `CommentVO` |
| `board_like` | 좋아요 _(polymorphic)_ | `LikeVO` |

<div align="center">

<!-- ERD 이미지 추가 예정 -->

</div>

---

## ⚙️ 기술 스택

**Backend**

<img src="https://img.shields.io/badge/Java_11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/Spring_MVC_5.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/MyBatis_3.5-C10000?style=for-the-badge"> <img src="https://img.shields.io/badge/HikariCP-2C2255?style=for-the-badge"> <img src="https://img.shields.io/badge/Apache_Tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black">

**Frontend**

<img src="https://img.shields.io/badge/HTML-E34F26?style=for-the-badge&logo=html5&logoColor=white"> <img src="https://img.shields.io/badge/CSS-1572B6?style=for-the-badge&logo=css3&logoColor=white"> <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"> <img src="https://img.shields.io/badge/JSP_/_JSTL-007396?style=for-the-badge&logo=java&logoColor=white">

**Database**

<img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white">

**Test & Tools**

<img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white"> <img src="https://img.shields.io/badge/Log4j2-D22128?style=for-the-badge"> <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/Eclipse_STS-2C2255?style=for-the-badge&logo=eclipseide&logoColor=white"> <img src="https://img.shields.io/badge/SQL_Developer-F80000?style=for-the-badge&logo=oracle&logoColor=white"> <img src="https://img.shields.io/badge/EXERD-555555?style=for-the-badge"> <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white">

---

## 📊 프로젝트 규모

| Controller | Service | Mapper | Domain VO | Table | 화면(JSP) | 공통(cmn) | 기능 |
|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| 9 | 10 | 7 | 7 | 7 | 19 | 10 | 42 |

---

## 🗓️ 개발 일정

| 주차 | 주요 작업 |
|:--:|:--|
| 1주차 | 기획 정리 · 화면 설계 · Git/GitHub 환경 구성 |
| 2주차 | HTML/CSS/JavaScript 기반 주요 화면 구현 |
| 3주차 | DB 설계 · ERD 작성 · Spring MVC 구조 설정 |
| 4주차 | 회원가입 · 로그인 · 마이페이지 구현 |
| 5주차 | 커뮤니티 게시판 CRUD · 댓글 기능 구현 |
| 6주차 | 공예 작품 CRUD · 날짜별 작업일지 구현 |
| 7주차 | 검색·필터 · 좋아요 · 통합 테스트 · 배포 및 발표 준비 |

---

## 📝 프로젝트 수행결과

<!-- 메인 피드 / 작품 상세 / 작업일지 / 커뮤니티 / 로그인·회원가입 / 마이페이지 화면 이미지 추가 예정 -->

---

## 🔗 참고

- [Behance](https://www.behance.net/)
- [Pinterest](https://www.pinterest.com/)

<div align="center">
<sub>🧶 <b>비트공방 · BITDA</b> — 크래프트 커뮤니티 웹 서비스</sub>
</div>
