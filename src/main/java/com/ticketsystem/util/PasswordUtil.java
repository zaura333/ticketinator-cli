package com.ticketsystem.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * Work factor 12 provides a good balance of security and performance.
 */
public final class PasswordUtil {

    private static final int BCRYPT_WORK_FACTOR = 12;

    private PasswordUtil() {}

    /**
     * Hashes a plain-text password with BCrypt.
     *
     * @param plainPassword raw password from user input
     * @return BCrypt hash (60 characters)
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword  raw password from user input
     * @param hashedPassword stored hash from the database
     * @return true if the password matches
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
