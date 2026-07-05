package com.pcwk.ehr.artwork.service;
 
import java.util.List;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.mapper.ArtworkEntryMapper;
import com.pcwk.ehr.mapper.ArtworkMapper;
 
@Service
public class ArtworkService {
 
	Logger log = LogManager.getLogger(getClass());
 
	@Autowired
	private ArtworkMapper artworkMapper;
 
	/** 작업일지 조회 조합용 (읽기 목적). 작업일지 CRUD 로직은 ArtworkEntry 담당 몫. */
	@Autowired
	private ArtworkEntryMapper artworkEntryMapper;
 
	// TODO: 삭제 오케스트레이션(첨부/댓글/좋아요 수동삭제)용 - 팀원 Mapper 연동 시 주입
	// @Autowired private FileMapper    fileMapper;
	// @Autowired private CommentMapper commentMapper;
	// @Autowired private LikeMapper    likeMapper;
 
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
	 * 삭제.
	 * ※ artwork_entry 는 FK CASCADE 자동삭제.
	 *   첨부/댓글/좋아요는 polymorphic(FK 없음) → 팀원 Mapper 연동 후 수동삭제 필요.
	 *   (연동 전까지는 artwork 만 삭제)
	 */
	@Transactional
	public int doDelete(ArtworkVO param) {
		// TODO 삭제 순서(팀원 Mapper 주입 후 활성화):
		// 1. (entry 댓글) commentMapper.deleteByTarget('ARTWORK_ENTRY', 각 entryId)
		// 2. 첨부  fileMapper.deleteByTarget('ARTWORK', artworkId)
		// 3. 댓글  commentMapper.deleteByTarget('ARTWORK', artworkId)
		// 4. 좋아요 likeMapper.deleteByTarget('ARTWORK', artworkId)
		// 5. 작품  artworkMapper.doDelete(param)  ← entry 는 여기서 CASCADE
		return artworkMapper.doDelete(param);
	}
 
	// ========================= 조회수 (개별 호출용) =========================
 
	/** 조회수 증가만 별도로 필요할 때 */
	@Transactional
	public int addViewCount(ArtworkVO param) {
		return artworkMapper.updateViewCount(param);
	}
}