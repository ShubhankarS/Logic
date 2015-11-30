package com.example.shubhankar.pillboxlogic;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Shubhankar on 23-11-2015.
 */
public class PillBox {
    byte[] data;
    BluetoothDevice device;
    int rssi;
    String deviceName;
    String address;
    ParcelUuid[] UUID;
    static String[] schedule = {"0", "0", "1", "0", "1", "0", "1", "0"};
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public PillBox(BluetoothDevice device, int rssi, byte[] data) {
        this.device = device;
        this.rssi = rssi;
        this.data = data;
        this.deviceName = device.getName();
        this.address = device.getAddress();
        this.UUID = device.getUuids();
    }


    public static String ParseData(byte[] data) {
        String hex = bytesToHex(data);
        String bin = toBinary(hex);
//        String bin = bytesToBinary(data);
        makeArray(bin);
        return bin;
    }

    public static String toBinary(String input) {

        String sb = "";
        for (int i = 0; i < input.length(); i++) {
            int hexstring = Integer.parseInt(input.substring(i, i + 1), 16);
//            sb = sb.concat("(" + String.format("%04d",
//                    Integer.parseInt(Integer.toBinaryString(hexstring))) + ")");
            sb = sb.concat(String.format("%04d",
                    Integer.parseInt(Integer.toBinaryString(hexstring))) + "");
        }
        return sb;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToBinary(byte[] bytes) {
        String BinChars = "";
        for (int z = 0; z < bytes.length; z++) {
            BinChars = BinChars.concat(String.format("%08d",
                    Integer.parseInt(Integer.toBinaryString(Integer.parseInt(String.valueOf(bytes[z]))))));
        }
        return BinChars;
    }

    public static void makeArray(String binaryweek) {

        //get the number of points to be read
        int count = pointsToRead();

        //get required data
        StringBuffer needed = new StringBuffer();
        needed.append(binaryweek.substring(0, count).toString());
        Log.d("Found required string", "" + needed.toString());

        //reverse to get oldest first
        needed.reverse();
        Log.d("Reversed needed string", "" + needed.toString());

        //no of day arrays to make
//        int noOfDays = count % 8 == 0 ? (int) count / 8 : 1 + (int) count / 8;
        int noOfDays = (int) Math.ceil((double) count / 8);
        Log.d("No of days needed", "" + noOfDays);

        //make date array for days
        ArrayList<String> days = new ArrayList<>();
        Date now = new Date();
        Calendar Temp = Calendar.getInstance();

        for (int k = 0; k < noOfDays; k++) {
            Temp.setTime(now);
            Temp.add(Calendar.DATE, -noOfDays + k + 1);
            String date = "";
            date = dateFormat.format(Temp.getTime());
            Log.d("Reduced date", "" + date);
            days.add(k, date);
        }

        //make a template array to be filled based on schedule
        String[] rawArray = makeEmptyArray(schedule);

        //make an arraylist for all data
        ArrayList<String[]> all = new ArrayList<>();

        //make arrays multiple day data
        for (int i = 0; i < noOfDays; i++) {
            String[] temp = makeDayData(rawArray, needed.substring(i * 8, (i * 8 + 8)
                    < needed.length() ? i * 8 + 8 : needed.length()));
            all.add(i, temp);
        }

        //verify size
        Log.d("Size of entire data", all.size() + " days");
    }

    private static int pointsToRead() {
        int points = 0;
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        Date lastRead = null;
        int houroffset = now.getHours();

        //see if date is same, if not, find difference
        try {
            lastRead = dateFormat.parse("2015-11-28");
            now = dateFormat.parse(dateFormat.format(now));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //no of additional days to take into account
        int dayoffset = (int) ((now.getTime() - lastRead.getTime()) / (24 * 60 * 60 * 1000));
        Log.d("Data to be read for", dayoffset + " days & " + houroffset + " hours");

        //calculate points based on day and hour
        points = dayoffset * 8 + 1 + (int) (houroffset / 3);
        Log.d("Formatted date", "Now: " + now + " last read: " + lastRead);
        Log.d("Calculated points count", "" + points);

        //if its been more than a week, look only for last 56 points
        return points <= 56 ? points : 56;

    }

    public static String[] makeEmptyArray(String[] schedule) {
        String[] newString = new String[8];
        for (int i = 0; i < 8; i++) {
            if (schedule[i].equalsIgnoreCase("1")) {
                newString[i] = "NULL";
            } else {
                newString[i] = schedule[i];
            }
        }
        Log.d("Template empty array", Arrays.toString(newString));
        return newString;
    }

    public static String[] makeDayData(String[] dayArray, String data) {
        String[] finalData = dayArray.clone();
        Log.d("Before evaluation", "" + data);
        //store data in a char array for easy access
        char[] dataArray = data.toCharArray();
        for (int i = 0; i < data.length(); i++) {

            //if pill was scheduled, look whether it was taken or not
            if (dayArray[i].equalsIgnoreCase("null")) {

                //if it is first slot, dont check early pill
                if (i == 0) {
                    if (String.valueOf(dataArray[i]).equalsIgnoreCase("1")) {
                        Log.d("Found timely pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i] = '0';
                    } else if (String.valueOf(dataArray[i + 1]).equalsIgnoreCase("1")) {
                        Log.d("Found late pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i + 1] = '0';
                    } else {
                        Log.d("Found no pill", "at " + i);
                        finalData[i] = "-1";
                    }
                }

                //if it is last slot, dont check late pill
                if (i == data.length() - 1) {
                    if (String.valueOf(dataArray[i - 1]).equalsIgnoreCase("1")) {
                        Log.d("Found early pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i - 1] = '0';
                    } else if (String.valueOf(dataArray[i]).equalsIgnoreCase("1")) {
                        Log.d("Found timely pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i] = '0';
                    } else {
                        Log.d("Found no pill", "at " + i);
                        finalData[i] = "-1";
                    }
                }

                //else check early, late and timely pills
                if (i > 0 && i < (data.length() - 1)) {
                    if (String.valueOf(dataArray[i - 1]).equalsIgnoreCase("1")) {
                        Log.d("Found early pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i - 1] = '0';
                    } else if (String.valueOf(dataArray[i]).equalsIgnoreCase("1")) {
                        Log.d("Found timely pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i] = '0';
                    } else if (String.valueOf(dataArray[i + 1]).equalsIgnoreCase("1")) {
                        Log.d("Found late pill", "at " + i);
                        finalData[i] = "1";
                        dataArray[i + 1] = '0';
                    } else {
                        Log.d("Found no pill", "at " + i);
                        finalData[i] = "-1";
                    }
                }
            }
            //if no pill is schedules
            else {
                finalData[i] = "0";
            }
        }
        Log.d("Remaining char array", Arrays.toString(dataArray));
        Log.d("After evaluation", Arrays.toString(finalData));
        return finalData;
    }
}
