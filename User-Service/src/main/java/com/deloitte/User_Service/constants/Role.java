package com.deloitte.User_Service.constants;

public enum Role {

    ADMIN,
    PROVIDER,
    PATIENT,
    OPS;

    public static boolean isValid(String role) {
        try {
            Role.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
