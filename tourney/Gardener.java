package tourney;
import battlecode.common.*;

public class Gardener extends RobotPlayer {
	
	public static Direction[] createMoveDirSequence() throws GameActionException {

        Direction[] dirSequence = new Direction[8]; // octagonal structure

        if (rc.getTeam() == Team.A) // Team A usually is top left
            for (int i=0; i<dirSequence.length; i++)
                dirSequence[i] = new Direction((float)(Math.PI / 4 * -i));
        else if (rc.getTeam() == Team.B) // and Team B usually is bottom right
            for (int i=0; i<dirSequence.length; i++)
                dirSequence[i] = new Direction((float)((Math.PI - Math.PI / 4 * i)));

        return dirSequence;
    }
	
    /*
     * Initiate gardener building sequence
     * @return array of directions
     */
    public static Direction[] createBuildDirSequence() throws GameActionException {

        Direction[] dirSequence = new Direction[6]; // hexagonal structure

        if (rc.getTeam() == Team.A) // Team A usually faces right
            for (int i=0; i<dirSequence.length; i++)
                dirSequence[i] = new Direction((float)(Math.PI / 3 * -i));
        else if (rc.getTeam() == Team.B) // and Team B usually faces left
            for (int i=0; i<dirSequence.length; i++)
                dirSequence[i] = new Direction((float)((Math.PI - Math.PI / 3 * i)));

        return dirSequence;
    }

    /*
     * Builds a robot
     * @param type RobotType wanted
     * @return true if robot can be built
     */
    public static boolean buildRobot(RobotType type) throws GameActionException {

    	if (foundHome) {
    		if (rc.canBuildRobot(type, buildSequence[sweetSpot])) {
	            rc.buildRobot(type, buildSequence[sweetSpot]);
	            Broadcast.incrementRobotCount(type);
	            return true;
    		}
    	}
		else { 
			for (int i=0; i<buildSequence.length; i++)
				if (rc.canBuildRobot(type, buildSequence[i])) {
		            rc.buildRobot(type, buildSequence[i]);
		            Broadcast.incrementRobotCount(type);
		            return true;
	    		}
		}
        return false;
    }
    
    public static boolean chooseBuild() throws GameActionException {
    	
    	if (!(rc.isBuildReady() && rc.hasRobotBuildRequirements(RobotType.SCOUT)))
    		return false;
    	
    	RobotInfo[] enemyRobots = rc.senseNearbyRobots(SENSE_RADIUS, rc.getTeam().opponent());
    	BulletInfo[] bullets = rc.senseNearbyBullets();
    	
    	for (int i=0; i<enemyRobots.length; i++)
    		if (enemyRobots[i].getType().equals(RobotType.TANK)) // if tank your done for
    			return false;
    	
        if (enemyRobots.length == 0 && bullets.length == 0  && Broadcast.getRobotCount(RobotType.SCOUT) == 0
        		// && Broadcast.getRobotCount(RobotType.SOLDIER) + Broadcast.getRobotCount(RobotType.LUMBERJACK) > 0
        		&& rc.getRoundNum() < rc.getRoundLimit() / 5) 
            if (buildRobot(RobotType.SCOUT)) 
            	return true;

    	if (!rc.hasRobotBuildRequirements(RobotType.SOLDIER)) // standard 100 bullet cost
    		return false;
    	
    	for (int i=0; i<enemyRobots.length; i++)
    		if (enemyRobots[i].getType().equals(RobotType.SCOUT))
    			if (buildRobot(RobotType.SOLDIER))
    				return true;
    	
    	if (enemyRobots.length > 0 || bullets.length > 0
    			|| (Broadcast.getRobotCount(RobotType.SOLDIER) == 0 && rc.getRoundNum() > rc.getRoundLimit() / 10)
    			|| Broadcast.getRobotCount(RobotType.SOLDIER) < Broadcast.getRobotCount(RobotType.GARDENER) && rc.getRoundNum() > rc.getRoundLimit() / 10)
    		if (buildRobot(RobotType.SOLDIER))
    			return true;
    	
    	TreeInfo[] enemyTrees = rc.senseNearbyTrees(SENSE_RADIUS, rc.getTeam().opponent());
    	TreeInfo[] neutralTrees = rc.senseNearbyTrees(SENSE_RADIUS, Team.NEUTRAL);
    	
    	if (enemyTrees.length + neutralTrees.length > 0
    			&& (Broadcast.getRobotCount(RobotType.LUMBERJACK) < 1 
    					|| (Broadcast.getRobotCount(RobotType.LUMBERJACK) < Broadcast.getRobotCount(RobotType.GARDENER)
    							|| (rc.getRoundNum() < rc.getRoundLimit() / 5 && Broadcast.getRobotCount(RobotType.LUMBERJACK) < Broadcast.getRobotCount(RobotType.SOLDIER)))))
            if (buildRobot(RobotType.LUMBERJACK)) 
            	return true;
    	
        if (foundHome)
        	if (buildRobot(RobotType.SOLDIER))
        		return true;
        
        return false;
    }
    
