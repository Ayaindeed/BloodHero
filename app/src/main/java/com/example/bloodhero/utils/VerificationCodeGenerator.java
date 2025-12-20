package com.example.bloodhero.utils;

import java.security.SecureRandom;

/**
 * Utility class for generating verification codes for donation completion
 */
public class VerificationCodeGenerator {
    
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluding similar looking chars
    private static final int CODE_LENGTH = 4;
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate a random 4-character verification code
     * @return A 4-character alphanumeric code
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
    
    /**
     * Validate a verification code format
     * @param code The code to validate
     * @return true if code is 4 characters and matches allowed characters
     */
    public static boolean isValidFormat(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }
        return code.toUpperCase().matches("[" + CHARACTERS + "]{" + CODE_LENGTH + "}");
    }
}
