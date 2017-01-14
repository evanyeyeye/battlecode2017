package draft_1;
import battlecode.common.*;

public class Soldier extends RobotPlayer {

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        initDirList();


        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
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
