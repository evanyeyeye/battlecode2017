package draft_1;
import battlecode.common.*;

public class Lumberjack extends RobotPlayer {

	public static int ID = 0;	
	
	public static boolean dying = false;
	
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
                		if (rc.canMove(dirToTree)) {
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
