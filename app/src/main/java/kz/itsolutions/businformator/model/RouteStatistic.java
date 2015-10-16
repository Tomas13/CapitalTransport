package kz.itsolutions.businformator.model;

public class RouteStatistic {

    private int avgSpeed;
    private int busesCount;

    public RouteStatistic(int avgSpeed, int busesCount) {
        this.avgSpeed = avgSpeed;
        this.busesCount = busesCount;
    }

    public int getAvgSpeed() {
        return avgSpeed;
    }

    public int getBusesCount() {
        return busesCount;
    }
}
