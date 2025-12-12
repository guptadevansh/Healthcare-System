package com.deloitte.User_Service.constants;

public enum Role {

    ADMIN,
    DOCTOR,
    PATIENT;

    public static boolean isValid(String role) {
        try {
            Role.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
