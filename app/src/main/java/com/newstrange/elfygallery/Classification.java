package com.newstrange.elfygallery;

public class Classification {
    public final float gender;
    public final float age;

    public Classification(float gender, float age) {
        this.gender = gender;
        this.age = age;
    }


    @Override
    public String toString() {
        return gender + " " + String.format("(%.1f%%) ", age * 100.0f);
    }

}
