package ru.sterkhovkv.space_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.ConstellationDTO;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.dto.StarCatalogDTO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StarCatalogLoaderImpl implements StarCatalogLoader {
    @Value("${starcatalog.filepathstars}")
    private String filePathStars;

    @Value("${starcatalog.filepathconstellations}")
    private String filePathConstellations;

    private List<StarCatalogDTO> stars;
    private final List<ConstellationDTO> constellations;

    public StarCatalogLoaderImpl() {
        stars = new ArrayList<>();
        constellations = new ArrayList<>();
    }
@Override
    public List<StarCatalogDTO> loadStarsFromFile() {

        if (!stars.isEmpty()) return stars;

        try (BufferedReader br = new BufferedReader(new FileReader(filePathStars))) {
            String line;

            line = br.readLine().trim();
            String[] starData = line.split("\\],\\[");

            for (String star : starData) {
                // Убираем лишние символы
                star = star.replaceAll("[\\[\\]]", "");
                String[] attributes = star.split(",");

                long idCatalog = 0;
                double vMag = 0.0;
                double raDeg = 0.0;
                double deDeg = 0.0;
                double bV = 0.0;

                // Проверяем и присваиваем значения
                if (attributes.length > 0 && !attributes[0].isEmpty()) {
                    idCatalog = Long.parseLong(attributes[0]);
                }
                if (attributes.length > 1 && !attributes[1].isEmpty()) {
                    vMag = Double.parseDouble(attributes[1]);
                }
                if (attributes.length > 2 && !attributes[2].isEmpty()) {
                    raDeg = Double.parseDouble(attributes[2]);
                }
                if (attributes.length > 3 && !attributes[3].isEmpty()) {
                    deDeg = Double.parseDouble(attributes[3]);
                }
                if (attributes.length > 4 && !attributes[4].isEmpty()) {
                    bV = Double.parseDouble(attributes[4]);
                }

                stars.add(new StarCatalogDTO(idCatalog, vMag, new SkyEquatorialCoordinates((raDeg / 15), deDeg), bV));
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        log.info("Loaded {} stars from {}", stars.size(), filePathStars);

        return stars;
    }

    @Override
    public List<ConstellationDTO> loadConstellationsFromFile() {
        if (!constellations.isEmpty()) return constellations;
        if (stars.isEmpty()) stars = loadStarsFromFile();

        try (BufferedReader br = new BufferedReader(new FileReader(filePathConstellations))) {
            String line;
            // Читаем файл построчно
            while ((line = br.readLine()) != null) {
                // Убираем лишние символы и разбиваем строку на массив
                line = line.trim();
                line = line.replaceAll("[\\[\\]\"]", "").trim();
                String[] attributes = line.split(",\\s*");

                // Извлекаем данные
                String abbreviation = attributes[0].replace("\"", ""); // Сокращенное название созвездия
                int lineCount = Integer.parseInt(attributes[1]); // Количество линий созвездия
                List<Long> starIds = new ArrayList<>();

                // Добавляем идентификаторы звезд в список
                for (int i = 2; i < attributes.length; i++) {
                    starIds.add(Long.parseLong(attributes[i]));
                }

                List<StarCatalogDTO> starConstellations = new ArrayList<>();

                for (Long starId : starIds) {
                    StarCatalogDTO starLine = stars.stream()
                            .filter(star -> star.getIdCatalog() == starId)
                            .findFirst()
                            .orElse(null);
                    if (starLine != null) {
                        starConstellations.add(starLine);
                    } else log.info("Star id not found: {}", starId);
                }

                // Создаем объект ConstellationDTO и добавляем его в список
                if (2 * lineCount == starConstellations.size()) {
                    ConstellationDTO constellation = new ConstellationDTO(abbreviation, lineCount, starConstellations);
                    constellations.add(constellation);
                } else
                    log.info("Incorrect constellation: {}", abbreviation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Ошибка формата числа: " + e.getMessage());
        }
        log.info("Loaded {} constellations from {}", constellations.size(), filePathConstellations);



        return constellations;
    }
}
