package ru.avantys.sportClubAttendance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.avantys.sportClubAttendance.exception.MembershipNotFoundException;
import ru.avantys.sportClubAttendance.model.Membership;
import ru.avantys.sportClubAttendance.model.Visit;
import ru.avantys.sportClubAttendance.repository.VisitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VisitService {
    private final VisitRepository visitRepository;
    private final MembershipService membershipService;

    public VisitService(VisitRepository visitRepository, MembershipService membershipService) {
        this.visitRepository = visitRepository;
        this.membershipService = membershipService;
    }

    public Visit createVisit(UUID membershipId, String zone) {
        Membership membership = membershipService.getMembershipById(membershipId)
                .orElseThrow(() -> new MembershipNotFoundException("Membership not found with id: " + membershipId));
        Visit oldVisit = visitRepository.findLastVisitByMembershipId(membershipId).orElse(null);
        closeOldVisit(oldVisit);

        Visit visit = new Visit();
        visit.setMembership(membership);
        visit.setEntryTime(LocalDateTime.now());
        visit.setZone(zone);

        return visitRepository.save(visit);
    }

    @Transactional(readOnly = true)
    public List<Visit> getVisitsByMembership(UUID membershipId) {
        return visitRepository.findByMembershipId(membershipId);
    }

    @Transactional(readOnly = true)
    public long getVisitCountByMembership(UUID membershipId) {
        return visitRepository.countByMembershipId(membershipId);
    }

    @Transactional(readOnly = true)
    public Optional<Visit> getLastVisitByMembership(UUID membershipId) {
        return visitRepository.findLastVisitByMembershipId(membershipId);
    }

    public Visit recordExit(UUID membershipId) {
        Visit lastVisit = visitRepository.findLastVisitByMembershipId(membershipId).orElse(null);
        if (lastVisit == null) return null;

        lastVisit.setExitTime(LocalDateTime.now());
        visitRepository.save(lastVisit);
        return lastVisit;
    }

    private void closeOldVisit(Visit visit) {
        if (visit == null || visit.getExitTime() == null) return;

        visit.setEntryTime(LocalDateTime.now());
        visitRepository.save(visit);
    }

}