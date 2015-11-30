package com.example.shubhankar.pillboxlogic;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Shubhankar on 23-11-2015.
 */
public class AnotherParser {
    public void printScanRecord(byte[] scanRecord) {

        // Simply print all raw bytes
//        try {
//            String decodedRecord = new String(scanRecord, "UTF-8");
//            Log.d("Another Parser", "decoded String : " + ByteArrayToString(scanRecord));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        // Parse data bytes into individual records
        List<AdvertiserRecord> records = AdvertiserRecord.parseScanRecord(scanRecord);

    }


    public static String ByteArrayToString(byte[] ba) {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }


    public static class AdvertiserRecord {

        public AdvertiserRecord(int length, int type, byte[] data) {
//            String decodedRecord = "";
//            try {
//                decodedRecord = new String(data, "UTF-8");
//
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }

            Log.d("Another Parser", "Length: " + length + " Type : " + type + " Data : "
                    + ByteArrayToString(data));

            if (type == -1) {
                Log.d("Manufacturer data", "" + PillBox.ParseData(data));
            }

        }


        public static List<AdvertiserRecord> parseScanRecord(byte[] scanRecord) {
            List<AdvertiserRecord> records = new ArrayList<AdvertiserRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];

                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];

                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

                records.add(new AdvertiserRecord(length, type, data));

                index += length;
            }

            return records;
        }
    }

//    public static String toBinary(String input) {
//
//        String sb = "";
//        for (int i = 0; i < input.length(); i++) {
//            int hexstring = Integer.parseInt(input.substring(i, i + 1), 16);
//            sb = sb.concat("(" + String.format("%04d",
//                    Integer.parseInt(Integer.toBinaryString(hexstring))) + ")");
//        }
//        return sb;
//    }
//
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexArray = "0123456789ABCDEF".toCharArray();
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
}
