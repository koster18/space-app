package ru.sterkhovkv.space_app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sterkhovkv.space_app.util.ZonedDateTimeDeserializer;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SatelliteDataDTO {
    @JsonProperty("OBJECT_NAME")
    private String objectName;

    @JsonProperty("OBJECT_ID")
    private String objectId;

    @JsonProperty("EPOCH")
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime epoch;

    @JsonProperty("MEAN_MOTION")
    private double meanMotion;

    @JsonProperty("ECCENTRICITY")
    private double eccentricity;

    @JsonProperty("INCLINATION")
    private double inclination;

    @JsonProperty("RA_OF_ASC_NODE")
    private double raOfAscNode;

    @JsonProperty("ARG_OF_PERICENTER")
    private double argOfPericenter;

    @JsonProperty("MEAN_ANOMALY")
    private double meanAnomaly;

    @JsonProperty("EPHEMERIS_TYPE")
    private int ephemerisType;

    @JsonProperty("CLASSIFICATION_TYPE")
    private String classificationType;

    @JsonProperty("NORAD_CAT_ID")
    private int noradCatId;

    @JsonProperty("ELEMENT_SET_NO")
    private int elementSetNo;

    @JsonProperty("REV_AT_EPOCH")
    private int revAtEpoch;

    @JsonProperty("BSTAR")
    private double bstar;

    @JsonProperty("MEAN_MOTION_DOT")
    private double meanMotionDot;

    @JsonProperty("MEAN_MOTION_DDOT")
    private double meanMotionDdot;

}
