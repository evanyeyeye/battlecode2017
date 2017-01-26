package draft_1;
import battlecode.common.*;

public class Gardener extends RobotPlayer {
    
    /*
     * TODO: dynamic building of trees
     * Initiate gardener building sequence
     * @return array of directions
     */
    public static Direction[] createBuildDirSequence() throws GameActionException {
    	
    	Direction[] dirSequence = new Direction[6]; // hexagonal structure 
   
    	if (rc.getTeam() == Team.A) // Team A usually faces right 
	    	for (int i=0; i<dirSequence.length; i++)
	    		dirSequence[i] = new Direction((float)(Math.PI / 3 * i));
    	else if (rc.getTeam() == Team.B) // and Team B usually faces right 
	    	for (int i=0; i<dirSequence.length; i++)
	    		dirSequence[i] = new Direction((float)(Math.PI - (Math.PI / 3 * i)));
    	
    	return dirSequence;
    }
    
    /*
     * Builds a robot
     * @param type RobotType wanted
     * @param dir Direction from Gardener 
     * @return true if robot can be built
     */
    public static boolean buildRobot(RobotType type, Direction dir) throws GameActionException {
    	
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            Broadcast.incrementRobotCount(type);
            return true;
        }
        
        return false;
    }
    
    public static boolean testHome() throws GameActionException {
    	
    	// System.out.println("Begin Test Home");
    	
    	RobotInfo[] robotsNearMe = rc.senseNearbyRobots((rc.getType().bodyRadius + rc.getType().sensorRadius)); 
    	for (int i=0; i<robotsNearMe.length; i++)
    		if (robotsNearMe[i].getType() == RobotType.ARCHON) { // make sure Gardener is not touching Archon
    			if (!rc.hasMoved() && rc.canMove(rc.getLocation().directionTo(robotsNearMe[i].getLocation()).opposite()))
    				rc.move(rc.getLocation().directionTo(robotsNearMe[i].getLocation()).opposite());
    			return false;
    		}
    		
    	int openCount = 0; // number of directions that are not blocked by objects
    	boolean openPath = false; // since no path finding implementation yet, I say if a path is open 2 strides in a row, there is an open path
    	int tempSweetSpot = 0; // 0 by default, set in createBuildDirSequence()
    	for (int i=0; i<buildSequence.length; i++) {
    		rc.setIndicatorDot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), 0, 0, 0);
			if (rc.onTheMap(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), rc.getType().bodyRadius) 
					&& !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), rc.getType().bodyRadius)) {
				openCount++;
				if (!openPath) { // structured awkwardly in order to reduce expensive bytecode operations
					openPath = rc.onTheMap(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius + (float)1.0), rc.getType().bodyRadius) 
							&& !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius + (float)1.0), rc.getType().bodyRadius);
					if (openPath) 
						tempSweetSpot = i; // annoying but necessary
				}
			} 
			// System.out.println("" + i + ": " + openCount);
    	}
    	
    	if (openCount > 2 && openPath) {
    		sweetSpot = tempSweetSpot;
    		return true;
    	}
    	return false;
    }
    
    public static int ID = 0;
    
    public static boolean dying = false;
    
    public static Direction[] buildSequence; // Initiate gardener building sequence 
    public static int sweetSpot; // Opening for robot production
    
    public static void run(RobotController rc) throws GameActionException {

    	RobotPlayer.rc = rc;
    	System.out.println("Gardener: Spawn");
        int spawnRound = rc.getRoundNum();
    	
        buildSequence = createBuildDirSequence();
        
        boolean foundHome = testHome(); // :(
        
        while (true) {
        	
            try {

            	// System.out.println("Starting Loop Bytecodes: " + Clock.getBytecodeNum());
            	
            	if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
            		Broadcast.decrementRobotCount(RobotType.GARDENER); // Broadcast death on low health
            		Broadcast.dying(ID);
            		ID = -ID; // render ID unusable
                    dying = true; // code will not enter this if statement again
            	}
            	
            	TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL); 
            	for (int i=0; i<neutralTrees.length; i++)
            		if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
            			rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees
	            
            	// System.out.println("Shook Trees Bytecodes: " + Clock.getBytecodeNum());
            	
            	TreeInfo[] closeTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, rc.getTeam());
            	if (closeTrees.length > 0) {
            		TreeInfo treeToWater = closeTrees[0];
            		float minHealth = closeTrees[0].getHealth();
            		for (int i=1; i<closeTrees.length; i++)
            			if (closeTrees[i].getHealth() < minHealth && rc.canWater(closeTrees[i].getLocation())) {
            				treeToWater = closeTrees[i];
            				minHealth = closeTrees[i].getHealth();
            			}
            		rc.water(treeToWater.getLocation()); // Water tree with lowest health
	            }
            	
            	// System.out.println("Watered Trees Bytecodes: " + Clock.getBytecodeNum());
            	
            	if (!foundHome) { // escaping homelessness
            		
            		if (rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().sensorRadius, Team.NEUTRAL).length + rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().sensorRadius, rc.getTeam().opponent()).length > 0 
            				&& Broadcast.getRobotCount(RobotType.LUMBERJACK) < 1)
            			for (int i=0; i<buildSequence.length; i++)
            				if (buildRobot(RobotType.LUMBERJACK, buildSequence[i]))
            					break;
            		
            		if (rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent()).length > 0 
            				|| rc.senseNearbyBullets(rc.getType().bulletSightRadius).length > 0
            				|| Broadcast.getRobotCount(RobotType.SOLDIER) + Broadcast.getRobotCount(RobotType.LUMBERJACK) < 1) // Emergency robot requirement scenarios
            			for (int i=0; i<buildSequence.length; i++)
            				if (buildRobot(RobotType.SOLDIER, buildSequence[i]))
            					break;
            		
            		for (int i=0; i<buildSequence.length; i++) {
            			if (rc.hasMoved())
            				break;
            			if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), rc.getType().bodyRadius) 
            					&& rc.canMove(buildSequence[i].opposite()))
            				rc.move(buildSequence[i].opposite());
            			
            		}
            		
            		// System.out.println("Tested Home Bytecodes: " + Clock.getBytecodeNum());
            		
            		foundHome = testHome() || rc.getRoundNum() < spawnRound + 50;
            		if (!foundHome) { // A little weird, but if testHome() is true than process below code without waiting for another loop.
            			Clock.yield();
            			continue;
            		}
            	}
            	
            	// System.out.println("Found Home Bytecodes: " + Clock.getBytecodeNum());
            	
            	for (int i=0; i<buildSequence.length; i++) {
            		if (i == sweetSpot)
            			continue;
            		if (rc.canPlantTree(buildSequence[i]) && (rc.senseNearbyTrees(rc.getType().strideRadius, rc.getTeam()).length < 2 
            				|| !(Broadcast.anyReinforcementsRequests() || !Broadcast.checkMainArchonDistress() 
            				|| Broadcast.getRobotCount(RobotType.SOLDIER) < Broadcast.getRobotCount(RobotType.GARDENER)-1))) // temporary solution
            			rc.plantTree(buildSequence[i]);
            	}
    			
            	// System.out.println("Planted Trees Bytecodes: " + Clock.getBytecodeNum());
            	
            	if (rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL).length + rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, rc.getTeam().opponent()).length > 0)
            		buildRobot(RobotType.LUMBERJACK, buildSequence[sweetSpot]);
            	else
            		buildRobot(RobotType.SOLDIER, buildSequence[sweetSpot]);
            	
            	// System.out.println("Built Soldiers Bytecodes: " + Clock.getBytecodeNum());

                Clock.yield();

            } catch (Exception e) {	
            	System.out.println("Gardener: Exception");
                e.printStackTrace();     
            }
        }
    }
}
