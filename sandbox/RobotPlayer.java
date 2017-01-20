package sandbox;
import battlecode.common.*;

/*
 * Testing enemy that does nothing
 */

public strictfp class RobotPlayer {
    static RobotController rc;
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        RobotPlayer.rc = rc;

        runArchon();
	}
    
    static void runArchon() throws GameActionException {
    	while (true) {
    		
    	}
    }
}
