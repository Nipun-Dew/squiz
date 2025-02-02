package com.example.squiz.services;

import com.example.squiz.dtos.SessionRequest;
import com.example.squiz.dtos.SessionResponse;
import com.example.squiz.entities.SessionsEB;
import com.example.squiz.entities.AnswersEB;
import com.example.squiz.entities.QuizEB;
import com.example.squiz.exceptions.customExceptions.*;
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
            Long parsedId = Long.parseLong(id);

            SessionsEB session = sessionsRepository.findById(parsedId)
                    .orElseThrow(() -> new NoContentException("No session found with ID: " + id));

            SessionResponse response = new SessionResponse().createSessionResponse(session);
            return ResponseEntity.ok(response);

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid session ID format: " + id);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching session: " + e.getMessage());
        }
    }

    public ResponseEntity<SessionResponse> createNewSession(SessionRequest sessionRequest, String username) {
        try {
            QuizEB quizEntity = quizRepository.findById(sessionRequest.getQuizId().longValue())
                    .orElseThrow(() -> new NotFoundException("No quiz found with ID: " + sessionRequest.getQuizId()));

            SessionsEB savedSession = sessionsRepository.save(sessionRequest.createSessionEntity(quizEntity, username));

            return ResponseEntity.ok(new SessionResponse().createSessionResponse(savedSession));

        } catch (NotFoundException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid quiz ID format: " + sessionRequest.getQuizId());
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while creating a session: " + e.getMessage());
        }
    }

    public ResponseEntity<List<SessionResponse>> getSessionsForQuiz(String quizId) {
        try {
            Long parsedQuizId = Long.parseLong(quizId);
            List<SessionsEB> results = sessionsRepository.findSessionsByQuiz_Id(parsedQuizId);

            if (results.isEmpty()) {
                throw new NoContentException("No sessions found for quiz ID: " + quizId);
            }

            List<SessionResponse> sessionsResponse = results.stream()
                    .map(result -> new SessionResponse().createSessionResponse(result))
                    .toList();

            return ResponseEntity.ok(sessionsResponse);

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage()); // Convert NoContent to NotFound (404)
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid quiz ID format: " + quizId);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching sessions: " + e.getMessage());
        }
    }

    public ResponseEntity<SessionResponse> getSessionForQuizByUserId(String quizId, String username) { // couldn't test properly
        try {
            Long parsedQuizId = Long.parseLong(quizId);
            Optional<SessionsEB> optionalSession = sessionsRepository.getSessionsForQuizByUserId(parsedQuizId, username);

            return optionalSession.map(session ->
                            ResponseEntity.ok(new SessionResponse().createSessionResponse(session)))
                    .orElseThrow(() -> new NoContentException("No session found for quiz ID: " + quizId + " and user: " + username));

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage()); // Converts 204 to 404
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid quiz ID format: " + quizId);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching session: " + e.getMessage());
        }
    }

    public ResponseEntity<Integer> getPointsForSession(String sessionId) {
        try {
            Long parsedSessionId = Long.parseLong(sessionId);
            List<AnswersEB> results = answersRepository.findAnswersBySession_Id(parsedSessionId);

            if (results.isEmpty()) {
                throw new NoContentException("No answers found for session ID: " + sessionId);
            }

            int points = (int) results.stream().filter(AnswersEB::getIsCorrectAnswer).count();
            return ResponseEntity.ok(points);

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage()); // Converts 204 to 404
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid session ID format: " + sessionId);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching session points: " + e.getMessage());
        }
    }

    public ResponseEntity<Integer> completeSession(String sessionId, String userName) {
        try {
            Optional<SessionsEB> optionalSession = sessionsRepository.findById(Long.parseLong(sessionId));

            if (optionalSession.isEmpty()) {
                throw new NoContentException("There is no session with this session id");

            }

            SessionsEB session = optionalSession.get();
            if (!session.getUserId().equals(userName)) {
                throw new UnauthorizedException("You are not authorized to complete this session");
            }

            session.setCompleted(true);
            SessionsEB savedSession = sessionsRepository.save(session);

            return ResponseEntity.ok(savedSession.getId().intValue());
        } catch (NoContentException e) {
            throw new NoContentException(e.getMessage());
        }
//        catch (UnauthorizedException e) {
//            throw new UnauthorizedException(e.getMessage());
//        }
        catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while completing the session: " + e.getMessage());
        }
    }
}
