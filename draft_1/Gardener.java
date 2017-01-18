package draft_1;
import battlecode.common.*;

public class Gardener extends RobotPlayer {

    static final MAX_HP = 40;

	static TreeInfo[] plantedOwner = new TreeInfo[100];
	static int treeIndex = 0;
	static int treeSize = 0;
	
    public static void run(RobotController rc) throws GameActionException {

        System.out.println("Gardener Spawn: " + rc.getID());
        
        RobotPlayer.rc = rc;
        initDirList();

        boolean init = true;
        
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                float archonCoords[] = Broadcast.getArchonLocation();
                archonLoc = new MapLocation(archonCoords[0], archonCoords[1]);

                /*
                Direction towardsArchon = rc.getLocation().directionTo(archonLoc);
                
                /*
                if (init) {
                	for (int i=0; i<5; i++) {
                		if (!tryMove(towardsArchon.opposite()))
                			tryMove(randomDirection());
                		Clock.yield();
                	}
                	init = false;
                } */
                
                /*
                 * Processes:
                 * ----------
                 * move
                 * water
                 * plant trees
                 * build robots
                 */
                
                // water
                /*TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
                if(nearbyTrees.length > 0 && rc.canWater(nearbyTrees[0].location) && nearbyTrees[0].getHealth() < nearbyTrees[0].getMaxHealth() - 5.0) {
                    rc.water(nearbyTrees[0].location);
                }

                // Build a soldier
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER)) {
                	if (rc.canBuildRobot(RobotType.SOLDIER, towardsArchon)) {
                		rc.buildRobot(RobotType.SOLDIER, towardsArchon);
                	} else {
                		Direction randomDirBuild = randomDirection();
                		while (!rc.canBuildRobot(RobotType.SOLDIER, randomDirBuild))
                			randomDirBuild = randomDirection();
                		rc.buildRobot(RobotType.SOLDIER, randomDirBuild);
                	}
                	Broadcast.incrementSoldierCount();
                }

                // plant
                if(rc.hasTreeBuildRequirements() && rc.canPlantTree(towardsArchon)) {
                    rc.plantTree(towardsArchon);
                    tryMove(towardsArchon.opposite());
                    treeSize++;
                }
                
                // move
                if (rc.getLocation().distanceTo(archonLoc) > 5.0)
                	tryMove(towardsArchon.opposite());
                
                /*
                if (rc.getTeamBullets() >= 200) 
                	rc.donate(10); */

                
                // end turn

                // rc.broadcast(((int)Math.random() * 1000), 10);
                // Generate a random direction
                // Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                MapLocation myLocation = rc.getLocation();
                Direction towardsArchon = myLocation.directionTo(archonLoc);
                // Randomly attempt to build a soldier
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER) && rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite()) && Math.random() < .8) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }
                
                // Move randomly
                if(myLocation.distanceTo(archonLoc) < 20) {
                    tryMove(towardsArchon.opposite());
                } else {
                    if(!tryMove(towardsArchon)) {
                        tryMove(randomDirection());
                    }
                }
                /*if(rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite())) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }*/
                if(!Broadcast.anyReinforcementsRequests()) {
                    Direction dir = randomDirection();
                    if(rc.canPlantTree(dir) && Math.random() < 0.1) {
                        rc.plantTree(dir);
                    }
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
