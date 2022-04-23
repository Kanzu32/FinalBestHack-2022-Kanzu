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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
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
	public void parseFromXML(MultipartFile file) throws IOException, XMLStreamException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(convertMultiPartToFile(file)));
		Station station = new Station();
		while (reader.hasNext()) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				StartElement startElement = nextEvent.asStartElement();
				switch (startElement.getName().getLocalPart()) {
					case "address":
						station = new Station();
						nextEvent = reader.nextEvent();
						if (!nextEvent.isCharacters()) {
							station.setAddress("-");
						} else {
							station.setAddress(nextEvent.asCharacters().getData());
						}
						break;
					case "latitude":
						nextEvent = reader.nextEvent();
						if (!nextEvent.isCharacters()) {
							station.setLatitude("-");
						} else {
							station.setLatitude(nextEvent.asCharacters().getData());
						}

						break;
					case "longtitude":
						nextEvent = reader.nextEvent();
						if (!nextEvent.isCharacters()) {
							station.setLongtitude("-");
						} else {
							station.setLongtitude(nextEvent.asCharacters().getData());
						}
						break;
					case "name":
						nextEvent = reader.nextEvent();
						station.setName(nextEvent.asCharacters().getData());
						break;
					case "country":
						nextEvent = reader.nextEvent();
						if (!nextEvent.isCharacters()) {
							station.setCountry("-");
						} else {
							station.setCountry(nextEvent.asCharacters().getData());
						}
						break;
					case "phone":
						nextEvent = reader.nextEvent();
						if (!nextEvent.isCharacters()) {
							station.setPhone("-");
						} else {
							station.setPhone(nextEvent.asCharacters().getData());
						}
						break;
					case "region":
						nextEvent = reader.nextEvent();
						if (!nextEvent.isCharacters()) {
							station.setRegion("-");
						} else {
							station.setRegion(nextEvent.asCharacters().getData());
						}
						if (!stationRepository.existsById(station.getName())) {
							stationRepository.save(station);
						}
						break;
				
				}
			}
//			if (nextEvent.isEndElement()) {
//				EndElement endElement = nextEvent.asEndElement();
//				if (endElement.getName().getLocalPart().equals("website")) {
//					websites.add(website);
//				}
//			}
		}
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
