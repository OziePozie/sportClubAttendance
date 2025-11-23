package ru.avantys.sportClubAttendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<String> recordEntry(@PathVariable UUID membershipId, @PathVariable String zone) {
        if (accessService.checkAccessRule(membershipId, zone)) {
            visitService.createVisit(membershipId, zone);
            return ResponseEntity.ok("Entry recorded successfully");
        }
        return ResponseEntity.badRequest().body("Entry recorded failed");
    }

    @PostMapping("/{membershipId}/exit")
    public ResponseEntity<String> recordExit(@PathVariable UUID membershipId) {
        visitService.recordExit(membershipId);
        return ResponseEntity.ok("Exit recorded successfully");
    }
}