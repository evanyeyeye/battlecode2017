package tourney;
import battlecode.common.*;

import java.util.HashSet;
import java.util.Iterator;

public class Archon extends RobotPlayer {

    static final int MAX_HP = 400;

    static MapLocation[] corners = new MapLocation[4];

    static HashSet<Integer> unusedIDs; // = new HashSet<Integer>(1);
    static HashSet<Integer>   usedIDs; // = new HashSet<Integer>(1);
    static void fulfillIDRequests() throws GameActionException {

        if(cycle_num < 8) return;
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

    static int hasSentSoldiers = 0;

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
                rc.broadcast(Broadcast.SOLDIER_COUNT_INDEX, rc.readBroadcast(Broadcast.SOLDIER_COUNT_INDEX) - 1);
            } else {
                if(current_code != -1 * reinforcements_slots[sl]) {
                    rc.broadcast(robot, reinforcements_slots[sl]);
                }
            }
        }

    }

    static int cycle_num = 0;

    static boolean archon_might_be_stuck = false;
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

                main_archon = main_archon || (!archon_might_be_stuck && Broadcast.checkMainArchon());
                if (main_archon) {
                    fulfillIDRequests();
                    fulfillReinforcementsRequests();
                }

                System.out.println("cycle: " + cycle_num);

                // Build gardeners
                Direction dir = randomDirection();

                if (rc.canHireGardener(dir) 
                		&& (Broadcast.getRobotCount(RobotType.GARDENER) < 3 
                				|| Broadcast.getRobotCount(RobotType.GARDENER) < Broadcast.getRobotCount(RobotType.SOLDIER) / 2)
                        && (rc.onTheMap(rc.getLocation().add(dir, rc.getType().bodyRadius + rc.getType().strideRadius + (float)2.0), rc.getType().bodyRadius)
                        && (Broadcast.getRobotCount(RobotType.GARDENER) == 0 
                        	|| !rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(dir, rc.getType().bodyRadius + rc.getType().strideRadius + (float)2.0), rc.getType().bodyRadius)))) {

                    rc.hireGardener(dir); // temporary check (TODO: DOESNT WORK ON TIGHT MAPS) until gardeners become legit
                    Broadcast.incrementRobotCount(RobotType.GARDENER);

                }

                if(cycle_num < 8) {
                    switch(cycle_num) {
                        case 3:
                            dynamicIDs_unallocated = new HashSet<Integer>();
                            break;
                        case 4:
                            dynamicIDs_allocated = new HashSet<Integer>();
                            break;
                        case 5:
                            usedIDs = new HashSet<Integer>();
                            break;
                        case 6:
                            unusedIDs = new HashSet<Integer>();
                            break;
                        case 7:
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
                if(cycle_num > 10 && rc.readBroadcast(Broadcast.GARDENER_COUNT_INDEX) == 0) {
                    main_archon = false;
                    archon_might_be_stuck = true;
                }

                TreeInfo[] neutralTrees = rc.senseNearbyTrees(INTERACT_RADIUS, Team.NEUTRAL);
                for (int i=0; i<neutralTrees.length; i++) {
                	Broadcast.requestLumberjack(neutralTrees[i]);
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees
                }
                
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
                
                RobotInfo[] allyRobots = rc.senseNearbyRobots(INTERACT_RADIUS + CALC_OFFSET, rc.getTeam()); // Gardener interference
                for (int i=0; i<allyRobots.length; i++)
                	if (allyRobots[i].getType() == RobotType.GARDENER)
                		if (tryMove(rc.getLocation().directionTo(allyRobots[i].getLocation()).opposite(), 2, 45))
                			break;
                

                // Low priority tasks
                //
                // Broadcast archon's location for other robots on the team to know
                if(main_archon) {
                    // System.out.println("Broadcasting location: " + archonLocation.x + " " + archonLocation.y);
                    //
                    if(hasSentSoldiers > 0)
                        hasSentSoldiers++;
                    if(hasSentSoldiers > 10) {
                        hasSentSoldiers = 0;
                    }
                    if(hasSentSoldiers == 0) {
                        if(rc.readBroadcast(Broadcast.SOLDIER_COUNT_INDEX) > 8) {
                            float x_s = 0.0f;
                            float y_s = 0.0f;
                            int num = 0;
                            for(MapLocation ml : rc.senseBroadcastingRobotLocations()) {
                                if(ml.distanceTo(archonLocation) > 40) {
                                    x_s += ml.x;
                                    y_s += ml.y;
                                    num++;
                                }
                            }
                            if(num != 0) {
                                Broadcast.requestReinforcements(new MapLocation(x_s/num, y_s/num));
                                hasSentSoldiers = 1;
                            }
                        }
                    }
                    rc.broadcast(0,Float.floatToRawIntBits(archonLocation.x));
                    rc.broadcast(1,Float.floatToRawIntBits(archonLocation.y));
                } else {
                    int main_archon_x = rc.readBroadcast(0);
                    if (main_archon_x != 0) {
                        // Group up Archons
                        int main_archon_y = rc.readBroadcast(1);
                        MapLocation mainArchonLocation = new MapLocation(Float.intBitsToFloat(main_archon_x), Float.intBitsToFloat(main_archon_y));
                        if(archonLocation.distanceTo(mainArchonLocation) > 20f)
                            tryMove(archonLocation.directionTo(mainArchonLocation));
                    }
                }

                System.out.println(Clock.getBytecodeNum());
                Clock.yield();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
