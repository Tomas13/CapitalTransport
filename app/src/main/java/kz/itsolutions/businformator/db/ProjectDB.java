package kz.itsolutions.businformator.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class ProjectDB {
    protected static final String DATABASE_NAME = "ProjectDB";
    protected static int DATABASE_VERSION = 13;
    protected static final String LOG_TAG = "astana_bus_db";

    protected static final String ROUTES = "Routes";
    protected static final String ROUTE_SERVER_ID = "ServerId";
    protected static final String ROUTE_NAME_RU = "NameRu";
    protected static final String ROUTE_NAME_KZ = "NameKz";
    protected static final String ROUTE_DESCRIPTION_RU = "DescriptionRu";
    protected static final String ROUTE_DESCRIPTION_KZ = "DescriptionKz";
    protected static final String ROUTE_POINT_FROM = "PointFrom";
    protected static final String ROUTE_POINT_TO = "PointTo";
    protected static final String ROUTE_NUMBER = "Number";
    protected static final String ROUTE_TRACK = "Track";
    protected static final String ROUTE_IS_FAVORITE = "IsFavorite";
    protected static final String ROUTE_LAST_SEEN_DATE = "LastSeenDate";

    protected static final String BUS_STOPS = "BusStops";
    protected static final String BUS_STOP_SERVER_ID = "ServerId";
    protected static final String BUS_STOP_NAME = "Name";
    protected static final String BUS_STOP_LONGITUDE = "Longitude";
    protected static final String BUS_STOP_LATITUDE = "Latitude";
    protected static final String BUS_STOP_DESCRIPTION = "Description";

    protected static final String ROUTE_BUS_STOPS = "RouteBusStops";
    protected static final String ROUTE_BUS_STOP_ROUTE_ID = "RouteServerId";
    protected static final String ROUTE_BUS_STOP_BUS_STOP_ID = "BusStopServerId";

    protected final Context mCtx;
    protected static DBHelper mDbHelper;
    protected static SQLiteDatabase mDb;

    protected class DBHelper extends SQLiteOpenHelper {

        private final String CREATE_TABLE_ROUTES = String
                .format("CREATE TABLE %s (id integer primary key autoincrement,"
                        + " %s integer, %s text, %s text, %s text, %s text, %s integer, %s text, %s integer, %s integer," +
                        " %s text, %s text)",
                        ROUTES, ROUTE_SERVER_ID, ROUTE_NAME_RU, ROUTE_NAME_KZ, ROUTE_DESCRIPTION_RU, ROUTE_DESCRIPTION_KZ,
                        ROUTE_NUMBER, ROUTE_TRACK, ROUTE_IS_FAVORITE, ROUTE_LAST_SEEN_DATE, ROUTE_POINT_FROM, ROUTE_POINT_TO);

        private final String CREATE_TABLE_BUS_STOPS = String
                .format("CREATE TABLE %s (id integer primary key autoincrement,"
                        + " %s integer, %s text, %s real, %s real, %s text)",
                        BUS_STOPS, BUS_STOP_SERVER_ID, BUS_STOP_NAME, BUS_STOP_LONGITUDE, BUS_STOP_LATITUDE, BUS_STOP_DESCRIPTION);

        private final String CREATE_TABLE_ROUTE_BUS_STOPS = String
                .format("CREATE TABLE %s (id integer primary key autoincrement,"
                        + " %s integer, %s integer)",
                        ROUTE_BUS_STOPS, ROUTE_BUS_STOP_ROUTE_ID, ROUTE_BUS_STOP_BUS_STOP_ID);

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (db != null) {
                db.execSQL(CREATE_TABLE_ROUTES);
                db.execSQL(CREATE_TABLE_BUS_STOPS);
                db.execSQL(CREATE_TABLE_ROUTE_BUS_STOPS);
            } else {
                Log.e(LOG_TAG, "SQLite ERROR: Couldn't create tables for database "
                        + DATABASE_NAME);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (db != null) {
                Log.w(LOG_TAG,
                        "Upgrading database, which will destroy all old data from tables ");
                db.execSQL(String.format("DROP TABLE IF EXISTS %s", ROUTES));
                db.execSQL(String.format("DROP TABLE IF EXISTS %s", BUS_STOPS));
                db.execSQL(String.format("DROP TABLE IF EXISTS %s", ROUTE_BUS_STOPS));
                onCreate(db);
            } else {
                Log.e(LOG_TAG,
                        "SQLite ERROR: Couldn't upgrade tables from database "
                                + DATABASE_NAME);
            }
        }
    }

    public ProjectDB(Context c) {
        this.mCtx = c;
    }

    public ProjectDB open() throws SQLException {
        mDbHelper = new DBHelper(mCtx);
        try {
            mDb = mDbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return this;
    }

    public ProjectDB openRead() throws SQLException {
        mDbHelper = new DBHelper(mCtx);
        mDb = mDbHelper.getReadableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }
}
