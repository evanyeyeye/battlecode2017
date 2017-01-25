package draft_1;
import battlecode.common.*;

import java.util.HashSet;
import java.util.Iterator;

public class Archon extends RobotPlayer {

    static final int MAX_HP = 400;

    static MapLocation[] corners = new MapLocation[4];

    static HashSet<Integer> unusedIDs; // = new HashSet<Integer>(1);
    static HashSet<Integer>   usedIDs; // = new HashSet<Integer>(1);
    static void fulfillIDRequests() throws GameActionException {

        if(cycle_num < 7) return;
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
    static HashSet<Integer> dynamicIDs_unallocated; // = new HashSet<Integer>(1);
    static HashSet<Integer> dynamicIDs_allocated; // = new HashSet<Integer>(1);

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

        if(cycle_num < 10) return;
        int age;
        int num_requests = 0;
        for(int i=0;i<reinforcements_slots.length;i++) {
            if(reinforcements_slots[i] != 0) {
                int[] t = Broadcast.readDynamicChannelCode2(reinforcements_slots[i]);
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
                System.out.println("REQUEST PULLED: " + i);
                continue;
            } else {
                rc.broadcast(i, age+1);
            }

            System.out.println("REQUEST STANDING: " + i + " : " + age);

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

            // Allocate slots to start the location of the request
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

            // If the same request hasn't been fulfilled
            int current_code = rc.readBroadcast(robot);
            if(current_code == Broadcast.DYING) {
                System.out.println("Revoking ID of dying robot: " + robot);
                System.out.println("Rest in peace my friend");
                unusedIDs.add(robot);
                usedIDs.remove(robot);
                rc.broadcast(robot, 0);
            } else {
                if(current_code != -1 * reinforcements_slots[sl]) {
                    rc.broadcast(robot, reinforcements_slots[sl]);
                }
            }
        }

    }

    static int cycle_num = 0;

    static boolean main_archon = false;
    // Main archon functions:
    // -- index and coordinate robots
    // Revoked:
    // -- when main archon has low health
    // -- and there are other archons to main


    public static void run(RobotController rc) throws GameActionException {

        System.out.println("Archon Spawn: " + rc.getID());

        RobotPlayer.rc = rc;

        while (true) {
            try {

                // if Archon has taken damage
                // rc.broadcast(Broadcast.ARCHON_IN_DISTRESS, 1);

                main_archon = main_archon || Broadcast.checkMainArchon();
                if (main_archon) {
                    fulfillIDRequests();
                    fulfillReinforcementsRequests();
                }

                System.out.println("cycle: " + cycle_num);

                // Build gardeners
                Direction dir = randomDirection();

                if (rc.canHireGardener(dir) && (Broadcast.getRobotCount(RobotType.GARDENER) < 3 || Broadcast.getRobotCount(RobotType.GARDENER) < Broadcast.getRobotCount(RobotType.SOLDIER) / 2)
                        && (rc.onTheMap(rc.getLocation().add(dir, rc.getType().bodyRadius + rc.getType().strideRadius + (float)2.0), rc.getType().bodyRadius)
                            && !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(dir, rc.getType().bodyRadius + rc.getType().strideRadius + (float)2.0), rc.getType().bodyRadius))) {

                    rc.hireGardener(dir); // temporary check (TODO: DOESNT WORK ON TIGHT MAPS) until gardeners become legit
                    Broadcast.incrementRobotCount(RobotType.GARDENER);

                }

                if(cycle_num < 7) {
                    switch(cycle_num) {
                        case 2:
                            dynamicIDs_unallocated = new HashSet<Integer>();
                            break;
                        case 3:
                            dynamicIDs_allocated = new HashSet<Integer>();
                            break;
                        case 4:
                            usedIDs = new HashSet<Integer>();
                            break;
                        case 5:
                            unusedIDs = new HashSet<Integer>();
                            break;
                        case 6:
                            for(int i = 500; i<1000; i++) {
                                unusedIDs.add(i);
                            }
                            for(int i = 400; i<490; i++) {
                                dynamicIDs_unallocated.add(i);
                            }
                            break;
                    }
                }
                cycle_num++;

                MapLocation archonLocation = rc.getLocation();

                float x = 0.0f;
                float y = 0.0f;
                int age = 0;

                float closestDistance = Float.MAX_VALUE;
                float enemyDistance;
                MapLocation closestEnemy = null;
                MapLocation enemyLocation;

                Team enemy = rc.getTeam().opponent();
                RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemy);

                for(RobotInfo en : enemies) {
                    enemyLocation = en.location;
                    enemyDistance = enemyLocation.distanceSquaredTo(archonLocation);
                    if(closestEnemy == null || enemyDistance < closestDistance) {
                        closestDistance = enemyDistance;
                        closestEnemy = enemyLocation;
                    }
                }

                boolean setBySelf = closestEnemy != null;

                for(int i : Broadcast.ARCHON_AVOID_ROBOTS) {
                    age = rc.readBroadcast(i);
                    if(age <= 0) continue;

                    rc.broadcast(i, age+1);

                    // Ignore other warnings because we
                    // prefer our own warnings
                    if(setBySelf) continue;

                    x = Float.intBitsToFloat(rc.readBroadcast(i+1));
                    y = Float.intBitsToFloat(rc.readBroadcast(i+2));

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


                // Low priority tasks
                //
                // Broadcast archon's location for other robots on the team to know
                if(main_archon) {
                    // System.out.println("Broadcasting location: " + archonLocation.x + " " + archonLocation.y);
                    rc.broadcast(0,Float.floatToRawIntBits(archonLocation.x));
                    rc.broadcast(1,Float.floatToRawIntBits(archonLocation.y));
                } else {
                    int main_archon_x = rc.readBroadcast(0);
                    if(main_archon_x != 0) {
                        // Group up Archons
                        int main_archon_y = rc.readBroadcast(1);
                        MapLocation mainArchonLocation = new MapLocation(Float.intBitsToFloat(main_archon_x), Float.intBitsToFloat(main_archon_y));
                        if(archonLocation.distanceTo(mainArchonLocation) > 9)
                            tryMove(archonLocation.directionTo(mainArchonLocation);
                    }
                }

                if (rc.getTeamBullets() >= 500.0 || rc.getRoundLimit() == rc.getRoundNum()) {
                    rc.donate((float) 100.0); // If over 10000 bullets, we win.
                }

                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
