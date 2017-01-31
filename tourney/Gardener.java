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
     * TODO: any angle building of trees
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
    
    public static boolean emergencyBuild() throws GameActionException {
    	
    	if (!(rc.isBuildReady() && rc.hasRobotBuildRequirements(RobotType.SOLDIER))) // standard 100 bullet cost
    		return false;
    		
    	if (rc.senseNearbyTrees(SENSE_RADIUS, rc.getTeam().opponent()).length + rc.senseNearbyTrees(SENSE_RADIUS, Team.NEUTRAL).length > 0
    			&& Broadcast.getRobotCount(RobotType.LUMBERJACK) < Broadcast.getRobotCount(RobotType.GARDENER))
            for (int i=0; i<buildSequence.length; i++)
                if (buildRobot(RobotType.LUMBERJACK, buildSequence[i])) 
                	return true;
                           
        if (rc.senseNearbyRobots(SENSE_RADIUS, rc.getTeam().opponent()).length > 0
                || rc.senseNearbyBullets(BULLET_RADIUS).length > 0
                || Broadcast.getRobotCount(RobotType.SOLDIER) < 1) // Emergency robot requirement scenarios
            for (int i=0; i<buildSequence.length; i++)
                if (buildRobot(RobotType.SOLDIER, buildSequence[i]))
                	return true;

        /*
            if ((rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length < 20
                && rc.senseNearbyBullets(rc.getType().bulletSightRadius).length < 5)
                && Broadcast.getRobotCount(RobotType.SCOUT) < 2 && 
                (Broadcast.getRobotCount(RobotType.SOLDIER) > 1 || Broadcast.getRobotCount(RobotType.LUMBERJACK) > 1))
            for (int i=0; i<buildSequence.length; i++)
                if (buildRobot(RobotType.SCOUT, buildSequence[i]))
                    break;
        */
        
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
        	
    		float scanRadius = rc.getType().sensorRadius / 2f - CALC_OFFSET;
    		MapLocation bestLocation = null;
    		float maxArea = Float.MIN_VALUE;
    		for (int i=0; i<moveSequence.length; i++) {
    			MapLocation tempLocation = rc.getLocation().add(moveSequence[i], scanRadius);
    			
    			if (tempLocation.equals(startLocation) || (float)(Math.abs(rc.getLocation().directionTo(closestArchon).degreesBetween(moveSequence[i]))) <= 45f)
    				continue;
    			float tempArea = calcArea(tempLocation, scanRadius, moveSequence[i]);
    			if (tempArea > maxArea) {
    				maxArea = tempArea;
    				bestLocation = tempLocation;
    			}
    		}
    		rc.setIndicatorDot(bestLocation, 255, 255, 255);
    		targetLocation = bestLocation;
    	    startLocation = rc.getLocation();
    	}
    	
    	rc.setIndicatorDot(targetLocation, 255, 255, 255);
    	
    	if (tryMove(targetLocation, 2, 45) || rc.getLocation().distanceTo(targetLocation) < rc.getType().bodyRadius || !rc.onTheMap(rc.getLocation().add(rc.getLocation().directionTo(targetLocation))))
    		targetLocation = null;
        
    	return testHome();
    }
    
    public static float calcArea(MapLocation center, float radius, Direction dir) throws GameActionException {
    	
		rc.setIndicatorDot(center, 0, 0, 0);
		
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
    
    /*
     * Really crappy method until we get pathfinding 
     */
    public static boolean tryMove(MapLocation ml, float degreeOffset, int checksPerSide) throws GameActionException {

        if (rc.hasMoved())
            return false;
        
		Direction dir = rc.getLocation().directionTo(ml);
    	float dist = rc.getLocation().distanceTo(ml);
    	
    	boolean temp = false;
        if (dist > rc.getType().strideRadius) 
        	dist = rc.getType().strideRadius;
        else
        	temp = true;
        if (rc.canMove(dir, dist)) { 
        	prevLocation = rc.getLocation();
            rc.move(dir, dist);
            return temp;
    	}
        
        int currentCheck = 1;

        while (currentCheck <= checksPerSide) {
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
            	prevLocation = rc.getLocation();
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return false;
            }
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
            	prevLocation = rc.getLocation();
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return false;
            }
            currentCheck++;
        }

        return false;
    }
    
    public static boolean testHome() throws GameActionException {

        // System.out.println("Begin Test Home");
    	RobotInfo[] robotsNearMe = rc.senseNearbyRobots();
    	for (int i=0; i<robotsNearMe.length; i++)
    		if (robotsNearMe[i].getType() == RobotType.ARCHON) // make sure Gardener is not touching Archon
    			return false;
    	
        int openCount = 0; // number of directions that are not blocked by objects
        boolean openPath = false; // since no path finding implementation yet, I say if a path is open 2 strides in a row, there is an open path
        int tempSweetSpot = 2; // 2 by default, set in createBuildDirSequence()
        for (int i=0; i<buildSequence.length; i++) {
            rc.setIndicatorDot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), 0, 0, 0);
            if (rc.onTheMap(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), rc.getType().bodyRadius)
                    && !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius), rc.getType().bodyRadius)) {
                openCount++;
                if (!openPath || i == 2) { // structured awkwardly in order to reduce expensive bytecode operations
                    openPath = rc.onTheMap(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius + 1.0f), rc.getType().bodyRadius)
                        && !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(buildSequence[i], rc.getType().bodyRadius + rc.getType().strideRadius + 1.0f), rc.getType().bodyRadius);
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

    public static Direction[] moveSequence;
    
    public static Direction[] buildSequence; // Declare gardener building sequence
    public static int sweetSpot; // Opening for robot production
    
    public static MapLocation targetLocation = null;
    public static MapLocation startLocation = null;
    public static MapLocation prevLocation = null;
    
    public static void run(RobotController rc) throws GameActionException {

    	RobotPlayer.rc = rc;
    	System.out.println("Gardener: Spawn");

    	moveSequence = createMoveDirSequence();
        buildSequence = createBuildDirSequence();

        boolean foundHome = false; // :(
		int spawnRound = rc.getRoundNum();

        while (true) {

            try {

                // System.out.println("Starting Loop Bytecodes: " + Clock.getBytecodeNum());

                if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
                    Broadcast.decrementRobotCount(RobotType.GARDENER); // Broadcast death on low health
                    Broadcast.dying(ID);
                    ID = -ID; // render ID unusable
                    dying = true; // code will not enter this if statement again
                }

                TreeInfo[] neutralTrees = rc.senseNearbyTrees(INTERACT_RADIUS, Team.NEUTRAL);
                for (int i=0; i<neutralTrees.length; i++)
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees

                // System.out.println("Shook Trees Bytecodes: " + Clock.getBytecodeNum());

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

                // System.out.println("Watered Trees Bytecodes: " + Clock.getBytecodeNum());

                if (!foundHome) { // escaping homelessness

                	emergencyBuild();

                    foundHome = findHome() || rc.getRoundNum() > spawnRound + rc.getRoundLimit() / 50;
                    
                    // System.out.println("Tested Home Bytecodes: " + Clock.getBytecodeNum());
                    
                    if (!foundHome) { // If testHome() is true than process below code without waiting for another loop.
                        Clock.yield();
                        continue;
                    }
                }

                // System.out.println("Found Home Bytecodes: " + Clock.getBytecodeNum());

                emergencyBuild(); // Respond to any emergencies before planting trees
                
                for (int i=0; i<buildSequence.length; i++) {
                    if (i == sweetSpot)
                        continue;
                    if (rc.canPlantTree(buildSequence[i]) && (rc.senseNearbyTrees(rc.getType().strideRadius, rc.getTeam()).length < 2 
                    		|| !(Broadcast.anyReinforcementsRequests() || !Broadcast.checkMainArchonDistress()
                    		|| Broadcast.getRobotCount(RobotType.SOLDIER) < Broadcast.getRobotCount(RobotType.GARDENER)-1))) // temporary solution
                        rc.plantTree(buildSequence[i]);
                }

                // System.out.println("Planted Trees Bytecodes: " + Clock.getBytecodeNum());
                
                /*if(rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent()).length < 1 && rc.senseNearbyBullets(rc.getType().bulletSightRadius).length < 1 && rc.getRoundNum() < rc.getRoundLimit() / 10) {
                    buildRobot(RobotType.SCOUT, buildSequence[sweetSpot]);
                }*/
                if (rc.senseNearbyTrees((rc.getType().bodyRadius + rc.getType().strideRadius), Team.NEUTRAL).length + rc.senseNearbyTrees((rc.getType().bodyRadius + rc.getType().strideRadius), rc.getTeam().opponent()).length > 0) {
                    buildRobot(RobotType.LUMBERJACK, buildSequence[sweetSpot]);
                } else {
                    buildRobot(RobotType.SOLDIER, buildSequence[sweetSpot]);
                }
                
                // System.out.println("Built Robots Bytecodes: " + Clock.getBytecodeNum());

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener: Exception");
                e.printStackTrace();
            }
        }
    }
}
