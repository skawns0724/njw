package com.kosa.myapp;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UploadFileController {
	
	@Autowired
	IUploadFileService uploadFileService;
	
	static final Logger logger = Logger.getLogger(UploadFileController.class);
	
	@RequestMapping(value="/upload/new", method=RequestMethod.GET)
	public String uploadFile(Model model) {
		model.addAttribute("dir", "/");
		return "upload/form";
	}
	
	@RequestMapping(value="/upload/new", method=RequestMethod.POST)
	public String uploadFile(@RequestParam(value="dir", required=false, defaultValue="/") String dir, 
							 @RequestParam MultipartFile file, 
							 RedirectAttributes redirectAttrs) {
		logger.info(file.getOriginalFilename());
		try {
			if(file!=null && !file.isEmpty()) {
				UploadFileVO newFile = new UploadFileVO();
				newFile.setDirectoryName(dir);
				newFile.setFileName(file.getOriginalFilename());
				newFile.setFileSize(file.getSize());
				newFile.setFileContentType(file.getContentType());
				newFile.setFileData(file.getBytes());
				uploadFileService.uploadFile(newFile);
			}
		}catch(Exception e) {
			logger.error(e.getMessage());
			redirectAttrs.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/upload/list";
	}
	
	@RequestMapping("/upload/list")
	public String getFileList(Model model) {
		model.addAttribute("fileList", uploadFileService.getAllFileList());
		return "upload/list";
	}
	
//	다운로드
	@RequestMapping("/pds/{fileId}")
	public ResponseEntity<byte[]> getBinaryFile(@PathVariable int fileId){
		UploadFileVO file = uploadFileService.getFile(fileId);
		HttpHeaders headers = new HttpHeaders();
		if(file != null) {
			String[] mtypes = file.getFileContentType().split("/");
			headers.setContentType(new MediaType(mtypes[0], mtypes[1]));
			headers.setContentLength(file.getFileSize());
			headers.setContentDispositionFormData("attachment",	file.getFileName());
			return new ResponseEntity<byte[]>(file.getFileData(), headers, HttpStatus.OK);
		}else {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND); //404에러
		}
		
	}
	
	// 삭제
	@RequestMapping("/upload/delete/{fileId}")
	public String deleteFile(@PathVariable int fileId) {
		uploadFileService.deleteFile(fileId);
		return "redirect:/upload/list";      // 삭제 후 list로 다시 돌아가게함
	}
	
}







