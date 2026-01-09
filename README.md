# Java Backend Финал BEST Hack '22
# [RU] Команда "Кодеры вроде бы"
 - ### Овсянников Александр (капитан)
 - ### Воротынцев Антон
 - ### Строева Ольга
 - ### Козявин Максим
 - ### Сафронова Анастасия

## Технологии
* Java;
* Postman;
* PostgreSQL.

## Описание задачи
Сервис работает с данными о заправках - получает и отправляет их в заданном формате. Есть множество разных источников, начиная от `.csv` файлов и заканчивая базами данных. Поэтому нужно написать сервис на Spring Boot со стандартным ORM, который будет брать данные из нескольких различных источников, и будет их преобразовывать в один формат, а затем сохранять в собственную БД, без необходимости дополнительного преобразования кода сервиса (нужно вынести это в отдельный файл с настройками). Преобразование обязательно должно быть одноуровневое, то есть без вложенных сущностей. Так как будет множество самых разных данных, тестовые данные не предоставляются (однако нужно предусмотреть, чтобы в программу можно было передать разные данные). База данных не предоставляется.

## Решение
Наша команда разработала сервис, который позволяет агрегировать данные из файлов с раcширением `xml`, `csv`, `json`. Поддерживается формат протокола SOAP и архитектурный подход REST. В файле `application.properties` прописывается расширение передаваемого файла в параметре `in_format`, а так же данные для подключения к БД. Примеры:

```java
 in_format=xml
```

```java
 in_format=csv
```
 
```java
 in_format=xml
```

В базе данных одна таблица, состоящая из 8 полей. Используется СУБД PostgreSQL. Создать таблицу можно выполнив sql запрос:

```java
 CREATE TABLE IF NOT EXISTS public.table1
(
    name character varying COLLATE pg_catalog."default" NOT NULL,
    address character varying COLLATE pg_catalog."default" NOT NULL,
    latitude character varying COLLATE pg_catalog."default" NOT NULL,
    longtitude character varying COLLATE pg_catalog."default" NOT NULL,
    country character varying COLLATE pg_catalog."default" NOT NULL,
    phone character varying COLLATE pg_catalog."default" NOT NULL,
    region character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT "Table1_pkey" PRIMARY KEY (name)
)
```

Для отправки файла в сервис можно использовать программу Postman. (как отправить файл через Postman можно посмотреть [здесь](https://www.youtube.com/watch?v=sFlPa_Vow3w). После указания расширения файла необходимо отправить post-запрос с файлом. Сервис проверяет совпадает ли расширение полученного файла и расширения заданного в application.properties. Если расширения не совпадают, то выкидывается исключение "Данный формат не поддерживается.". В случае успешного сохранения сервис вернет сообщение "File added to the database.". Дальше происходит вызов конкретной функции для нужного формата. Функция обработки этой логики:

```java
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
```

Для обработки разных файлов используется `class ConvertServiceImpl`, он наследуется от интерфейса `ConvertService`, при внедрении других файлов требуется добавить функцию в этот интерфейс и реализовать её в классе `ConvertServiceImpl`.

```java
public interface ConvertService {
	void parseFromJSON(MultipartFile file) throws IOException;
	void parseFromXML(MultipartFile file) throws SAXException, IOException, ParserConfigurationException, XMLStreamException;
	void parseFromCSV(MultipartFile file) throws IOException;
}
```

### Обработка json файлов
Для обработки используется библиотека Gson. Из json'a данные преобразуются в коллекцию, далее производится проверка на дублирование данных по полю name в БД и ели таких данных нет, производится запись. Функция обработки json файлов:

```java
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
```

### Обработка csv файлов
Для обработки используется библиотека OpenCSV. Читаем файл построчно, разбивая на атрибуты для модели Station, проверяем по полю name есть ли такая запись в БД, если нет, то записываем в БД. Функция обработки:

```java
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
```

### Обработка xml файлов
Для обработки используется библиотека StAX. Считывание из файла происходит последовательно, перебираем все теги и ищем нужные для нашей модели данных, формируем сущность, проверяем на дублирование по полю name БД, если нет , то записываем в БД. Функция обработки:

```java
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
		}
	}
```

В корне есть тестовые файлы для проверки работы.

## [EN] Description of the task
The service works with data about gas stations - it receives and sends it in a given format. There are many different sources, ranging from `.csv` files to databases. Therefore, you need to write a service in Spring Boot with a standard ORM, which will take data from several different sources, and will convert it into one format, and then save it into its own database, without the need for additional conversion of the service code (you need to put this in a separate file with settings). The transformation must be single-level, that is, without nested entities. Since there will be a lot of different data, test data is not provided (however, provisions must be made so that different data can be passed into the program). No database provided.

## Problem solution
Our team has developed a service that allows you to aggregate data from files with the extensions `xml`, `csv`, `json`. The SOAP protocol format and the REST architectural approach are supported. The `application.properties` file specifies the extension of the transferred file in the `in_format` parameter, as well as the data for connecting to the database. Examples:

```java
 in_format=xml
```

```java
 in_format=csv
```
 
```java
 in_format=xml
```

The database has one table consisting of 8 fields. PostgreSQL DBMS is used. You can create a table by running an sql query:

```java
 CREATE TABLE IF NOT EXISTS public.table1
(
    name character varying COLLATE pg_catalog."default" NOT NULL,
    address character varying COLLATE pg_catalog."default" NOT NULL,
    latitude character varying COLLATE pg_catalog."default" NOT NULL,
    longtitude character varying COLLATE pg_catalog."default" NOT NULL,
    country character varying COLLATE pg_catalog."default" NOT NULL,
    phone character varying COLLATE pg_catalog."default" NOT NULL,
    region character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT "Table1_pkey" PRIMARY KEY (name)
)
```

To send a file to the service, you can use the Postman program. After specifying the file extension, you must send a post request with the file. The service checks whether the extension of the received file matches the extension specified in `application.properties`. If the extensions do not match, then the exception "This format is not supported." is thrown. If the save is successful, the service will return the message “File added to the database.”. Next, a specific function is called for the required format. The function to handle this logic is:

```java
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
```

To process different files, the `class ConvertServiceImpl` is used, it inherits from the `ConvertService` interface; when implementing other files, you need to add a function to this interface and implement it in the `ConvertServiceImpl` class.

```java
public interface ConvertService {
	void parseFromJSON(MultipartFile file) throws IOException;
	void parseFromXML(MultipartFile file) throws SAXException, IOException, ParserConfigurationException, XMLStreamException;
	void parseFromCSV(MultipartFile file) throws IOException;
}
```

### Processing json files
The Gson library is used for processing. From json, the data is converted into a collection, then a check is made for duplication of data in the name field in the database and if there is no such data, a record is made. Function for processing json files:

```java
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
```

### Processing csv files
The OpenCSV library is used for processing. We read the file line by line, dividing it into attributes for the Station model, check in the name field whether there is such an entry in the database, if not, then write it to the database. Processing function:

```java
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
```

### Processing xml files
The StAX library is used for processing. Reading from the file occurs sequentially, we go through all the tags and look for the data needed for our data model, form the entity, check for duplication using the name field of the database, if not, then write it to the database. Processing function:

```java
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
		}
	}
```

There are test files in the root to check operation.
