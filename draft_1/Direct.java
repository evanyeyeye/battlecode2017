package draft_1;
import battlecode.common.*;

public class Direct {

    static RobotController rc;

    public static int BEFORE_SEND = 5;

    public static int BEFORE_RETREAT = 5;

    public static void initDirector(RobotController r){
        rc = r;
    }

    public static Direction toEnemyGroup(){
        MapLocation[] broadcastingLocations = rc.senseBroadcastingRobotLocations();
        boolean send = false;
        for(int i = 1; i <= BEFORE_SEND; i++){
            if(broadcastingLocations[i].distanceTo(broadcastingLocations[i-1]) < 10.0)
                send = true;
            else {
                send = false;
                break;
            }
        }
        if(send)
            return rc.getLocation().directionTo(broadcastingLocations[0]);
        else
            return null;
    }

    public static boolean retreat(){
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        return robots.length > 4;
    }
}