package com.example.squiz.services;

import com.example.squiz.dtos.SessionRequest;
import com.example.squiz.dtos.SessionResponse;
import com.example.squiz.entities.SessionsEB;
import com.example.squiz.entities.AnswersEB;
import com.example.squiz.entities.QuizEB;
import com.example.squiz.repos.SessionsRepository;
import com.example.squiz.repos.AnswersRepository;
import com.example.squiz.repos.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SessionsService {
    private final SessionsRepository sessionsRepository;
    private final QuizRepository quizRepository;
    private final AnswersRepository answersRepository;

    @Autowired
    public SessionsService(SessionsRepository sessionsRepository,
                           QuizRepository quizRepository, AnswersRepository answersRepository) {
        this.sessionsRepository = sessionsRepository;
        this.quizRepository = quizRepository;
        this.answersRepository = answersRepository;
    }

    public ResponseEntity<SessionResponse> getSession(String id) {
        try {
            Optional<SessionsEB> optionalSession = sessionsRepository.findById(Long.parseLong(id));

            return optionalSession
                    .map(session -> ResponseEntity.ok(new SessionResponse().createSessionResponse(session)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new SessionResponse()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SessionResponse());
        }
    }

    public ResponseEntity<SessionResponse> createNewSession(SessionRequest sessionRequest, String username) {
        try {
            QuizEB quizEntity = quizRepository.findById(sessionRequest.getQuizId().longValue())
                    .orElseThrow();
            SessionsEB savedSession = sessionsRepository
                    .save(sessionRequest.createSessionEntity(quizEntity, username));
            return ResponseEntity.ok(new SessionResponse().createSessionResponse(savedSession));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SessionResponse());
        }
    }

    public ResponseEntity<List<SessionResponse>> getSessionsForQuiz(String quizId) {
        try {
            List<SessionsEB> results = sessionsRepository.findSessionsByQuiz_Id(Long.parseLong(quizId));
            List<SessionResponse> sessionsResponse = results.stream()
                    .map(result -> new SessionResponse().createSessionResponse(result))
                    .toList();
            return ResponseEntity.ok(sessionsResponse);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
    }

    public ResponseEntity<SessionResponse> getSessionForQuizByUserId(String quizId, String username) {
        try {
            Optional<SessionsEB> optionalSession = sessionsRepository.getSessionsForQuizByUserId(Long.parseLong(quizId), username);
            return optionalSession.map(session ->
                            ResponseEntity.ok(new SessionResponse().createSessionResponse(session)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(new SessionResponse()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SessionResponse());
        }
    }

    public ResponseEntity<Integer> getPointsForSession(String sessionId) {
        try {
            List<AnswersEB> results = answersRepository.findAnswersBySession_Id(Long.parseLong(sessionId));
            Integer points = results.stream().filter(AnswersEB::getIsCorrectAnswer).toList().size();

            return ResponseEntity.ok(points);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1);
        }
    }

    public ResponseEntity<Integer> completeSession(String sessionId, String userName) {
        try {
            Optional<SessionsEB> optionalSession = sessionsRepository.findById(Long.parseLong(sessionId));

            if (optionalSession.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(-1);
            }

            SessionsEB session = optionalSession.get();
            if (!session.getUserId().equals(userName)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(-1);
            }

            session.setCompleted(true);
            SessionsEB savedSession = sessionsRepository.save(session);

            return ResponseEntity.ok(savedSession.getId().intValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
