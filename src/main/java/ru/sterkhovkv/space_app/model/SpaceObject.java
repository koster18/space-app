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

import java.time.ZonedDateTime;

@Entity
@Table(name = "satellites")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public final class SpaceObject {
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

    @Column(name = "space_station")
    private Boolean spaceStation;

    @Column(name = "tle_line1")
    private String tleLine1;

    @Column(name = "tle_line2")
    private String tleLine2;
}
