package com.pcwk.ehr.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

// file 서비스(FileService) 테스트 — DB 만 다루는 getFile/selectByTarget/setRep 만 검증한다.
// upload/remove 는 실제 파일을 디스크에 저장·삭제까지 해서 이 테스트에선 다루지 않는다.
// 데이터는 매퍼로 직접 넣어(디스크 우회) 준비한다.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class FileServiceJUnit {

	private static final Logger log = LogManager.getLogger(FileServiceJUnit.class);

	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private FileService fileService;

	// 초기화·시드·검증은 매퍼로 직접(디스크 우회)
	@Autowired
	private FileMapper fileMapper;

	@BeforeEach
	public void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(fileService);
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

	// 대상 스코프 전건 삭제(격리 초기화, DB만)
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

	// 매퍼로 직접 시드(디스크 우회) 후 발급된 fileId 반환
	private int seedFile(int sortNo, String isRep, String saveFileNm) {
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
		assertNotNull(fileService);
		assertNotNull(fileMapper);
	}

	/** getFile — file_id 로 단건 조회 */
	//@Disabled
	@Test
	public void getFile() {
		log.debug("---------------------------");
		log.debug("*getFile()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 매퍼로 단건 시드
		//3. service.getFile(fileId) → not null, fileId 일치

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 매퍼로 단건 시드");
		int fileId = seedFile(1, "Y", "junit-svc-get-1.jpg");

		//3
		log.debug("3. service.getFile(fileId) → not null, fileId 일치");
		FileVO param = new FileVO();
		param.setFileId(fileId);
		FileVO outVO = fileService.getFile(param);
		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(fileId, outVO.getFileId());
	}

	/** selectByTarget — 대상별 목록 */
	//@Disabled
	@Test
	public void selectByTarget() {
		log.debug("---------------------------");
		log.debug("*selectByTarget()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 매퍼로 2건 시드(sort 1, 2)
		//3. service.selectByTarget → size 2

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 매퍼로 2건 시드(sort 1, 2)");
		seedFile(1, "Y", "junit-svc-list-1.jpg");
		seedFile(2, "N", "junit-svc-list-2.jpg");

		//3
		log.debug("3. service.selectByTarget → size 2");
		List<FileVO> list = fileService.selectByTarget(targetKey());
		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertEquals(2, list.size());
	}

	/** setRep — 지정 파일을 대표(sort_no=1, is_rep=Y)로 승격 */
	//@Disabled
	@Test
	public void setRep() {
		log.debug("---------------------------");
		log.debug("*setRep()*");
		log.debug("---------------------------");
		//1. 대상 초기화 후 0건 확인
		//2. 매퍼로 비대표(sort 2) 단건 시드
		//3. service.setRep(fileId+memberId) → >=0
		//4. 매퍼 doSelectOne 으로 sort_no=1·is_rep=Y 확인

		//1
		log.debug("1. 대상 초기화 후 0건 확인");
		assertEquals(0, fileMapper.countByTarget(targetKey()));

		//2
		log.debug("2. 매퍼로 비대표(sort 2) 단건 시드");
		int fileId = seedFile(2, "N", "junit-svc-rep-2.jpg");

		//3
		log.debug("3. service.setRep(fileId+memberId) → >=0");
		FileVO param = new FileVO();
		param.setFileId(fileId);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = fileService.setRep(param);
		log.debug("setRep flag={}", flag);
		assertTrue(flag >= 0);

		//4
		log.debug("4. 매퍼 doSelectOne 으로 sort_no=1·is_rep=Y 확인");
		FileVO check = new FileVO();
		check.setFileId(fileId);
		FileVO outVO = fileMapper.doSelectOne(check);
		assertNotNull(outVO);
		assertEquals(1, outVO.getSortNo());
		assertEquals("Y", outVO.getIsRep());
	}
}
