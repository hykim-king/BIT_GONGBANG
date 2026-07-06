package com.pcwk.ehr.artwork.service;
 
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.cmn.TargetType;
import com.pcwk.ehr.comment.domain.CommentVO;
import com.pcwk.ehr.file.domain.FileVO;
import com.pcwk.ehr.file.service.FileService;
import com.pcwk.ehr.like.domain.LikeVO;
import com.pcwk.ehr.mapper.ArtworkEntryMapper;
import com.pcwk.ehr.mapper.ArtworkMapper;
import com.pcwk.ehr.mapper.CommentMapper;
import com.pcwk.ehr.mapper.LikeMapper;
 
@Service
public class ArtworkService {
 
	Logger log = LogManager.getLogger(getClass());
 
	@Autowired
	private ArtworkMapper artworkMapper;
 
	/** 작업일지 조회 조합용 (읽기 목적). 작업일지 CRUD 로직은 ArtworkEntry 담당 몫. */
	@Autowired
	private ArtworkEntryMapper artworkEntryMapper;

	// 삭제 오케스트레이션용 : polymorphic(target_type/target_id) 참조라 FK 가 없어 수동삭제 필요.
	// (각 Service/Mapper 의 deleteByTarget 을 '호출'만 함 - 해당 코드는 수정하지 않음)
	// 첨부는 fileService 사용 : DB 행 + 디스크 물리파일까지 정리 (IOException 발생 가능)
	@Autowired
	private FileService   fileService;   // 첨부(attach_file) - DB+디스크 삭제
	@Autowired
	private CommentMapper commentMapper; // 댓글(board_comment)
	@Autowired
	private LikeMapper    likeMapper;    // 좋아요(board_like)

	public ArtworkService() {
		log.debug("ArtworkService");
	}
 
	// ========================= 조회 =========================
 
	/**
	 * 목록 조회 (게시판 분기).
	 *   완성게시판   : param.isStatus='Y'
	 *   공개작업게시판 : param.isStatus=null (전체)
	 */
	public List<ArtworkVO> doRetrieve(ArtworkVO param) {
		return artworkMapper.doRetrieve(param);
	}
 
	/** 목록 총건수 (페이징용) */
	public int selectCount(ArtworkVO param) {
		return artworkMapper.selectCount(param);
	}
 
