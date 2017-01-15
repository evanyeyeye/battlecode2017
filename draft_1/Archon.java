package draft_1;
import battlecode.common.*;

import java.util.HashSet;
import java.util.Iterator;

public class Archon extends RobotPlayer {
	
    static MapLocation[] corners = new MapLocation[4];

    // Currently only indeces 500-999 are used, but we allocate all 1000
    // spaces for math simplicity
    static int[] robots = new int[1000];

    static HashSet<Integer> unusedIDs = new HashSet<Integer>();
    static void fulfillIDRequests() throws GameActionException {

        Iterator<Integer> it = unusedIDs.iterator();
        for(int i : Broadcast.ID_REQUESTS) {
            int status = rc.readBroadcast(i);
            if(unusedIDs.isEmpty()) return;
            if(status == 1) {
                int newIndex = it.next();
                rc.broadcast(i, newIndex);
                unusedIDs.remove(newIndex);
            }
        }

    }

    static boolean main_archon = false;
    // Main archon functions:
    // -- index and coordinate robots
    // Revoked:
    // -- when main archon has low health
    // -- and there are other archons to main


    public static void run(RobotController rc) throws GameActionException {
    	
    	RobotPlayer.rc = rc;
        initDirList();
        
        System.out.println("Archon Spawn: " + rc.getID());

        for(int i = 500; i<1000; i++) {
            unusedIDs.add(i);
        }
        
        while (true) {
            try {

                main_archon = main_archon || Broadcast.checkMainArchon();
                if(main_archon) {
                    fulfillIDRequests();
                }

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
