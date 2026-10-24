package br.dev.luka.esfinge.autenticacao.adapter.out.jdbc;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class CredentialHash {
    private CredentialHash() {}
    public static String salt() {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    public static String password(String password, String salt) {
        var spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 120000, 256);
        try { return HexFormat.of().formatHex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded()); }
        catch (GeneralSecurityException e) { throw new IllegalStateException("Criptografia indisponivel"); }
        finally { spec.clearPassword(); }
    }
    public static String token(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (GeneralSecurityException e) { throw new IllegalStateException("Criptografia indisponivel"); }
    }
    public static boolean matches(String actual, String expected) {
        return MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
