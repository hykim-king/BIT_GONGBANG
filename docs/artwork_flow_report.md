# Artwork / ArtworkEntry 모듈 플로우 보고서

## 1. 개요
게시판 하나(`artwork` 테이블)를 `is_status` 값으로 **완성게시판(Y)**과 **공개작업게시판(N/하이브리드)** 두 화면으로 나눠 보여주고, 공개작업 상세에는 제작 과정을 기록하는 **작업일지(`artwork_entry`)** 타임라인이 함께 표시되는 구조입니다.

```
[JSP/브라우저] → Controller(URL 매핑, 파라미터 바인딩) → Service(트랜잭션·조합 로직) → Mapper/XML(SQL) → DB
```
- **Controller**: 로직/SQL 없음. URL 배정 + `@ModelAttribute`로 파라미터 수집 + Service 호출 + 결과를 Model/redirect로 전달만 함.
- **Service**: `@Transactional`로 트랜잭션 경계 관리, 여러 Mapper 호출을 조합.
- **Mapper/XML**: 실제 SQL. MyBatis가 인터페이스 메서드와 XML의 `id`를 이름으로 매칭해서 실행.

## 2. 이번에 수정한 부분 (직전 코드 검토에서 발견)

### 수정 1 — 조회수 증가 없는 순수 조회 메서드 추가
`src/main/java/com/pcwk/ehr/artwork/service/ArtworkService.java`
```java
/** 상세 조회 + 조회수 1 증가 (완성게시판 상세용). */
@Transactional
public ArtworkVO doSelectOne(ArtworkVO param) {
    artworkMapper.updateViewCount(param);
    return artworkMapper.doSelectOne(param);
}

/**
 * 상세 조회 (조회수 증가 없음).
 * 수정 폼 렌더링, 수정/삭제 후 is_status 확인용 재조회처럼
 * "보여주기만 하고 조회수는 올리면 안 되는" 곳에서 사용.
 */
public ArtworkVO findOne(ArtworkVO param) {
    return artworkMapper.doSelectOne(param);
}
```
`artworkMapper.doSelectOne` 자체는 원래 순수 SELECT라 부작용이 없었고, 부작용은 이걸 감싸는 **Service의 `doSelectOne`이 조회수 UPDATE를 같이 실행**하는 데서 생겼던 것입니다. 그래서 UPDATE 없이 SELECT만 하는 `findOne`을 Service에 추가해서 분리했습니다.

### 수정 2 — 수정 폼에서 조회수가 오르던 문제 해결
`src/main/java/com/pcwk/ehr/artwork/controller/ArtworkController.java`
```java
@GetMapping("/complete/modify")
public String completeModifyForm(ArtworkVO param, Model model) {
    log.debug("completeModifyForm param: " + param);

    ArtworkVO outVO = artworkService.findOne(param);   // 조회수 증가 없는 순수 조회
    model.addAttribute("vo", outVO);
    return "artwork/complete/modify";
}
```
`doSelectOne` → `findOne`으로 교체 한 줄만으로 해결. 이제 수정 폼을 열어도 조회수가 안 오릅니다.

### 수정 3 — `doUpdate`/`doDelete`가 게시판 구분 없이 무조건 complete로 튕기던 문제 해결
```java
@PostMapping("/doUpdate")
public String doUpdate(@ModelAttribute ArtworkVO param) {
    log.debug("doUpdate param: " + param);

    artworkService.doUpdate(param);                              // 제목/내용/수정일 갱신
    ArtworkVO saved = artworkService.findOne(param);             // 조회수 증가 없이 is_status 재확인
    String isStatus = saved != null ? saved.getIsStatus() : param.getIsStatus(); // 재조회 실패 시 폴백
    return "redirect:" + viewUrl(isStatus, param.getArtworkId()); // is_status 에 맞는 게시판 상세로 이동
}

@PostMapping("/doDelete")
public String doDelete(@ModelAttribute ArtworkVO param) {
    log.debug("doDelete param: " + param);

    ArtworkVO target = artworkService.findOne(param);            // 삭제 전 is_status 확보 (삭제 후엔 조회 불가)
    String isStatus = target != null ? target.getIsStatus() : param.getIsStatus(); // 재조회 실패 시 폴백
    artworkService.doDelete(param);                              // 실제 삭제
    return "redirect:" + listUrl(isStatus);                      // is_status 에 맞는 게시판 목록으로 이동
}

/** is_status='Y' → 완성 상세, 그 외('N') → 공개작업 상세 */
private String viewUrl(String isStatus, int artworkId) {
    String board = "Y".equals(isStatus) ? "/artwork/complete/view" : "/artwork/working/view";
    return board + "?artworkId=" + artworkId;
}

/** is_status='Y' → 완성 목록, 그 외('N') → 공개작업 목록 */
private String listUrl(String isStatus) {
    return "Y".equals(isStatus) ? "/artwork/complete/list" : "/artwork/working/list";
}
```
**설계 포인트**: JSP가 hidden input으로 `isStatus`를 잘 넘겨줄지 아직 불확실하고(JSP 미구현 상태), `doDelete`는 삭제 후엔 그 레코드를 다시 조회할 수 없기 때문에 — **폼 입력값에 의존하지 않고 DB에서 직접 `is_status`를 재확인**하는 방식으로 만들었습니다. `doDelete`는 삭제 *전에* 미리 조회해서 상태를 확보해둡니다. `findOne`은 조회수를 안 올리는 메서드라 이 재조회 자체가 또 다른 부작용을 만들지도 않습니다.

