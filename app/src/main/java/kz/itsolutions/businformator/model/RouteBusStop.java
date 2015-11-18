package kz.itsolutions.businformator.model;

import android.util.Log;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import kz.itsolutions.businformator.db.DBHelper;
import org.json.JSONException;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by arna on 04.10.14.
 */
public class RouteBusStop implements Serializable{

    public static final String FIELD_ID = "_id";
    public static final String FIELD_ROUTE_ID = "route_id";
    public static final String FIELD_BUS_STOP_ID = "bus_stop_id";
    public static final String FIELD_SEQUENCE = "sequence";

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private long id;

    @DatabaseField(columnName = FIELD_ROUTE_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Route route;

    @DatabaseField(columnName = FIELD_BUS_STOP_ID, canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private BusStop busStop;

    @DatabaseField(columnName = FIELD_SEQUENCE)
    private int sequence;

    public RouteBusStop() {
    }

    public RouteBusStop(DBHelper dbHelper, int routeId, int busStopId, int sequence) throws JSONException {
        this.route = Route.getByServerId(dbHelper, routeId);
        this.busStop = BusStop.getByServerId(dbHelper, busStopId);
        this.sequence = sequence;
    }

    public static List<RouteBusStop> getAll(DBHelper dbHelper) {
        List<RouteBusStop> result = new LinkedList<RouteBusStop>();
        Dao<RouteBusStop, Integer> dao = dbHelper.getRouteBusStopDao();
        try {
            result = dao.queryBuilder().limit(100L).query();
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return result;
    }
}
