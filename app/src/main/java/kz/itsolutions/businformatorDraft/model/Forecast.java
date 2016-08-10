package kz.itsolutions.businformatordraft.model;

public class Forecast {

    private Route mRoute;
    private int nextBusId;
    private int nextBusDistance;
    private int nextBusTime;

    private int nextBus2Id;
    private int nextBus2Distance;
    private int nextBus2Time;

    public Forecast(Route route, int nextBusId, int nextBusDistance, int nextBusTime, int nextBus2Id, int nextBus2Distance, int nextBus2Time) {
        this.mRoute = route;
        this.nextBusId = nextBusId;
        this.nextBusDistance = nextBusDistance;
        this.nextBusTime = nextBusTime;
        this.nextBus2Id = nextBus2Id;
        this.nextBus2Distance = nextBus2Distance;
        this.nextBus2Time = nextBus2Time;
    }

    public Route getRoute() {
        return mRoute;
    }

    public int getNextBusId() {
        return nextBusId;
    }

    public int getNextBus1Distance() {
        return nextBusDistance;
    }

    public int getNextBus1Time() {
        return nextBusTime;
    }

    public int getNextBus2Id() {
        return nextBus2Id;
    }

    public int getNextBus2Distance() {
        return nextBus2Distance;
    }

    public int getNextBus2Time() {
        return nextBus2Time;
    }
}
