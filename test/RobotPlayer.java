package test;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
        }
	}
    static MapLocation archonLoc;
    static int counter = 0;
    
    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        boolean hasHiredGardener = false;
        // The code you want your robot to perform every round should be in this loop
        while (true) {
 
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if(rc.hasRobotBuildRequirements(RobotType.GARDENER) && rc.canHireGardener(dir) && !hasHiredGardener) {
                	rc.hireGardener(dir);
                	hasHiredGardener = true;
                } else if (rc.hasRobotBuildRequirements(RobotType.GARDENER) && rc.canHireGardener(dir) && Math.random() < .5) {
                    rc.hireGardener(dir);
                }

                // Move randomly
                tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);
                if(rc.getTeamBullets() > 200.0) {
                	if(counter == 10) {
                		rc.donate((float) 10.0);
                		counter = 0;
                	} else {
                		counter++;
                	}
        		}
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                archonLoc = new MapLocation(xPos,yPos);

                rc.broadcast(((int)Math.random() * 1000), 10);
                // Generate a random direction
                Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.hasRobotBuildRequirements(RobotType.SOLDIER) && rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite()) && Math.random() < .8) {
                    rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }
                if (rc.hasRobotBuildRequirements(RobotType.LUMBERJACK) && rc.canBuildRobot(RobotType.LUMBERJACK, towardsArchon.opposite()) && Math.random() < .1 && rc.isBuildReady()) {
                   rc.buildRobot(RobotType.LUMBERJACK, towardsArchon.opposite());
                }
                
                // Move randomly
                if(!tryMove(towardsArchon)) {
                	tryMove(randomDirection());
                }
                /*if(rc.canBuildRobot(RobotType.SOLDIER, towardsArchon.opposite())) {
                	rc.buildRobot(RobotType.SOLDIER, towardsArchon.opposite());
                }*/
                Direction dir = randomDirection();
                if(rc.canPlantTree(dir) && Math.random() < 0.2) {
                    rc.plantTree(dir);
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                if(trees.length > 0 && rc.canWater(trees[0].location) && Math.random() < 0.3) {
                    rc.water(trees[0].location);
                    //rc.shake(trees[0].location);
                    /*if(rc.getTeamBullets() > 100.0) {
                        rc.donate((float) 10.0);
                    }*/
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireTriadShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
                        tryMove(rc.getLocation().directionTo(robots[0].location).opposite());
                    }
                }
                //Direction towardsArchon = new Direction((float)Math.atan((archonLoc.x-rc.getLocation().x)/(archonLoc.y-rc.getLocation().y)));
                // Move randomly
                //tryMove(towardsArchon.opposite());
                tryMove(randomDirection());
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            	/*
            	//MapLocation myLocation = rc.getLocation();
                //Direction towardsArchon = new Direction((float) Math.atan((archonLoc.x-myLocation.x)/(archonLoc.y-myLocation.y)));
                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireTriadShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
                        tryMove(rc.getLocation().directionTo(robots[0].location).opposite());
                    } else {
                    	tryMove(rc.getLocation().directionTo(robots[0].location));
                    }
                } else {
                	tryMove(randomDirection());
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();*/

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                TreeInfo[] trees = rc.senseNearbyTrees();
                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        tryMove(toEnemy);
                    }

                }
                if (trees.length > 0 && !rc.hasAttacked()) {
                	for(int i = 0; i < trees.length; i++) {
                		Direction towardsTree = new Direction((float) Math.atan((trees[i].location.x-rc.getLocation().x)/(trees[i].location.y-rc.getLocation().y)));
                		tryMove(towardsTree);
                		if (rc.canChop(trees[i].location))
                			rc.chop(trees[i].location);
                	}
                } else {
                	tryMove(randomDirection());
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

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

        while(currentCheck<=checksPerSide) {
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

        // A move never happened, so return false.
        return false;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
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
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
}
