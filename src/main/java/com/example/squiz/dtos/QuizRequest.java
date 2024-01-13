package com.example.squiz.dtos;

import com.example.squiz.entities.QuizEB;

import java.time.LocalDateTime;

public class QuizRequest {
    private String creatorId;
    private String timeDuration;
    private String state;
    private String dueDate;

    public QuizRequest() {
    }

    public QuizEB createQuizEntity() {
        QuizEB newQuiz = new QuizEB();
        newQuiz.setCreatorId(Integer.parseInt(creatorId));
        newQuiz.setCreatedDate(LocalDateTime.now());
        newQuiz.setModifiedDate(LocalDateTime.now());
        newQuiz.setDueDate(LocalDateTime.parse(dueDate));
        newQuiz.setState(state);
        newQuiz.setTimeDuration(Integer.parseInt(timeDuration));
        return newQuiz;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(String timeDuration) {
        this.timeDuration = timeDuration;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
