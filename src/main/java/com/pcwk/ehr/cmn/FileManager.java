package com.pcwk.ehr.cmn;

import java.io.File;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일 업로드/삭제 공용 유틸 (v2 M4 FIL).
 */
public class FileManager {

	private FileManager() {
	}

	public static String createSaveFileName(String originalFileName) {
		String ext = "";
		int dot = originalFileName.lastIndexOf('.');
		if (dot >= 0) {
			ext = originalFileName.substring(dot);
		}
		return UUID.randomUUID().toString().replace("-", "") + ext;
	}

	public static void upload(MultipartFile file, String saveDirectory, String saveFileName) throws Exception {
		File dir = new File(saveDirectory);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		file.transferTo(new File(dir, saveFileName));
	}

	public static void deletePhysicalFile(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return;
		}
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}

}
