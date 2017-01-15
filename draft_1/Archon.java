package draft_1;
import battlecode.common.*;

import java.util.HashSet;
import java.util.Iterator;

public class Archon extends RobotPlayer {

    static MapLocation[] corners = new MapLocation[4];

    static HashSet<Integer> unusedIDs = new HashSet<Integer>();
    static HashSet<Integer>   usedIDs = new HashSet<Integer>();
    static void fulfillIDRequests() throws GameActionException {

        Iterator<Integer> it = unusedIDs.iterator();
        for(int i : Broadcast.ID_REQUESTS) {
            int status = rc.readBroadcast(i);
            if(unusedIDs.isEmpty()) return;
            if(status == 1) {
                int newIndex = it.next();
                rc.broadcast(i, newIndex);
                usedIDs.add(newIndex);
                unusedIDs.remove(newIndex);
            }
        }

    }

    // [400 - 490)
    static HashSet<Integer> dynamicIDs_unallocated = new HashSet<Integer>();
    static HashSet<Integer> dynamicIDs_allocated   = new HashSet<Integer>();

    static boolean allocate(int slot, int information) throws GameActionException {
        if(dynamicIDs_allocated.contains(slot)) {
            return false;
        }
        rc.broadcast(slot, information);
        dynamicIDs_unallocated.remove(slot);
        dynamicIDs_allocated.add(slot);
        return true;
    }
    static boolean deallocate(int slot) {
        if(dynamicIDs_unallocated.contains(slot)) {
            return false;
        }
        dynamicIDs_allocated.remove(slot);
        dynamicIDs_unallocated.add(slot);
        return true;
    }

    static int reinforcements_slots[] = new int[4];
    static void fulfillReinforcementsRequests() throws GameActionException {

        int age;
        int num_requests = 0;
        for(int i=0;i<reinforcements_slots.length;i++) {
            if(reinforcements_slots[i] != 0) {
                int[] t = Broadcast.readLocationCode(reinforcements_slots[i]);
                int x = t[0];
                int y = t[1];
                deallocate(x);
                deallocate(y);
                reinforcements_slots[i] = 0;
            }
        }
        for(int i : Broadcast.REINFORCEMENTS_REQUESTS) {

            age = rc.readBroadcast(i);
            if(age == 0) continue;
            if(age > Broadcast.REINFORCEMENTS_FULFILL_TIME) {
                rc.broadcast(i, 0);
                continue;
            }

            if(dynamicIDs_unallocated.isEmpty()) continue;
            Iterator<Integer> it = dynamicIDs_unallocated.iterator();
            int slot_x = it.next();
            int slot_y;
            if(it.hasNext()) {
                slot_y = it.next();
            } else {
                continue;
            }

            if(slot_x < 0 || slot_y < 0) continue;

            allocate(slot_x, rc.readBroadcast(i+1));
            allocate(slot_y, rc.readBroadcast(i+2));

            reinforcements_slots[num_requests++] = Broadcast.genDynamicCode2(slot_x, slot_y, Soldier.REINFORCE);
        }

        if(num_requests == 0) return;

        // Split requests across robots for now
        Iterator<Integer> it = usedIDs.iterator();
        while(it.hasNext()) {
            int robot = it.next();
            int sl = (int)(Math.random() * num_requests);
            rc.broadcast(robot, reinforcements_slots[sl]);
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
        for(int i = 400; i<490; i++) {
            dynamicIDs_unallocated.add(i);
        }

        while (true) {
            try {

                main_archon = main_archon || Broadcast.checkMainArchon();
                if(main_archon) {
                    fulfillIDRequests();
                    fulfillReinforcementsRequests();
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
