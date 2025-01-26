package ru.sterkhovkv.space_app.util;

import lombok.extern.slf4j.Slf4j;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.RectangularCoordinates;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.model.SpaceObject;

import java.time.Duration;
import java.time.ZonedDateTime;


@Slf4j
public class SkyCoordinatesTranslator {
    // Постоянные для расчетов
    // Гравитационная постоянная Земли m, км(3)сек(-2)
    private static final double GRAVITY_CONST = 398600.4415;
    // Угловая скорость вращения Земли, рад/с
    private static final double MEAN_MOTION_EARTH = 7.2921151467E-5;
    // Радиус Земли, км
    private static final double EARTH_RADIUS = 6378135.0 / 1000;
    // Юлианская дата 0.5 на 1 января 2000 года
    private static final double JULIAN_TIME_2000 = 2451545.0;
    // Сидерический день в юлианских днях
    private static final double STAR_SECOND = 1.00273790935;
    // Количество секунд в дне
    private static final double SECONDS_IN_A_DAY = 86400.0;

    // Convert Sky coordinates [Ra, Dec] at time zonedDateTimeUTC
    // to visible SkyHorizontalCoordinates [Az, Alt] in observer position [lat, lon]
    public static SkyHorizontalCoordinates calculateSkyHorizontalCoordinates(
            SkyEquatorialCoordinates skyCoords,
            EarthPositionCoordinates earthCoords,
            ZonedDateTime zonedDateTimeUTC) {

        // Преобразование координат из градусов в радианы
        double alpha = Math.toRadians(skyCoords.getRa() * 15);
        double delta = Math.toRadians(skyCoords.getDec());
        double phi = Math.toRadians(earthCoords.getLat());
        double lon = Math.toRadians(earthCoords.getLon());

        // Вычисление сидерического времени UTC в радианах
        double localSiderealTime = Math.toRadians(calculateUTCSiderealTime(
                zonedDateTimeUTC,
                earthCoords.getLon()));

        // Вычисление часового угла (H)
        double H = localSiderealTime - alpha;
        if (H < 0) {
            H += 2 * Math.PI;
        }
        if (H > Math.PI) {
            H = H - 2 * Math.PI;
        }

        // Вычисление азимута (A)
        double A = Math.atan2(Math.sin(H),
                Math.cos(H) * Math.sin(phi) -
                        Math.tan(delta) * Math.cos(phi));

        // Вычисление угла места (h)
        double sinH = Math.sin(delta) * Math.sin(phi) +
                Math.cos(delta) * Math.cos(phi) * Math.cos(H);
        double h = Math.asin(sinH);

        // Приведение азимута к положительному значению, нормирование севера
        A -= Math.PI;
        if (A < 0) {
            A += 2 * Math.PI;
        }

        // Преобразование углов в градусы
        double azimuth = Math.toDegrees(A);
        double altitude = Math.toDegrees(h);

        return new SkyHorizontalCoordinates(azimuth, altitude);
    }

    // Return sidereal time UTC for local observer position in degrees
    // input longitude in degrees
    public static double calculateUTCSiderealTime(ZonedDateTime now, double longitude) {
        double julianDay = calculateJulianDay(now);
        double siderealTime = 280.46061837 + 360.98564736629 * (julianDay - JULIAN_TIME_2000) + longitude;
        siderealTime = siderealTime % 360;
        if (siderealTime < 0) {
            siderealTime += 360;
        }
        return siderealTime;
    }

    // Return julian day in decimal day for given UTC time
    public static double calculateJulianDay(ZonedDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        double hour = dateTime.getHour() + dateTime.getMinute() / 60.0 + dateTime.getSecond() / 3600.0;

        if (month <= 2) {
            year -= 1;
            month += 12;
        }

        int A = year / 100;
        int B = 2 - A + A / 4;

        return (int) (365.25 * (year + 4716)) + (int) (30.6001 * (month + 1)) + day + hour / 24.0 + B - 1524.5;
    }

