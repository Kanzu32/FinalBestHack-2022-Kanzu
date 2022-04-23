package com.Coders_seem_to_be.FinalBestHack2.service;

import com.Coders_seem_to_be.FinalBestHack2.entity.Station;
import com.Coders_seem_to_be.FinalBestHack2.repository.StationRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
	public List<Station> parseFromSOAP(MultipartFile file) {
		return null;
	}

	@Override
	public List<Station> parseFromXML(MultipartFile file) {
		return null;
	}

	@Override
	public List<Station> parseFromCSV(MultipartFile file) {
		return null;
	}

	@Override
	public List<Station> parseFromDB(MultipartFile file) {
		return null;
	}
}
