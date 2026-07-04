package com.pcwk.ehr.file.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.pcwk.ehr.cmn.FileManager;
import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.file.domain.FileVO;
import com.pcwk.ehr.file.service.FileService;

@Controller
@RequestMapping("/file")
public class FileController {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private FileService fileService;

	@Autowired
	private FileManager fileManager;

	public FileController() {
		log.debug("FileController");
	}

	// file_up — multipart: files + targetType, targetId, memberId
	@PostMapping("/upload.do")
	@ResponseBody
	public MessageVO upload(
			@RequestParam("files") MultipartFile[] files,
			FileVO param) {
		log.debug("upload param: " + param);
		try {
			List<FileVO> saved = fileService.upload(files, param);
			return new MessageVO("200", "업로드 성공", saved);
		} catch (IllegalArgumentException e) {
			log.debug("upload fail: " + e.getMessage());
			return new MessageVO("400", e.getMessage());
		} catch (IOException e) {
			log.error("upload io error", e);
			return new MessageVO("500", "파일 저장에 실패했습니다.");
		}
	}

	// 대상별 첨부 목록 (sort_no 순)
	@PostMapping("/doRetrieve.do")
	@ResponseBody
	public MessageVO doRetrieve(FileVO param) {
		log.debug("doRetrieve param: " + param);
		return new MessageVO("200", "조회 성공", fileService.selectByTarget(param));
	}

	// file_rep — 대표 지정 (1번 슬롯)
	@PostMapping("/setRep.do")
	@ResponseBody
	public MessageVO setRep(FileVO param) {
		log.debug("setRep param: " + param);
		int flag = fileService.setRep(param);
		if (flag == 1) {
			return new MessageVO("200", "대표 이미지 지정 성공");
		}
		return new MessageVO("400", "대표 이미지 지정 실패(권한 없음 또는 데이터 없음)");
	}

	// file_del
	@PostMapping("/remove.do")
	@ResponseBody
	public MessageVO remove(FileVO param) {
		log.debug("remove param: " + param);
		try {
			int flag = fileService.remove(param);
			if (flag == 1) {
				return new MessageVO("200", "파일 삭제 성공");
			}
			return new MessageVO("400", "파일 삭제 실패(권한 없음 또는 데이터 없음)");
		} catch (IOException e) {
			log.error("remove io error", e);
			return new MessageVO("500", "파일 삭제에 실패했습니다.");
		}
	}

	// file_down — fileId 로 원본 다운로드
	@GetMapping("/download.do")
	public void download(
			@RequestParam(name = "fileId") int fileId,
			HttpServletResponse response) throws IOException {
		log.debug("download fileId: " + fileId);

		FileVO param = new FileVO();
		param.setFileId(fileId);
		FileVO file = fileService.getFile(param);
		if (file == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		try {
			Path path = fileManager.resolveAbsolutePath(file.getFilePath(), file.getSaveFileNm());
			if (!Files.isRegularFile(path)) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			String orgFileNm = file.getOrgFileNm() != null ? file.getOrgFileNm() : file.getSaveFileNm();
			String encoded = URLEncoder.encode(orgFileNm, StandardCharsets.UTF_8.name()).replace("+", "%20");

			if (file.getMimeType() != null && !file.getMimeType().isEmpty()) {
				response.setContentType(file.getMimeType());
			} else {
				response.setContentType("application/octet-stream");
			}
			response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
			response.setContentLengthLong(Files.size(path));

			try (InputStream in = Files.newInputStream(path);
					OutputStream out = response.getOutputStream()) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
			}
		} catch (IllegalArgumentException e) {
			log.warn("download blocked — invalid path for fileId={}", fileId);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