    // Return RectangularCoordinates [x, y, z] position of Satellite in km
    // in TEME coordinate system by calculating its orbit in kepler constants
    public static RectangularCoordinates calculateSatelliteCoordinates(
            SpaceObject satelliteData,
            ZonedDateTime zonedDateTimeUTC) {

        // Получаем Кеплеровы элементы орбиты
        // Эксцентриситет орбиты e
        double eccentricity = satelliteData.getEccentricity();
        // Наклонение i, радианы
        double inclination = Math.toRadians( satelliteData.getInclination());
        // Долгота восходящего узла Омега, радианы
        double raOfAscNode = Math.toRadians(satelliteData.getRaOfAscNode());
        // Аргумент перицентра w, радианы
        double argOfPericenter = Math.toRadians(satelliteData.getArgOfPericenter());
        // Средняя аномалия M0, радианы
        double meanAnomaly = Math.toRadians(satelliteData.getMeanAnomaly());
        // Время эпохи t0, дробные дни
        ZonedDateTime epochTime = satelliteData.getEpoch();
        // Частота обращения n, радианы/сек
        double meanMotion = satelliteData.getMeanMotion() * (2 * Math.PI / SECONDS_IN_A_DAY);
        // Учитываем meanMotionDot (влияние атмосферного сопротивления на орбиту спутника), радианы/день^2
        double meanMotionDot = satelliteData.getMeanMotionDot() * (2 * Math.PI );
        // Коэффициент торможения
        double bStar = satelliteData.getBstar();

        // Вычисляем большую полуось a, км
        double semiMajorAxis = Math.pow((GRAVITY_CONST / Math.pow(meanMotion, 2)), (1.0 / 3.0));

        // Вычисляем время, прошедшее с момента эпохи (в днях)
        Duration duration = Duration.between(epochTime, zonedDateTimeUTC);
        double timeElapsed = duration.getSeconds() / SECONDS_IN_A_DAY;

        // Вычисляем среднюю аномалию на текущий момент времени t, радианы
        double meanAnomalyNow = meanAnomaly + (meanMotion * SECONDS_IN_A_DAY) * timeElapsed + meanMotionDot * Math.pow(timeElapsed, 2);

        meanAnomalyNow = (meanAnomalyNow % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);

        // Находим эксцентрическую аномалию E, радианы
        double E = meanAnomalyNow;
        double tolerance = 1e-10;
        for (int i = 0; i < 10; i++) {
            double E_next = E + (meanAnomalyNow - (E - eccentricity * Math.sin(E))) / (1 - eccentricity * Math.cos(E));
            if (Math.abs(E_next - E) < tolerance) {
                break;
            }
            E = E_next;
        }

        // Вычисляем истинную аномалию ν
        double trueAnomaly1 = 2 * Math.atan(Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * Math.tan(E / 2));
        double trueAnomaly = 2 * Math.atan2(
                Math.sqrt(1 + eccentricity) * Math.sin(E / 2),
                Math.sqrt(1 - eccentricity) * Math.cos(E / 2));

        trueAnomaly = (trueAnomaly % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);

        // Вычисляем радиус-вектор r, км
        double radius = semiMajorAxis * (1 - Math.pow(eccentricity, 2)) / (1 + eccentricity * Math.cos(trueAnomaly));

        // Коррекция радиуса на основе bStar (атмосферные потери)
        double atmosphericDrag = 1 - bStar * Math.exp(-radius / EARTH_RADIUS);
        radius *= atmosphericDrag;

        // Вычисляем аргумент широты u, радианы
        double argLatitude = argOfPericenter + trueAnomaly;
        argLatitude = (argLatitude % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);

        // Вычисляем координаты спутника в системе координат TEME, км
        double x = radius * (Math.cos(argLatitude) * Math.cos(raOfAscNode) -
                Math.sin(argLatitude) * Math.sin(raOfAscNode) * Math.cos(inclination));
        double y = radius * (Math.sin(raOfAscNode) * Math.cos(argLatitude) +
                Math.sin(argLatitude) * Math.cos(raOfAscNode) * Math.cos(inclination));
        double z = radius * (Math.sin(argLatitude) * Math.sin(inclination));

        //log.info("Satellite position in TEME x = {}, y = {}, z = {}", x, y, z);

        return new RectangularCoordinates(x, y, z);

    }

    // Return SkyEquatorialCoordinates [Ra, Dec] as it will be visible
    // from observer position for satellite in given time
    public static SkyEquatorialCoordinates calculateEquatorialCoordinates(
            SpaceObject satelliteData,
            EarthPositionCoordinates observerCoordinates,
            ZonedDateTime zonedDateTimeUTC) {

        RectangularCoordinates satelliteCoordinates = calculateSatelliteCoordinates(satelliteData, zonedDateTimeUTC);

        RectangularCoordinates observerECICoordinates = calculateECIObserverCoordinates(observerCoordinates, calculateJulianDay(zonedDateTimeUTC));
        double x = satelliteCoordinates.getX() - observerECICoordinates.getX();
        double y = satelliteCoordinates.getY() - observerECICoordinates.getY();
        double z = satelliteCoordinates.getZ() - observerECICoordinates.getZ();

        double range = Math.sqrt(x * x + y * y + z * z);
        double delta = Math.asin( z / range );

        double alpha = Math.atan2(y, x);
        alpha = (alpha % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);

        double raHours = (Math.toDegrees(alpha) / 360) * 24;
        double decDegrees = Math.toDegrees(delta);

        //log.info("Ra/Dec: {} hours, {} deg", raHours, decDegrees);

        return new SkyEquatorialCoordinates(raHours, decDegrees);
    }

