package tourney;
import battlecode.common.*;

public strictfp class RobotPlayer {

    static RobotController rc;
    
    final static float CALC_OFFSET = (float).001;
    public static float INTERACT_RADIUS;
    public static float SENSE_RADIUS;
    public static float BULLET_RADIUS;
    
    static MapLocation[] enemyArchonLocations;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        Broadcast.initBroadcaster(rc);
        Direct.initDirector(rc);

        INTERACT_RADIUS = rc.getType().bodyRadius + GameConstants.INTERACTION_DIST_FROM_EDGE;
        SENSE_RADIUS = rc.getType().bodyRadius + rc.getType().sensorRadius;
        BULLET_RADIUS = rc.getType().bodyRadius + rc.getType().bulletSightRadius;
        
        enemyArchonLocations = rc.getInitialArchonLocations(rc.getTeam().opponent());
        
        switch (rc.getType()) {
            case ARCHON:
                Archon.run(rc);
                break;
            case GARDENER:
                Gardener.run(rc);
                break;
            case SOLDIER:
                Soldier.run(rc);
                break;
            case LUMBERJACK:
                Lumberjack.run(rc);
                break;
            case TANK:
                Tank.run(rc);
                break;
            case SCOUT:
                Scout.run(rc);
                break;
        }
    }

    /*
     * Better move method
     * @return true only if location is reached
     */
    public static boolean tryMove(MapLocation ml, float degreeOffset, int checksPerSide) throws GameActionException {

        if (rc.hasMoved())
            return false;
        
		Direction dir = rc.getLocation().directionTo(ml);
    	float dist = rc.getLocation().distanceTo(ml);
    	
    	boolean reached = false;
        if (dist > rc.getType().strideRadius) 
        	dist = rc.getType().strideRadius;
        else
        	reached = true;
        
        if (rc.canMove(dir, dist)) { 
            rc.move(dir, dist);
            return reached;
    	}
        
        int currentCheck = 1;

        while (currentCheck <= checksPerSide) {
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return false;
            }
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return false;
            }
            currentCheck++;
        }

        return false;
    }
    
    /*
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    
    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir, 30, 4);
    }

    /**
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        if (rc.hasMoved())
            return false;

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        
        // Now try a bunch of similar angles
        int currentCheck = 1;

        while (currentCheck <= checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        return false;
    }
    
    public static boolean tryDodge(BulletInfo bullet) throws GameActionException {
    	if (!willCollideWithMe(bullet)) {
    		return true;
    	}
    	Direction dirToBot = bullet.location.directionTo(rc.getLocation());
    	if (tryMove(dirToBot.rotateLeftDegrees(90), 15, 2)) {
    		return true;
    	}
    	if (tryMove(dirToBot.rotateRightDegrees(90), 15, 2)) {
    		return true;
    	}
    	return tryMove(dirToBot.opposite(), 15, 2);
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    public static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta));

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
}
