package com.pcwk.ehr.cmn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.pcwk.ehr.file.domain.FileVO;

<<<<<<< Updated upstream
/**
 * 첨부파일 물리 저장/삭제 유틸.
 * DB 메타는 FileService + FileMapper 담당.
 */
=======
// 디스크(물리 파일) 전담 — DB attach_file 은 FileService + FileMapper 가 처리
>>>>>>> Stashed changes
@Component
public class FileManager {

	private static final Logger log = LogManager.getLogger(FileManager.class);

<<<<<<< Updated upstream
	private static final List<String> ALLOWED_EXT = Arrays.asList("jpg", "jpeg", "png", "webp");

=======
	// 허용 확장자 (이미지만)
	private static final List<String> ALLOWED_EXT = Arrays.asList("jpg", "jpeg", "png", "webp");

	// upload.properties / upload.local.properties
>>>>>>> Stashed changes
	@Value("${upload.root.path}")
	private String uploadRootPath;

	@Value("${upload.max.size}")
	private long maxFileSize;

<<<<<<< Updated upstream
	/**
	 * 디스크에 저장하고 DB insert용 메타를 채운 FileVO 반환.
	 * fileId, memberId, isRep, sortNo는 Service에서 설정.
	 */
	public FileVO save(MultipartFile file, TargetType targetType, int targetId) throws IOException {
		// 파일 유효성 검사 메서드 
		validate(file);

		// 파일 이름 추출
		String orgFileNm = FilenameUtils.getName(file.getOriginalFilename());
		// 파일 확장자 추출
		String ext = resolveExtension(orgFileNm, file.getContentType());
		// 파일 이름 생성 UUID 사용
		String saveFileNm = UUID.randomUUID().toString().replace("-", "") + "." + ext;
		// 파일 저장 경로 생성
		String relativeDir = targetType.name() + "/" + targetId;

		Path dir = Paths.get(uploadRootPath, relativeDir);
		// 파일 저장 경로 생성, 예시 : targettype = artwork, targetId = 1, 경로 : D:/upload/bit_gongbang/artwork/1
		Files.createDirectories(dir);
		// 파일 저장 경로 생성, 예시 : targettype = artwork, targetId = 1, 경로 : D:/upload/bit_gongbang/artwork/1/1.jpg
		Path dest = dir.resolve(saveFileNm);
		// 위에서 만든 경로에 파일을 복사해서 저장 
=======
	// 디스크 저장 → DB insert 용 FileVO 메타 반환 (fileId·memberId·isRep·sortNo 는 FileService 가 설정)
	public FileVO save(MultipartFile file, TargetType targetType, int targetId) throws IOException {
		validate(file);

		String orgFileNm = FilenameUtils.getName(file.getOriginalFilename());
		String ext = resolveExtension(orgFileNm, file.getContentType());
		// 저장 파일명은 UUID — 원본명 그대로 쓰면 중복·보안 이슈
		String saveFileNm = UUID.randomUUID().toString().replace("-", "") + "." + ext;
		// 상대 경로: ARTWORK/42 또는 ARTWORK_ENTRY/7
		String relativeDir = targetType.name() + "/" + targetId;

		Path dir = Paths.get(uploadRootPath, relativeDir);
		Files.createDirectories(dir);
		Path dest = dir.resolve(saveFileNm);
>>>>>>> Stashed changes
		file.transferTo(dest.toFile());

		FileVO vo = new FileVO();
		vo.setTargetType(targetType);
		vo.setTargetId(targetId);
		vo.setOrgFileNm(orgFileNm);
		vo.setSaveFileNm(saveFileNm);
		vo.setFilePath(relativeDir.replace('\\', '/'));
		vo.setMimeType(file.getContentType());

		log.debug("saved file: {}", dest);
		return vo;
	}

<<<<<<< Updated upstream
	/** 물리 파일 삭제 (없으면 무시) */
=======
	// DB 레코드 삭제 전/후 호출 — 실제 파일만 지움 (없으면 무시)
>>>>>>> Stashed changes
	public void deletePhysical(FileVO vo) throws IOException {
		if (vo == null || vo.getSaveFileNm() == null || vo.getSaveFileNm().isEmpty()) {
			return;
		}
		Path path = resolveAbsolutePath(vo.getFilePath(), vo.getSaveFileNm());
		if (Files.deleteIfExists(path)) {
			log.debug("deleted file: {}", path);
		}
	}

<<<<<<< Updated upstream
	/** 다운로드용 절대 경로 */
=======
	// 다운로드·삭제 시 — upload.root.path + file_path + save_file_nm
>>>>>>> Stashed changes
	public Path resolveAbsolutePath(String filePath, String saveFileNm) {
		return Paths.get(uploadRootPath, filePath, saveFileNm).normalize();
	}

<<<<<<< Updated upstream
    // 파일 유효성 검사 메서드
=======
	// 빈 파일·5MB 초과·허용 확장자 아님 → IllegalArgumentException
>>>>>>> Stashed changes
	public void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}
		if (file.getSize() > maxFileSize) {
			throw new IllegalArgumentException("파일 크기는 최대 5MB까지 가능합니다.");
		}

		String orgFileNm = file.getOriginalFilename();
		if (orgFileNm == null || orgFileNm.trim().isEmpty()) {
			throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
		}

		String ext = resolveExtension(FilenameUtils.getName(orgFileNm), file.getContentType());
		if (!ALLOWED_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException("이미지 파일(jpg, png, webp)만 업로드할 수 있습니다.");
		}
	}

<<<<<<< Updated upstream
=======
	// 확장자: 파일명 우선, 없으면 Content-Type 으로 추론
>>>>>>> Stashed changes
	private String resolveExtension(String orgFileNm, String contentType) {
		String ext = FilenameUtils.getExtension(orgFileNm);
		if (ext != null && !ext.isEmpty()) {
			return ext.toLowerCase(Locale.ROOT);
		}
		if (contentType == null) {
			return "";
		}
		switch (contentType.toLowerCase(Locale.ROOT)) {
		case "image/jpeg":
			return "jpg";
		case "image/png":
			return "png";
		case "image/webp":
			return "webp";
		default:
			return "";
		}
	}
}
