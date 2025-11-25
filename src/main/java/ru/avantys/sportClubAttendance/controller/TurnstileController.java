package ru.avantys.sportClubAttendance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.avantys.sportClubAttendance.model.Visit;
import ru.avantys.sportClubAttendance.service.AccessService;
import ru.avantys.sportClubAttendance.service.VisitService;

import java.util.UUID;

@RestController
@RequestMapping("/api/turnstile")
public class TurnstileController {
    private final VisitService visitService;
    private final AccessService accessService;

    public TurnstileController(VisitService visitService, AccessService accessService) {
        this.visitService = visitService;
        this.accessService = accessService;
    }

    @PostMapping("/{zone}/{membershipId}/entry")
    public ResponseEntity<Visit> recordEntry(@PathVariable UUID membershipId, @PathVariable String zone) {
        if (accessService.checkAccessRule(membershipId, zone)) {
            Visit visit = visitService.createVisit(membershipId, zone);
            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @PostMapping("/{membershipId}/exit")
    public ResponseEntity<Visit> recordExit(@PathVariable UUID membershipId) {
        Visit visit = visitService.recordExit(membershipId);
        return ResponseEntity.status(HttpStatus.CREATED).body(visit);
    }
}