    /*
     * Finds a home for the Gardener
     * @return true if found home
     */
    public static boolean findHome() throws GameActionException {

    	if (targetLocation == null) {
        	MapLocation closestArchon = null;
        	RobotInfo[] robotsNearMe = rc.senseNearbyRobots();
        	for (int i=0; i<robotsNearMe.length; i++)
        		if (robotsNearMe[i].getType() == RobotType.ARCHON) {  // do not go towards Archon
        			closestArchon = robotsNearMe[i].getLocation();
        			break;
        		}
        	
        	// System.out.println("START: " + startLocation);
    		float scanRadius = rc.getType().sensorRadius / 2f - CALC_OFFSET;
    		MapLocation bestLocation = null;
    		float maxArea = Float.MIN_VALUE;
    		for (int i=0; i<moveSequence.length; i++) {
    			MapLocation tempLocation = rc.getLocation().add(moveSequence[i], scanRadius);
    			if (tempLocation.equals(startLocation) || (closestArchon != null && (float)(Math.abs(rc.getLocation().directionTo(closestArchon).degreesBetween(moveSequence[i]))) <= 45f))
    				continue;
    			float tempArea = calcArea(tempLocation, scanRadius, moveSequence[i]);
    			if (tempArea > maxArea) {
    				maxArea = tempArea;
    				bestLocation = tempLocation;
    			}
    		}
    		// System.out.println("BEST: " + bestLocation);
    		// rc.setIndicatorDot(bestLocation, 255, 255, 255);
    		targetLocation = bestLocation;
    	    startLocation = rc.getLocation();
    	}
    	
    	// rc.setIndicatorDot(targetLocation, 255, 255, 255);
    	
    	prevLocation = rc.getLocation();
    	if (tryMove(targetLocation, 2, 45) 
    			|| (!rc.canMove(targetLocation) && rc.getLocation().distanceTo(targetLocation) < rc.getType().bodyRadius) 
    			|| !rc.onTheMap(rc.getLocation().add(rc.getLocation().directionTo(targetLocation)))
    			|| stepsCount >= 20) {
    		stepsCount = 0;
    		targetLocation = null;
    	}
    	
    	stepsCount += 1;

    	return testHome();
    }
    
    public static float calcArea(MapLocation center, float radius, Direction dir) throws GameActionException {
    	
		// rc.setIndicatorDot(center, 0, 0, 0);
		
    	float area = 0f;
    	if (rc.onTheMap(center, radius))
			area = (float)(Math.PI * Math.pow(radius, 2f));
    	else {
    		MapLocation low;
    		MapLocation high;
    		if (rc.onTheMap(center)) {
    			low = center;
    			high = center.add(dir, radius);
    		} else {
    			low = rc.getLocation();
    			high = center;
    		}
    		MapLocation mid = low.add(dir, low.distanceTo(high) / 2f);
    		float calcOffset = .1f;
    		while (low.directionTo(high).equals(dir) && low.distanceTo(high) > calcOffset) {
    			if (rc.onTheMap(mid)) 
    				low = mid.add(dir, calcOffset);
    			else
    				high = mid.add(dir.opposite(), calcOffset);
    			mid = low.add(dir, low.distanceTo(high) / 2f);
    		}
    		float height = rc.getLocation().distanceTo(mid); // isn't exact but oh well
    		area = (float)(Math.pow(radius, 2f) * Math.acos((radius-height)/radius) - (radius-height) * Math.sqrt(2*radius*height-Math.pow(height, 2)));
    	}
    	
    	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(center, radius, null);
    	for (int i=0; i<nearbyRobots.length; i++)
    		area -= (float)(Math.PI * Math.pow(nearbyRobots[i].getRadius(), 2f)); // won't be exact either
    	
    	TreeInfo[] nearbyTrees = rc.senseNearbyTrees(center, radius, null);
		for (int i=0; i<nearbyTrees.length; i++)
    		area -= (float)(Math.PI * Math.pow(nearbyTrees[i].getRadius(), 2f));
    	
    	return area;
    }
    
