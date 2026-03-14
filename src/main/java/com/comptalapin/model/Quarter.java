package com.comptalapin.model;

public class Quarter {
    private Long id;
    private int year;
    private int number; // 1, 2, 3, 4
    private QuarterStatus status;

    public Quarter() {
        this.status = QuarterStatus.OPEN;
    }

    public Quarter(int year, int number) {
        this.year = year;
        this.number = number;
        this.status = QuarterStatus.OPEN;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public QuarterStatus getStatus() { return status; }
    public void setStatus(QuarterStatus status) { this.status = status; }

    public String getLabel() {
        return "T" + number + " " + year;
    }

    /** Returns the 3 month numbers for this quarter (1-based) */
    public int[] getMonths() {
        int startMonth = (number - 1) * 3 + 1;
        return new int[]{startMonth, startMonth + 1, startMonth + 2};
    }

    @Override
    public String toString() {
        return getLabel() + " (" + status.getLabel() + ")";
    }
}
