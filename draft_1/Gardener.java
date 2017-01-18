package draft_1;
import battlecode.common.*;

public class Gardener extends RobotPlayer {

	static TreeInfo[] plantedOwner = new TreeInfo[100];
	static int treeIndex = 0;
	
    public static void run(RobotController rc) throws GameActionException {

    	System.out.println("Gardener Spawn: " + rc.getID());
    	
        RobotPlayer.rc = rc;
        initDirList();

        boolean init = true;
        
        while (true) {

            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER) && rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite()) && Math.random() < .8) {
                    Broadcast.incrementSoldierCount();
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }
                if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK) && rc.canBuildRobot(RobotType.LUMBERJACK, towardsArchon.opposite()) && Math.random() < .1 && rc.isBuildReady()) {
                   rc.buildRobot(RobotType.LUMBERJACK, towardsArchon.opposite());
                }
                
                /*
                 * Processes:
                 * ----------
                 * move
                 * water
                 * plant trees
                 * build robots
                 */
                
                // move
                if (rc.getLocation().distanceTo(archonLoc) < 5.0)
                	tryMove(towardsArchon.opposite());
                else
                	tryMove(randomDirection());

                // water
                TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
                if(nearbyTrees.length > 0 && rc.canWater(nearbyTrees[0].location)) {
                    rc.water(nearbyTrees[0].location);
                    //test
                    System.out.println("Just watered: " + rc.canBuildRobot(RobotType.SOLDIER, randomDirection()));
                }

                // Build a soldier
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER)) {
                	if (rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite())) {
                		rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                	} else {
                		Direction randomDirBuild = randomDirection();
                		while (!rc.canBuildRobot(RobotType.SOLDIER, randomDirBuild))
                			randomDirBuild = randomDirection();
                		rc.buildRobot(RobotType.SOLDIER, randomDirBuild);
                	}
                	Broadcast.incrementSoldierCount();
                }
                
                // plant
                Direction dir = randomDirection();
                if(rc.hasTreeBuildRequirements() && rc.canPlantTree(dir) && Math.random() < .1) {
                    rc.plantTree(dir);
                }

                if (rc.getTeamBullets() >= 200) 
                	rc.donate(10);

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
}
