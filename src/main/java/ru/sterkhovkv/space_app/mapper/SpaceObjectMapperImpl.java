package ru.sterkhovkv.space_app.mapper;

import org.springframework.stereotype.Component;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;
import ru.sterkhovkv.space_app.model.SpaceObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Component
public class SpaceObjectMapperImpl implements SpaceObjectMapper {

    //                 1         2         3         4         5         6
    //       0123456789012345678901234567890123456789012345678901234567890123456789
    //line1="1 00005U 58002B   00179.78495062  .00000023  00000-0  28098-4 0  4753";
    //line2="2 00005  34.2682 348.7242 1859667 331.7664  19.3264 10.82419157413667";
    @Override
    public SpaceObject spaceObjectFromTLE(SatelliteTLEDTO satelliteTLEDTO) {
        String line1 = satelliteTLEDTO.getLine1();
        String line2 = satelliteTLEDTO.getLine2();

        double nddot = parseDoubleWithSign(line1.charAt(44), line1, 45, 50);
        nddot *= Math.pow(10.0, parseDouble(line1, 50, 52));
        double bstar = parseDoubleWithSign(line1.charAt(53), line1, 54, 59);
        bstar *= Math.pow(10.0d, parseDouble(line1, 59, 61));

        return SpaceObject.builder()
                .objectName(satelliteTLEDTO.getName())
                .epoch(parseEpoch(line1.substring(18, 32).trim()))
                .meanMotion(parseDouble(line2, 52, 63))
                .eccentricity(parseDoubleWithSign('+', line2, 26, 33))
                .inclination(parseDouble(line2, 8, 16))
                .raOfAscNode(parseDouble(line2, 17, 25))
                .argOfPericenter(parseDouble(line2, 34, 42))
                .meanAnomaly(parseDouble(line2, 43, 51))
                .classificationType(String.valueOf(line1.charAt(7)))
                .noradCatId(Integer.parseInt(line1.substring(2, 7).trim()))
                .revAtEpoch((int) parseDouble(line2, 63, 68))
                .bstar(bstar)
                .meanMotionDot(parseDoubleWithSign(line1.charAt(33), line1, 35, 44))
                .meanMotionDdot(nddot)
                .visible(true)
                .tleLine1(line1)
                .tleLine2(line2)
                .build();
    }

    @Override
    public void setFieldsFromTLE(SpaceObject spaceObject, SatelliteTLEDTO satelliteTLEDTO) {
        String line1 = satelliteTLEDTO.getLine1();
        String line2 = satelliteTLEDTO.getLine2();

        double nddot = parseDoubleWithSign(line1.charAt(44), line1, 45, 50);
        nddot *= Math.pow(10.0, parseDouble(line1, 50, 52));
        double bstar = parseDoubleWithSign(line1.charAt(53), line1, 54, 59);
        bstar *= Math.pow(10.0d, parseDouble(line1, 59, 61));

        spaceObject.setObjectName(satelliteTLEDTO.getName());
        spaceObject.setEpoch(parseEpoch(line1.substring(18, 32).trim()));
        spaceObject.setMeanMotion(parseDouble(line2, 52, 63));
        spaceObject.setEccentricity(parseDoubleWithSign('+', line2, 26, 33));
        spaceObject.setInclination(parseDouble(line2, 8, 16));
        spaceObject.setRaOfAscNode(parseDouble(line2, 17, 25));
        spaceObject.setArgOfPericenter(parseDouble(line2, 34, 42));
        spaceObject.setMeanAnomaly(parseDouble(line2, 43, 51));
        spaceObject.setClassificationType(String.valueOf(line1.charAt(7)));
        spaceObject.setNoradCatId(Integer.parseInt(line1.substring(2, 7).trim()));
        spaceObject.setRevAtEpoch((int) parseDouble(line2, 63, 68));
        spaceObject.setBstar(bstar);
        spaceObject.setMeanMotionDot(parseDoubleWithSign(line1.charAt(33), line1, 35, 44));
        spaceObject.setMeanMotionDdot(nddot);
        spaceObject.setTleLine1(line1);
        spaceObject.setTleLine2(line2);
    }

    @Override
    public SpaceObject spaceObjectFromDto(SatelliteDataDTO satelliteDataDTO) {
        SpaceObject spaceObject = new SpaceObject();
        setFieldsFromDto(spaceObject, satelliteDataDTO);
        return spaceObject;
    }

    @Override
    public void setFieldsFromDto(SpaceObject spaceObject, SatelliteDataDTO satelliteDataDTO) {
        spaceObject.setObjectName(satelliteDataDTO.getObjectName());
        spaceObject.setEpoch(satelliteDataDTO.getEpoch());
        spaceObject.setMeanMotion(satelliteDataDTO.getMeanMotion());
        spaceObject.setEccentricity(satelliteDataDTO.getEccentricity());
        spaceObject.setInclination(satelliteDataDTO.getInclination());
        spaceObject.setRaOfAscNode(satelliteDataDTO.getRaOfAscNode());
        spaceObject.setArgOfPericenter(satelliteDataDTO.getArgOfPericenter());
        spaceObject.setMeanAnomaly(satelliteDataDTO.getMeanAnomaly());
        spaceObject.setClassificationType(satelliteDataDTO.getClassificationType());
        spaceObject.setNoradCatId(satelliteDataDTO.getNoradCatId());
        spaceObject.setRevAtEpoch(satelliteDataDTO.getRevAtEpoch());
        spaceObject.setBstar(satelliteDataDTO.getBstar());
        spaceObject.setMeanMotionDot(satelliteDataDTO.getMeanMotionDot());
        spaceObject.setMeanMotionDdot(satelliteDataDTO.getMeanMotionDdot());
    }

    private static ZonedDateTime parseEpoch(String epochString) {
        int year = Integer.parseInt(epochString.substring(0, 2).trim());
        year += (year > 56) ? 1900 : 2000;

        int doy = Integer.parseInt(epochString.substring(2, 5).trim());
        double dfrac = Double.parseDouble("0" + epochString.substring(5).trim());

        dfrac *= 24.0d;
        int hr = (int) dfrac;
        dfrac = 60.0d * (dfrac - hr);
        int mn = (int) dfrac;
        dfrac = 60.0d * (dfrac - mn);
        int sc = (int) dfrac;

        dfrac = 1000.d * (dfrac - sc);
        int milli = (int) dfrac;

        GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.YEAR, year);
        gc.set(Calendar.DAY_OF_YEAR, doy);
        gc.set(Calendar.HOUR_OF_DAY, hr);
        gc.set(Calendar.MINUTE, mn);
        gc.set(Calendar.SECOND, sc);
        gc.set(Calendar.MILLISECOND, milli);

        return gc.toInstant().atZone(ZoneId.of("UTC"));
    }

    private static double parseDoubleWithSign(char sign, String str, int start, int end) {
        double num = Double.parseDouble("0." + str.substring(start, end).trim());
        return (sign == '-') ? -num : num;
    }

    private static double parseDouble(String str, int start, int end) {
        return Double.parseDouble(str.substring(start, end).trim());
    }
}
