package io.twillmott.synct.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;

/**
 * An {@link AttributeConverter} to allow us to symmetrically encrypt sensitive data within the database using AES and
 * a secret provided by a properties source.
 */
@Component
public class DatabaseColumnEncryptor implements AttributeConverter<String, String> {

    private static final String AES = "AES";

    private final Key key;
    private final Cipher cipher;

    // No args constructor for liquibase to use
    public DatabaseColumnEncryptor() {
        key = null;
        cipher = null;
    }

    @Autowired
    public DatabaseColumnEncryptor(@Value("${secrets.database.encryption.secret}") String secret) throws Exception {
        key = new SecretKeySpec(secret.getBytes(), AES);
        cipher = Cipher.getInstance(AES);
    }


    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
