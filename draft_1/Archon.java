package draft_1;
import battlecode.common.*;

public class Archon extends RobotPlayer {
	
    public static void run(RobotController rc) throws GameActionException {
    	
    	RobotPlayer.rc = rc;
        initDirList();
        
        System.out.println("Archon Spawn: " + rc.getID());
        
        while (true) {
            try {
                
                // build gardeners
                if ((rc.getRobotCount() == rc.getInitialArchonLocations(rc.getTeam().opponent()).length || rc.readBroadcast(3) < rc.getRobotCount() / 3) && rc.hasRobotBuildRequirements(RobotType.GARDENER)) {
                    for (int i=0; i<dirList.length; i++) {
                        if (rc.canHireGardener(dirList[i])) {
                            rc.hireGardener(dirList[i]);
                            break;
                        }
                    }
                }
                
                if(Math.random() < 0.05)
                    tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }

    }
    
}
