package com.sishuo.bobo_provocation;

import java.util.ArrayList;

// Class for a JSON package for the status of a Player before making their move
// only includes relevant constructors, getters and setters

public class StatusDTO {
    
    private int[] counters;
    private int statusCode; // statusCode = 1 or 2 or 3; 1 = normal; 2 = freezed; 3 = layoffed
    private ArrayList<Integer> availableMoves;

    private int p1moveID;
    private int p2moveID;
    private String roundResult;
    private boolean shouldContinue;

    public StatusDTO(int p2moveID, String roundResult) {
        this.counters = null;
        this.statusCode = -1;
        this.availableMoves = null;
        this.p1moveID = -2;
        this.p2moveID = p2moveID;
        this.roundResult = roundResult;
        this.shouldContinue = false;
    }

    public StatusDTO(int[] counters, int statusCode, ArrayList<Integer> availableMoves) {
        this.counters = counters;
        this.statusCode = statusCode;
        this.availableMoves = availableMoves;
        this.p1moveID = -1;
        this.p2moveID = -1;
        this.roundResult = null;
        this.shouldContinue = true;
    }
    
    public StatusDTO(int[] counters, int statusCode, ArrayList<Integer> availableMoves, int p1moveID, int p2moveID, String roundResult, boolean shouldContinue) {
        this.counters = counters;
        this.statusCode = statusCode;
        this.availableMoves = availableMoves;
        this.p1moveID = p1moveID;
        this.p2moveID = p2moveID;
        this.roundResult = roundResult;
        this.shouldContinue = shouldContinue;
    }


    public int[] getCounters() {
        return this.counters;
    }

    public void setCounters(int[] counters) {
        this.counters = counters;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public ArrayList<Integer> getAvailableMoves() {
        return this.availableMoves;
    }

    public void setAvailableMoves(ArrayList<Integer> availableMoves) {
        this.availableMoves = availableMoves;
    }

    public int getP1moveID() {
        return this.p1moveID;
    }

    public void setP1moveID(int p1moveID) {
        this.p1moveID = p1moveID;
    }

    public int getP2moveID() {
        return this.p2moveID;
    }

    public void setP2moveID(int p2moveID) {
        this.p2moveID = p2moveID;
    }

    public String getRoundResult() {
        return this.roundResult;
    }

    public void setRoundResult(String roundResult) {
        this.roundResult = roundResult;
    }

    public boolean getShouldContinue() {
        return this.shouldContinue;
    }

    public void setShouldContinue(boolean shouldContinue) {
        this.shouldContinue = shouldContinue;
    }


}
