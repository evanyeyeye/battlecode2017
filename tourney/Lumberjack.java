package tourney;
import battlecode.common.*;

public class Lumberjack extends RobotPlayer {

    public static int ID = 0;

    public static boolean dying = false;
    
    public static boolean isChopping = false;
    public static TreeInfo targetTree = null;

    public static MapLocation searchLocation = null;
    
    static void checkForRequests() throws GameActionException {
    	
        MapLocation closestLocation = null;
        float closestDistance = Float.MAX_VALUE;
	
        int treeID = 0; // This treeID is not the real tree ID, it's just made up
        int minTreeID = 0;
        int broadcast_index = 0;
        
        for (int i : Broadcast.LUMBERJACK_REQUESTS) {
            treeID = rc.readBroadcast(i);
            if (treeID > 0) {
                MapLocation ml = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(i+1)),
                        Float.intBitsToFloat(rc.readBroadcast(i+2)));
                float dist = rc.getLocation().distanceTo(ml) - Float.intBitsToFloat(rc.readBroadcast(i+3)) - rc.getType().bodyRadius - CALC_OFFSET; 
                if (closestLocation == null || closestDistance > dist) {
                    closestLocation = ml;
                    closestDistance = dist;
                    minTreeID = treeID;
                    broadcast_index = i;
                }
            }
        }
        
        if (minTreeID == 0) return;      
        Direction dirToTree = rc.getLocation().directionTo(closestLocation);
        float distToTree = closestDistance;
        
        if (distToTree > rc.getType().strideRadius)
        	distToTree = rc.getType().strideRadius;
        if (rc.canMove(dirToTree, distToTree)) { // TODO: pathfinding :)
            rc.move(dirToTree, distToTree);
            isChopping = false;
        }

        if (!rc.hasAttacked() && closestDistance < (rc.getType().bodyRadius + rc.getType().strideRadius)) // TODO: needs reworking
            if (rc.canChop(closestLocation)) 
                rc.chop(closestLocation);
			else if (rc.readBroadcast(broadcast_index) == minTreeID) // The tree is gone
                rc.broadcast(broadcast_index, 0);
    }
    
    /*
     * Finds the closest tree to the Lumberjack
     * @param trees sensed trees
     * @return true if found tree
     */
    public static boolean findTree(TreeInfo[] trees) throws GameActionException {
    	for (int i=0; i<trees.length; i++) {
            if (trees[i].getTeam() == rc.getTeam())
                continue;
            if (tryMove(trees[i].getLocation(), 2f, 45)) {
                isChopping = false;
                return true;
            }
        }
    	return false;
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

                TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL);
                for (int i=0; i<neutralTrees.length; i++)
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees

                RobotInfo[] enemyRobots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam().opponent());
                if (!rc.hasAttacked()) {
                    if (enemyRobots.length >= rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam()).length + rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam()).length)
                        rc.strike(); // attack enemies if close enough and worth it
                } else if (rc.senseNearbyBullets().length > 0) { // if there is nearby combat
                    enemyRobots = rc.senseNearbyRobots(SENSE_RADIUS, rc.getTeam().opponent());
                    if (!rc.hasMoved()) {
                        for (int i=0; i<enemyRobots.length; i++) {
                            Direction dirToEnemy = rc.getLocation().directionTo(enemyRobots[i].getLocation());
                            if (rc.canMove(dirToEnemy)) {
                                rc.move(dirToEnemy); // move towards closest enemy
                                isChopping = false;
                                break;
                            }
                        }
                    }
                }
                
                checkForRequests();
                	
                if (isChopping) 
                	if (rc.canChop(targetTree.getLocation()))
                		rc.chop(targetTree.getLocation());
                	else
                		isChopping = false;               	

                if (!isChopping && !rc.hasMoved()) // no enemies, so move to nearest Tree
                    if (!findTree(rc.senseNearbyTrees())) {
                    	if (searchLocation == null) 
                    		searchLocation = enemyArchonLocations[0];
                    	if (tryMove(enemyArchonLocations[0], 2, 45))
                    		searchLocation = null;
                    }
                	
                if (!rc.hasAttacked()) { // first try attacking enemy trees
                    TreeInfo[] enemyTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam().opponent());
                    for (int i=0; i<enemyTrees.length; i++) {
                        MapLocation enemyTreeLocation = enemyTrees[i].getLocation();
                        if (rc.canChop(enemyTreeLocation)) {
                            rc.chop(enemyTreeLocation);
                            isChopping = true;
                            targetTree = enemyTrees[i];
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
                			isChopping = true;
                			targetTree = neutralTrees[i];
                			break;
                		}
                	}
                
                if (!rc.hasAttacked())
                	for (int i=0; i<neutralTrees.length; i++) { // if nothing in your robot life worked so far you can be sad and chop normal trees
                		MapLocation neutralTreeLocation = neutralTrees[i].getLocation();
                		if (rc.canChop(neutralTreeLocation)) {
                			rc.chop(neutralTreeLocation);
                			isChopping = true;
                			targetTree = neutralTrees[i];
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