	/**
	 * 상세 조회 + 조회수 1 증가 (완성게시판 상세용).
	 */
	@Transactional
	public ArtworkVO doSelectOne(ArtworkVO param) {
		artworkMapper.updateViewCount(param);   // 조회수 +1
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
 
	/**
	 * 공개작업 상세 : 작품 + 작업일지 타임라인 조합 (조회수 +1).
	 * 조회수 증가 → 작품 조회 → 작업일지 목록을 VO(entryList)에 담아 반환.
	 * 조회수 증가와 두 조회가 한 트랜잭션.
	 */
	@Transactional
	public ArtworkVO viewWithEntry(ArtworkVO param) {
		artworkMapper.updateViewCount(param);                 // 조회수 +1
		ArtworkVO artwork = artworkMapper.doSelectOne(param); // 작품 본문
 
		ArtworkEntryVO entryParam = new ArtworkEntryVO();
		entryParam.setArtworkId(param.getArtworkId());
		List<ArtworkEntryVO> entryList = artworkEntryMapper.doRetrieve(entryParam); // 작업일지(최신순)
 
		if (artwork != null) {
			artwork.setEntryList(entryList);                  // VO 에 작업일지 담기
		}
		return artwork;
	}
 
	// ========================= 등록 =========================
 
	/**
	 * 등록.
	 * 완성 등록  : isStatus='Y' + compDt 세팅(Controller/여기서 결정)
	 * 공개작업 등록: isStatus='N' + compDt=null
	 * selectKey 로 등록 후 param.artworkId 에 새 PK 채워짐.
	 */
	@Transactional
	public int doSave(ArtworkVO param) {
		return artworkMapper.doSave(param);
	}
 
	// ========================= 수정 =========================
 
	/** 수정 (작성자/관리자 권한 체크는 Controller 인터셉터 또는 여기서 보강) */
	@Transactional
	public int doUpdate(ArtworkVO param) {
		return artworkMapper.doUpdate(param);
	}
 
	/** 완성 전환 : 공개작업('N') -> 완성('Y') + comp_dt=SYSDATE */
	@Transactional
	public int complete(ArtworkVO param) {
		return artworkMapper.updateStatus(param);
	}
 
	// ========================= 삭제 =========================
 
	/**
	 * 삭제 (오케스트레이션).
	 * artwork_entry(작업일지)는 FK CASCADE 로 자동삭제되지만,
	 * 첨부/댓글/좋아요는 polymorphic(target_type/target_id, FK 없음)이라 CASCADE 가 안 걸리므로
	 * artwork 를 지우기 '전에' 참조 데이터를 직접 지워 orphan(고아 데이터)을 방지한다.
	 *
	 * 삭제 순서(한 트랜잭션):
	 *   1. 각 작업일지(ARTWORK_ENTRY) 참조 첨부/댓글/좋아요 삭제 (entry 는 곧 CASCADE 로 사라지므로 미리 정리)
	 *   2. 작품(ARTWORK) 참조 첨부/댓글/좋아요 삭제
	 *   3. 작품 삭제        ← 이 시점에 남은 작업일지(artwork_entry)는 FK CASCADE 로 함께 삭제
	 *
	 * 첨부는 fileService 로 DB 행 + 디스크 물리파일까지 삭제하므로 IOException 이 전파될 수 있고,
	 * 그 경우 @Transactional 에 의해 DB 변경 전체가 롤백된다.
	 */
	@Transactional(rollbackFor = Exception.class)
	public int doDelete(ArtworkVO param) throws IOException {
		int artworkId = param.getArtworkId();

		// 1. 이 작품에 딸린 작업일지 목록을 먼저 조회 (곧 CASCADE 로 지워질 대상들)
		ArtworkEntryVO entryParam = new ArtworkEntryVO();
		entryParam.setArtworkId(artworkId);
		List<ArtworkEntryVO> entryList = artworkEntryMapper.doRetrieve(entryParam);

		// 2. 각 작업일지(ARTWORK_ENTRY)를 참조하는 첨부/댓글/좋아요를 먼저 삭제 (FK 없어 CASCADE 안 됨)
		for (ArtworkEntryVO entry : entryList) {
			deleteReferences(TargetType.ARTWORK_ENTRY, entry.getArtworkEntry());
		}

		// 3. 작품(ARTWORK) 본체를 참조하는 첨부/댓글/좋아요 삭제
		deleteReferences(TargetType.ARTWORK, artworkId);

		// 4. 작품 삭제 (남은 artwork_entry 는 여기서 FK CASCADE 로 자동삭제)
		return artworkMapper.doDelete(param);
	}

	/**
	 * 특정 대상(target_type + target_id)을 참조하는 첨부/댓글/좋아요를 일괄 삭제.
	 * 기존 Service/Mapper 의 deleteByTarget 을 호출만 한다(해당 코드는 수정하지 않음).
	 * ※ 첨부는 fileService.deleteByTarget 사용 → DB 행 + 디스크 물리파일까지 삭제(IOException 가능).
	 */
	private void deleteReferences(TargetType targetType, int targetId) throws IOException {
		// 첨부(attach_file) 삭제 : DB 행 + 디스크 물리파일
		FileVO fileParam = new FileVO();
		fileParam.setTargetType(targetType);
		fileParam.setTargetId(targetId);
		fileService.deleteByTarget(fileParam);

		// 댓글(board_comment) 삭제
		CommentVO commentParam = new CommentVO();
		commentParam.setTargetType(targetType);
		commentParam.setTargetId(targetId);
		commentMapper.deleteByTarget(commentParam);

		// 좋아요(board_like) 삭제
		LikeVO likeParam = new LikeVO();
		likeParam.setTargetType(targetType);
		likeParam.setTargetId(targetId);
		likeMapper.deleteByTarget(likeParam);
	}
 
	// ========================= 조회수 (개별 호출용) =========================
 
	/** 조회수 증가만 별도로 필요할 때 */
	@Transactional
	public int addViewCount(ArtworkVO param) {
		return artworkMapper.updateViewCount(param);
	}
}