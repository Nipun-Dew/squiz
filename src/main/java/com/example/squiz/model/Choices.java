package com.example.squiz.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity @Table(name="t_choices")
public class Choices {
    @Id @SequenceGenerator(name = "choices_sequence", sequenceName = "choices_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "choices_sequence")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private Questions questions;

    @OneToOne(mappedBy = "choices")
    private Answers answers;

    private String choiceNumber;
    private String choiceText;
    private String helperText;
    private Boolean attachments;
    private Boolean correctAnswer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Questions getQuestions() {
        return questions;
    }

    public void setQuestions(Questions questions) {
        this.questions = questions;
    }

    public Answers getAnswers() {
        return answers;
    }

    public void setAnswers(Answers answers) {
        this.answers = answers;
    }

    public String getChoiceNumber() {
        return choiceNumber;
    }

    public void setChoiceNumber(String choiceNumber) {
        this.choiceNumber = choiceNumber;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public String getHelperText() {
        return helperText;
    }

    public void setHelperText(String helperText) {
        this.helperText = helperText;
    }

    public Boolean getAttachments() {
        return attachments;
    }

    public void setAttachments(Boolean attachments) {
        this.attachments = attachments;
    }

    public Boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
