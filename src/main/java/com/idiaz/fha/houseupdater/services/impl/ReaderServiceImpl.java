package com.idiaz.fha.houseupdater.services.impl;

import com.idiaz.fha.houseupdater.bo.Inmueble;
import com.idiaz.fha.houseupdater.bo.Location;
import com.idiaz.fha.houseupdater.bs.FirebaseService;
import com.idiaz.fha.houseupdater.bs.ReaderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReaderServiceImpl implements ReaderService {

    private Logger log = LogManager.getLogger(ReaderServiceImpl.class);

    @Autowired
    private FirebaseService firebaseService;

    @Value("${app.url.fha.page}")
    private String fhaURL;

    @Value("${app.url.cases.trunk}")
    private String trunkUri;

    @Value("${app.folder.saver}")
    private String folderPath;

    @PostConstruct
    private void init() {
        log.info("Extracting from " + fhaURL);
        while (true) {
            try {
                extractFromFHAPage();
                Thread.sleep(28800000);
            } catch (Exception e) {
                log.error(e);
            }

        }
    }

    private void extractFromFHAPage() {
        Document doc = null;
        URL url = null;
        try {
            url = new URL(fhaURL);
        } catch (MalformedURLException e) {
            log.error(e);
        }
        try {
            doc = Jsoup.parse(url, 1200000);
            extractAllInformation(doc);
        } catch (IOException e) {
            log.error(e);
        }

    }

    private void extractAllInformation(Document doc) {
        List<String> cities = new ArrayList<>();
        for (Element table : doc
                .select("table")) {
            for (Element row : table.select("tr")) {
                Elements tds = row.select("td");
                if (tds.isEmpty()) {
                    continue;
                }

                if (tds.size() > 3) {
                    Inmueble inmueble = new Inmueble();

                    if (tds.get(0).text() != null && !tds.get(0).text().isEmpty()) {
                        try {
                            inmueble.setCode(tds.get(0).text());

                            if (tds.get(1).text() != null && !tds.get(1).text().isEmpty()) {
                                String address = tds.get(1).text();
                                inmueble.setAddress(address);
                                String fullURl = trunkUri + tds.get(0).select("a").attr("href");
                                inmueble.setLinkToGo(fullURl);

                                try {
                                    String city = address.substring(address.lastIndexOf(",") + 2, address.length());
                                    inmueble.setCity(city);
                                    if (!cities.contains(city)) {
                                        cities.add(city);
                                    }
                                } catch (Exception e) {

                                }
                            }
                            if (tds.get(2).text() != null && !tds.get(2).text().isEmpty()) {

                                String[] split = tds.get(2).text().split("Q");
                                Double amount = null;
                                for (String s : split) {
                                    try {
                                        s = s.replace(",", "");
                                        amount = Double.parseDouble(s.trim());
                                    } catch (Exception e) {
//                                        log.error("monto no valido " + s);
                                    }
                                }
                                if (amount != null) {
                                    inmueble.setPrice(amount);
                                } else {
                                    continue;
                                }
                            }
                            if (tds.get(3).text() != null && !tds.get(3).text().isEmpty()) {
                                if (tds.get(3).text().trim().equals("1")) {
                                    inmueble.setAvailable(false);
                                } else {
                                    inmueble.setAvailable(true);
                                }
                            } else {
                                inmueble.setAvailable(true);
                            }

                        } catch (Exception e) {
                            log.error("Casa sin codigo ");
                            continue;
                        }
                    }
                    inmueble = getDetailforThis(inmueble);
                    log.info("Agregada: " + inmueble.toString());
                    firebaseService.insertToFirebase(inmueble);

                }
            }
        }
        for (String city : cities) {
            log.info(city);
            Location location = new Location();
            location.setName(city);
            firebaseService.insertToLocations(location);
        }
    }

    private Inmueble getDetailforThis(Inmueble inmueble) {

        Document doc = null;
        URL url = null;

        try {
            url = new URL(inmueble.getLinkToGo());
        } catch (MalformedURLException e) {
            log.error(e);
        }
        try {
            doc = Jsoup.parse(url, 180000);
        } catch (IOException e) {
            log.error(e);
        }

        Boolean saved = false;
        for (Element table : doc
                .select("table")) {
            for (Element row : table.select("tr")) {
                if (!saved) {
                    Elements tds = row.select("td");
                    if (tds.isEmpty()) {
                        continue;
                    }
                    if (tds.size() > 1) {
                        String[] fields = tds.get(0).select("p").text().split(" ");
                        String hl = tds.get(0).text();
                        if (hl.contains("Ambientes:")) {
                            String vector[] = hl.split("Ambientes:");
                            for (String pal : vector) {
                                if (pal.contains("Precio:")) {
                                    String[] v2 = pal.split("Precio:");
                                    if (v2.length > 0) {
                                        inmueble.setEnvironments(v2[0]);
                                    }
                                }
                            }
                        }

                        Boolean fie = false;
                        Boolean ctr = false;
                        String finca = "";

                        for (String field : fields) {
                            if (!field.trim().isEmpty()) {
                                if (!fie) {
                                    try {
                                        String fa = field.replace(",", "");
                                        Double value = Double.parseDouble(fa.trim());
                                        inmueble.setFieldArea(value);
                                        fie = true;
                                    } catch (Exception e) {
                                        fie = false;
                                    }
                                } else if (!ctr) {
                                    try {
                                        String fc = field.replace(",", "");
                                        Double value = Double.parseDouble(fc.trim());
                                        inmueble.setConsArea(value);
                                        ctr = true;
                                    } catch (Exception e) {
                                        ctr = false;
                                    }
                                } else {
                                    finca += field + " ";
                                }


                            }
                        }
                        inmueble.setPropertyRegistry(finca);
                        saved = true;
                    }
                }
            }
        }

        try {
            getImages(doc, inmueble);
        } catch (Exception e) {
            log.error("No se pudo extraer imagenes");
        }
        return inmueble;
    }

    private void getImages(Document doc, Inmueble inmueble) throws IOException {

        inmueble.setImages(new ArrayList<>());
        Elements img = doc.getElementsByTag("img");
        List<String> imagesList = new ArrayList<>();
        for (Element el : img) {

            //for each element get the srs url
            String src = el.absUrl("src");
            if (src.contains("FotosSlider")) {
                log.info("src attribute is : " + src);
                if (src.contains(" ")) {
                    src = src.replace(" ", "%20");
                }
                imagesList.add(src);
            }


        }
        if (!imagesList.isEmpty() && imagesList.size() > 0) {
            saveImages(inmueble, imagesList);
        }
    }


    private void saveImages(Inmueble inmueble, List<String> imagesList) {

        for (String src : imagesList)
            //Open a URL Stream
            try {

                Integer indexname = src.lastIndexOf("/");
                String name = src.substring(indexname, src.length());


                URL url = new URL(src);
                InputStream in = url.openStream();

                OutputStream out = new BufferedOutputStream(new FileOutputStream(folderPath + name));
                log.info(folderPath+name);

                for (int b; (b = in.read()) != -1; ) {
                    out.write(b);
                }
                out.close();
                in.close();

                firebaseService.insertImageOnFirebaseStorage(inmueble,folderPath+name,name);
            } catch (Exception e) {
                log.error("Error al guardar foto", e);
            }


    }

}
