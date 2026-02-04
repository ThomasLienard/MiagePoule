package com.miage.pouleAPI.services.interfaces;

public interface EncryptionService {
    EncryptedData encrypt(byte[] data) throws Exception;
    byte[] decrypt(EncryptedData encryptedData) throws Exception;
    String generateMasterKey() throws Exception;

    class EncryptedData {
        private final byte[] data;
        private final String encryptedKey;
        private final String iv;

        public EncryptedData(byte[] data, String encryptedKey, String iv) {
            this.data = data;
            this.encryptedKey = encryptedKey;
            this.iv = iv;
        }

        public byte[] getData() { return data; }
        public String getEncryptedKey() { return encryptedKey; }
        public String getIv() { return iv; }
    }
}