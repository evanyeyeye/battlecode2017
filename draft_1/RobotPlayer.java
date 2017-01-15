package draft_1;
import battlecode.common.*;

public strictfp class RobotPlayer {
	
    static RobotController rc;
    
    static MapLocation[] initialEnemyArchonLocations; 
    
    static MapLocation archonLoc;
    static Direction[] dirList = new Direction[8];
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Broadcast.initBroadcaster(rc);
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
    
    /**
     * Initializes list of every pi/4 radian directions 
     */ 
    public static void initDirList() {
    	for (int i=0; i<dirList.length; i++)
    		dirList[i] = new Direction((float)(2 * Math.PI * ((float)i) / dirList.length));
    }

    /**
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
        return tryMove(dir, 20, 3);
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
        boolean moved = false;
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

class Broadcast {

    static RobotController rc;

    public static int REINFORCEMENTS_FULFILL_TIME = 10;


    /*
     * BEGIN INDEX ALLOCATION
     * BEGIN INDEX ALLOCATION
     * BEGIN INDEX ALLOCATION
     */

    public static int MAP_DIMENSIONS[]          = {0, 1};
    public static int REINFORCEMENTS_REQUESTS[] = {200, 203, 206, 209};

    // Each robot takes 3 indeces: [age, x, y]
    public static int ARCHON_AVOID_ROBOTS[]     = {100, 103, 106, 109, 112, 115, 118, 121, 124, 127};

    // Only 10 robots can simultaneously request IDs
    public static int ID_REQUESTS[]             = {490, 491, 492, 493, 494, 495, 496, 497, 498, 499};

    // FOR INDECES > 500:
    // -- Indeces greater than 500 are used for specific robot actions
    // -- Each robot that requests an ID will constantly check their spot

    /*
     * END INDEX ALLOCATION
     * END INDEX ALLOCATION
     * END INDEX ALLOCATION
     */

    public static void initBroadcaster(RobotController r) {
        rc = r;
    }


    // Returns 0 if request has been ignored
    // Returns [490, 500) if request is placed
    // Returns [500, 1000) if request succeeded
    public static int requestID(int ticket) throws GameActionException {
        if(ticket == 0) {
            for(int i : ID_REQUESTS) {
                if(i == 0) {
                    rc.broadcast(i, 1);
                    // Establish ticket
                    return i;
                }
            }
            return 0;
        }
        int id = rc.readBroadcast(ticket);
        if(id >= 500) {
            // Revoke ticket
            rc.broadcast(ticket, 0);
            return id;
        }
        return ticket;
    }

    static void broadcastLocation(int index, float x, float y) throws GameActionException {
        int x_i = Float.floatToRawIntBits(x);
        int y_i = Float.floatToRawIntBits(y);

        // Set age to 1
        rc.broadcast(index, 1);
        rc.broadcast(index+1, x_i);
        rc.broadcast(index+2, y_i);
    }

    // Returns true if broadcast succeeded
    // Returns false if all channels are occupied
    public static boolean requestReinforcements(MapLocation ml) throws GameActionException {

        System.out.println("Requesting reinforcements at: " + ml.x + " " + ml.y);

        int age;
        int min_age = Integer.MAX_VALUE;
        int min_index = 0;
        for(int i : REINFORCEMENTS_REQUESTS) {
            age = rc.readBroadcast(i);
            if(age == 0) {
                broadcastLocation(i, ml.x, ml.y);
                return true;
            }
            if(age < min_age) {
                min_age = age;
                min_index = i;
            }
        }

        // If all other requests are still being fulfilled,
        // it is a waste of time to constantly switch to new ones
        if(min_age <= REINFORCEMENTS_FULFILL_TIME) return false;
        broadcastLocation(min_index, ml.x, ml.y);
        return true;
    }

    public static void alertArchon(MapLocation ml) throws GameActionException {

        System.out.println("Alerting archon of belligerent at: " + ml.x + " " + ml.y);

        int age;
        int min_age = Integer.MAX_VALUE;
        int min_index = 0;
        for(int i : ARCHON_AVOID_ROBOTS) {
            age = rc.readBroadcast(i);
            if(age == 0) {
                broadcastLocation(i, ml.x, ml.y);
                return;
            }
            if(age < min_age) {
                min_age = age;
                min_index = i;
            }
        }
        broadcastLocation(min_index, ml.x, ml.y);

    }

}
