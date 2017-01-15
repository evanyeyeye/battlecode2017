package draft_1;
import battlecode.common.*;

public class Soldier extends RobotPlayer {

    static int ID = 0;

    /*
     * SOLDIER SPECIFIC CODES
     */

    final public static int REINFORCE = 1;
    final static int RETURN_TO_ARCHON = 1001; // not implemented

    /*
     * END SOLDIER SPECIFIC CODES
     */

    // 0 == Need ID
    // [490,499] == ID request processing
    // [500,999] == ID established

    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        initDirList();


        System.out.println("I'm a soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            if(ID < 500) {
                ID = Broadcast.requestID(ID);
            }

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                if(ID > 500) {
                    int code = rc.readBroadcast(ID);
                    if(Broadcast.isLocationCode(code)) {
                        int[] coordinates = Broadcast.readLocationCode(code);
                        int x = rc.readBroadcast(coordinates[0]);
                        int y = rc.readBroadcast(coordinates[1]);
                        int type = coordinates[2];
                        switch(type) {
                            case REINFORCE:
                                // System.out.println("Responding to reinforcement request");
                                tryMove(rc.getLocation().directionTo(new MapLocation(x, y)));
                                break;
                        }
                    }
                }

                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    Broadcast.requestReinforcements(myLocation);
                    if (rc.canFireTriadShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
                        tryMove(rc.getLocation().directionTo(robots[0].location).opposite());
                    }
                }
                //Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                // Move randomly
                //tryMove(towardsArchon.opposite());
                tryMove(randomDirection());
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
                /*
                //MapLocation myLocation = rc.getLocation();
                //Direction towardsArchon = new Direction((float) Math.atan((archonLoc.x-myLocation.x)/(archonLoc.y-myLocation.y)));
                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireTriadShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
                        tryMove(rc.getLocation().directionTo(robots[0].location).opposite());
                    } else {
                        tryMove(rc.getLocation().directionTo(robots[0].location));
                    }
                } else {
                    tryMove(randomDirection());
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();*/

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }

    }
}