### 수정 4 — 완성작 등록 시 완성일(`comp_dt`)이 비어 들어가던 문제 해결
`src/main/resources/mapper/artwork/ArtworkMapper.xml` (`doSave`)
```xml
INSERT INTO artwork (
    artwork_id, member_id, category_id, is_status,
    title, content, view_count, reg_dt, mod_dt, comp_dt
) VALUES (
    #{artworkId}, #{memberId}, #{categoryId}, #{isStatus},
    #{title}, #{content}, 0, SYSDATE, NULL,
    <choose>
        <when test='isStatus == "Y"'>SYSDATE</when>
        <otherwise>#{compDt,jdbcType=DATE}</otherwise>
    </choose>
)
```
기존엔 `completeDoSave`가 `isStatus`만 `"Y"`로 세팅하고 `compDt`는 아무도 채우지 않아서, 완성으로 등록해도 `comp_dt`가 `NULL`로 저장됐습니다. Java 쪽에서 날짜 문자열을 만들어 넣는 대신, **SQL에서 `is_status`값에 따라 `comp_dt`를 분기**하도록 고쳤습니다: `'Y'`로 등록되면 `SYSDATE`로 자동 세팅되고, `'N'`(공개작업)으로 등록되면 기존처럼 `NULL`이 들어갑니다. `ArtworkController.completeDoSave`의 TODO 주석도 제거하고 자동 세팅된다는 설명으로 교체했습니다.

## 3. Artwork 플로우

### 3-1. 완성작 목록 — `GET /artwork/complete/list`
```
completeList(param)
  └─ param.setIsStatus("Y")
  └─ artworkService.doRetrieve(param)  ──▶ artworkMapper.doRetrieve  (is_status='Y' 필터)
  └─ artworkService.selectCount(param) ──▶ artworkMapper.selectCount (동일 필터, 총건수)
  └─ model.addAttribute("list"/"totalCnt") → complete/list.jsp
```

### 3-2. 완성작 상세 — `GET /artwork/complete/view`
```
completeView(param)
  └─ artworkService.doSelectOne(param)   [@Transactional]
        1) artworkMapper.updateViewCount(param)   // view_count + 1
        2) artworkMapper.doSelectOne(param)       // 본문 SELECT
  └─ model.addAttribute("vo", outVO) → complete/view.jsp
```

### 3-3. 완성작 등록 — `GET/POST /artwork/complete/reg`, `/complete/doSave`
```
completeDoSave(param)
  └─ param.setIsStatus("Y")
  └─ artworkService.doSave(param) ──▶ artworkMapper.doSave
        selectKey(BEFORE): seq_artwork.NEXTVAL → param.artworkId 채움
        INSERT ... view_count=0, reg_dt=SYSDATE, comp_dt=(is_status='Y' ? SYSDATE : #{compDt})
  └─ redirect: /artwork/complete/view?artworkId={새 PK}
```
✅ **수정됨**(수정 4 참고): `is_status='Y'`로 등록되면 `comp_dt`가 SQL에서 자동으로 `SYSDATE`로 채워짐.

### 3-4. 완성작 수정 폼 — `GET /artwork/complete/modify` ✅ 수정됨
`findOne`(조회수 증가 없음) 사용 → 폼 진입만으로 조회수 안 오름.

### 3-5. 공개작업 목록 — `GET /artwork/working/list` (하이브리드 필터)
```
workingList(param)                        // isStatus 세팅 안 함
  └─ artworkService.doRetrieve(param) ──▶ artworkMapper.doRetrieve
        SQL: is_status='N' OR EXISTS(artwork_entry.artwork_id = artwork.artwork_id)
  └─ selectCount 도 동일 분기
```
완성 전환됐어도 작업일지가 있으면 계속 노출, 일지 없이 바로 완성으로 등록된 건 제외.

