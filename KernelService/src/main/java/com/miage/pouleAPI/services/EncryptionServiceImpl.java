package com.miage.pouleAPI.services;

import com.miage.pouleAPI.services.interfaces.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionServiceImpl implements EncryptionService {

    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_SIZE = 256;
    private static final int IV_LENGTH = 12;

    @Value("${app.encryption.master-key:}")
    private String masterKeyBase64;

    @Override
    public EncryptedData encrypt(byte[] data) throws Exception {
        try {
            // Générer une clé unique pour ce document
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE);
            SecretKey documentKey = keyGenerator.generateKey();

            // Générer un IV unique
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            // Chiffrer les données
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, documentKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(data);

            // Chiffrer la clé du document avec la clé maître
            String encryptedDocumentKey = encryptKey(documentKey.getEncoded());

            return new EncryptedData(encryptedData, encryptedDocumentKey, Base64.getEncoder().encodeToString(iv));

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    @Override
    public byte[] decrypt(EncryptedData encryptedData) throws Exception {
        try {
            // Déchiffrer la clé du document
            byte[] documentKeyBytes = decryptKey(encryptedData.getEncryptedKey());
            SecretKey documentKey = new SecretKeySpec(documentKeyBytes, "AES");

            // Déchiffrer les données
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            byte[] iv = Base64.getDecoder().decode(encryptedData.getIv());
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, documentKey, parameterSpec);

            return cipher.doFinal(encryptedData.getData());

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    @Override
    public String generateMasterKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey masterKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(masterKey.getEncoded());
    }

    private String encryptKey(byte[] keyToEncrypt) throws Exception {
        if (masterKeyBase64 == null || masterKeyBase64.isEmpty()) {
            return Base64.getEncoder().encodeToString(keyToEncrypt);
        }

        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKeyBase64);
        SecretKey masterKey = new SecretKeySpec(masterKeyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);

        byte[] encryptedKey = cipher.doFinal(keyToEncrypt);
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    private byte[] decryptKey(String encryptedKeyBase64) throws Exception {
        if (masterKeyBase64 == null || masterKeyBase64.isEmpty()) {
            // Mode développement sans clé maître
            return Base64.getDecoder().decode(encryptedKeyBase64);
        }

        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKeyBase64);
        SecretKey masterKey = new SecretKeySpec(masterKeyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, masterKey);

        byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyBase64);
        return cipher.doFinal(encryptedKey);
    }
}