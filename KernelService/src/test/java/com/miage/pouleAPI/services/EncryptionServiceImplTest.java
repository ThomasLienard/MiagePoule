package com.miage.pouleAPI.services;

import com.miage.pouleAPI.services.interfaces.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceImplTest {

    private EncryptionServiceImpl sut;

    @BeforeEach
    void setUp() {
        sut = new EncryptionServiceImpl();
    }

    @Test
    void generateMasterKey_returnsBase64KeyOf32Bytes() throws Exception {
        String keyBase64 = sut.generateMasterKey();
        assertNotNull(keyBase64);
        byte[] decoded = java.util.Base64.getDecoder().decode(keyBase64);
        assertEquals(32, decoded.length);
    }

    @Test
    void encryptDecrypt_roundTrip_withoutMasterKey() throws Exception {
        ReflectionTestUtils.setField(sut, "masterKeyBase64", "");
        byte[] plain = "this is a secret".getBytes();

        EncryptionService.EncryptedData ed = sut.encrypt(plain);
        assertNotNull(ed);
        assertNotNull(ed.getData());
        assertNotNull(ed.getEncryptedKey());
        assertNotNull(ed.getIv());

        byte[] decrypted = sut.decrypt(ed);
        assertArrayEquals(plain, decrypted);
    }

    @Test
    void encryptDecrypt_roundTrip_withMasterKey() throws Exception {
        // Generate a master key and set it
        String master = sut.generateMasterKey();
        ReflectionTestUtils.setField(sut, "masterKeyBase64", master);

        byte[] plain = "another secret".getBytes();
        EncryptionService.EncryptedData ed = sut.encrypt(plain);
        byte[] decrypted = sut.decrypt(ed);
        assertArrayEquals(plain, decrypted);
    }

    @Test
    void decrypt_withInvalidData_throwsRuntimeException() {
        ReflectionTestUtils.setField(sut, "masterKeyBase64", "");

        EncryptionService.EncryptedData bad = new EncryptionService.EncryptedData("bad".getBytes(), "not-base64", "iv");
        assertThrows(RuntimeException.class, () -> sut.decrypt(bad));
    }
}
