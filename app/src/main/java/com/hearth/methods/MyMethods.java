package com.hearth.methods;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hearth.R;
import com.hearth.alarm.ChatNotifications;
import com.hearth.alarm.MembershipNotification;
import com.hearth.alarm.OrderCompletedNotifications;
import com.hearth.alarm.OrderNotifications;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class MyMethods {
    public static class Notifications {
        public static void orders(Context context, String orderUid, String orderBuyer, String orderItem, long orderTimestamp) {
            // create order notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                CharSequence name = "Order notifications";
                String description = "Order notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("hearth_order_notifications", name, importance);
                notificationChannel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            // check if order has already been notified to the current user
            boolean hasBeenNotified = MyMethods.Cache.getBoolean(context, "order_"+orderUid+"_hasBeenNotified");
            if (!hasBeenNotified) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, OrderNotifications.class);
                Bundle bundle = new Bundle();
                bundle.putString("order_buyername", orderBuyer);
                bundle.putString("order_item", orderItem);
                intent.putExtras(bundle);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) orderTimestamp, intent, 0);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);

                // set order hasBeenNotified to true
                MyMethods.Cache.setBoolean(context, "order_"+orderUid+"_hasBeenNotified", true);
            }
        }

        public static void chat(Context context, String photoUrl, String fullName, String message, long timestamp) {
            // create chat notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                CharSequence name = "Chat notifications";
                String description = "Chat notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("hearth_chat_notifications", name, importance);
                notificationChannel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            // check if chat has already been notified to the current user
            long lastChatNotified = MyMethods.Cache.getLong(context, "last_chat_notified");
            if (lastChatNotified == timestamp) {
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, ChatNotifications.class);
            //Bundle bundle = new Bundle();
            intent.putExtra("chat_author", fullName);
            intent.putExtra("chat_authorPhotoUrl", photoUrl);
            intent.putExtra("chat_message", message);
            intent.putExtra("chat_timestamp", timestamp);
            //intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) timestamp, intent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);

            // set last chat notified to current chat
            MyMethods.Cache.setLong(context, "last_chat_notified", timestamp);
        }

        public static void orderCompleted(Context context, String adminName, String itemName, String uid) {
            // create order notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                CharSequence name = "Order notifications";
                String description = "Order notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("hearth_order_notifications", name, importance);
                notificationChannel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, OrderCompletedNotifications.class);
            //Bundle bundle = new Bundle();
            intent.putExtra("order_adminname", adminName);
            intent.putExtra("order_itemName", itemName);
            intent.putExtra("order_uid", uid);
            //intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        }

        public static void membership(Context context, int responseCode, String familyName) {
            // create order notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                CharSequence name = "Membership notifications";
                String description = "Membership notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("hearth_membership_notifications", name, importance);
                notificationChannel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, MembershipNotification.class);
            intent.putExtra("response_code", responseCode);
            intent.putExtra("family_name", familyName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, 0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        }
    }

    public static class Time {
        public static String toMilitary(long millis){
            // return time in 4 digit hhss format
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);

            long hour = calendar.get(Calendar.HOUR_OF_DAY);
            long minute = calendar.get(Calendar.MINUTE);

            return String.format("%02d", hour)+""+String.format("%02d", minute);
        }

        public static String toClock(long millis){
            // return time in HH:mm AM/PM format
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);

            long hour = calendar.get(Calendar.HOUR_OF_DAY);
            long minute = calendar.get(Calendar.MINUTE);
            String period = "";

            if (hour < 12){
                if (hour == 0) {
                    hour += 12;
                }
                period = "AM";
            }
            else if (hour >= 12){
                if (hour > 12) {
                    hour -= 12;
                }
                period = "PM";
            }

            return String.format("%02d", hour) + ":" + String.format("%02d", minute) + " " + period;
        }

        public static String toDate(Context context, long millis){
            // return time in day format (today, yesterday, 2 days ago)
            String output;

            // get system time
            Calendar systemCalendar = Calendar.getInstance();
            systemCalendar.setTimeInMillis(System.currentTimeMillis());
            long systemHour = systemCalendar.get(Calendar.DAY_OF_YEAR);

            // get mission time
            Calendar missionCalendar = Calendar.getInstance();
            missionCalendar.setTimeInMillis(millis);
            long missionHour = missionCalendar.get(Calendar.DAY_OF_YEAR);

            long dateDifference = systemHour - missionHour;
            Toast.makeText(context, "system: "+systemHour + " mission: "+missionHour +" diff: "+dateDifference, Toast.LENGTH_SHORT).show();

            switch ((int) dateDifference) {
                case 0: output = "Today";
                    break;
                case 1: output = "Yesterday";
                    break;
                default:
                    output = dateDifference + " days ago";
                    break;
            }

            return output;
        }
    }

    public static class DoubleMethods {
        public static String formatDoubleToString(double dbl){
            // check if double number has decimal values (remainder)
            double remainder = dbl % 1;
            String outputString;
            if (remainder == 0){
                outputString = String.format("%.0f", dbl);
            }
            else{
                outputString = String.format("%.2f", dbl);
            }
            return outputString;
        }
    }

    public static class EditTextMethods {
        public static void limitCharacters(EditText editText, String input) {
            if (input.length() > 8) {
                String limiter = input.substring(0, input.length() - 1);
                editText.setText(limiter);
                editText.setSelection(editText.getText().length());
            }
        }

        public static void decimalPlaceValueLimiter(EditText editText, String dblToStr){
            // check for decimal point and at least 1 decimal value
            if (dblToStr.contains(".") && !(dblToStr.endsWith("."))){
                // split by the decimal point
                String[] splitString = dblToStr.split(Pattern.quote("."));

                // count decimal places
                int decimalPlaces = splitString[1].length();

                // limit decimal places to 2 if it exceeds
                if (decimalPlaces > 2){
                    editText.setText(dblToStr.substring(0, dblToStr.length() - 1));
                    editText.setSelection(editText.getText().length());
                }
            }
        }

        public static void incrementDouble(EditText editText, double i){
            // get value
            String stringPrice = editText.getText().toString();
            double dblReward;

            // check if input exists
            if (TextUtils.isEmpty(stringPrice)){
                dblReward = 0;
            }
            else{
                dblReward = Double.parseDouble(stringPrice);
            }

            // increment/decrement value
            dblReward+=i;

            // disallow negative outputs
            if (dblReward < 0){
                dblReward = 0;
            }

            // clean up trailing decimal values
            double remainder = dblReward % 1;
            if (remainder == 0){
                editText.setText(String.format("%.0f", dblReward));
            }
            else{
                editText.setText(String.format("%.2f", dblReward));
            }

            // output
            editText.setSelection(editText.getText().length());
        }
    }

    public static class Cache {
        public static String getString(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, "");
        }

        public static void setString(Context context, String key, String value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(key, value).apply();
        }

        public static int getInt(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getInt(key, 0);
        }

        public static void setInt(Context context, String key, int value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt(key, value).apply();
        }

        public static double getDouble(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            return Double.longBitsToDouble(sharedPreferences.getLong(key, 0));
        }

        public static void setDouble(Context context, String key, double value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
        }

        public static long getLong(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getLong(key, 0);
        }

        public static void setLong(Context context, String key, long value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putLong(key, value).apply();
        }

        public static boolean getBoolean(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, false);
        }

        public static void setBoolean(Context context, String key, boolean value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(key, value).apply();
        }

        public static boolean getMissionDone(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_mission_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, false);
        }

        public static void setMissionDone(Context context, String key, boolean value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_mission_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(key, value).apply();
        }

        public static Set<String> getStringSet(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_mission_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getStringSet(key, new HashSet<>());
        }

        public static void setStringSet(Context context, String key, Set<String> set){
            SharedPreferences sharedPreferences = context.getSharedPreferences("hearth_mission_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putStringSet(key, set).apply();
        }
    }

    public static class Generate {
        public static void warningDialog(Context context, String title, String message){
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
            materialAlertDialogBuilder.setTitle(title);
            materialAlertDialogBuilder.setMessage(message);
            materialAlertDialogBuilder.setPositiveButton("Okay", (dialogInterface, i) -> { });
            materialAlertDialogBuilder.show();
        }

        @SuppressLint("UnsafeOptInUsageError")
        public static void badgeForBottomNav(BadgeDrawable badgeDrawable, View view, long l){
            if (l > 0){
                badgeDrawable.setNumber((int) l);
                badgeDrawable.setVisible(true);
                badgeDrawable.setHorizontalOffset(85);
                badgeDrawable.setVerticalOffset(22);
                BadgeUtils.attachBadgeDrawable(badgeDrawable, view);
            }
            else{
                badgeDrawable.setVisible(false);
            }
        }

        public static void banner(String link, ImageView imageView){
            Picasso.get().load(link).resizeDimen(R.dimen._240dp, R.dimen._0dp).onlyScaleDown().into(imageView);
        }

        public static void image(String link, ImageView imageView){
            Picasso.get().load(link).into(imageView);
        }

        public static void image(String link, ImageView imageView, int i){
            Picasso.get().load(link).resize(i, i).centerInside().into(imageView);
        }

        public static void image(String link, ImageView imageView, int width, int height){
            Picasso.get().load(link).resize(width, height).into(imageView);
        }

        public static int randomNumber(int min, int max){
            final int random = new Random().nextInt((max - min) + 1) + min;
            return random;
        }

        public static String familyIconUrl(){
            ArrayList<String> photoUrls = new ArrayList<>();
            photoUrls.add("https://1.bp.blogspot.com/-vQDB-jzNh5g/U82zGkw2AHI/AAAAAAAAjKs/Tu61uwfkWZk/s800/animal_mark01_buta.png");
            photoUrls.add("https://3.bp.blogspot.com/-tVdeLo7vHdM/U82zGrKXN_I/AAAAAAAAjKo/Akh_OyYLMdQ/s800/animal_mark02_kuma.png");
            photoUrls.add("https://2.bp.blogspot.com/-XIjdkGsG_u8/U82zGlpgNUI/AAAAAAAAjKw/dxsQGmNb7BU/s800/animal_mark03_inu.png");
            photoUrls.add("https://3.bp.blogspot.com/-2AkJE-MGLfc/U82zHahLd6I/AAAAAAAAjLI/0zEEK9QOsdM/s800/animal_mark04_neko.png");
            photoUrls.add("https://2.bp.blogspot.com/-F2XdQD40qiI/U82zHl-YcUI/AAAAAAAAjK8/bNZItoYU3Qo/s800/animal_mark05_zou.png");
            photoUrls.add("https://4.bp.blogspot.com/-ZaNYAtCBvFE/U82zH6Gn8jI/AAAAAAAAjLE/U6usr9E8rok/s800/animal_mark06_uma.png");
            photoUrls.add("https://2.bp.blogspot.com/-aDf7XweReek/U82zIqQMY_I/AAAAAAAAjLQ/POasanBJzuA/s800/animal_mark07_lion.png");
            photoUrls.add("https://4.bp.blogspot.com/-s3QaUKsDsSc/U82zJLLE0tI/AAAAAAAAjLc/3h9Ge6JqsIw/s800/animal_mark08_kaba.png");
            photoUrls.add("https://4.bp.blogspot.com/-0uhh2DUaFqc/U82zJSQ_QSI/AAAAAAAAjLo/VZsybH8SfeQ/s800/animal_mark09_tora.png");
            photoUrls.add("https://1.bp.blogspot.com/-ErzyFYbu3BE/U82zJvbu-RI/AAAAAAAAjLk/z2cNkQEPdrM/s800/animal_mark10_usagi.png");
            photoUrls.add("https://3.bp.blogspot.com/-F3aKqLX4CeM/U82zKK_aRjI/AAAAAAAAjLs/d92OlApO4iU/s800/animal_mark11_panda.png");
            photoUrls.add("https://3.bp.blogspot.com/-P3sBaIyGqRE/U82zKphdy0I/AAAAAAAAjL0/k-G1al3DRho/s150/animal_mark12_saru.png");
            photoUrls.add("https://3.bp.blogspot.com/-h4mM4eR1lSo/U82zK9dcvmI/AAAAAAAAjMU/fmgufOAY97A/s150/animal_mark13_penguin.png");
            photoUrls.add("https://2.bp.blogspot.com/-QlS3RfPllyQ/U82zLDlY7fI/AAAAAAAAjMA/t0NJ7n_K4wY/s150/animal_mark14_hitsuji.png");
            photoUrls.add("https://4.bp.blogspot.com/-n3GPXtCuIHc/U82zLub22BI/AAAAAAAAjMI/dhN0ua_xgeM/s150/animal_mark15_koala.png");
            // pick random family icon from arraylist
            int randomNumber = MyMethods.Generate.randomNumber(0, 14);
            return photoUrls.get(randomNumber);
        }

        public static String familyCode(){
            // Start of declaration of variables
            String[] characters = {
                    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                    "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                    "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
            };
            // list of 3-7-character sensitive words to filter out from the randomly generated code
            String[] sensitiveWords = {
                    "abo",
                    "ass",
                    "bra",
                    "cum",
                    "die",
                    "dix",
                    "ero",
                    "evl",
                    "fag",
                    "fat",
                    "fok",
                    "fuc",
                    "fuk",
                    "gay",
                    "gin",
                    "gob",
                    "god",
                    "goy",
                    "gun",
                    "gyp",
                    "hiv",
                    "jap",
                    "jew",
                    "jiz",
                    "kid",
                    "kkk",
                    "kum",
                    "lez",
                    "lsd",
                    "mad",
                    "nig",
                    "nip",
                    "pee",
                    "pom",
                    "poo",
                    "pot",
                    "pud",
                    "sex",
                    "sob",
                    "sos",
                    "tit",
                    "tnt",
                    "uck",
                    "wab",
                    "wog",
                    "wop",
                    "wtf",
                    "xtc",
                    "xxx",
                    "abbo",
                    "alla",
                    "anal",
                    "anus",
                    "arab",
                    "arse",
                    "babe",
                    "barf",
                    "bast",
                    "blow",
                    "bomb",
                    "bomd",
                    "bong",
                    "boob",
                    "boom",
                    "burn",
                    "butt",
                    "chav",
                    "chin",
                    "cigs",
                    "clit",
                    "cock",
                    "coon",
                    "crap",
                    "cumm",
                    "cunn",
                    "cunt",
                    "dago",
                    "damn",
                    "dead",
                    "dego",
                    "deth",
                    "dick",
                    "died",
                    "dies",
                    "dike",
                    "dink",
                    "dive",
                    "dong",
                    "doom",
                    "dope",
                    "drug",
                    "dumb",
                    "dyke",
                    "fart",
                    "fear",
                    "fire",
                    "floo",
                    "fore",
                    "fuck",
                    "fuks",
                    "geez",
                    "geni",
                    "gipp",
                    "gook",
                    "groe",
                    "gypo",
                    "gypp",
                    "hapa",
                    "hebe",
                    "heeb",
                    "hell",
                    "hobo",
                    "hoes",
                    "hole",
                    "homo",
                    "honk",
                    "hook",
                    "hore",
                    "hork",
                    "horn",
                    "ikey",
                    "itch",
                    "jade",
                    "jeez",
                    "jiga",
                    "jigg",
                    "jism",
                    "jizm",
                    "jizz",
                    "jugs",
                    "kike",
                    "kill",
                    "kink",
                    "kock",
                    "koon",
                    "krap",
                    "kums",
                    "kunt",
                    "kyke",
                    "laid",
                    "lezz",
                    "lies",
                    "limy",
                    "mams",
                    "meth",
                    "milf",
                    "mofo",
                    "moky",
                    "muff",
                    "munt",
                    "nazi",
                    "nigg",
                    "nigr",
                    "nook",
                    "nude",
                    "nuke",
                    "oral",
                    "orga",
                    "orgy",
                    "paki",
                    "payo",
                    "peck",
                    "perv",
                    "phuk",
                    "phuq",
                    "pi55",
                    "piky",
                    "pimp",
                    "piss",
                    "pixy",
                    "pohm",
                    "poon",
                    "poop",
                    "porn",
                    "pric",
                    "pros",
                    "pube",
                    "pudd",
                    "puke",
                    "puss",
                    "pusy",
                    "quim",
                    "ra8s",
                    "rape",
                    "rere",
                    "rump",
                    "scag",
                    "scat",
                    "scum",
                    "sexy",
                    "shag",
                    "shat",
                    "shav",
                    "shit",
                    "sick",
                    "skum",
                    "slav",
                    "slut",
                    "smut",
                    "snot",
                    "spic",
                    "spig",
                    "spik",
                    "spit",
                    "suck",
                    "taff",
                    "tang",
                    "tard",
                    "teat",
                    "tits",
                    "turd",
                    "twat",
                    "vibr",
                    "wank",
                    "wetb",
                    "whit",
                    "whiz",
                    "whop",
                    "wuss",
                    "abuse",
                    "adult",
                    "allah",
                    "angie",
                    "angry",
                    "arabs",
                    "argie",
                    "asian",
                    "asses",
                    "balls",
                    "beast",
                    "bible",
                    "bitch",
                    "black",
                    "blind",
                    "boang",
                    "bogan",
                    "bombs",
                    "boner",
                    "boobs",
                    "booby",
                    "boody",
                    "boong",
                    "booty",
                    "bunga",
                    "chink",
                    "choad",
                    "chode",
                    "cocky",
                    "cohee",
                    "color",
                    "cooly",
                    "cra5h",
                    "crabs",
                    "crack",
                    "crash",
                    "crime",
                    "darky",
                    "death",
                    "demon",
                    "devil",
                    "dildo",
                    "dirty",
                    "drunk",
                    "eatme",
                    "enema",
                    "enemy",
                    "erect",
                    "fagot",
                    "fairy",
                    "faith",
                    "farty",
                    "fatah",
                    "fatso",
                    "feces",
                    "felch",
                    "fight",
                    "forni",
                    "fraud",
                    "fubar",
                    "fucck",
                    "fucka",
                    "fucks",
                    "fugly",
                    "fuuck",
                    "ginzo",
                    "girls",
                    "goyim",
                    "gross",
                    "gubba",
                    "gyppo",
                    "gyppy",
                    "hamas",
                    "harem",
                    "honky",
                    "horny",
                    "hoser",
                    "husky",
                    "hussy",
                    "hymen",
                    "hymie",
                    "idiot",
                    "jebus",
                    "jesus",
                    "jigga",
                    "jiggy",
                    "jihad",
                    "jizim",
                    "joint",
                    "kafir",
                    "kills",
                    "kinky",
                    "knife",
                    "kotex",
                    "kraut",
                    "latin",
                    "lesbo",
                    "lezbe",
                    "lezbo",
                    "lezzo",
                    "limey",
                    "loser",
                    "lugan",
                    "lynch",
                    "mafia",
                    "mgger",
                    "mggor",
                    "mocky",
                    "moles",
                    "moron",
                    "naked",
                    "nasty",
                    "necro",
                    "negro",
                    "niger",
                    "nigga",
                    "nigra",
                    "nigre",
                    "nymph",
                    "osama",
                    "pansy",
                    "panti",
                    "pendy",
                    "peni5",
                    "penis",
                    "piker",
                    "pikey",
                    "pixie",
                    "pocha",
                    "pocho",
                    "pommy",
                    "porno",
                    "prick",
                    "pu55i",
                    "pu55y",
                    "pubic",
                    "pussy",
                    "queef",
                    "queer",
                    "rabbi",
                    "randy",
                    "raped",
                    "raper",
                    "roach",
                    "sadis",
                    "sadom",
                    "sandm",
                    "satan",
                    "screw",
                    "semen",
                    "seppo",
                    "sexed",
                    "shhit",
                    "shite",
                    "shits",
                    "shoot",
                    "sissy",
                    "skank",
                    "slant",
                    "slave",
                    "slime",
                    "slopy",
                    "sluts",
                    "slutt",
                    "smack",
                    "sodom",
                    "sooty",
                    "spank",
                    "sperm",
                    "spick",
                    "spunk",
                    "squaw",
                    "stagg",
                    "taboo",
                    "teste",
                    "titty",
                    "tramp",
                    "trots",
                    "twink",
                    "urine",
                    "usama",
                    "vomit",
                    "vulva",
                    "whash",
                    "whore",
                    "willy",
                    "addict",
                    "africa",
                    "areola",
                    "asshat",
                    "assman",
                    "attack",
                    "babies",
                    "beaner",
                    "beaver",
                    "biatch",
                    "bigass",
                    "bigger",
                    "bitchy",
                    "biteme",
                    "blacks",
                    "bohunk",
                    "boonga",
                    "boonie",
                    "brea5t",
                    "breast",
                    "bugger",
                    "buried",
                    "byatch",
                    "cacker",
                    "cancer",
                    "chinky",
                    "christ",
                    "church",
                    "coitus",
                    "commie",
                    "condom",
                    "coolie",
                    "crappy",
                    "creamy",
                    "crimes",
                    "crotch",
                    "cummer",
                    "cunntt",
                    "dahmer",
                    "dammit",
                    "damnit",
                    "darkie",
                    "desire",
                    "diddle",
                    "doodoo",
                    "dyefly",
                    "escort",
                    "ethnic",
                    "faeces",
                    "faggot",
                    "failed",
                    "farted",
                    "fatass",
                    "fckcum",
                    "feltch",
                    "fetish",
                    "firing",
                    "fister",
                    "flange",
                    "flydie",
                    "flydye",
                    "fondle",
                    "fucked",
                    "fucker",
                    "fuckin",
                    "fuckit",
                    "fuckme",
                    "fungus",
                    "gaysex",
                    "geezer",
                    "german",
                    "gringo",
                    "gummer",
                    "gyppie",
                    "harder",
                    "hardon",
                    "heroin",
                    "herpes",
                    "hijack",
                    "hindoo",
                    "hitler",
                    "hodgie",
                    "honger",
                    "honkey",
                    "hooker",
                    "horney",
                    "hummer",
                    "iblowu",
                    "incest",
                    "insest",
                    "israel",
                    "jewish",
                    "jigger",
                    "jizzim",
                    "jizzum",
                    "kaffer",
                    "kaffir",
                    "kaffre",
                    "kanake",
                    "kigger",
                    "killed",
                    "killer",
                    "kondum",
                    "krappy",
                    "kummer",
                    "lesbin",
                    "libido",
                    "licker",
                    "lickme",
                    "liquor",
                    "lolita",
                    "looser",
                    "lotion",
                    "macaca",
                    "mockey",
                    "mockie",
                    "molest",
                    "mormon",
                    "moslem",
                    "murder",
                    "muslim",
                    "niggah",
                    "niggaz",
                    "nigger",
                    "niggle",
                    "niggor",
                    "niggur",
                    "niglet",
                    "nignog",
                    "nipple",
                    "nittit",
                    "nlgger",
                    "nlggor",
                    "nookey",
                    "nookie",
                    "noonan",
                    "nooner",
                    "nudger",
                    "orgasm",
                    "orgies",
                    "pecker",
                    "penile",
                    "period",
                    "phuked",
                    "pimped",
                    "pimper",
                    "pissed",
                    "pisser",
                    "pisses",
                    "pissin",
                    "pistol",
                    "polack",
                    "pommie",
                    "pooper",
                    "popimp",
                    "pudboy",
                    "pussie",
                    "racial",
                    "racist",
                    "rapist",
                    "rectum",
                    "reefer",
                    "reject",
                    "retard",
                    "ribbed",
                    "rigger",
                    "rimjob",
                    "robber",
                    "russki",
                    "sexing",
                    "sexpot",
                    "sextoy",
                    "sexual",
                    "shited",
                    "shitty",
                    "skanky",
                    "slopey",
                    "slutty",
                    "snatch",
                    "sniper",
                    "sodomy",
                    "soviet",
                    "spooge",
                    "spunky",
                    "stiffy",
                    "stroke",
                    "stupid",
                    "sucker",
                    "suckme",
                    "swalow",
                    "tampon",
                    "tantra",
                    "terror",
                    "tinkle",
                    "titjob",
                    "tittie",
                    "toilet",
                    "tongue",
                    "tortur",
                    "tosser",
                    "tranny",
                    "trojan",
                    "turnon",
                    "uterus",
                    "vagina",
                    "virgin",
                    "wanker",
                    "weapon",
                    "weenie",
                    "weewee",
                    "whites",
                    "whitey",
                    "wigger",
                    "willie",
                    "wuzzie",
                    "yankee",
                    "zigabo",
                    "addicts",
                    "african",
                    "amateur",
                    "analsex",
                    "aroused",
                    "assault",
                    "assfuck",
                    "asshole",
                    "asshore",
                    "asskiss",
                    "asslick",
                    "asswipe",
                    "badfuck",
                    "banging",
                    "baptist",
                    "barface",
                    "bastard",
                    "bazooms",
                    "beatoff",
                    "bestial",
                    "bigbutt",
                    "bitcher",
                    "bitches",
                    "bitchez",
                    "bitchin",
                    "blowjob",
                    "bollick",
                    "bollock",
                    "bombers",
                    "bombing",
                    "bondage",
                    "boobies",
                    "brothel",
                    "buggery",
                    "bumfuck",
                    "buttman",
                    "carruth",
                    "chinese",
                    "clogwog",
                    "cocaine",
                    "cocknob",
                    "colored",
                    "coondog",
                    "crapola",
                    "crapper",
                    "cumfest",
                    "cumming",
                    "cumquat",
                    "cumshot",
                    "deposit",
                    "destroy",
                    "dickman",
                    "dickwad",
                    "dipshit",
                    "disease",
                    "doo-doo",
                    "drunken",
                    "dumbass",
                    "ecstacy",
                    "execute",
                    "fagging",
                    "failure",
                    "fairies",
                    "farting",
                    "fatfuck",
                    "felatio",
                    "felcher",
                    "fisting",
                    "flasher",
                    "fuckbag",
                    "fuckers",
                    "fuckher",
                    "fuckina",
                    "fucking",
                    "fuckoff",
                    "fuckpig",
                    "fuckyou",
                    "funeral",
                    "funfuck",
                    "gangsta",
                    "genital",
                    "getiton",
                    "goddamn",
                    "handjob",
                    "hiscock",
                    "honkers",
                    "hookers",
                    "hooters",
                    "hosejob",
                    "hostage",
                    "hotdamn",
                    "hustler",
                    "illegal",
                    "israeli",
                    "jackass",
                    "jackoff",
                    "japcrap",
                    "jerkoff",
                    "jigaboo",
                    "jiggabo",
                    "jimfish",
                    "juggalo",
                    "killing",
                    "kissass",
                    "kumming",
                    "kumquat",
                    "lactate",
                    "lesbain",
                    "lesbayn",
                    "lesbian",
                    "liberal",
                    "livesex",
                    "lovegoo",
                    "lovegun",
                    "lowlife",
                    "lubejob",
                    "lucifer",
                    "mexican",
                    "mideast",
                    "mulatto",
                    "muncher",
                    "nastyho",
                    "negroes",
                    "negroid",
                    "negro's",
                    "niggard",
                    "niggers",
                    "niggled",
                    "niggles",
                    "orgasim",
                    "pansies",
                    "panties",
                    "peehole",
                    "pee-pee",
                    "penises",
                    "phuking",
                    "phukked",
                    "phungky",
                    "pindick",
                    "pissing",
                    "pissoff",
                    "playboy",
                    "pooping",
                    "poverty",
                    "puddboy",
                    "puntang",
                    "pussies",
                    "quashie",
                    "quickie",
                    "radical",
                    "raghead",
                    "rearend",
                    "redneck",
                    "reestie",
                    "refugee",
                    "remains",
                    "rimming",
                    "russkie",
                    "schlong",
                    "scrotum",
                    "servant",
                    "sexfarm",
                    "sextogo",
                    "sextoys",
                    "shaggin",
                    "sheeney",
                    "shinola",
                    "shitcan",
                    "shitfit",
                    "shiting",
                    "shitola",
                    "shitted",
                    "shitter",
                    "skumbag",
                    "slapper",
                    "snigger",
                    "spitter",
                    "strapon",
                    "suckoff",
                    "suicide",
                    "swallow",
                    "tarbaby",
                    "titfuck",
                    "titties",
                    "torture",
                    "trannie",
                    "triplex",
                    "twinkie",
                    "upskirt",
                    "urinary",
                    "urinate",
                    "vaginal",
                    "vatican",
                    "wanking",
                    "waysted",
                    "welcher",
                    "welfare",
                    "wetback",
                    "wetspot",
                    "whacker",
                    "whigger",
                    "whiskey"
            };
            final int min = 0, max = 35;

            StringBuilder stringBuilder = new StringBuilder();
            String code = "";
            boolean containsSensitiveWord = false;
            // End of declaration of variables

            // Continually check the randomly generated string
            // if it contains bad words,
            // if so then regenerate a different code
            do{
                // Start of 7-iteration loop to build a random string
                for (int i=0; i<7; i++){
                    final int random = new Random().nextInt((max - min) + 1) + min; // get a random number
                    stringBuilder.append(characters[random]);
                }
                code = stringBuilder.toString();
                // End of 7-iteration loop to build a random string

                for(int j=0; j<sensitiveWords.length; j++){
                    if (code.contains(sensitiveWords[j])){
                        containsSensitiveWord = true;
                    }
                    else{
                        containsSensitiveWord = false;
                    }
                }
            }while(containsSensitiveWord == true);
            return code;
        }
    }
}
