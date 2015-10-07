/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author salum
 */
public class Person {
    private String firstName;
    private String lastName;
    /**
     * Format: 
     * 0 Not known
     * 1 Male 
     * 2 Female
     * 9 Not specified
     */
    private int gender;
    private LocalDate birthDate;
    private final DateTimeFormatter dateFormatter;


    public Person(String dateFormat) {
        dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGender(String gender) {
        Integer temp = Integer.getInteger(gender);
        if (temp == null) {
            this.gender = 9;
        } else {
            this.gender = temp;
        }
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setBirthDate(String birthDate) {

        this.birthDate = LocalDate.parse(birthDate, dateFormatter);
    }

    public Double verfiy(Person elmoPerson) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

