package ru.avantys.sportClubAttendance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "access_rule")
public class AccessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @Column(name = "zones", nullable = false)
    private Set<String> zones;

    @Column(name = "valid_from_time", nullable = false)
    private LocalTime validFromTime;

    @Column(name = "valid_to_time", nullable = false)
    private LocalTime validToTime;

    @Column(name = "allowed_days", nullable = false)
    private String allowedDays;

    @Column(name = "priority")
    private Integer priority = 0;

    public AccessRule() {}

    public AccessRule(Membership membership, Set<String> zones, LocalTime validFromTime,
                      LocalTime validToTime, String allowedDays, Integer priority) {
        this.membership = membership;
        this.zones = zones;
        this.validFromTime = validFromTime;
        this.validToTime = validToTime;
        this.allowedDays = allowedDays;
        this.priority = priority;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Membership getMembership() { return membership; }
    public void setMembership(Membership membership) { this.membership = membership; }

    public void setZones(Set<String> zones) {
        this.zones = zones;
    }
    public Set<String> getZones() {
        return zones;
    }

    public LocalTime getValidFromTime() { return validFromTime; }
    public void setValidFromTime(LocalTime validFromTime) { this.validFromTime = validFromTime; }

    public LocalTime getValidToTime() { return validToTime; }
    public void setValidToTime(LocalTime validToTime) { this.validToTime = validToTime; }

    public String getAllowedDays() { return allowedDays; }
    public void setAllowedDays(String allowedDays) { this.allowedDays = allowedDays; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
