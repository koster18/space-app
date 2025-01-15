package ru.sterkhovkv.space_app.service.Impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.ConstellationDTO;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.dto.StarCatalogDTO;
import ru.sterkhovkv.space_app.dto.StarMapDTO;
import ru.sterkhovkv.space_app.model.Satellite;
import ru.sterkhovkv.space_app.model.SpaceStation;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SatelliteDBService;
import ru.sterkhovkv.space_app.service.SkyMapService;
import ru.sterkhovkv.space_app.service.SpaceStationDBService;
import ru.sterkhovkv.space_app.service.StarCatalogLoader;
import ru.sterkhovkv.space_app.util.Constants;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class SkyMapServiceImpl implements SkyMapService {

    private final StarCatalogLoader starCatalogLoader;
    private final ObserverService observerService;
    private final SatelliteDBService satelliteDBService;
    private final SpaceStationDBService spaceStationDBService;

    private final int width;
    private final int height;
    private final int radius;
    private final int centerX;
    private final int centerY;

    @Autowired
    public SkyMapServiceImpl(StarCatalogLoader starCatalogLoader,
                             ObserverService observerService,
                             SatelliteDBService satelliteDBService,
                             SpaceStationDBService spaceStationDBService) {
        this.starCatalogLoader = starCatalogLoader;
        this.observerService = observerService;
        this.satelliteDBService = satelliteDBService;
        this.spaceStationDBService = spaceStationDBService;

        this.width = Constants.SKY_MAP_WIDTH;
        this.height = Constants.SKY_MAP_HEIGHT;
        this.radius = (height / 2 - Constants.MAP_LINES_OFFSET);
        this.centerX = width / 2;
        this.centerY = height / 2;
    }

    //coordinates.az = [0; 360]
    //coordinates.alt = [0; 90]

    @Override
    public String drawSkyMap(ZonedDateTime dateTime,
                             Boolean drawStars,
                             Boolean drawConstellationLines,
                             Boolean drawSatellites,
                             Boolean showSmallSatellites) {
        BufferedImage skyMap = createSkyMap(dateTime, drawStars, drawConstellationLines, drawSatellites, showSmallSatellites);
        String base64Image = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(skyMap, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64Image;
    }


    public BufferedImage createSkyMap(ZonedDateTime dateTime,
                                      boolean showStars,
                                      boolean showConstellationLines,
                                      boolean showSpaceStations,
                                      boolean showSmallSatellites) {
        BufferedImage skyMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = skyMap.createGraphics();

        // Установка фона
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        // Рисование круга
        g2d.setColor(Color.WHITE);
        g2d.drawOval(50, 50, width - 100, height - 100);

        // Рисование сетки
        drawGrid(g2d, width, height);

        // Рисование звезды
        //drawStar(g2d, coordinates, width, height);

        long startTime = System.currentTimeMillis();

        EarthPositionCoordinates position = observerService.getObserverPosition();

        if (showStars) drawAllStar(g2d, calculateStarPositions(position, dateTime));

        if (showConstellationLines) drawConstellations(g2d, calculateConstellationLines(position, dateTime));

        if (showSpaceStations) drawSatellites(g2d, calculateSpaceStationsCoordinates(position, dateTime));

        if (showSmallSatellites) drawSatellites(g2d, calculateSmallSatelliteCoordinates(position, dateTime));

        log.info("Время выполнения метода drawAllStar: {} мс", (System.currentTimeMillis() - startTime));

        g2d.dispose();
        return skyMap;
    }

    private void drawGrid(Graphics2D g2d, int width, int height) {
        // Рисуем окружности для значений высоты (altitude)
        for (int i = 0; i <= 90; i += 10) {
            int radius = (int) ((height / 2 - Constants.MAP_LINES_OFFSET) * (i / 90.0));
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.drawOval((width / 2) - radius, (height / 2) - radius, radius * 2, radius * 2);
            g2d.drawString((90 - i) + "°", width / 2 + radius + 5, height / 2);
        }

        // Рисуем стороны света
        g2d.setColor(Color.WHITE);
        g2d.drawLine(width / 2, Constants.MAP_LINES_OFFSET, width / 2, height - Constants.MAP_LINES_OFFSET);
        g2d.drawLine(Constants.MAP_LINES_OFFSET, height / 2, width - Constants.MAP_LINES_OFFSET, height / 2);

        // Рисуем метки для сторон света
        g2d.drawString("N", width / 2 - 5, 40);
        g2d.drawString("E", 40, height / 2 + 5);
        g2d.drawString("S", width / 2 - 5, height - 30);
        g2d.drawString("W", width - 40, height / 2 + 5);

        // Добавляем калибровочные линии для азимута
        g2d.setColor(new Color(255, 255, 255, 50));
        for (int i = 0; i < 360; i += 30) {
            double azimuthRad = Math.toRadians(i);
            int lineLength = Constants.MAP_LINES_LENGTH;

            int x1 = (int) (width / 2 - (height / 2 - Constants.MAP_LINES_OFFSET) * Math.sin(azimuthRad));
            int y1 = (int) (height / 2 - (height / 2 - Constants.MAP_LINES_OFFSET) * Math.cos(azimuthRad));
            int x2 = (int) (width / 2 - (height / 2 - Constants.MAP_LINES_OFFSET - lineLength) * Math.sin(azimuthRad));
            int y2 = (int) (height / 2 - (height / 2 - Constants.MAP_LINES_OFFSET - lineLength) * Math.cos(azimuthRad));

            g2d.drawLine(x1, y1, x2, y2);
            if (i <= 90) {
                g2d.drawString(i + "°", x1 - Constants.MAP_TEXT_OFFSET, y1 - Constants.MAP_TEXT_OFFSET);
            } else if (i <= 180) {
                g2d.drawString(i + "°", x1 - 2 * Constants.MAP_TEXT_OFFSET, y1 + Constants.MAP_TEXT_OFFSET);
            } else if (i <= 270) {
                g2d.drawString(i + "°", x1 + Constants.MAP_TEXT_OFFSET / 2, y1 + Constants.MAP_TEXT_OFFSET);
            } else {
                g2d.drawString(i + "°", x1 + Constants.MAP_TEXT_OFFSET / 2, y1 - Constants.MAP_TEXT_OFFSET / 2);
            }
        }
    }

    private void drawStar(Graphics2D g2d, SkyHorizontalCoordinates coordinates) {
        // Преобразуем координаты в радианы
        double azimuthRad = Math.toRadians(coordinates.getAz());
        double altitudeRad = Math.toRadians(coordinates.getAlt());

        // Вычисляем радиус на уровне высоты по уравнению линии
        //double r = radius * Math.cos(altitudeRad);
        double r = radius * (1 - 2 * altitudeRad / Math.PI);

        // Вычисляем координаты звезды
        int x = (int) (centerX - r * Math.sin(azimuthRad));
        int y = (int) (centerY - r * Math.cos(azimuthRad));

        // Рисуем звезду
        g2d.setColor(Color.GREEN);
        int starSize = Constants.MAP_STAR_SIZE;
        g2d.fillOval(x - starSize / 2, y - starSize / 2, starSize, starSize);
    }

    private void drawAllStar(Graphics2D g2d, List<StarMapDTO> stars) {

        // Находим минимальную и максимальную vMag для нормализации
        double minVMag = Double.MAX_VALUE;
        double maxVMag = Double.MIN_VALUE;

        for (StarMapDTO star : stars) {
            if (star.getVMag() < minVMag) minVMag = star.getVMag();
            if (star.getVMag() > maxVMag) maxVMag = star.getVMag();
        }

        for (StarMapDTO star : stars) {
            // Преобразуем координаты в радианы
            double azimuthRad = Math.toRadians(star.getCoordinates().getAz());
            double altitudeRad = Math.toRadians(star.getCoordinates().getAlt());

            // Вычисляем радиус на уровне высоты по уравнению линии
            //double r = radius * Math.cos(altitudeRad);
            double r = radius * (1 - 2 * altitudeRad / Math.PI);

            // Вычисляем координаты звезды
            int x = (int) (centerX - r * Math.sin(azimuthRad));
            int y = (int) (centerY - r * Math.cos(azimuthRad));

            // Нормализуем яркость
            double normalizedBrightness = (maxVMag - star.getVMag()) / (maxVMag - minVMag);
            int starSize = (int) (Constants.MAP_STAR_SIZE * normalizedBrightness) + 1;

            // Определяем цвет на основе bV
            Color starColor = getStarColor(star.getBV());

            // Рисуем звезду
            g2d.setColor(starColor);
            g2d.fillOval(x - starSize / 2, y - starSize / 2, starSize, starSize);
        }
    }

    private static Color getStarColor(double bV) {
        // Определяем цвет на основе bV
        Color starColor;
        if (bV < 0) {
            // Голубые звезды
            starColor = Color.getHSBColor(210 / 360f, 1.0f, 1.0f);
        } else if (bV >= 0 && bV < 0.5) {
            // Белые звезды
            starColor = Color.WHITE;
        } else if (bV >= 0.5 && bV < 1.5) {
            // Желтые звезды
            starColor = Color.YELLOW;
        } else if (bV >= 1.5 && bV < 2.5) {
            // Оранжевые звезды
            starColor = Color.ORANGE;
        } else {
            // Красные звезды
            starColor = Color.RED;
        }
        return starColor;
    }

    private void drawConstellations(Graphics2D g2d, List<ConstellationLine> lines) {

        for (ConstellationLine line : lines) {
            double startLineAz = Math.toRadians(line.startLineAz);
            double startLineAlt = Math.toRadians(line.startLineAlt);
            double endLineAz = Math.toRadians(line.endLineAz);
            double endLineAlt = Math.toRadians(line.endLineAlt);

            if (startLineAlt > 0 && endLineAlt > 0) {

                // Вычисляем радиус на уровне высоты по уравнению линии
                //double r1 = radius * Math.cos(startLineAlt);
                double r1 = radius * (1 - 2 * startLineAlt / Math.PI);
                double r2 = radius * (1 - 2 * endLineAlt / Math.PI);

                // Вычисляем начало линии
                int x1 = (int) (centerX - r1 * Math.sin(startLineAz));
                int y1 = (int) (centerY - r1 * Math.cos(startLineAz));

                // Вычисляем конец линии
                int x2 = (int) (centerX - r2 * Math.sin(endLineAz));
                int y2 = (int) (centerY - r2 * Math.cos(endLineAz));

                g2d.setColor(Color.WHITE);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void drawSatellites(Graphics2D g2d, List<SatelliteMapDTO> satellites) {
        if (!satellites.isEmpty()) {
            for (SatelliteMapDTO satellite : satellites) {
                if (satellite.getVisible()) {
                    // Преобразуем координаты в радианы
                    double azimuthRad = Math.toRadians(satellite.getCoordinates().getAz());
                    double altitudeRad = Math.toRadians(satellite.getCoordinates().getAlt());

                    // Вычисляем радиус на уровне высоты по уравнению линии
                    //double r = radius * Math.cos(altitudeRad);
                    double r = radius * (1 - 2 * altitudeRad / Math.PI);

                    // Вычисляем координаты звезды
                    int x = (int) (centerX - r * Math.sin(azimuthRad));
                    int y = (int) (centerY - r * Math.cos(azimuthRad));

                    // Рисуем звезду
                    g2d.setColor(Color.GREEN);
                    int starSize = Constants.MAP_STAR_SIZE;
                    g2d.fillOval(x - starSize / 2, y - starSize / 2, starSize, starSize);
                    g2d.drawString(satellite.getObjectName(),
                            x - satellite.getObjectName().length() / 2,
                            y - 20);
                }
            }
        }
    }

    private List<ConstellationLine> calculateConstellationLines(EarthPositionCoordinates coordinates, ZonedDateTime dateTime) {
        List<ConstellationDTO> constellations = starCatalogLoader.loadConstellationsFromFile();

        List<ConstellationLine> constellationLines = new ArrayList<>();

        for (ConstellationDTO constellation : constellations) {
            for (int lineNum = 0; lineNum < constellation.getLineCount(); lineNum++) {
                StarCatalogDTO star1 = constellation.getStars().get(2 * lineNum);
                StarCatalogDTO star2 = constellation.getStars().get(2 * lineNum + 1);
                SkyHorizontalCoordinates startLine = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                        star1.getCoordinates(), coordinates, dateTime);
                SkyHorizontalCoordinates endLine = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                        star2.getCoordinates(), coordinates, dateTime);

                if (startLine.getAlt() > 0 && endLine.getAlt() > 0) {
                    constellationLines.add(new ConstellationLine(startLine.getAz(), startLine.getAlt(), endLine.getAz(), endLine.getAlt()));
                }
            }
        }
        log.info("Lines: {}", constellationLines.size());
        return constellationLines;
    }

    public List<StarMapDTO> calculateStarPositions(EarthPositionCoordinates coordinates, ZonedDateTime dateTime) {

        List<StarCatalogDTO> starsFromFile = starCatalogLoader.loadStarsFromFile();
        List<StarMapDTO> stars = new ArrayList<>();

        for (StarCatalogDTO starFromFile : starsFromFile) {
            long idCatalog = starFromFile.getIdCatalog();
            double bV = starFromFile.getBV();
            double vMag = starFromFile.getVMag();
            SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                    starFromFile.getCoordinates(), coordinates, dateTime);
            if (horizontalCoordinates.getAlt() > 0) {
                stars.add(new StarMapDTO(idCatalog, horizontalCoordinates, vMag, bV));
            }
        }
        log.info("Calculated {} stars for drawing", stars.size());
        return stars;
    }

    @Data
    @AllArgsConstructor
    static class ConstellationLine {
        double startLineAz;
        double startLineAlt;
        double endLineAz;
        double endLineAlt;
    }

    public List<SatelliteMapDTO> calculateSpaceStationsCoordinates(EarthPositionCoordinates position, ZonedDateTime dateTime) {
        List<SatelliteMapDTO> satelliteCoordinates = new ArrayList<>();

        List<SpaceStation> spaceStations = spaceStationDBService.getVisibleSpaceStationsFromDB();
        if (!spaceStations.isEmpty()) {
            for (SpaceStation spaceStation : spaceStations) {
                SkyEquatorialCoordinates equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
                        Satellite.fromSpaceStation(spaceStation), position, dateTime);
                SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                        equatorialCoordinates, position, dateTime);
                SatelliteMapDTO satelliteMapDTO = new SatelliteMapDTO();
                satelliteMapDTO.setCoordinates(horizontalCoordinates);
                satelliteMapDTO.setObjectId(spaceStation.getNoradCatId());
                satelliteMapDTO.setObjectName(spaceStation.getObjectName());
                satelliteMapDTO.setVisible(spaceStation.getVisible());
                if (satelliteMapDTO.getVisible() && (horizontalCoordinates.getAlt() > 0)) satelliteCoordinates.add(satelliteMapDTO);
//                if (satellite.getObjectName().equals("ISS (ZARYA)")) {
//                    log.info("Equatorial MKS: {}", equatorialCoordinates);
//                    log.info("Horizontal MKS: {}", horizontalCoordinates);
//                    log.info("Rect MKS: {}", SkyCoordinatesTranslator.calculateSatelliteCoordinates(satellite, dateTime));
//                    log.info("Siderial Time: {}", SkyCoordinatesTranslator.calculateUTCSiderealTime(dateTime, position.getLon()));
//                }
            }
        } else log.info("Satellites DB empty");
        return satelliteCoordinates;
    }


    public List<SatelliteMapDTO> calculateSmallSatelliteCoordinates(EarthPositionCoordinates position, ZonedDateTime dateTime) {
        List<SatelliteMapDTO> satelliteCoordinates = new ArrayList<>();

        List<Satellite> satellites = satelliteDBService.getSatellitesFromDB();
        if (!satellites.isEmpty()) {
            for (Satellite satellite : satellites) {
                SkyEquatorialCoordinates equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
                        satellite, position, dateTime);
                SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                        equatorialCoordinates, position, dateTime);
                SatelliteMapDTO satelliteMapDTO = new SatelliteMapDTO();
                satelliteMapDTO.setCoordinates(horizontalCoordinates);
                satelliteMapDTO.setObjectId(satellite.getNoradCatId());
                satelliteMapDTO.setObjectName(satellite.getObjectName());
                satelliteMapDTO.setVisible(satellite.getVisible());
                if (satelliteMapDTO.getVisible() && (horizontalCoordinates.getAlt() > 0)) satelliteCoordinates.add(satelliteMapDTO);
            }
        } else log.info("Satellites DB empty");
        return satelliteCoordinates;
    }
}
