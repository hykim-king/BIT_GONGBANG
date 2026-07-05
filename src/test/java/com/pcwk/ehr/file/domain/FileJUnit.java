package com.pcwk.ehr.file.domain;

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
import com.pcwk.ehr.mapper.FileMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class FileJUnit {

	private static final Logger log = LogManager.getLogger(FileJUnit.class);

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

	private void cleanupByTarget() {
		FileVO key = targetKey();
		fileMapper.deleteByTarget(key);
	}

	private FileVO targetKey() {
		FileVO key = new FileVO();
		key.setTargetType(TEST_TARGET_TYPE);
		key.setTargetId(TEST_TARGET_ID);
		return key;
	}

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

	private int saveFile(int sortNo, String isRep, String saveFileNm) {
		FileVO vo = newTemplate(sortNo, isRep, saveFileNm);
		int flag = fileMapper.doSave(vo);
		assertEquals(1, flag);
		assertTrue(vo.getFileId() > 0);
		return vo.getFileId();
	}

	/** 1. 등록 */
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");

		FileVO vo = newTemplate(1, "Y", "junit-save-1.jpg");
		int flag = fileMapper.doSave(vo);

		log.debug("doSave flag={}, fileId={}", flag, vo.getFileId());
		assertEquals(1, flag);
		assertTrue(vo.getFileId() > 0);
	}

	/** 2. 단건 조회 */
	@Disabled
	@Test
	public void doSelectOne() {
		log.debug("---------------------------");
		log.debug("*doSelectOne()*");
		log.debug("---------------------------");
		int fileId = saveFile(1, "Y", "junit-select-1.jpg");

		FileVO param = new FileVO();
		param.setFileId(fileId);
		FileVO outVO = fileMapper.doSelectOne(param);

		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(fileId, outVO.getFileId());
		assertEquals("junit-test.jpg", outVO.getOrgFileNm());
	}

	/** 3. 대상별 목록 (sort_no 순) */
	@Disabled
	@Test
	public void selectByTarget() {
		log.debug("---------------------------");
		log.debug("*selectByTarget()*");
		log.debug("---------------------------");
		saveFile(1, "Y", "junit-list-1.jpg");
		saveFile(2, "N", "junit-list-2.jpg");

		List<FileVO> list = fileMapper.selectByTarget(targetKey());

		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(1, list.get(0).getSortNo());
		assertEquals(2, list.get(1).getSortNo());
	}

	/** 4. 대상별 첨부 수 */
	@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		saveFile(1, "Y", "junit-count-1.jpg");

		int cnt = fileMapper.countByTarget(targetKey());

		log.debug("count={}", cnt);
		assertEquals(1, cnt);
	}

	/** 5. 대표 지정 (sort_no=1, is_rep=Y) */
	@Disabled
	@Test
	public void updateRep() {
		log.debug("---------------------------");
		log.debug("*updateRep()*");
		log.debug("---------------------------");
		int fileId = saveFile(2, "N", "junit-rep-2.jpg");

		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = fileMapper.updateRep(param);
		assertEquals(1, flag);

		FileVO outVO = fileMapper.doSelectOne(param);
		assertEquals(1, outVO.getSortNo());
		assertEquals("Y", outVO.getIsRep());
	}

	/** 6. 슬롯·대표 동시 변경 */
	@Disabled
	@Test
	public void updateSortAndRep() {
		log.debug("---------------------------");
		log.debug("*updateSortAndRep()*");
		log.debug("---------------------------");
		int fileId = saveFile(2, "N", "junit-sort-2.jpg");

		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setSortNo(1);
		param.setIsRep("Y");
		int flag = fileMapper.updateSortAndRep(param);
		assertEquals(1, flag);

		FileVO outVO = fileMapper.doSelectOne(param);
		assertEquals(1, outVO.getSortNo());
		assertEquals("Y", outVO.getIsRep());
	}

	/** 7. 슬롯별 조회 */
	@Disabled
	@Test
	public void selectBySortNo() {
		log.debug("---------------------------");
		log.debug("*selectBySortNo()*");
		log.debug("---------------------------");
		saveFile(1, "Y", "junit-slot-1.jpg");

		FileVO param = targetKey();
		param.setSortNo(1);
		FileVO outVO = fileMapper.selectBySortNo(param);

		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(1, outVO.getSortNo());
	}

	/** 8. 슬롯 번호 -1 (비대표 슬롯 삭제 후) */
	@Disabled
	@Test
	public void decrementSortNoAfter() {
		log.debug("---------------------------");
		log.debug("*decrementSortNoAfter()*");
		log.debug("---------------------------");
		saveFile(1, "Y", "junit-dec-1.jpg");
		int fileId2 = saveFile(2, "N", "junit-dec-2.jpg");
		int fileId3 = saveFile(3, "N", "junit-dec-3.jpg");

		FileVO del = new FileVO();
		del.setFileId(fileId2);
		del.setMemberId(TEST_MEMBER_ID);
		fileMapper.doDelete(del);

		FileVO param = targetKey();
		param.setSortNo(2);
		int flag = fileMapper.decrementSortNoAfter(param);
		assertTrue(flag >= 1);

		FileVO check = new FileVO();
		check.setFileId(fileId3);
		FileVO moved = fileMapper.doSelectOne(check);
		assertEquals(2, moved.getSortNo());
	}

	/** 9. 삭제 (본인) */
	@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		int fileId = saveFile(1, "Y", "junit-del-1.jpg");

		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = fileMapper.doDelete(param);
		assertEquals(1, flag);

		FileVO outVO = fileMapper.doSelectOne(param);
		assertNull(outVO);
	}

	/** 10. 대상별 일괄 삭제 */
	@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		saveFile(1, "Y", "junit-bulk-1.jpg");

		int flag = fileMapper.deleteByTarget(targetKey());
		assertTrue(flag >= 1);
		assertEquals(0, fileMapper.countByTarget(targetKey()));
	}
}
