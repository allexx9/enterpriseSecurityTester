package com.allexx9.controller;

/**
 * Created by aleksandrsutkov on 26.10.14.
 */
public class Record {
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        if (question != null) {
            this.question = question;
        }
        if (question == null) {
            throw new IllegalArgumentException();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Record(String question, int id, double cost) {
        this.question = question;
        this.id = id;
        this.cost = cost;
    }

    private String question;
    private int id;
    private double cost;

    @Override
    public String toString() {
        return "Record{" +
                "question='" + question + '\'' +
                ", id=" + id +
                ", cost=" + cost +
                '}';
    }
}
