package draft_1;
import battlecode.common.*;

public class Archon extends RobotPlayer {
	
    MapLocation[] corners = new MapLocation[4];

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
                MapLocation archonLocation = rc.getLocation();

                MapLocation[] enemyLocations = new MapLocation[10];
                int currentLocationIndex = 0;

                float x = 0.0f;
                float y = 0.0f;
                int age = 0;
                for(int i : Broadcast.ARCHON_AVOID_ROBOTS) {
                    age = rc.readBroadcast(i);
                    if(age <= 0) continue;

                    x = Float.intBitsToFloat(rc.readBroadcast(i+1));
                    y = Float.intBitsToFloat(rc.readBroadcast(i+2));
                    rc.broadcast(i, age+1);

                    enemyLocations[currentLocationIndex++] = new MapLocation(x,y);
                }

                if(currentLocationIndex != 0) {
                    // Figure out closest belligerent
                    // Move away
                }
                
                if(Math.random() < 0.05)
                    tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                rc.broadcast(0,(int)archonLocation.x);
                rc.broadcast(1,(int)archonLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }

    }
    
}
