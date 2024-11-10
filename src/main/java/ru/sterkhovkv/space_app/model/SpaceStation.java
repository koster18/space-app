package ru.sterkhovkv.space_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Entity
@Table(name = "space_stations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SpaceStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "epoch")
    private ZonedDateTime epoch;

    @Column(name = "mean_motion")
    private double meanMotion;

    @Column(name = "eccentricity")
    private double eccentricity;

    @Column(name = "inclination")
    private double inclination;

    @Column(name = "ra_of_asc_node")
    private double raOfAscNode;

    @Column(name = "arg_of_pericenter")
    private double argOfPericenter;

    @Column(name = "mean_anomaly")
    private double meanAnomaly;

    @Column(name = "classification_type")
    private String classificationType;

    @Column(name = "norad_cat_id")
    private int noradCatId;

    @Column(name = "rev_at_epoch")
    private int revAtEpoch;

    @Column(name = "bstar")
    private double bstar;

    @Column(name = "mean_motion_dot")
    private double meanMotionDot;

    @Column(name = "mean_motion_ddot")
    private double meanMotionDdot;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "tle_line_1")
    private String tleLine1;

    @Column(name = "tle_line_2")
    private String tleLine2;

    // Метод конвертации из Satellite в SpaceStation
    public static SpaceStation fromSatellite(Satellite satellite) {
        return SpaceStation.builder()
                .id(satellite.getId())
                .objectName(satellite.getObjectName())
                .epoch(satellite.getEpoch())
                .meanMotion(satellite.getMeanMotion())
                .eccentricity(satellite.getEccentricity())
                .inclination(satellite.getInclination())
                .raOfAscNode(satellite.getRaOfAscNode())
                .argOfPericenter(satellite.getArgOfPericenter())
                .meanAnomaly(satellite.getMeanAnomaly())
                .classificationType(satellite.getClassificationType())
                .noradCatId(satellite.getNoradCatId())
                .revAtEpoch(satellite.getRevAtEpoch())
                .bstar(satellite.getBstar())
                .meanMotionDot(satellite.getMeanMotionDot())
                .meanMotionDdot(satellite.getMeanMotionDdot())
                .visible(satellite.getVisible())
                .tleLine1(satellite.getTleLine1())
                .tleLine2(satellite.getTleLine2())
                .build();
    }
    //                 1         2         3         4         5         6
    //       0123456789012345678901234567890123456789012345678901234567890123456789
    //line1="1 00005U 58002B   00179.78495062  .00000023  00000-0  28098-4 0  4753";
    //line2="2 00005  34.2682 348.7242 1859667 331.7664  19.3264 10.82419157413667";
    public static SpaceStation fromTLE(SatelliteTLEDTO satelliteTLEDTO) {
        String line1 = satelliteTLEDTO.getLine1();
        String line2 = satelliteTLEDTO.getLine2();

        double nddot = gdi(line1.charAt(44),line1,45,50);
        nddot *= Math.pow(10.0, gd(line1,50,52));
        double bstar = gdi(line1.charAt(53),line1,54,59);
        bstar *= Math.pow(10.0d, gd(line1,59,61));

        return SpaceStation.builder()
                .objectName(satelliteTLEDTO.getName())
                .epoch(parseEpoch(line1.substring(18,32).trim()))
                .meanMotion(gd(line2,52,63))
                .eccentricity(gdi('+',line2,26,33))
                .inclination(gd(line2,8,16))
                .raOfAscNode(gd(line2,17,25))
                .argOfPericenter(gd(line2,34,42))
                .meanAnomaly(gd(line2,43,51))
                .classificationType(String.valueOf(line1.charAt(7)))
                .noradCatId(Integer.parseInt(line1.substring(2,7).trim()))
                .revAtEpoch((int)gd(line2,63,68))
                .bstar(bstar)
                .meanMotionDot(gdi(line1.charAt(33),line1,35,44))
                .meanMotionDdot(nddot)
                .visible(true)
                .tleLine1(line1)
                .tleLine2(line2)
                .build();
    }

    private static ZonedDateTime parseEpoch(String epochString) {
        int year = Integer.parseInt(epochString.substring(0,2).trim());

        if(year > 56)
        {
            year += 1900;
        }
        else
        {
            year += 2000;
        }

        int doy = Integer.parseInt(epochString.substring(2,5).trim());
        double dfrac = Double.parseDouble("0"+epochString.substring(5).trim());

        dfrac *= 24.0d;
        int hr = (int)dfrac;
        dfrac = 60.0d*(dfrac - hr);
        int mn = (int)dfrac;
        dfrac = 60.0d*(dfrac - mn);
        int sc = (int)dfrac;

        dfrac = 1000.d*(dfrac-sc);
        int milli = (int)dfrac;

        GregorianCalendar gc = new GregorianCalendar();

        gc.set(Calendar.YEAR, year);
        gc.set(Calendar.DAY_OF_YEAR, doy);
        gc.set(Calendar.HOUR_OF_DAY, hr);
        gc.set(Calendar.MINUTE, mn);
        gc.set(Calendar.SECOND, sc);
        gc.set(Calendar.MILLISECOND, milli);

        return gc.toInstant().atZone(ZoneId.of("UTC"));
    }

    private static double gdi(char sign, String str, int start, int end)
    {
        double num = 0;
        num = Double.parseDouble("0."+str.substring(start,end).trim());
        if(sign == '-') num *= -1.0d;
        return num;
    }

    private static double gd(String str, int start, int end)
    {
        double num = 0;
        num = Double.parseDouble(str.substring(start,end).trim());
        return num;
    }
}
