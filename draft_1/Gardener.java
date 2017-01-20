package draft_1;
import battlecode.common.*;

public class Gardener extends RobotPlayer {

    static final int MAX_HP = 40;

	static TreeInfo[] plantedOwner = new TreeInfo[4];
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
                }*/
              

                MapLocation myLocation = rc.getLocation();
                Direction towardsArchon = myLocation.directionTo(archonLoc);
                
                // end turn

                rc.broadcast(((int)Math.random() * 1000), 10);

                // Randomly attempt to build a soldier
                if ((init || Math.random() < 0.3) && rc.hasRobotBuildRequirements(RobotType.SCOUT) && rc.canBuildRobot(RobotType.SCOUT, towardsArchon.opposite())) {
                    rc.buildRobot(RobotType.SCOUT, towardsArchon.opposite());
                    Broadcast.incrementScoutCount();
                    init = false;
                }
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER) && rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite())) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                    Broadcast.incrementSoldierCount();
                }
                
                
                // Move
                if (treeSize < 4) {
		            float dist = myLocation.distanceTo(archonLoc);
		            if(dist < 14) {
		                tryMove(towardsArchon.opposite());
		            } else {
		                if(dist < 20 || !tryMove(towardsArchon)) {
		                    tryMove(randomDirection());
		                }
		            }
		        } else if (!tryMove(rc.getLocation().directionTo(plantedOwner[treeIndex].getLocation()))) {
	        		tryMove(randomDirection());
	        	}

                if(!Broadcast.anyReinforcementsRequests()) {
                    Direction dir = randomDirection();
                    if( rc.canPlantTree(dir) && (treeSize * Broadcast.getGardenerCount() < Broadcast.getSoldierCount()) && treeSize < 4) {
                        rc.plantTree(dir);
                        TreeInfo[] trees = rc.senseNearbyTrees();
                        if (trees.length > 0)
                        	plantedOwner[treeIndex] = trees[0];
                        treeIndex = (treeIndex + 1) % treeSize;
                        treeSize++;
                    }
                }

                if (treeSize < 4) {
		            TreeInfo[] trees = rc.senseNearbyTrees();
		            if(trees.length > 0 && rc.canWater(trees[0].location)) {
		                rc.water(trees[0].location);
		            }
		        } else {
		        	if(rc.canWater(plantedOwner[treeIndex].location)) {
		                rc.water(plantedOwner[treeIndex].location);
		                treeIndex = (treeIndex + 1) % treeSize;
		            } else if (rc.getLocation().distanceTo(plantedOwner[treeIndex].location) < .5)
		            	treeIndex = (treeIndex + 1) % treeSize;
		        }
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }

    }
}
