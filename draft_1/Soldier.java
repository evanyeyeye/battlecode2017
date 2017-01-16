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


                float archon_x = Float.intBitsToFloat(rc.readBroadcast(
                            Broadcast.MAIN_ARCHON_POSITION[0]));
                float archon_y = Float.intBitsToFloat(rc.readBroadcast(
                            Broadcast.MAIN_ARCHON_POSITION[1]));

                MapLocation myLocation = rc.getLocation();

                MapLocation archonLocation = new MapLocation(archon_x, archon_y);
                if(Broadcast.checkMainArchonDistress()) {
                    tryMove(myLocation.directionTo(archonLocation));
                }

                if(ID > 500) {
                    int code = rc.readBroadcast(ID);
                    // code <= 0 means the action has been disabled
                    // or no action has been transmitted
                    if(code > 0 && Broadcast.isDynamicChannelCode(code)) {
                        int[] coordinates = Broadcast.readDynamicChannelCode2(code);
                        int x = rc.readBroadcast(coordinates[0]);
                        int y = rc.readBroadcast(coordinates[1]);
                        float x_f = Float.intBitsToFloat(x);
                        float y_f = Float.intBitsToFloat(y);
                        int type = coordinates[2];
                        switch(type) {
                            case REINFORCE:
                                System.out.println("Responding to reinforcement request at: " + x_f +  " " + y_f);
                                MapLocation requestedLocation = new MapLocation(x_f, y_f);

                                if(myLocation.distanceTo(requestedLocation) < 4) {
                                    rc.broadcast(ID, code*-1);
                                    break;
                                }
                                try {
                                    tryMove(myLocation.directionTo(requestedLocation));
                                } catch(Exception e) {
                                    System.out.println("EXCEPTION: TRIED TO MOVE TO: " + x_f + " " + y_f);
                                }
                                break;
                        }
                    }
                }

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
