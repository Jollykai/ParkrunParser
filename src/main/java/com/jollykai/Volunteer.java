package com.jollykai;

public class Volunteer {
    private final int ID;
    private final String name;
    private int volunteeringCounter;

    public Volunteer(int ID, String name) {
        this.ID = ID;
        this.name = name;
        this.volunteeringCounter = 0;
    }

    @Override
    public String toString() {
        return "Волонтер - " + name + ". Волонтерств всего: " + volunteeringCounter;
    }

    public int getID() {
        return ID;
    }

    public int getVolunteeringCounter() {
        return volunteeringCounter;
    }

    public void setVolunteeringCounter(int volunteeringCounter) {
        this.volunteeringCounter = volunteeringCounter;
    }
}
