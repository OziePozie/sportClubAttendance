package ru.avantys.sportClubAttendance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.avantys.sportClubAttendance.dto.AccessRuleDto;
import ru.avantys.sportClubAttendance.model.AccessRule;
import ru.avantys.sportClubAttendance.model.Membership;
import ru.avantys.sportClubAttendance.repository.AccessRuleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccessRuleRepository accessRuleRepository;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private AccessService accessService;

    @Test
    void createAccessRule_success() {
        UUID membershipId = UUID.randomUUID();
        Membership membership = new Membership();
        membership.setId(membershipId);

        AccessRuleDto dto = new AccessRuleDto(
                null,
                membershipId,
                Set.of("A", "B"),
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                "12345",
                1
        );

        when(membershipService.getMembershipById(membershipId))
                .thenReturn(Optional.of(membership));

        when(accessRuleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccessRule result = accessService.createAccessRule(dto, membershipId);

        assertEquals(membership, result.getMembership());
        assertEquals(Set.of("A", "B"), result.getZones());
        assertEquals("12345", result.getAllowedDays());
    }

    @Test
    void createAccessRule_membershipNotFound() {
        UUID membershipId = UUID.randomUUID();

        AccessRuleDto dto = new AccessRuleDto(
                null, membershipId, Set.of("A"),
                LocalTime.NOON, LocalTime.MIDNIGHT,
                "12345", 1
        );

        when(membershipService.getMembershipById(membershipId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> accessService.createAccessRule(dto, membershipId));
    }

    @Test
    void getAccessRulesByMembership_success() {
        UUID membershipId = UUID.randomUUID();

        List<AccessRule> rules = List.of(new AccessRule(), new AccessRule());

        when(accessRuleRepository.findByMembershipId(membershipId)).thenReturn(rules);

        List<AccessRule> result = accessService.getAccessRulesByMembership(membershipId);

        assertEquals(2, result.size());
    }

    @Test
    void deleteAccessRule_success() {
        UUID id = UUID.randomUUID();

        when(accessRuleRepository.existsById(id)).thenReturn(true);

        accessService.deleteAccessRule(id);

        verify(accessRuleRepository).deleteById(id);
    }

    @Test
    void deleteAccessRule_notFound() {
        UUID id = UUID.randomUUID();

        when(accessRuleRepository.existsById(id)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> accessService.deleteAccessRule(id));
    }

    @Test
    void checkAccessRule_membershipNotActive() {
        UUID membershipId = UUID.randomUUID();

        when(membershipService.isActiveMembership(membershipId)).thenReturn(false);

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertFalse(result);
    }

    @Test
    void checkAccessRule_noValidRules() {
        UUID membershipId = UUID.randomUUID();

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId)).thenReturn(List.of());

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertFalse(result);
    }

    @Test
    void checkAccessRule_validRuleAllowsZone() {
        UUID membershipId = UUID.randomUUID();

        AccessRule rule = new AccessRule();
        rule.setAllowedDays(String.valueOf(LocalDate.now().getDayOfWeek().getValue()));
        rule.setValidFromTime(LocalTime.now().minusHours(1));
        rule.setValidToTime(LocalTime.now().plusHours(1));
        rule.setPriority(1);
        rule.setZones(Set.of("A", "B"));

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId))
                .thenReturn(List.of(rule));

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertTrue(result);
    }

    @Test
    void checkAccessRule_highestPriorityWins() {
        UUID membershipId = UUID.randomUUID();

        AccessRule low = new AccessRule();
        low.setAllowedDays(String.valueOf(LocalDate.now().getDayOfWeek().getValue()));
        low.setValidFromTime(LocalTime.now().minusHours(1));
        low.setValidToTime(LocalTime.now().plusHours(1));
        low.setPriority(1);
        low.setZones(Set.of("A"));

        AccessRule high = new AccessRule();
        high.setAllowedDays(low.getAllowedDays());
        high.setValidFromTime(low.getValidFromTime());
        high.setValidToTime(low.getValidToTime());
        high.setPriority(10);
        high.setZones(Set.of("B"));

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId))
                .thenReturn(List.of(low, high));

        boolean result = accessService.checkAccessRule(membershipId, "B");

        assertTrue(result);
    }

    @Test
    void checkAccessRule_ruleDoesNotAllowZone() {
        UUID membershipId = UUID.randomUUID();

        AccessRule rule = new AccessRule();
        rule.setAllowedDays(String.valueOf(LocalDate.now().getDayOfWeek().getValue()));
        rule.setValidFromTime(LocalTime.now().minusHours(1));
        rule.setValidToTime(LocalTime.now().plusHours(1));
        rule.setPriority(1);
        rule.setZones(Set.of("C"));

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId))
                .thenReturn(List.of(rule));

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertFalse(result);
    }
}
