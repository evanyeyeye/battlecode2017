package tourney;
import battlecode.common.*;

public class Scout extends RobotPlayer {

	public static int ID = 0;
	
	public static boolean dying = false;
	
	public static boolean flee = false;
	
	public static Direction targetDirection; 
	
	public static Direction fleeFrom; 
	
    public static void run(RobotController rc) {
    	
        RobotPlayer.rc = rc;
        System.out.println("Scout: Spawn");
        Team enemy = rc.getTeam().opponent();
        Team ally  = rc.getTeam();
        targetDirection = rc.getLocation().directionTo(enemyArchonLocations[0]);
        RobotInfo[] enemies;
        BulletInfo[] bullets;
       // dir = randomDirection();
        while (true) {

            try {
            	enemies = rc.senseNearbyRobots(rc.getType().sensorRadius, enemy);
            	bullets = rc.senseNearbyBullets(rc.getType().bulletSightRadius);

            	if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {

                    Broadcast.decrementRobotCount(RobotType.SCOUT); // Broadcast death on low health
                    Broadcast.dying(ID);
                    ID = -ID; // render ID unusable
                    dying = true;
                }
            	
        		if (ID < 500 && !dying) {
                    ID = Broadcast.requestID(ID);
                }

        		TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
                for (int i=0; i < neutralTrees.length; i++) {
                    MapLocation loc = neutralTrees[i].getLocation();
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation())) {
                        rc.shake(loc); // Collect free bullets from neutral trees
                        targetDirection = (rc.getLocation().directionTo(loc));                        Broadcast.requestLumberjack(neutralTrees[i]);
                    }
                }
                
                if(enemies.length > 0) {
	            	/*if((enemies[0].getType() == RobotType.ARCHON 
	            				|| enemies[0].getType() == RobotType.GARDENER) 
	            				&& rc.senseNearbyBullets(rc.getType().bulletSightRadius).length < 5 ) {
	            			dir = (rc.getLocation().directionTo(enemies[0].getLocation()));
	        		} */
	        		if(dying || bullets.length > 5) {
	    				flee = true;
	    				fleeFrom = rc.getLocation().directionTo(bullets[0].getLocation()).opposite();
    					tryMove(rc.getLocation().directionTo(bullets[0].getLocation()).opposite());
	    			}
	        		
            		if(rc.canFireSingleShot() && !rc.hasAttacked() && rc.getLocation().distanceTo(enemies[0].getLocation()) < 4) {
        				rc.fireSingleShot(rc.getLocation().directionTo(enemies[0].getLocation()));
        			}
                } else if(bullets.length < 5 || enemies.length == 0) {
                	flee = false;
                	targetDirection = randomDirection();
                }
            	//}
                
        		//if(!rc.hasMoved()) {
    			while (!rc.onTheMap(rc.getLocation().add(targetDirection, rc.getType().strideRadius))) {
        			targetDirection = targetDirection.rotateLeftDegrees((float) 45.0);
        		}
        			
    			/*if(enemies.length > 0 && dir.degreesBetween(rc.getLocation().directionTo(enemies[0].getLocation())) < 10) {
    				dir = rc.getLocation().directionTo(enemies[0].getLocation()).opposite();
    			}*/
    			if(flee) {
    				tryMove(fleeFrom);
    			}
	        		
                
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
