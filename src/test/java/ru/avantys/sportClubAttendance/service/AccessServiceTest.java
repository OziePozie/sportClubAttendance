package ru.avantys.sportClubAttendance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.avantys.sportClubAttendance.dto.AccessRuleDto;
import ru.avantys.sportClubAttendance.exception.AccessRuleNotFoundException;
import ru.avantys.sportClubAttendance.exception.MembershipNotFoundException;
import ru.avantys.sportClubAttendance.model.AccessRule;
import ru.avantys.sportClubAttendance.model.Membership;
import ru.avantys.sportClubAttendance.repository.AccessRuleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccessRuleRepository accessRuleRepository;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private AccessService accessService;

    /**
     * Тестирует успешное создание правила доступа.
     * Проверяет, что при корректных входных данных правило создается,
     * сохраняется в репозитории и все его поля правильно устанавливаются.
     */
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

        when(accessRuleRepository.save(any(AccessRule.class)))
                .thenAnswer(inv -> {
                    AccessRule rule = inv.getArgument(0);
                    rule.setId(UUID.randomUUID()); // Симулируем сохранение с ID
                    return rule;
                });

        AccessRule result = accessService.createAccessRule(dto, membershipId);

        assertEquals(membership, result.getMembership());
        assertEquals(Set.of("A", "B"), result.getZones());
        assertEquals("12345", result.getAllowedDays());
        assertNotNull(result.getId());
    }

    /**
     * Тестирует попытку создания правила доступа с несуществующим абонементом.
     * Проверяет, что при отсутствии абонемента выбрасывается исключение MembershipNotFoundException.
     */
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

        assertThrows(MembershipNotFoundException.class,
                () -> accessService.createAccessRule(dto, membershipId));
    }

    /**
     * Тестирует получение списка правил доступа по идентификатору абонемента.
     * Проверяет, что метод корректно возвращает все правила, связанные с указанным абонементом.
     */
    @Test
    void getAccessRulesByMembership_success() {
        UUID membershipId = UUID.randomUUID();

        AccessRule rule1 = new AccessRule();
        rule1.setId(UUID.randomUUID());
        AccessRule rule2 = new AccessRule();
        rule2.setId(UUID.randomUUID());
        List<AccessRule> rules = List.of(rule1, rule2);

        when(accessRuleRepository.findByMembershipId(membershipId)).thenReturn(rules);

        List<AccessRule> result = accessService.getAccessRulesByMembership(membershipId);

        assertEquals(2, result.size());
        verify(accessRuleRepository).findByMembershipId(membershipId);
    }

    /**
     * Тестирует успешное удаление правила доступа.
     * Проверяет, что при существующем идентификаторе правило удаляется из репозитория.
     */
    @Test
    void deleteAccessRule_success() {
        UUID id = UUID.randomUUID();

        when(accessRuleRepository.existsById(id)).thenReturn(true);

        accessService.deleteAccessRule(id);

        verify(accessRuleRepository).existsById(id);
        verify(accessRuleRepository).deleteById(id);
    }

    /**
     * Тестирует попытку удаления несуществующего правила доступа.
     * Проверяет, что при отсутствии правила выбрасывается исключение AccessRuleNotFoundException
     * и метод deleteById не вызывается.
     */
    @Test
    void deleteAccessRule_notFound() {
        UUID id = UUID.randomUUID();

        when(accessRuleRepository.existsById(id)).thenReturn(false);

        assertThrows(AccessRuleNotFoundException.class,
                () -> accessService.deleteAccessRule(id));

        verify(accessRuleRepository, never()).deleteById(id);
    }

    /**
     * Тестирует проверку доступа при отсутствии активных правил.
     * Проверяет, что если для активного абонемента нет правил доступа,
     * доступ к любой зоне запрещен.
     */
    @Test
    void checkAccessRule_noValidRules() {
        UUID membershipId = UUID.randomUUID();

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId)).thenReturn(List.of());

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertFalse(result);
        verify(accessRuleRepository).findByMembershipId(membershipId);
    }

    /**
     * Тестирует проверку доступа при наличии валидного правила, разрешающего доступ к зоне.
     * Проверяет, что при выполнении всех условий (активный абонемент, правильный день недели,
     * текущее время в разрешенном интервале) доступ к указанной зоне разрешен.
     */
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

    /**
     * Тестирует приоритетность правил доступа.
     * Проверяет, что при наличии нескольких валидных правил выбирается правило
     * с наивысшим приоритетом, и доступ определяется именно этим правилом.
     */
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

    /**
     * Тестирует проверку доступа к зоне, которая не указана в правиле.
     * Проверяет, что даже при валидном правиле доступ запрещен, если
     * запрашиваемая зона отсутствует в списке разрешенных зон правила.
     */
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

    /**
     * Тестирует обработку правил с высоким приоритетом, но невалидных по времени.
     * Проверяет, что правило с высоким приоритетом, но невалидное по времени,
     * игнорируется, и выбирается следующее валидное правило с более низким приоритетом.
     */
    @Test
    void checkAccessRule_highPriorityButInvalidTime_shouldSkipInvalid() {
        UUID membershipId = UUID.randomUUID();

        // Это правило должно быть выбрано, т.к второе невалидно по времени
        AccessRule lowPriorityValid = createRule(
                Set.of("A"),
                LocalTime.now().minusHours(2),
                LocalTime.now().plusHours(2),
                1
        );

        // Это правило имеет высокий приоритет, но невалидно по времени (сейчас не 1-2 часа ночи)
        AccessRule highPriorityInvalid = createRule(
                Set.of("B"),
                LocalTime.of(1, 0),
                LocalTime.of(2, 0),
                100
        );

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId))
                .thenReturn(List.of(lowPriorityValid, highPriorityInvalid));

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertTrue(result);

        // Проверим, что зона B не доступна
        boolean resultForB = accessService.checkAccessRule(membershipId, "B");
        assertFalse(resultForB);
    }

    /**
     * Тестирует проверку доступа при несоответствии дня недели.
     * Проверяет, что доступ запрещен, если текущий день недели
     * не входит в список разрешенных дней правила, даже если
     * все остальные условия выполнены.
     */
    @Test
    void checkAccessRule_invalidDayOfWeek_shouldReturnFalse() {
        UUID membershipId = UUID.randomUUID();

        AccessRule rule = new AccessRule();
        // Устанавливаем день, отличный от текущего
        int currentDay = LocalDate.now().getDayOfWeek().getValue();
        int differentDay = (currentDay % 7) + 1; // Следующий день
        rule.setAllowedDays(String.valueOf(differentDay));
        rule.setValidFromTime(LocalTime.now().minusHours(1));
        rule.setValidToTime(LocalTime.now().plusHours(1));
        rule.setPriority(1);
        rule.setZones(Set.of("A"));

        when(membershipService.isActiveMembership(membershipId)).thenReturn(true);
        when(accessRuleRepository.findByMembershipId(membershipId))
                .thenReturn(List.of(rule));

        boolean result = accessService.checkAccessRule(membershipId, "A");

        assertFalse(result);
    }

    /**
     * Вспомогательный метод для создания тестового правила доступа.
     * Создает правило с указанными параметрами и текущим днем недели.
     *
     * @param zones список разрешенных зон
     * @param from время начала действия правила
     * @param to время окончания действия правила
     * @param priority приоритет правила
     * @return созданное правило доступа
     */
    private AccessRule createRule(Set<String> zones, LocalTime from, LocalTime to, int priority) {
        AccessRule r = new AccessRule();
        r.setZones(zones);
        r.setValidFromTime(from);
        r.setValidToTime(to);
        r.setAllowedDays(String.valueOf(LocalDate.now().getDayOfWeek().getValue()));
        r.setPriority(priority);
        return r;
    }
}