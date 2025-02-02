package com.example.squiz.services;

import com.example.squiz.dtos.QuizRequest;
import com.example.squiz.dtos.QuizResponse;
import com.example.squiz.dtos.info.QuizInfoResponse;
import com.example.squiz.dtos.info.QuizQuestionInfoResponse;
import com.example.squiz.entities.QuizEB;
import com.example.squiz.exceptions.customExceptions.*;
import com.example.squiz.repos.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Long.parseLong;

@Service
public class QuizService {
    private final QuizRepository quizRepository;

    @Autowired
    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public ResponseEntity<QuizInfoResponse> getQuiz(String id) {
        Optional<QuizEB> optionalQuiz = quizRepository.findQuizById(parseLong(id));

        if (optionalQuiz.isPresent()) {
            QuizInfoResponse quizInfoResponse = new QuizInfoResponse().createQuizInfoResponse(optionalQuiz.get());
            return ResponseEntity.ok(quizInfoResponse);  // Return 200 OK with the quiz data
        } else {
            throw new NotFoundException("Quiz not found with ID: " + id);
        }
    }


    @Transactional
    public ResponseEntity<Integer> createNewQuiz(QuizRequest quizRequest, String username) {
        try {
            QuizEB savedQuiz = quizRepository.save(quizRequest.createQuizEntity(username));
            String identifier = generateQuizIdentifier(username, savedQuiz.getId().toString());
            savedQuiz.setIdentifier(identifier);
            QuizEB updatedQuiz = quizRepository.save(savedQuiz);

            return ResponseEntity.ok(updatedQuiz.getId().intValue());
        } catch (Exception e) {
            throw new BadRequestException("Failed to create quiz: " + e.getMessage());
        }
    }

    public ResponseEntity<QuizResponse> findQuizByIdentifier(String identifier) {
        try {
            QuizEB quiz = quizRepository.findQuizByIdentifier(identifier)
                    .orElseThrow(() -> new BadRequestException("No quiz found for identifier: " + identifier));

            QuizResponse quizResponse = new QuizResponse().createQuizResponse(quiz);
            return ResponseEntity.ok(quizResponse);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while finding the quiz: " + e.getMessage());
        }
    }



    public ResponseEntity<QuizQuestionInfoResponse> findQuestionsByQuiz(String quizId) {
        try {
            QuizEB quiz = quizRepository.findQuizById(parseLong(quizId))
                    .orElseThrow(() -> new BadRequestException("No quiz found for ID: " + quizId));

            QuizQuestionInfoResponse quizResponse = new QuizQuestionInfoResponse().createQuizResponse(quiz);
            return ResponseEntity.ok(quizResponse);

        } catch (BadRequestException e) {
            throw new NotFoundException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid quiz ID format: " + quizId);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching quiz questions: " + e.getMessage());
        }
    }


    public ResponseEntity<Integer> changeQuizState(String quizId, String newState, String username) {
        try {
            Optional<QuizEB> optionalQuiz = quizRepository.findQuizById(parseLong(quizId));

            if (optionalQuiz.isEmpty()) {
                throw new NoContentException("Quiz not found for the given ID: " + quizId);
            }

            QuizEB quiz = optionalQuiz.get();
            if (!quiz.getCreatorId().equals(username)) {
                throw new UnauthorizedException("User is not authorized to change the state of this quiz.");
            }

            quiz.setState(newState);
            QuizEB updatedQuiz = quizRepository.save(quiz);

            return ResponseEntity.ok(updatedQuiz.getId().intValue());
        } catch (Exception e) {
            throw new BadRequestException("An error occurred while changing the quiz state: " + e.getMessage());
        }
    }



    public ResponseEntity<List<QuizResponse>> findQuizzesForUser(String username) {
        try {
            List<QuizEB> quizzes = quizRepository.findQuizByCreatorId(username);

            if (quizzes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ArrayList<>());
            }

            List<QuizResponse> quizResponses = quizzes.stream()
                    .map(result -> new QuizResponse().createQuizResponse(result))
                    .toList();

            return ResponseEntity.ok(quizResponses);
        } catch (Exception e) {
            throw new BadRequestException("An error occurred while fetching quizzes for the user: " + e.getMessage());
        }
    }


    private String generateQuizIdentifier(String username, String quizId) {
        Instant now = Instant.now();
        long randTimestamp = now.getEpochSecond();
        return username + "@" + quizId + randTimestamp;
    }
}
