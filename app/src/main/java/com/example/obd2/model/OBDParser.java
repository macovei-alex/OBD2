package com.example.obd2.model;


import android.util.Log;

import com.example.obd2.Utils;

public class OBDParser {

    private static final String TAG = OBDParser.class.getSimpleName();

    private static final int NUMBER_LENGTH = 2;
    private static final int NUMBER_BASE = 16;

    private final int[] positions;


    public OBDParser(OBDSettings[] settings) {
        int startOffset = 4;

        final int enableHeaderPos = Utils.reverseIndexOf(settings, OBDSettings.ENABLE_HEADERS);
        final int disableHeadersPos = Utils.reverseIndexOf(settings, OBDSettings.DISABLE_HEADERS);
        if (enableHeaderPos > disableHeadersPos) {
            startOffset += 5;
        }

        positions = new int[4];
        for (int i = 0; i < 4; i++) {
            positions[i] = startOffset + i * NUMBER_LENGTH;
        }
    }


    public OBDData parseMessage(String message) {
        OBDData data = new OBDData();
        String noWSMessage = removeWhiteSpaces(message);

        if (noWSMessage.length() < positions[0] + NUMBER_LENGTH) {
            Log.e(TAG, "Message too short: " + noWSMessage);
            return data;
        }

        if (noWSMessage.equals("NO DATA")) {
            return data;
        }

        data.a = parseHexNumber(noWSMessage, positions[0]);
        data.b = parseHexNumber(noWSMessage, positions[1]);
        data.c = parseHexNumber(noWSMessage, positions[2]);
        data.d = parseHexNumber(noWSMessage, positions[3]);

        return data;
    }


    private String removeWhiteSpaces(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        if (sb.length() == str.length()) {
            return str;
        }
        return sb.toString();
    }

    /**
     * Parses a number from a hexadecimal string. The parsing starts at the given position and has the
     * length {@link #NUMBER_LENGTH}.
     * @param str The string to parse.
     * @param startPos The position to start parsing.
     * @return The parsed number.
     */
    private int parseHexNumber(String str, int startPos) {
        if (str.length() < startPos + NUMBER_LENGTH) {
            return 0;
        }

        int result = 0;
        for (int i = 0; i < NUMBER_LENGTH; i++) {
            final char c = str.charAt(startPos + i);
            final int digit = Character.digit(c, NUMBER_BASE);
            if (digit != -1) {
                result = result * NUMBER_BASE + digit;
            }
        }

        return result;
    }
}