### 3-6. 공개작업 상세 — `GET /artwork/working/view`
```
workingView(param)
  └─ artworkService.viewWithEntry(param)   [@Transactional]
        1) artworkMapper.updateViewCount(param)
        2) artworkMapper.doSelectOne(param)               → artwork 본문
        3) artworkEntryMapper.doRetrieve({artworkId})     → 작업일지 목록(최신순)
        4) artwork.setEntryList(entryList)
```

### 3-7. 공개작업 등록 — `POST /working/doSave`
`param.setIsStatus("N")` 강제 → `doSave` (comp_dt는 NULL로 저장됨, 정상).

### 3-8. 공통 액션 (수정본)
| 액션 | 흐름 | 상태 |
|---|---|---|
| `doUpdate` | `doUpdate` → `findOne`으로 is_status 확인 → 해당 게시판 상세로 redirect | ✅ 수정됨 |
| `doDelete` | 삭제 전 `findOne`으로 is_status 확보 → `doDelete` → 해당 게시판 목록으로 redirect | ✅ 수정됨 |
| `complete`(전환) | `complete` → `artworkMapper.updateStatus` (`is_status='Y'`, `comp_dt=SYSDATE`) → complete/view | 기존 그대로(원래도 맞는 로직) |

## 4. ArtworkEntry(작업일지) 플로우

```
ArtworkEntryController (/artworkEntry/**)
  ├─ GET /reg         → 등록 폼 렌더
  ├─ POST /doSave     → artworkEntryService.doSave → artworkEntryMapper.doSave
  │                       selectKey(BEFORE): seq_artwork_entry.NEXTVAL → artworkEntry 채움
  │                       INSERT (artwork_entry, artwork_id, content, reg_dt=SYSDATE, mod_dt=NULL)
  │                       redirect → /artwork/working/view?artworkId=...
  ├─ POST /doUpdate   → artworkEntryService.doUpdate → artworkEntryMapper.doUpdate
  │                       UPDATE content, mod_dt=SYSDATE  WHERE artwork_entry=PK
  │                       redirect → /artwork/working/view?artworkId=...
  └─ POST /doDelete   → artworkEntryService.doDelete → artworkEntryMapper.doDelete
                          DELETE WHERE artwork_entry=PK (단건. 상위 artwork 삭제 시엔 FK CASCADE로 별도 자동삭제)
                          redirect → /artwork/working/view?artworkId=...
```
- 목록(`entry_list`)은 별도 화면 없이 `ArtworkService.viewWithEntry()`가 `working/view`에서 조합해서 내려줌.
- `member_id`가 없는 이유: 작성자 = 상위 `artwork` 소유자로 간주(설계서 명시).
- 첨부파일(`target_type=ARTWORK_ENTRY`)은 `attach_file` 테이블 소관이라 이 모듈에서 다루지 않음(TODO).

## 5. 데이터 모델 요약

| VO | 핵심 컬럼 | 비고 |
|---|---|---|
| `ArtworkVO` | artworkId(PK), memberId(FK), categoryId(FK), isStatus(Y/N), title, content, viewCount, regDt, modDt, compDt | `entryList`(파생), `days`/`likeWeight`(메인화면 기본값), `nickname`/`categoryNm`/`likeCount`(JOIN 파생) |
| `ArtworkEntryVO` | artworkEntry(PK), artworkId(FK, CASCADE), content, regDt, modDt | member_id 없음 |

## 6. 테스트 커버리지 현황
- `ArtworkMapperJUnit` / `ArtworkEntryMapper`(test) : Mapper 계층만 검증 (Mapper 직접 autowire)
- Service/Controller 계층은 아직 자동화 테스트 없음 (`findOne`, 수정된 redirect 분기 로직도 아직 미검증 — MockMvc 등으로 추가 권장)

## 7. 남은 알려진 이슈 (이번엔 손대지 않음)
- [ ] `doDelete` 시 첨부/댓글/좋아요 수동삭제 미구현 (다른 팀원 Mapper 연동 대기)
- [ ] 작성자/관리자 권한 체크 미구현
- [ ] Controller/Service 자동화 테스트 없음
- [ ] `working_modify`(CC-WRK-04, 공개작업 수정 폼) 화면/메서드가 아직 없음 — 현재 `doUpdate`는 `complete/modify` 폼에서만 실질적으로 진입 가능
