package com.example.virtualpetpompi.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import com.example.virtualpetpompi.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dan.rares
 * - Holds database tables
 * - methods for managing the data
 */
public class DataBase extends SQLiteOpenHelper {

    private final static String DB_NAME = "stepsData";
    private final static int DB_VERSION = 3;
    private final static String DB_USER = "User";
    private final static String DB_ACHIEV = "achievement";
    private final static String DB_HAS_ACHIEV = "hasAchievement";

    private static DataBase instance;
    private static final AtomicInteger openCounter = new AtomicInteger();

    private DataBase(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DataBase getInstance(final Context c) {
        if (instance == null) {
            instance = new DataBase(c.getApplicationContext());
        }
        openCounter.incrementAndGet();
        return instance;
    }

    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB_NAME + " (date INTEGER, steps INTEGER)");
        db.execSQL("CREATE TABLE " + DB_USER + " (id INTEGER PRIMARY KEY,username TEXT DEFAULT 'User1', coins INTEGER)");
        db.execSQL("CREATE TABLE " + DB_ACHIEV + " (id INTEGER PRIMARY KEY,name TEXT DEFAULT 'achievement', nrSteps INTEGER)");
        db.execSQL("CREATE TABLE " + DB_HAS_ACHIEV + " (idUser INTEGER,idAchievement INTEGER,PRIMARY KEY (idUser, idAchievement),FOREIGN KEY (idUser)" + "REFERENCES " + DB_USER + " (id) ON DELETE CASCADE ON UPDATE NO ACTION, FOREIGN KEY (idAchievement)" + "REFERENCES " + DB_ACHIEV + " (id) ON DELETE CASCADE ON UPDATE NO ACTION)");

    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            // drop PRIMARY KEY constraint
            db.execSQL("CREATE TABLE " + DB_NAME + "2 (date INTEGER, steps INTEGER)");
            db.execSQL("INSERT INTO " + DB_NAME + "2 (date, steps) SELECT date, steps FROM " +
                    DB_NAME);
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("ALTER TABLE " + DB_NAME + "2 RENAME TO " + DB_NAME + "");
        }
    }

    /**
     * Inserts a new entry in the database, if there is no entry for the given
     * date yet. Steps should be the current number of steps and it's negative
     * value will be used as offset for the new date. Also adds 'steps' steps to
     * the previous day, if there is an entry for that date.
     * <p/>
     * This method does nothing if there is already an entry for 'date' - use
     *
     * @param date  the date in ms since 1970
     * @param steps the current step value to be used as negative offset for the
     *              new day; must be >= 0
     */
    public void insertNewDay(long date, int steps) {
        getWritableDatabase().beginTransaction();
        try {
            Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"date"}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (c.getCount() == 0 && steps >= 0) {

                // add 'steps' to yesterdays count
                addToLastEntry(steps);

                // add today
                ContentValues values = new ContentValues();
                values.put("date", date);
                // use the negative steps as offset
                values.put("steps", -steps);
                getWritableDatabase().insert(DB_NAME, null, values);
            }
            c.close();

            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    /**
     * Adds the given number of steps to the last entry in the database
     *
     * @param steps the number of steps to add
     */
    public void addToLastEntry(int steps) {
        getWritableDatabase().execSQL("UPDATE " + DB_NAME + " SET steps = steps + " + steps +
                " WHERE date = (SELECT MAX(date) FROM " + DB_NAME + ")");
    }

    /**
     * Get the total of steps taken without today's value
     *
     * @return number of steps taken, ignoring today
     */
    public int getTotalWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(steps)"}, "steps > 0 AND date > 0 AND date < ?",
                        new String[]{String.valueOf(Util.getToday())}, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }

    /**
     * Get the maximum of steps walked in one day and the date that happened
     *
     * @return a pair containing the date (Date) in millis since 1970 and the
     * step value (Integer)
     */
    public Pair<Date, Integer> getRecordData() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"date, steps"}, "date > 0", null, null, null,
                        "steps DESC", "1");
        c.moveToFirst();
        Pair<Date, Integer> p = new Pair<>(new Date(c.getLong(0)), c.getInt(1));
        c.close();
        return p;
    }

    /**
     * Get the number of steps taken for a specific date.
     * <p/>
     * If date is Util.getToday(), this method returns the offset which needs to
     * be added to the value returned by getCurrentSteps() to get today's steps.
     *
     * @param date the date in millis since 1970
     * @return the steps taken on this date or Integer.MIN_VALUE if date doesn't
     * exist in the database
     */
    public int getSteps(final long date) {
        Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"steps"}, "date = ?",
                new String[]{String.valueOf(date)}, null, null, null);
        c.moveToFirst();
        int re;
        if (c.getCount() == 0) re = Integer.MIN_VALUE;
        else re = c.getInt(0);
        c.close();
        return re;
    }

    /**
     * Gets the last num entries in descending order of date (newest first)
     *
     * @param num the number of entries to get
     * @return a list of long,integer pair - the first being the date, the second the number of steps
     */
    public List<Pair<Long, Integer>> getLastEntries(int num) {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"date", "steps"}, "date > 0", null, null, null,
                        "date DESC", String.valueOf(num));
        int max = c.getCount();
        List<Pair<Long, Integer>> result = new ArrayList<>(max);
        if (c.moveToFirst()) {
            do {
                result.add(new Pair<>(c.getLong(0), c.getInt(1)));
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    /**
     * Removes all entries with negative values.
     * <p/>
     * Only call this directly after boot, otherwise it might remove the current
     * day as the current offset is likely to be negative
     */
    void removeNegativeEntries() {
        getWritableDatabase().delete(DB_NAME, "steps < ?", new String[]{"0"});
    }

    /**
     * Removes all entries from the future.
     */
    void removeFutureEntries() {
        getWritableDatabase().delete(DB_NAME, "date > ?", new String[]{String.valueOf(Util.getToday())});
    }

    /**
     * Get the number of 'valid' days (= days with a step value > 0).
     * <p/>
     * The current day is not added to this number.
     *
     * @return the number of days with a step value > 0, return will be >= 0
     */
    public int getDaysWithoutToday() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"COUNT(*)"}, "steps > ? AND date < ? AND date > 0",
                        new String[]{String.valueOf(0), String.valueOf(Util.getToday())}, null,
                        null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return Math.max(re, 0);
    }

    /**
     * Get the number of 'valid' days (= days with a step value > 0).
     * <p/>
     * The current day is also added to this number, even if the value in the
     * database might still be < 0.
     * <p/>
     * It is safe to divide by the return value as this will be at least 1 (and
     * not 0).
     *
     * @return the number of days with a step value > 0, return will be >= 1
     */
    public int getDays() {
        // today's is not counted yet
        return this.getDaysWithoutToday() + 1;
    }

    /**
     * Saves the current 'steps since boot' sensor value in the database.
     *
     * @param steps since boot
     */
    public void saveCurrentSteps(int steps) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        if (getWritableDatabase().update(DB_NAME, values, "date = -1", null) == 0) {
            values.put("date", -1);
            getWritableDatabase().insert(DB_NAME, null, values);
        }
    }

    /**
     * Saves the User in the database.
     *
     * @param steps since made in the app
     */
    public void saveUser(int steps) {
        ContentValues values = new ContentValues();
        int savedCoins = steps / 100;
        values.put("coins", savedCoins);
        if (getWritableDatabase().update(DB_USER, values, "username = 'User1'", null) == 0) {
            getWritableDatabase().insert(DB_USER, null, values);
        }
    }

    /**
     * Saves the Achievements in the database.
     */
    public void saveAchievement() {
        ContentValues values = new ContentValues();
        values.put("nrSteps", 10000);
        getWritableDatabase().insert(DB_ACHIEV, null, values);
        ContentValues values2 = new ContentValues();
        values2.put("nrSteps", 20000);
        getWritableDatabase().insert(DB_ACHIEV, null, values2);
        ContentValues values3 = new ContentValues();
        values3.put("nrSteps", 30000);
        getWritableDatabase().insert(DB_ACHIEV, null, values3);

    }

    /**
     * Saves the Achievements the user has in the database.
     */
    public void saveHasAchievement(int user, int achievement) {
        ContentValues values = new ContentValues();
        values.put("idUser", user);
        values.put("idAchievement", achievement);
        getWritableDatabase().insert(DB_HAS_ACHIEV, null, values);

    }

    /**
     * gets the Achievements the user has.
     *
     * @return top achievement
     */
    public int getAchievement() {
        int p = 0;
        try {
            Cursor c = getReadableDatabase()
                    .query(DB_HAS_ACHIEV, new String[]{"idAchievement"}, "idAchievement > 0", null, null, null,
                            "idAchievement DESC", "1");
            c.moveToFirst();
            p = c.getInt(0);
            c.close();
        } catch (Exception e) {
            Log.e("error", "no Database");
        }

        return p;
    }

    /**
     * Reads the latest saved value for the 'steps since boot' sensor value.
     *
     * @return the current number of steps saved in the database or 0 if there
     * is no entry
     */
    public int getCurrentSteps() {
        int re = getSteps(-1);
        return re == Integer.MIN_VALUE ? 0 : re;
    }
}
