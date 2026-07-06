package com.pcwk.ehr.file.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.pcwk.ehr.cmn.TargetType;
import com.pcwk.ehr.file.domain.FileVO;
import com.pcwk.ehr.mapper.FileMapper;

// attach_file(첨부파일) 매퍼 테스트 — 첨부 등록/조회/대표지정/슬롯정리/삭제.
// 테스트용 대상(ARTWORK, targetId=99999)의 첨부만 비우고 검증한다.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class FileMapperJUnit {

	private static final Logger log = LogManager.getLogger(FileMapperJUnit.class);

	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private FileMapper fileMapper;

	@BeforeEach
	public void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(fileMapper);
		cleanupByTarget();
	}

	@AfterEach
	public void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
		cleanupByTarget();
	}

	// 대상 스코프 전건 삭제(격리 초기화)
	private void cleanupByTarget() {
		fileMapper.deleteByTarget(targetKey());
	}

	// target_type + target_id 만 채운 대상키
	private FileVO targetKey() {
		FileVO key = new FileVO();
		key.setTargetType(TEST_TARGET_TYPE);
		key.setTargetId(TEST_TARGET_ID);
		return key;
	}

	// FileVO 템플릿(대상 스코프 고정, sortNo·isRep·saveFileNm 만 가변)
	private FileVO newTemplate(int sortNo, String isRep, String saveFileNm) {
		FileVO vo = new FileVO();
		vo.setMemberId(TEST_MEMBER_ID);
		vo.setTargetType(TEST_TARGET_TYPE);
		vo.setTargetId(TEST_TARGET_ID);
		vo.setOrgFileNm("junit-test.jpg");
		vo.setSaveFileNm(saveFileNm);
		vo.setFilePath("ARTWORK/" + TEST_TARGET_ID);
		vo.setMimeType("image/jpeg");
		vo.setSortNo(sortNo);
		vo.setIsRep(isRep);
		return vo;
	}

	// 단건 등록 후 발급된 fileId 반환
	private int saveFile(int sortNo, String isRep, String saveFileNm) {
		FileVO vo = newTemplate(sortNo, isRep, saveFileNm);
		int flag = fileMapper.doSave(vo);
		assertEquals(1, flag);
		assertTrue(vo.getFileId() > 0);
		return vo.getFileId();
	}

	//@Disabled
	@Test
	public void beans() {
		log.debug("---------------------------");
		log.debug("*beans()*");
		log.debug("---------------------------");
		assertNotNull(fileMapper);
	}

	/** 1. 등록 — file_id 발급(>0) */
	//@Disabled
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 단건 등록(flag=1, fileId>0)
		//3. 대상 건수 1건 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 단건 등록");
		FileVO vo = newTemplate(1, "Y", "junit-save-1.jpg");
		int flag = fileMapper.doSave(vo);
		log.debug("doSave flag={}, fileId={}", flag, vo.getFileId());
		assertEquals(1, flag);
		assertTrue(vo.getFileId() > 0);

		//3
		log.debug("3. 대상 건수 1건 확인");
		assertEquals(1, fileMapper.countByTarget(targetKey()));
	}

	/** 2. 단건 조회 */
	//@Disabled
	@Test
	public void doSelectOne() {
		log.debug("---------------------------");
		log.debug("*doSelectOne()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 단건 등록
		//3. file_id 로 조회 및 비교

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 단건 등록");
		int fileId = saveFile(1, "Y", "junit-select-1.jpg");

		//3
		log.debug("3. file_id 로 조회 및 비교");
		FileVO param = new FileVO();
		param.setFileId(fileId);
		FileVO outVO = fileMapper.doSelectOne(param);
		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(fileId, outVO.getFileId());
		assertEquals("junit-test.jpg", outVO.getOrgFileNm());
	}

	/** 3. 대상별 목록 (sort_no 순) */
	//@Disabled
	@Test
	public void selectByTarget() {
		log.debug("---------------------------");
		log.debug("*selectByTarget()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 2건 등록(sort 1, 2)
		//3. 목록 조회 및 sort 순 비교

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 2건 등록(sort 1, 2)");
		saveFile(1, "Y", "junit-list-1.jpg");
		saveFile(2, "N", "junit-list-2.jpg");

		//3
		log.debug("3. 목록 조회 및 sort 순 비교");
		List<FileVO> list = fileMapper.selectByTarget(targetKey());
		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(1, list.get(0).getSortNo());
		assertEquals(2, list.get(1).getSortNo());
	}

	/** 4. 대상별 첨부 수 */
	//@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 단건 등록
		//3. count=1 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 단건 등록");
		saveFile(1, "Y", "junit-count-1.jpg");

		//3
		log.debug("3. count=1 확인");
		int cnt = fileMapper.countByTarget(targetKey());
		log.debug("count={}", cnt);
		assertEquals(1, cnt);
	}

	/** 5. 대표 지정 (sort_no=1, is_rep=Y) */
	//@Disabled
	@Test
	public void updateRep() {
		log.debug("---------------------------");
		log.debug("*updateRep()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 비대표(sort 2) 단건 등록
		//3. updateRep 후 sort_no=1·is_rep=Y 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 비대표(sort 2) 단건 등록");
		int fileId = saveFile(2, "N", "junit-rep-2.jpg");

		//3
		log.debug("3. updateRep 후 sort_no=1·is_rep=Y 확인");
		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = fileMapper.updateRep(param);
		assertEquals(1, flag);

		FileVO outVO = fileMapper.doSelectOne(param);
		assertNotNull(outVO);
		assertEquals(1, outVO.getSortNo());
		assertEquals("Y", outVO.getIsRep());
	}

	/** 6. 슬롯·대표 동시 변경 */
	//@Disabled
	@Test
	public void updateSortAndRep() {
		log.debug("---------------------------");
		log.debug("*updateSortAndRep()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 비대표(sort 2) 단건 등록
		//3. updateSortAndRep(sort 1·Y) 후 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 비대표(sort 2) 단건 등록");
		int fileId = saveFile(2, "N", "junit-sort-2.jpg");

		//3
		log.debug("3. updateSortAndRep(sort 1·Y) 후 확인");
		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setSortNo(1);
		param.setIsRep("Y");
		int flag = fileMapper.updateSortAndRep(param);
		assertEquals(1, flag);

		FileVO outVO = fileMapper.doSelectOne(param);
		assertNotNull(outVO);
		assertEquals(1, outVO.getSortNo());
		assertEquals("Y", outVO.getIsRep());
	}

	/** 7. 슬롯별 조회 */
	//@Disabled
	@Test
	public void selectBySortNo() {
		log.debug("---------------------------");
		log.debug("*selectBySortNo()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 1번 슬롯 단건 등록
		//3. target + sort_no 로 조회 및 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 1번 슬롯 단건 등록");
		saveFile(1, "Y", "junit-slot-1.jpg");

		//3
		log.debug("3. target + sort_no 로 조회 및 확인");
		FileVO param = targetKey();
		param.setSortNo(1);
		FileVO outVO = fileMapper.selectBySortNo(param);
		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(1, outVO.getSortNo());
	}

	/** 8. 슬롯 번호 -1 (비대표 슬롯 삭제 후) */
	//@Disabled
	@Test
	public void decrementSortNoAfter() {
		log.debug("---------------------------");
		log.debug("*decrementSortNoAfter()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 3건 등록(sort 1,2,3) 후 2번 삭제
		//3. decrementSortNoAfter(2) 후 3번이 2번으로 당겨졌는지 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 3건 등록(sort 1,2,3) 후 2번 삭제");
		saveFile(1, "Y", "junit-dec-1.jpg");
		int fileId2 = saveFile(2, "N", "junit-dec-2.jpg");
		int fileId3 = saveFile(3, "N", "junit-dec-3.jpg");

		FileVO del = new FileVO();
		del.setFileId(fileId2);
		del.setMemberId(TEST_MEMBER_ID);
		fileMapper.doDelete(del);

		//3
		log.debug("3. decrementSortNoAfter(2) 후 3번이 2번으로 당겨졌는지 확인");
		FileVO param = targetKey();
		param.setSortNo(2);
		int flag = fileMapper.decrementSortNoAfter(param);
		assertTrue(flag >= 1);

		FileVO check = new FileVO();
		check.setFileId(fileId3);
		FileVO moved = fileMapper.doSelectOne(check);
		assertNotNull(moved);
		assertEquals(2, moved.getSortNo());
	}

	/** 9. 삭제 (본인) */
	//@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 단건 등록
		//3. doDelete 후 조회 시 null 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 단건 등록");
		int fileId = saveFile(1, "Y", "junit-del-1.jpg");

		//3
		log.debug("3. doDelete 후 조회 시 null 확인");
		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = fileMapper.doDelete(param);
		assertEquals(1, flag);

		FileVO outVO = fileMapper.doSelectOne(param);
		assertNull(outVO);
	}

	/** 10. 대상별 일괄 삭제 */
	//@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 단건 등록 후 1건 확인
		//3. deleteByTarget 후 0건 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 단건 등록 후 1건 확인");
		saveFile(1, "Y", "junit-bulk-1.jpg");
		assertEquals(1, fileMapper.countByTarget(targetKey()));

		//3
		log.debug("3. deleteByTarget 후 0건 확인");
		int flag = fileMapper.deleteByTarget(targetKey());
		assertTrue(flag >= 1);
		assertEquals(0, fileMapper.countByTarget(targetKey()));
	}
}
