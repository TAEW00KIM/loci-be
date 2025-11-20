package com.teamloci.loci.global.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GeoUtils {

    private final static int SCALE = 3;

    public static String generateBeaconId(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;

        BigDecimal lat = BigDecimal.valueOf(latitude).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal lon = BigDecimal.valueOf(longitude).setScale(SCALE, RoundingMode.HALF_UP);

        String rawKey = lat.toString() + "|" + lon.toString();

        return sha256(rawKey);
    }

    private static String sha256(String originalString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
