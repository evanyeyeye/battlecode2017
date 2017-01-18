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
                archonLoc = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));

                Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                
                if (init) {
                	for (int i=0; i<5; i++) {
                		if (!tryMove(towardsArchon.opposite()))
                			tryMove(randomDirection());
                		Clock.yield();
                	}
                	init = false;
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
                if (rc.getLocation().distanceTo(archonLoc) < 5.0 && !tryMove(towardsArchon.opposite()))
                	tryMove(randomDirection());
                
                Direction dir = randomDirection();
                if(rc.canPlantTree(dir) && Broadcast.getSoldierCount() >= Broadcast.getGardenerCount()) {
                    rc.plantTree(dir);
                }
                
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
