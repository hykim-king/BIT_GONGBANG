package com.pcwk.ehr.artworkentry.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.mapper.ArtworkEntryMapper;

/**
 * ArtworkEntryService - 작업일지(artwork_entry) 비즈니스 로직.
 * ArtworkService 와 동일 관례: 인터페이스/Impl 분리 없이 단일 @Service 구체 클래스.
 * SQL 은 Mapper 에 위임, 여기서는 트랜잭션 경계와 위임만 담당.
 *
 * [설계서 매핑 - CC-ENT-01 / M3-2 작업일지]
 *   entry_list   : doRetrieve  (작품별 목록, artwork_id 기준)
 *   entry_reg    : doSave      (등록)
 *   entry_modify : doUpdate    (수정)
 *   entry_del    : doDelete    (삭제)
 *
 * ※ 첨부파일(target_type=ARTWORK_ENTRY)은 attach_file 테이블 소관(다른 모듈)이라
 *   이 서비스에서는 다루지 않는다. (담당 팀원 Mapper 연동 후 별도 반영)
 */
@Service
public class ArtworkEntryService {

	Logger log = LogManager.getLogger(getClass()); // 클래스 전용 로거

	@Autowired
	private ArtworkEntryMapper artworkEntryMapper; // 작업일지 CRUD SQL 위임 대상

	public ArtworkEntryService() {
		log.debug("ArtworkEntryService"); // 빈 생성 확인용 로그
	}

	// ========================= 조회 =========================

	/** 목록 조회 : 특정 작품(artwork_id)의 작업일지 목록 (최신순) */
	public List<ArtworkEntryVO> doRetrieve(ArtworkEntryVO param) {
		return artworkEntryMapper.doRetrieve(param); // artwork_id 조건으로 목록 위임 조회
	}

	/** 상세 조회 : 작업일지 PK(artwork_entry) 단건 (수정/삭제 전 확인용) */
	public ArtworkEntryVO doSelectOne(ArtworkEntryVO param) {
		return artworkEntryMapper.doSelectOne(param); // PK 기준 단건 위임 조회
	}

	// ========================= 등록 =========================

	/** 등록 : selectKey(BEFORE) 로 PK 발급 후 저장, param.artworkEntry 에 새 PK 채워짐 */
	@Transactional
	public int doSave(ArtworkEntryVO param) {
		return artworkEntryMapper.doSave(param); // 등록 위임 (PK 발급 + INSERT)
	}

	// ========================= 수정 =========================

	/** 수정 : 내용(content) 변경 + 수정일(mod_dt) 갱신 */
	@Transactional
	public int doUpdate(ArtworkEntryVO param) {
		return artworkEntryMapper.doUpdate(param); // 내용/수정일 갱신 위임
	}

	// ========================= 삭제 =========================

	/**
	 * 삭제 : 작업일지 단건 삭제.
	 * ※ 상위 artwork 가 삭제되는 경우엔 FK ON DELETE CASCADE 로 자동 삭제되므로
	 *   이 메서드는 개별 작업일지 삭제(entry_del) 전용.
	 */
	@Transactional
	public int doDelete(ArtworkEntryVO param) {
		// TODO 첨부(attach_file, target_type=ARTWORK_ENTRY) 수동삭제 필요 - 담당 팀원 Mapper 연동 후 활성화
		return artworkEntryMapper.doDelete(param); // 단건 삭제 위임
	}
}
