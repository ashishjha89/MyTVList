package com.mytvlist.model;

/**
 * Created by ashish on 20/9/15.
 */
public class Cast {

    private String mCharecterName;

    private Person mPerson;

    public void setCharecterName(String name) {
        mCharecterName = name;
    }

    public String getCharecterName() {
        return mCharecterName;
    }

    public void setPerson(Person person) {
        mPerson = person;
    }

    public Person getPerson() {
        return mPerson;
    }
}