    // Polar coordinates to Rectangular:
    // Theta is vertical pi/2 to -pi/2 (usually latitude or declination)
    // Phi is horizontal 0 to 2pi, or -pi to pi (usually longitude or Right Ascension)
    // R is the radius in any units
    // Return arguments [x, y, z] in units as R
    public static double[] polarToRect(double theta, double phi, double r) {
        // Convert range theta to (0 to pi)
        theta = Math.PI / 2 - theta;
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.sin(theta) * Math.sin(phi);
        double z = r * Math.cos(theta);
        return new double[]{x, y, z};
    }

    // Rectangular coordinates to Polar:
    // x is left/right, y is forward/backward, z is up/down
    // Return arguments lat, lon in radians, r in units as x, y, z
    public static double[] rectToPolar(double x, double y, double z) {
        double r = Math.sqrt(x * x + y * y + z * z);
        double lon = Math.atan2(y, x);
        double lat = Math.acos(z / r);
        // Make sure lon is positive, and lat is in range +/-90deg
        if (lon < 0) {
            lon += (2 * Math.PI);
        }
        lat = 0.5 * Math.PI - lat;
        return new double[]{lat, lon, r};
    }

    // Calculates rectangular coordinates [x, y, z] in ECI (Earth-Centered Inertial)
    // input julianDate in decimal days, gpsCoordinates in ECEF (Earth-Centered Earth-Fixed)
    // output RectangularCoordinates in km
    public static RectangularCoordinates calculateECIObserverCoordinates(EarthPositionCoordinates gpsCoordinates, double julianDate) {
        // Преобразуем широту и долготу из градусов в радианы
        double latitude = Math.toRadians(gpsCoordinates.getLat());
        double longitude = Math.toRadians(gpsCoordinates.getLon());

        // Вычисляем угловую позицию в ECI с учетом долготы
        double theta = thetaG_JD(julianDate) + longitude;
        // Применяем модуль для theta, чтобы получить значение в диапазоне [0, 2π)
        theta -= (2 * Math.PI) * Math.floor(theta / (2 * Math.PI));

        // Вычисляем радиус в плоскости XY
        double r = EARTH_RADIUS * Math.cos(latitude);

        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        double z = EARTH_RADIUS * Math.sin(latitude);

        // Возвращаем координаты в ECI (X, Y, Z)
        return new RectangularCoordinates(x, y, z);
    }


    // Returns angle for Earth rotation on jd time in radians
    // using to convert from ECEF (Earth-Centered Earth-Fixed) into
    // ECI (Earth-Centered Inertial)
    // input jd in decimal days
    public static double thetaG_JD(double jd) {
        // Вычисляем UT (Universal Time) и обновляем jd
        double UT = jd + 0.5 - Math.floor(jd + 0.5);
        jd -= UT; // Убираем UT из jd

        // Вычисляем TU (Time in Julian centuries)
        double TU = (jd - JULIAN_TIME_2000) / 36525.0;

        // Вычисляем GMST (Greenwich Mean Sidereal Time)
        double GMST = 24110.54841
                + TU * (8640184.812866 + TU * (0.093104 - TU * 6.2E-6));

        // Применяем модуль для GMST
        GMST = GMST + SECONDS_IN_A_DAY * STAR_SECOND * UT;
        GMST -= SECONDS_IN_A_DAY * Math.floor(GMST / SECONDS_IN_A_DAY);

        // Возвращаем угловую позицию в радианах
        return ((2 * Math.PI) * GMST) / SECONDS_IN_A_DAY;
    }

    // Returns string for Decimal Degrees
    public static String getString(double decimalDegrees, String direction) {
        double absDecimalDegrees = Math.abs(decimalDegrees);
        int degrees = (int) absDecimalDegrees;
        double minutesDecimal = (absDecimalDegrees - degrees) * 60;
        int minutes = (int) minutesDecimal;
        double secondsDecimal = (minutesDecimal - minutes) * 60;
        int seconds = (int) Math.round(secondsDecimal);
        return String.format("%d° %d' %d\" %s", degrees, minutes, seconds, direction);
    }
}
