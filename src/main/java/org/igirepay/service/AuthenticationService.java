
package org.igirepay.service;

import org.igirepay.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AuthenticationService {


    private final SecureRandom random = new SecureRandom();


    public void createPIN(int accountId, String pin)
            throws Exception {

        checkPIN(pin);


        byte[] salt = new byte[16];
        random.nextBytes(salt);

        String saltText =
                Base64.getEncoder().encodeToString(salt);


        String pinHash = hashPIN(pin, salt);

        String sql =
                "UPDATE accounts SET pin_salt = ?, pin_hash = ? " +
                        "WHERE account_id = ? AND pin_hash IS NULL";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, saltText);
            statement.setString(2, pinHash);
            statement.setInt(3, accountId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalStateException(
                        "Account does not exist or already has a PIN."
                );
            }
        }
    }

    public boolean validatePIN(int accountId, String pin)
            throws Exception {

        checkPIN(pin);

        String sql =
                "SELECT pin_salt, pin_hash FROM accounts " +
                        "WHERE account_id = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "Account does not exist."
                    );
                }

                String savedSalt = result.getString("pin_salt");
                String savedHash = result.getString("pin_hash");

                if (savedSalt == null || savedHash == null) {
                    return false;
                }


                byte[] salt =
                        Base64.getDecoder().decode(savedSalt);

                String enteredHash = hashPIN(pin, salt);


                return java.security.MessageDigest.isEqual(
                        savedHash.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8),
                        enteredHash.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8)
                );
            }
        }
    }


    public void changePIN(int accountId,
                          String oldPIN,
                          String newPIN) throws Exception {


        checkPIN(newPIN);


        if (oldPIN.equals(newPIN)) {
            throw new IllegalArgumentException(
                    "The new PIN must be different."
            );
        }


        if (!validatePIN(accountId, oldPIN)) {
            throw new IllegalArgumentException(
                    "The old PIN is incorrect."
            );
        }


        byte[] salt = new byte[16];
        random.nextBytes(salt);

        String saltText =
                Base64.getEncoder().encodeToString(salt);

        String newHash = hashPIN(newPIN, salt);

        String sql =
                "UPDATE accounts SET pin_salt = ?, pin_hash = ? " +
                        "WHERE account_id = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, saltText);
            statement.setString(2, newHash);
            statement.setInt(3, accountId);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Account does not exist."
                );
            }
        }
    }


    private void checkPIN(String pin) {

        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException(
                    "PIN must contain exactly four digits."
            );
        }
    }


    private String hashPIN(String pin, byte[] salt)
            throws Exception {

        PBEKeySpec specification = new PBEKeySpec(
                pin.toCharArray(), salt, 600000, 256
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            byte[] hash = factory.generateSecret(
                    specification
            ).getEncoded();

            return Base64.getEncoder()
                    .encodeToString(hash);

        } finally {
            specification.clearPassword();
        }
    }
}
