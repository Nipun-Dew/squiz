package com.example.squiz.controllers;

import com.example.squiz.services.SessionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${api.prefix}")
public class PointsController {
    private final SessionsService sessionsService;

    @Autowired
    public PointsController(SessionsService sessionsService) {
        this.sessionsService = sessionsService;
    }

    @GetMapping(value = "/points/session/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Integer> getPointsBySessionId(@PathVariable String id) {
        return sessionsService.getPointsForSession(id);
    }
}
