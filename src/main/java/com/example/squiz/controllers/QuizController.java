package com.example.squiz.controllers;

import com.example.squiz.dtos.QuizRequest;
import com.example.squiz.dtos.QuizResponse;
import com.example.squiz.dtos.info.QuizInfoResponse;
import com.example.squiz.dtos.info.QuizQuestionInfoResponse;
import com.example.squiz.services.QuizService;
import com.example.squiz.utils.AuthenticationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "${api.prefix}")
public class QuizController implements AuthenticationUtil {
    private final QuizService service;

    @Autowired
    public QuizController(QuizService service) {
        this.service = service;
    }

    @GetMapping(value = "/quiz/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<QuizInfoResponse> getQuizById(@PathVariable String id) {
        return service.getQuiz(id);
    }

    @GetMapping(value = "/quizzes", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<QuizResponse>> getQuizzesForCreator(Authentication authentication) {
        String username = getUsername(authentication);
        return service.findQuizzesForUser(username);
    }

    @GetMapping(value = "/quiz/questions/{quizId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<QuizQuestionInfoResponse> getQuestionsMetadata(@PathVariable String quizId) {
        return service.findQuestionsByQuiz(quizId);
    }

    @PostMapping(value = "/quiz/play", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<QuizResponse> getQuizForIdentifier(@RequestBody Map<String, String> requestBody) {
        String identifier = requestBody.get("identifier");
        return service.findQuizByIdentifier(identifier);
    }

    @PostMapping(value = "/quiz/publish", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Integer> publishQuiz(@RequestBody Map<String, String> requestBody, Authentication authentication) {
        String username = getUsername(authentication);
        return service.changeQuizState(requestBody.get("quizId"), "PUBLISHED", username);
    }

    @PostMapping("/quiz")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Integer> createQuiz(@RequestBody QuizRequest quiz, Authentication authentication) {
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        }
        return service.createNewQuiz(quiz, username);
    }
}
