package com.Coders_seem_to_be.FinalBestHack2.controller;

import com.Coders_seem_to_be.FinalBestHack2.service.ConvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

@RestController
public class FileController {

	@Value("${in_format}")
	private String format;

	@Autowired
	private ConvertService convertService;

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
		String rawType = file.getContentType();
		String type = rawType.substring(rawType.lastIndexOf("/")+1);
		if (!type.equals(format)) {
			throw new IOException("Формат полученного файла не совпадает с форматом, указанным в настройках.");
		} else if (format.equals("json")) {
			convertService.parseFromJSON(file);
		} else if (format.equals("csv")){
			convertService.parseFromCSV(file);
		} else if (format.equals("xml") || format.equals("soap")){
			convertService.parseFromXML(file);
		} else {
			throw new IOException("Данный формат не поддерживается.");
		}
		return file.getContentType();

	}

}
