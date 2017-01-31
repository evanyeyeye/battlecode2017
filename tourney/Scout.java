package tourney;
import battlecode.common.*;

public class Scout extends RobotPlayer {

	public static int ID = 0;
	
	public static boolean dying = false;
	
	public static boolean flee = false;
	
	public static Direction targetDirection; 
	
    public static void run(RobotController rc) {
    	
        RobotPlayer.rc = rc;
        System.out.println("Scout: Spawn");

        targetDirection = rc.getLocation().directionTo(enemyArchonLocations[0]);
        
        while (true) {

            try {
            	
            	if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
                    Broadcast.decrementRobotCount(RobotType.SCOUT); // Broadcast death on low health
                    Broadcast.dying(ID);
                    ID = -ID; // render ID unusable
                    dying = true;
                }
            	
        		if (ID < 500 && !dying) {
                    ID = Broadcast.requestID(ID);
                }

        		TreeInfo[] neutralTrees = rc.senseNearbyTrees(INTERACT_RADIUS, Team.NEUTRAL);
                for (int i=0; i<neutralTrees.length; i++)
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees

                // TODO: dodging is cancer at the moment, yet to be implemented
                
                if (!rc.hasMoved() && !flee) { // than collect
                		
	                for (int i=0; i<neutralTrees.length; i++) 
	                	if (neutralTrees[i].getContainedBullets() > 0) {
	                        tryMove(neutralTrees[i].getLocation(), 2, 45); 
	                        break;
	                	}     
	                
	                if (!rc.hasMoved())
	                	if (!tryMove(targetDirection, 2, 45))
	                		targetDirection = targetDirection.rotateRightDegrees(90f); // bounce simulation lolol
	
                }
                
                /* buggy - will lock the scout in place if no trees are around
            	RobotInfo[] enemyRobots = rc.senseNearbyRobots(SENSE_RADIUS/2f, rc.getTeam().opponent());
                if (enemyRobots.length > 0) { 
                	flee = true;
            		tryMove(rc.getLocation().directionTo(enemyRobots[0].getLocation()).opposite(), 2, 45); // utilize range capabilities, don't wait for bullet
                } else 
                	flee = false;
				*/
                
        		Clock.yield();

                
            } catch (Exception e) {
                System.out.println("Scout: Exception");
                e.printStackTrace();
            }
        }
    }
}
