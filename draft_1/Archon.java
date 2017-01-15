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

                float x = 0.0f;
                float y = 0.0f;
                int age = 0;

                float closestDistance = Float.MAX_VALUE;
                float enemyDistance;
                MapLocation closestEnemy = null;
                MapLocation enemyLocation;
                for(int i : Broadcast.ARCHON_AVOID_ROBOTS) {
                    age = rc.readBroadcast(i);
                    if(age <= 0) continue;

                    x = Float.intBitsToFloat(rc.readBroadcast(i+1));
                    y = Float.intBitsToFloat(rc.readBroadcast(i+2));
                    rc.broadcast(i, age+1);

                    enemyLocation = new MapLocation(x,y);
                    enemyDistance = archonLocation.distanceSquaredTo(enemyLocation);
                    if(closestEnemy == null || enemyDistance < closestDistance) {
                        closestEnemy = enemyLocation;
                        closestDistance = enemyDistance;
                    }
                }
                if(closestEnemy != null) {
                    System.out.println("Archon moving away from: " + closestEnemy.x + " " + closestEnemy.y);
                    tryMove(closestEnemy.directionTo(archonLocation));
                };
                
                if(Math.random() < 0.05)
                    tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }

    }
    
}
