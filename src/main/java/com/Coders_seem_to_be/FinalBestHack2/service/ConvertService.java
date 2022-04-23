package com.Coders_seem_to_be.FinalBestHack2.service;

import com.Coders_seem_to_be.FinalBestHack2.entity.Station;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ConvertService {
	void parseFromJSON(MultipartFile file) throws IOException;
	List<Station> parseFromSOAP(MultipartFile file);
	List<Station> parseFromXML(MultipartFile file);
	List<Station> parseFromCSV(MultipartFile file);
	List<Station> parseFromDB(MultipartFile file);

}
