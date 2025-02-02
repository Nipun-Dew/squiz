package com.example.squiz.services;

import com.example.squiz.dtos.AnswersResponse;
import com.example.squiz.dtos.info.QuestionsInfoRequest;
import com.example.squiz.dtos.info.QuestionsInfoResponse;
import com.example.squiz.entities.AnswersEB;
import com.example.squiz.entities.ChoicesEB;
import com.example.squiz.entities.QuestionsEB;
import com.example.squiz.entities.QuizEB;
import com.example.squiz.exceptions.customExceptions.*;
import com.example.squiz.repos.AnswersRepository;
import com.example.squiz.repos.ChoiceRepository;
import com.example.squiz.repos.QuestionsRepository;
import com.example.squiz.repos.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Long.parseLong;

@Service
public class QuestionsService {
    private final QuestionsRepository questionsRepository;
    private final QuizRepository quizRepository;
    private final ChoiceRepository choiceRepository;
    private final AnswersRepository answersRepository;

    @Autowired
    public QuestionsService(QuestionsRepository questionsRepository,
                            QuizRepository quizRepository,
                            ChoiceRepository choiceRepository, AnswersRepository answersRepository) {
        this.questionsRepository = questionsRepository;
        this.quizRepository = quizRepository;
        this.choiceRepository = choiceRepository;
        this.answersRepository = answersRepository;
    }

    public ResponseEntity<QuestionsInfoResponse> getQuestionInfo(String id) {
        try {
            QuestionsEB question = questionsRepository.findById(parseLong(id))
                    .orElseThrow(() -> new NoContentException("No question found with ID: " + id));

            QuestionsInfoResponse response = new QuestionsInfoResponse().createQuestionInfoResponse(question);
            return ResponseEntity.ok(response);

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid question ID format: " + id);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching question info: " + e.getMessage());
        }
    }

    public ResponseEntity<QuestionsInfoResponse> getQuestionInfoWithAnswer(String questionId, String sessionId) {
        try {
            QuestionsEB question = questionsRepository.findById(parseLong(questionId))
                    .orElseThrow(() -> new NoContentException("No question found with ID: " + questionId));

            Optional<AnswersEB> optionalAnswer = answersRepository.findByQuestionIdAndSessionId(parseLong(questionId), parseLong(sessionId));

            QuestionsInfoResponse questionsInfoResponse = new QuestionsInfoResponse().createQuestionInfoResponse(question);

            if (optionalAnswer.isPresent()) {
                List<AnswersResponse> answersResponse = new ArrayList<>();
                answersResponse.add(new AnswersResponse().createAnswerResponse(optionalAnswer.get()));
                questionsInfoResponse.setAnswers(answersResponse);
            } else {
                questionsInfoResponse.setAnswers(new ArrayList<>());
            }

            return ResponseEntity.ok(questionsInfoResponse);

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid question ID or session ID format: " + questionId + ", " + sessionId);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching question info with answers: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<Integer> createNewQuestion(QuestionsInfoRequest questionsInfoRequest, String username) {
        try {
            QuizEB quizEntity = quizRepository.findById(questionsInfoRequest.getQuestion().getQuizId().longValue())
                    .orElseThrow(() -> new NotFoundException("Quiz not found for ID: " + questionsInfoRequest.getQuestion().getQuizId()));

            if (!quizEntity.getCreatorId().equals(username)) {
                throw new UnauthorizedException("User is not the creator of the relevant Quiz.");
            }

            QuestionsEB savedQuestion = questionsRepository.save(questionsInfoRequest.getQuestion().createQuestionEntity(quizEntity));

            choiceRepository.saveAll(questionsInfoRequest.getAnswers().stream()
                    .map(req -> req.createChoiceEntity(savedQuestion))
                    .toList());

            return ResponseEntity.ok(savedQuestion.getId().intValue());

        } catch (NotFoundException | UnauthorizedException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid quiz ID format: " + questionsInfoRequest.getQuestion().getQuizId());
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while creating a new question: " + e.getMessage());
        }
    }

    public ResponseEntity<Integer> updateQuestion(QuestionsInfoRequest questionInfoRequest, String username) {
        try {
            QuizEB quizEntity = quizRepository.findById(questionInfoRequest.getQuestion().getQuizId().longValue())
                    .orElseThrow(() -> new NotFoundException("Quiz not found for ID: " + questionInfoRequest.getQuestion().getQuizId()));

            if (!quizEntity.getCreatorId().equals(username)) {
                throw new UnauthorizedException("User is not the creator of the relevant Quiz.");
            }

            QuestionsEB questionEntity = questionInfoRequest.getQuestion().createQuestionEntity(quizEntity);
            questionEntity.setId(questionInfoRequest.getQuestion().getQuestionId());

            QuestionsEB savedQuestion = questionsRepository.save(questionEntity);

            List<ChoicesEB> choices = questionInfoRequest.getAnswers().stream().map(choice -> {
                ChoicesEB choiceEntity = choice.createChoiceEntity(savedQuestion);
                choiceEntity.setId(choice.getChoiceId());
                return choiceEntity;
            }).toList();

            choiceRepository.saveAll(choices);

            return ResponseEntity.ok(savedQuestion.getId().intValue());

        } catch (NotFoundException | UnauthorizedException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid quiz ID format: " + questionInfoRequest.getQuestion().getQuizId());
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while updating the question: " + e.getMessage());
        }
    }

    public ResponseEntity<List<QuestionsInfoResponse>> findQuestionsByQuiz(String quizId) {
        try {
            if (!quizId.matches("\\d+")) {
                throw new BadRequestException("Invalid quiz ID format: " + quizId);
            }

            Long parsedQuizId = parseLong(quizId);

            QuizEB quiz = quizRepository.findById(parsedQuizId)
                    .orElseThrow(() -> new NotFoundException("Quiz not found for ID: " + quizId));

            List<QuestionsEB> results = questionsRepository.findQuestionsByQuiz_Id(parsedQuizId);

            if (results.isEmpty()) {
                throw new NoContentException("No questions found for quiz ID: " + quizId);
            }

            List<QuestionsInfoResponse> questionResponses = results.stream()
                    .map(result -> new QuestionsInfoResponse().createQuestionInfoResponse(result))
                    .toList();

            return ResponseEntity.ok(questionResponses);

        } catch (BadRequestException | NotFoundException | NoContentException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching questions: " + e.getMessage());
        }
    }
}
