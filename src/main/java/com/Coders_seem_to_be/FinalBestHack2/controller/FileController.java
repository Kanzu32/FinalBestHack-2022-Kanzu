package com.Coders_seem_to_be.FinalBestHack2.controller;

import com.Coders_seem_to_be.FinalBestHack2.entity.Station;
import com.Coders_seem_to_be.FinalBestHack2.service.ConvertService;
import com.Coders_seem_to_be.FinalBestHack2.service.ConvertServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

@RestController
public class FileController {
	public static final String PATH_TO_PROPERTIES = "src/main/resources/application.properties";

	@Value("${in_format}")
	private String format;

	@Autowired
	private ConvertService convertService;

	public void getFrom(String path) {

//		if (format.toLowerCase() == "json") {
//			try {
//				convertService.parseFromJSON();
//			} catch (){}
//		}



	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		String rawType = file.getContentType();
		String type = rawType.substring(rawType.lastIndexOf("/")+1);
		if (!type.equals(format)) {
			throw new IOException("Формат полученного файла не совпадает с форматом, указанным в настройках.");
		} else {
			convertService.parseFromJSON(file);
		}
		return file.getContentType();

	}

}
