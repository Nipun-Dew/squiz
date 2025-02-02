package com.example.squiz.controllers;

import com.example.squiz.dtos.ChoiceResponse;
import com.example.squiz.services.ChoiceService;
import com.example.squiz.utils.UserDetailsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "${api.prefix}")
public class ChoiceController implements UserDetailsUtil {
    private final ChoiceService choiceService;

    @Autowired
    public ChoiceController(ChoiceService choiceService) {
        this.choiceService = choiceService;
    }

    @GetMapping(value = "/choice/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<ChoiceResponse> getChoiceById(@PathVariable String id) {
        return choiceService.getChoice(id);
    }

    @GetMapping(value = "/choice/question/{questionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<ChoiceResponse>> getChoicesForQuestion(@PathVariable String questionId) {
        return choiceService.getChoicesForQuestion(questionId);
    }
}
