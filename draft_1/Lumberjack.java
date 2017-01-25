package draft_1;
import battlecode.common.*;

public class Lumberjack extends RobotPlayer {

    public static int ID = 0;

    public static boolean dying = false;

    static void checkForRequests(MapLocation myLocation) throws GameActionException {
        MapLocation closestLocation = null;
        double closestDistance = Double.MAX_VALUE;
        // This treeID is not the real tree ID, it's just made up
        int treeID = 0;
        int minTreeID = 0;
        int broadcast_index = 0;
        for(int i : Broadcast.LUMBERJACK_REQUESTS) {
            treeID = rc.readBroadcast(i);
            if(treeID >= 0) {
                MapLocation ml = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(i+1)),
                        Float.intBitsToFloat(rc.readBroadcast(i+2)));
                double dist = myLocation.distanceTo(ml);
                if(closestLocation == null || closestDistance > dist) {
                    closestLocation = ml;
                    closestDistance = dist;
                    minTreeID = treeID;
                    broadcast_index = i;
                }
            }
        }
        if(minTreeID == 0) return;
        if(closestDistance < (rc.getType().bodyRadius + rc.getType().strideRadius)) {
            if(rc.canChop(closestLocation)) {
                rc.chop(closestLocation);
            } else {
                // The tree is gone
                if(rc.readBroadcast(broadcast_index) == minTreeID) {
                    rc.broadcast(broadcast_index, 0);
                }
            }
        }
        Direction dir = myLocation.directionTo(closestLocation);
        if(rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public static void run(RobotController rc) {

        RobotPlayer.rc = rc;
        System.out.println("Lumberjack: Spawn");

        while (true) {

            try {

                if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
                    Broadcast.decrementRobotCount(RobotType.LUMBERJACK); // Broadcast death on low health
                    Broadcast.dying(ID);
                    ID = -ID; // render ID unusable
                    dying = true; // code will not enter this if statement again
                }

                MapLocation myLocation = rc.getLocation();

                checkForRequests(myLocation);

                TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL);
                for (int i=0; i<neutralTrees.length; i++)
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees

                RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam().opponent());
                if (!rc.hasAttacked()) {
                    if (enemyRobots.length > rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam()).length + rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam()).length)
                        rc.strike(); // attack enemies if close enough and worth it
                } else if (rc.senseNearbyBullets().length > 0) { // if there is nearby combat
                    enemyRobots = rc.senseNearbyRobots(rc.getType().bodyRadius + rc.getType().sensorRadius, rc.getTeam().opponent());
                    if (!rc.hasMoved()) {
                        for (int i=0; i<enemyRobots.length; i++) {
                            Direction dirToEnemy = rc.getLocation().directionTo(enemyRobots[i].getLocation());
                            if (rc.canMove(dirToEnemy)) {
                                rc.move(dirToEnemy); // move towards closest enemy
                                break;
                            }
                        }
                    }
                }


                if (!rc.hasMoved()) { // no enemies, so move to nearest Tree
                    TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
                    for (int i=0; i<nearbyTrees.length; i++) {
                        if (nearbyTrees[i].getTeam() == rc.getTeam())
                            continue;
                        Direction dirToTree = rc.getLocation().directionTo(nearbyTrees[i].getLocation());
                        if (rc.canMove(dirToTree)) { // TODO: pathfinding :)
                            rc.move(dirToTree);
                            break;
                        }
                    }
                }

                if (!rc.hasAttacked()) { // first try attacking enemy trees
                    TreeInfo[] enemyTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam().opponent());
                    for (int i=0; i<enemyTrees.length; i++) {
                        MapLocation enemyTreeLocation = enemyTrees[i].getLocation();
                        if (rc.canChop(enemyTreeLocation)) {
                            rc.chop(enemyTreeLocation);
                            break;
                        }
                    }
                }

                if (!rc.hasAttacked())  // than try neutral trees (already called in beginning for shaking trees)
                    for (int i=0; i<neutralTrees.length; i++) {
                        if (neutralTrees[i].getContainedRobot() == null) // prioritize trees with robots inside
                            continue;
                        MapLocation neutralTreeLocation = neutralTrees[i].getLocation();
                        if (rc.canChop(neutralTreeLocation)) {
                            rc.chop(neutralTreeLocation);
                            break;
                        }
                    }
                if (!rc.hasAttacked()) // nested if statement to avoid both using bytecodes (senseNearbyTrees) and to avoid unnecessary for loop
                    for (int i=0; i<neutralTrees.length; i++) { // if nothing in your robot life worked so far you can be sad and chop normal trees
                        MapLocation neutralTreeLocation = neutralTrees[i].getLocation();
                        if (rc.canChop(neutralTreeLocation)) {
                            rc.chop(neutralTreeLocation);
                            break;
                        }
                    }

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack: Exception");
                e.printStackTrace();
            }
        }
    }
}
