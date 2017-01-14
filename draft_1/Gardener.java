package draft_1;
import battlecode.common.*;

public class Gardener extends RobotPlayer {

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        initDirList();
        
        try {
        	rc.broadcast(3,  rc.readBroadcast(3) + 1);
        } catch (Exception e) {
            System.out.println("Gardener Exception");
            e.printStackTrace();
        }
        
        System.out.println("I'm a gardener!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SOLDIER, randomDirection())) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }
                if (rc.canBuildRobot(RobotType.LUMBERJACK, towardsArchon.opposite()) && Math.random() < .5 && rc.isBuildReady()) {
                   rc.buildRobot(RobotType.LUMBERJACK, towardsArchon.opposite());
                }
                
                // Move randomly
                if(!tryMove(towardsArchon)) {
                    tryMove(randomDirection());
                }
                /*if(rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite())) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }*/
                Direction dir = randomDirection();
                if(rc.canPlantTree(dir) && Math.random() < 0.2) {
                    rc.plantTree(dir);
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                if(rc.canWater() && Math.random() < 0.3) {
                    rc.water(trees[0].location);
                    //rc.shake(trees[0].location);
                    /*if(rc.getTeamBullets() > 100.0) {
                        rc.donate((float) 10.0);
                    }*/
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }

    }
}
