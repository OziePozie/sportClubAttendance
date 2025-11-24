package ru.avantys.sportClubAttendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.avantys.sportClubAttendance.model.AccessRule;
import ru.avantys.sportClubAttendance.model.Membership;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record AccessRuleDto(
        UUID id,
        UUID membershipId,
        Set<String> zones,
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime validFromTime,
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime validToTime,
        String allowedDays,
        Integer priority
) {
    public static AccessRuleDto from(AccessRule accessRule) {
        return new AccessRuleDto(
                accessRule.getId(),
                accessRule.getMembership().getId(),
                accessRule.getZones(),
                accessRule.getValidFromTime(),
                accessRule.getValidToTime(),
                accessRule.getAllowedDays(),
                accessRule.getPriority()
        );
    }

    public static AccessRule toAccessRule(AccessRuleDto accessRuleDto, Membership membership) {
        var accessRule = new AccessRule();
        accessRule.setMembership(membership);
        accessRule.setZones(accessRuleDto.zones());
        accessRule.setValidFromTime(accessRuleDto.validFromTime());
        accessRule.setValidToTime(accessRuleDto.validToTime());
        accessRule.setAllowedDays(accessRuleDto.allowedDays());
        accessRule.setPriority(accessRuleDto.priority());
        return accessRule;
    }
}