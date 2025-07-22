package com.afkanerd.deku.DefaultSMS.Commons;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation;
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations;
import com.afkanerd.deku.DefaultSMS.Models.NativeSMSDB;
import com.afkanerd.deku.DefaultSMS.R;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.lang.annotation.Native;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

    public static boolean isShortCode(String address) {
        if(address.length() < 4)
            return true;
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(address);
        return !PhoneNumberUtils.isWellFormedSmsAddress(address) || matcher.find();
    }

    public static String getFormatCompleteNumber(String data, String defaultRegion) {
        data = data.replaceAll("%2B", "+")
                .replaceAll("-", "")
                .replaceAll("%20", "")
                .replaceAll(" ", "")
                .replaceFirst("^0+", "");

        if(data.length() < 5)
            return data;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String outputNumber = data;
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(data, defaultRegion);
            long nationalNumber = phoneNumber.getNationalNumber();
            long countryCode = phoneNumber.getCountryCode();

            return "+" + countryCode + nationalNumber;
        } catch(NumberParseException e) {
            if(e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                data = outputNumber
                        .replaceAll("sms[to]*:", "")
                        .replaceFirst("^0+", "");
                if (data.startsWith(defaultRegion)) {
                    outputNumber = "+" + data;
                } else {
                    outputNumber = "+" + defaultRegion + data;
                }
                return outputNumber;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String[] getCountryNationalAndCountryCode(String data) throws NumberParseException {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(data, "");
        long nationNumber = phoneNumber.getNationalNumber();
        long countryCode = phoneNumber.getCountryCode();

        return new String[]{String.valueOf(countryCode), String.valueOf(nationNumber)};
    }

    public static String getFormatForTransmission(String data, String defaultRegion){
        data = data.replaceAll("%2B", "+")
                .replaceAll("%20", "")
                .replaceAll("sms[to]*:", "");

        // Remove any non-digit characters except the plus sign at the beginning of the string
        String strippedNumber = data.replaceAll("[^0-9+;]", "");
        if(strippedNumber.length() > 6) {
            // If the stripped number starts with a plus sign followed by one or more digits, return it as is
            if (!strippedNumber.matches("^\\+\\d+")) {
                strippedNumber = "+" + defaultRegion + strippedNumber;
            }
            return strippedNumber;
        }

        // If the stripped number is not a valid phone number, return an empty string
        return data;
    }

    public static String getFormatNationalNumber(String data, String defaultRegion) {
        data = data.replaceAll("%2B", "+")
                .replaceAll("%20", "");
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String outputNumber = data;
        try {
            try {
                Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(data, defaultRegion);

                return String.valueOf(phoneNumber.getNationalNumber());
            } catch(NumberParseException e) {
                if(e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
                    data = data.replaceAll("sms[to]*:", "");
                    if (data.startsWith(defaultRegion)) {
                        outputNumber = "+" + data;
                    } else {
                        outputNumber = "+" + defaultRegion + data;
                    }
                    return getFormatNationalNumber(outputNumber, defaultRegion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String formatDateExtended(Context context, long epochTime) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - epochTime;

        Date currentDate = new Date(currentTime);
        Date targetDate = new Date(epochTime);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat fullDayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat shortDayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat shortMonthDayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        if (isYesterday(currentDate, targetDate)) { // yesterday
            return context.getString(R.string.single_message_thread_yesterday) + " • " + timeFormat.format(targetDate);
        } else if (diff < DateUtils.DAY_IN_MILLIS) { // today
            return timeFormat.format(targetDate);
        } else if (isSameWeek(currentDate, targetDate)) { // within the same week
            return fullDayFormat.format(targetDate) + " • " + timeFormat.format(targetDate);
        } else { // greater than 1 week
            return shortDayFormat.format(targetDate) + ", " + shortMonthDayFormat.format(targetDate)
                    + " • " + timeFormat.format(targetDate);
        }
    }

    private static boolean isYesterday(Date date1, Date date2) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyDDD", Locale.getDefault());
        String day1 = dayFormat.format(date1);
        String day2 = dayFormat.format(date2);

        int dayOfYear1 = Integer.parseInt(day1.substring(4));
        int dayOfYear2 = Integer.parseInt(day2.substring(4));
        int year1 = Integer.parseInt(day1.substring(0, 4));
        int year2 = Integer.parseInt(day2.substring(0, 4));

        return (year1 == year2 && dayOfYear1 - dayOfYear2 == 1)
                || (year1 - year2 == 1 && dayOfYear1 == 1 && dayOfYear2 == 365);
    }

    private static boolean isSameWeek(Date date1, Date date2) {
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyyww", Locale.getDefault());
        String week1 = weekFormat.format(date1);
        String week2 = weekFormat.format(date2);
        return week1.equals(week2);
    }

    public static String formatDate(Context context, long epochTime) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - epochTime;

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTimeInMillis(epochTime);

        // Check if the date is today
        if (dateCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                dateCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            // Use relative time or time if less than a day
            if (diff < DateUtils.HOUR_IN_MILLIS) {
                return DateUtils.getRelativeTimeSpanString(epochTime, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();
            } else if (diff < DateUtils.DAY_IN_MILLIS) {
                return DateUtils.formatDateTime(context, epochTime, DateUtils.FORMAT_SHOW_TIME);
            }
        } else if (dateCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1) {
            // Show "yesterday" if the date is yesterday
            return context.getString(R.string.thread_conversation_timestamp_yesterday);
        } else {
            // Use standard formatting for other dates
            return DateUtils.formatDateTime(context, epochTime, DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE);
        }
        return null;
    }

    public static String getUserCountry(Context context) {
        String countryCode = null;

        // Check if network information is available
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            // Get the TelephonyManager to access network-related information
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                // Get the ISO country code from the network
                countryCode = tm.getNetworkCountryIso().toUpperCase(Locale.US);
            }
        }
        return String.valueOf(PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode));
    }

    public static boolean isBase64Encoded(String input) {
        try {
            byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
//            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            String reencodedString = Base64.encodeToString(decodedBytes, Base64.DEFAULT)
                            .replaceAll("\\n", "");

            return input.replaceAll("\\n", "").equals(reencodedString);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSameMinute(Long date1, Long date2) {
        java.util.Date date = new java.util.Date(date1);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(date);

        String previousDateString = String.valueOf(date2);
        java.util.Date previousDate = new java.util.Date(Long.parseLong(previousDateString));
        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTime(previousDate);

        return !((prevCalendar.get(Calendar.HOUR_OF_DAY) != currentCalendar.get(Calendar.HOUR_OF_DAY)
                || (prevCalendar.get(Calendar.MINUTE) != currentCalendar.get(Calendar.MINUTE))
                || (prevCalendar.get(Calendar.DATE) != currentCalendar.get(Calendar.DATE))));
    }

    public static boolean isSameHour(Long date1, Long date2) {
        java.util.Date date = new java.util.Date(date1);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(date);

        String previousDateString = String.valueOf(date2);
        java.util.Date previousDate = new java.util.Date(Long.parseLong(previousDateString));
        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTime(previousDate);

        return !((prevCalendar.get(Calendar.HOUR_OF_DAY) != currentCalendar.get(Calendar.HOUR_OF_DAY)
                || (prevCalendar.get(Calendar.DATE) != currentCalendar.get(Calendar.DATE))));
    }

}
