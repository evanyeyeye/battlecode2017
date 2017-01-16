package draft_1;
import battlecode.common.*;

public class Gardener extends RobotPlayer {

    public static void run(RobotController rc) throws GameActionException {

        RobotPlayer.rc = rc;
        initDirList();

        try {
        	rc.broadcast(3,  rc.readBroadcast(3) + 1);
        } catch (Exception e) {
            System.out.println("Gardener Exception");
            e.printStackTrace();
        }

        System.out.println("Gardener Spawn: " + rc.getID());

        boolean hasSpawnedScout = false;

        while (true) {

            try {

                // Listen for home archon's location
                archonLoc = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));

                Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));

                // Randomly attempt to build a soldier in this direction
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER) && rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite()) && Math.random() < 0.8) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }

                /*
                if (rc.hasRobotBuildRequirements(RobotType.SCOUT) && rc.canBuildRobot(RobotType.SCOUT, towardsArchon.opposite()) && !hasSpawnedScout) {
                    rc.buildRobot(RobotType.SCOUT, towardsArchon.opposite());
                    hasSpawnedScout = true;
                } */

                // Move randomly
                //if(!tryMove(towardsArchon)) {
                    tryMove(randomDirection());
                //}

                Direction dir = randomDirection();
                if(rc.canPlantTree(dir) && Math.random() < 0.2) {
                    rc.plantTree(dir);
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                if(trees.length > 0 && rc.canWater(trees[0].location) && Math.random() < 0.3) {
                    rc.water(trees[0].location);
                    //rc.shake(trees[0].location);
                    /*if(rc.getTeamBullets() > 100.0) {
                        rc.donate((float) 10.0);
                    }*/
                }

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }

    }
}
