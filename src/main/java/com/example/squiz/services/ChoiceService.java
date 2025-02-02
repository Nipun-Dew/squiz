package com.example.squiz.services;

import com.example.squiz.dtos.ChoiceResponse;
import com.example.squiz.entities.ChoicesEB;
import com.example.squiz.exceptions.customExceptions.BadRequestException;
import com.example.squiz.exceptions.customExceptions.InternalServerErrorException;
import com.example.squiz.exceptions.customExceptions.NoContentException;
import com.example.squiz.exceptions.customExceptions.NotFoundException;
import com.example.squiz.repos.ChoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChoiceService {
    private final ChoiceRepository choiceRepository;

    @Autowired
    public ChoiceService(ChoiceRepository choiceRepository) {
        this.choiceRepository = choiceRepository;
    }

    public ResponseEntity<ChoiceResponse> getChoice(String id) {
        try {
            Long parsedId = Long.parseLong(id);
            Optional<ChoicesEB> optionalChoice = choiceRepository.findById(parsedId);

            return optionalChoice.map(choice -> ResponseEntity.ok(new ChoiceResponse().createChoiceResponse(choice)))
                    .orElseThrow(() -> new NoContentException("No choice found for ID: " + id));

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage());  // Converts 204 to 404
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid choice ID format: " + id);
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching choice: " + e.getMessage());
        }
    }

    public ResponseEntity<List<ChoiceResponse>> getChoicesForQuestion(String questionId) {
        try {
            Long parsedQuestionId = Long.parseLong(questionId);  // Parse the question ID to Long
            List<ChoicesEB> results = choiceRepository.findChoicesByQuestions_Id(parsedQuestionId);

            if (results.isEmpty()) {
                throw new NoContentException("No choices found for question ID: " + questionId);
            }

            List<ChoiceResponse> choiceResponses = results.stream()
                    .map(result -> new ChoiceResponse().createChoiceResponse(result))
                    .toList();

            return ResponseEntity.ok(choiceResponses);

        } catch (NoContentException e) {
            throw new NotFoundException(e.getMessage());  // Converts 204 to 404
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid question ID format: " + questionId);  // Converts invalid ID format to 400
        } catch (Exception e) {
            throw new InternalServerErrorException("An unexpected error occurred while fetching choices: " + e.getMessage());
        }
    }
}
