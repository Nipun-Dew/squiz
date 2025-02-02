package com.example.squiz.controllers;

import com.example.squiz.dtos.QuestionsRequest;
import com.example.squiz.dtos.info.QuestionsInfoRequest;
import com.example.squiz.dtos.info.QuestionsInfoResponse;
import com.example.squiz.services.QuestionsService;
import com.example.squiz.utils.UserDetailsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "${api.prefix}")
public class QuestionsController implements UserDetailsUtil {
    private final QuestionsService questionsService;

    @Autowired
    public QuestionsController(QuestionsService questionsService) {
        this.questionsService = questionsService;
    }

    @GetMapping(value = "/question/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<QuestionsInfoResponse> getQuestionById(@PathVariable String id) {
        return questionsService.getQuestionInfo(id);
    }

    @GetMapping(value = "/question", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<QuestionsInfoResponse> getQuestionInfoWithAnswer(@RequestParam(defaultValue = "0") String questionId,
                                                                           @RequestParam(defaultValue = "0") String sessionId) {
        return questionsService.getQuestionInfoWithAnswer(questionId, sessionId);
    }

    @GetMapping(value = "/question/quiz/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<QuestionsInfoResponse>> getQuestionsForQuiz(@PathVariable String id) {
        return questionsService.findQuestionsByQuiz(id);
    }

    @PostMapping(value = "/question", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Integer> createQuestion(@RequestBody QuestionsInfoRequest question,
                                                  Authentication authentication) {
        String username = extractUser(authentication);
        return questionsService.createNewQuestion(question, username);
    }

    @PutMapping(value = "/question", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Integer> updateQuestion(@RequestBody QuestionsInfoRequest question,
                                                  Authentication authentication) {
        String username = extractUser(authentication);
        return questionsService.updateQuestion(question, username);
    }
}