    public static boolean testHome() throws GameActionException {
    	
    	RobotInfo[] allyRobots = rc.senseNearbyRobots(SENSE_RADIUS, rc.getTeam());
    	for (int i=0; i<allyRobots.length; i++)
    		if (allyRobots[i].getType() == RobotType.ARCHON || allyRobots[i].getType() == RobotType.GARDENER) // make sure Gardener is not touching Archon
    			return false;
    	
    	RobotInfo[] enemyRobots = rc.senseNearbyRobots(SENSE_RADIUS, rc.getTeam().opponent());
    	if (enemyRobots.length > allyRobots.length)
    		return false;
    	
        int openCount = 0; // number of directions that are not blocked by objects
        boolean openPath = false; // I say if a path is open 2 strides in a row, there is an open path
        int tempSweetSpot = 2; // 2 by default
        for (int i=0; i<buildSequence.length; i++) {
            // rc.setIndicatorDot(rc.getLocation().add(buildSequence[i], INTERACT_RADIUS), 0, 0, 0);
            if (rc.onTheMap(rc.getLocation().add(buildSequence[i], INTERACT_RADIUS), GameConstants.BULLET_TREE_RADIUS)
                    && !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], INTERACT_RADIUS), GameConstants.BULLET_TREE_RADIUS)) {
                openCount++;
                if (!openPath || i == 2) { // structured awkwardly in order to reduce expensive bytecode operations
                    openPath = rc.onTheMap(rc.getLocation().add(buildSequence[i], INTERACT_RADIUS + 1.0f), rc.getType().bodyRadius)
                        && !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], INTERACT_RADIUS + 1.0f), rc.getType().bodyRadius);
                    if (openPath)
                        tempSweetSpot = i; 
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
    
    // public static int ID = 0;

    public static boolean dying = false;

    public static Direction[] moveSequence;
    
    public static Direction[] buildSequence; // Declare gardener building sequence
    public static int sweetSpot; // Opening for robot production
    
    public static MapLocation targetLocation = null;
    public static MapLocation startLocation = null;
    
    public static MapLocation prevLocation = null; // useless as of now, never got chance to implement 
    public static int stepsCount = 0;
    
    public static boolean foundHome = false;
    
    public static void run(RobotController rc) throws GameActionException {

    	RobotPlayer.rc = rc;
    	System.out.println("Gardener: Spawn");

    	moveSequence = createMoveDirSequence();
        buildSequence = createBuildDirSequence();

        while (true) {

            try {

                // System.out.println("Starting Loop Bytecodes: " + Clock.getBytecodeNum());

                if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
                    Broadcast.decrementRobotCount(RobotType.GARDENER); // Broadcast death on low health
                    rc.broadcast(Broadcast.FOUND_HOME_COUNT_INDEX, rc.readBroadcast(Broadcast.FOUND_HOME_COUNT_INDEX)-1);
                    // Broadcast.dying(ID);
                    // ID = -ID; // render ID unusable
                    dying = true; // code will not enter this if statement again
                }

                TreeInfo[] neutralTrees = rc.senseNearbyTrees(INTERACT_RADIUS, Team.NEUTRAL);
                for (int i=0; i<neutralTrees.length; i++) {
                	Broadcast.requestLumberjack(neutralTrees[i]);
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees
                }
                
                TreeInfo[] closeTrees = rc.senseNearbyTrees(INTERACT_RADIUS, rc.getTeam());
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

                int roundNum = rc.getRoundNum();
                int roundLim = rc.getRoundLimit();
                if (roundNum >= roundLim-1)
                	rc.donate(rc.getTeamBullets());
                else if (rc.getTeamBullets() >= 800.0) {
                    rc.donate((float)(50 * (7.5 + (20 - 7.5) * (roundNum/roundLim)))); // Donate enough for 20 victory points
                } 
                
                if (!foundHome) { // escaping homelessness
              
                	chooseBuild(); // build based on surroundings
                	
                	foundHome = findHome() 
                			|| rc.senseNearbyRobots(SENSE_RADIUS, rc.getTeam().opponent()).length > 0; 
                			/*|| (rc.readBroadcast(Broadcast.FOUND_HOME_COUNT_INDEX) < 3 
                					&& rc.readBroadcast(Broadcast.FOUND_HOME_COUNT_INDEX) < rc.getRoundNum() / (rc.getRoundLimit() / 30));*/
                    
                    if (!foundHome) { // If testHome() is true than process below code without waiting for another loop.
                        Clock.yield();
                        continue;
                    } else {
                    	rc.broadcast(Broadcast.FOUND_HOME_COUNT_INDEX, rc.readBroadcast(Broadcast.FOUND_HOME_COUNT_INDEX)+1);
                    }
                }
                
                for (int i=0; i<buildSequence.length; i++) {
                    if (i == sweetSpot)
                        continue;
                    if (rc.canPlantTree(buildSequence[i]) 
                    		&& (rc.senseNearbyTrees(rc.getType().strideRadius, rc.getTeam()).length < 2 
                    				|| !(Broadcast.anyReinforcementsRequests() || Broadcast.checkMainArchonDistress()) 
            						|| (rc.getRoundNum() > rc.getRoundLimit() / 5 && Broadcast.getRobotCount(RobotType.SOLDIER) < Broadcast.getRobotCount(RobotType.GARDENER))))
                        rc.plantTree(buildSequence[i]);
                }
                
                chooseBuild(); // build based on surroundings
                
		        // System.out.println("Ending Loop Bytecodes: " + Clock.getBytecodeNum());
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener: Exception");
                e.printStackTrace();
            }
        }
    }
}
