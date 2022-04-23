package com.Coders_seem_to_be.FinalBestHack2.service;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.Coders_seem_to_be.FinalBestHack2.entity.Station;
import com.Coders_seem_to_be.FinalBestHack2.repository.StationRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConvertServiceImpl implements ConvertService{

	@Autowired
	StationRepository stationRepository;

	@Override
	public void parseFromJSON(MultipartFile file) throws IOException {
		Gson gson = new Gson();
		Type userListType = new TypeToken<ArrayList<Station>>(){}.getType();
		String str = new String(file.getBytes());
		List<Station> stationListJSON = gson.fromJson(str, userListType);
		for (Station station : stationListJSON) {
			if (!stationRepository.existsById(station.getName())) {
				stationRepository.save(station);
			}
		}

	}

	@Override
	public void parseFromSOAP(MultipartFile file) {

	}

	@Override
	public void parseFromXML(MultipartFile file) {
	}

	@Override
	public void parseFromCSV(MultipartFile file) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(convertMultiPartToFile(file)), '|', '"', 1);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (!stationRepository.existsById(nextLine[3])) {
				Station station = new Station();
				station.setAddress(nextLine[0]);
				station.setLatitude(nextLine[1]);
				station.setLongtitude(nextLine[2]);
				station.setName(nextLine[3]);
				station.setCountry(nextLine[4]);
				station.setPhone(nextLine[5]);
				station.setRegion(nextLine[6]);
				stationRepository.save(station);
			}
		}

	}

	@Override
	public void parseFromDB(MultipartFile file) {
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File( file.getOriginalFilename() );
		FileOutputStream fos = new FileOutputStream( convFile );
		fos.write( file.getBytes() );
		fos.close();
		return convFile;
	}
}
