package io.tabletoptools.hawthorne.model;

import java.util.Date;

public class AdventurerRegistration {

    private String email;
    private String townName;
    private String ruleTwo;
    private Date birthdate;

    public AdventurerRegistration() {
    }

    public AdventurerRegistration(String email, String townName, String ruleTwo, Date birthdate) {
        this.email = email;
        this.townName = townName;
        this.ruleTwo = ruleTwo;
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getRuleTwo() {
        return ruleTwo;
    }

    public void setRuleTwo(String ruleTwo) {
        this.ruleTwo = ruleTwo;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
}
