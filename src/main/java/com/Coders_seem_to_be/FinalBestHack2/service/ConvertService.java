package com.Coders_seem_to_be.FinalBestHack2.service;

import com.Coders_seem_to_be.FinalBestHack2.entity.Station;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface ConvertService {
	void parseFromJSON(MultipartFile file) throws IOException;
	void parseFromSOAP(MultipartFile file);
	void parseFromXML(MultipartFile file) throws SAXException, IOException, ParserConfigurationException, XMLStreamException;
	void parseFromCSV(MultipartFile file) throws IOException;
	void parseFromDB(MultipartFile file);

}
