package draft_1;
import battlecode.common.*;

public class Scout extends RobotPlayer {
	public static double[] goToEdge() {
		Direction way = randomDirection();
		double[] count = new double[2];
		count[0] = 0.0;
		count[1] = 0.0;

		try {

			while(rc.onTheMap(rc.getLocation(), RobotType.SCOUT.sensorRadius)) {
				if(tryMove(way)) {
					count[0] += (Math.cos(way.radians) * RobotType.SCOUT.strideRadius);
					count[1] += (Math.sin(way.radians) * RobotType.SCOUT.strideRadius);
				}
			}
			System.out.println("I am at the edge");

		} catch (Exception e) {
			System.out.println("This will never happen");
		}
		return count;
	}
	
	/*public static double[] skirtEdge() {
		Direction way = randomDirection();
		double[] count = new double[2];
		count[0] = 0.0;
		count[1] = 0.0;

		try {
			while(rc.onTheMap(rc.getLocation())) {
				
				
				tryMove(Direction.NORTH);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			while(rc.onTheMap(rc.getLocation())) {
				tryMove(Direction.EAST);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			while(rc.onTheMap(rc.getLocation())) {
				tryMove(Direction.WEST);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			while(rc.onTheMap(rc.getLocation())) {
				tryMove(Direction.SOUTH);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			/*while(rc.onTheMap(rc.getLocation(), RobotType.SCOUT.sensorRadius)) {
				if(tryMove(way)) {
					count[0] += (Math.cos(way.radians) * RobotType.SCOUT.strideRadius);
					count[1] += (Math.sin(way.radians) * RobotType.SCOUT.strideRadius);
				}
			}
			System.out.println("I am at the edge");

		} catch (Exception e) {
			System.out.println("This will never happen");
		}
		return count;
	}*/
	
	public static MapLocation findEnemy() {
		RobotInfo[] robots = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadius, rc.getTeam().opponent());
        //TreeInfo[] trees = rc.senseNearbyTrees();
        if(robots.length > 0) { // && !rc.hasAttacked()) {
        	//MapLocation[] locations = new MapLocation[robots.length];
        	/*for(int i = 0; i < robots.length; i++) {
        		locations[i] = robots[i].getLocation();
        	}*/
        	//return locations;
        	return robots[0].getLocation();
        }
        return null;

	}
	
	public static MapLocation findTree() {
		TreeInfo[] trees = rc.senseNearbyTrees(RobotType.SCOUT.sensorRadius);
        if(trees.length > 0) {
        	for(int i = 0; i < trees.length; i++) {
        		if(trees[i].getTeam() != rc.getTeam().opponent() && trees[i].getTeam() != rc.getTeam() && rc.canShake(trees[i].getID()) && trees[i].getContainedBullets() > 5) {
        			return trees[i].getLocation();
        		}
        	}
        }
        return null;
	}
	
	public static boolean init = true;
	//public static double range = 50.0;
	public static Direction dir = Direction.EAST;
	
    public static void run(RobotController rc) {
    	
        RobotPlayer.rc = rc;

        System.out.println("Scout: Spawn");
        Team enemy = rc.getTeam().opponent();
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	/*double minMapSize[] = new double[2];
            	if(init) {
            		minMapSize = skirtEdge();
            		init = false;
            	}
            	*/
            	
            	//should detect bullets and find where to move to instead; implement in dodge()
            	//double temp = range * 2;
            	dir = randomDirection();
            	//while(range > 0.0) {
        		MapLocation attackable = findEnemy();
        		//MapLocation shakeable = findTree();
        		//if(shakeable != null) {
        			//tryMove(rc.getLocation().directionTo(shakeable));
       			 	//rc.shake(shakeable);
        		TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL);
                for (int i=0; i < neutralTrees.length; i++) {
                    MapLocation loc = neutralTrees[i].getLocation();
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation())) {
                        rc.shake(loc); // Collect free bullets from neutral trees
                    }
                    Broadcast.requestLumberjack(loc);
                }
        		//}
        		if(attackable != null) {
        			if(rc.getHealth() < 5) {
        				tryMove(rc.getLocation().directionTo(attackable).opposite());
        			} else {
        				tryMove(rc.getLocation().directionTo(attackable));
        				rc.fireSingleShot(rc.getLocation().directionTo(attackable));
        			}
        		}
        		if(!rc.onTheMap(rc.getLocation())) {
        			tryMove(dir.rotateLeftDegrees((float) 90.0));
        		} else {
        			tryMove(dir);
        			//range -= RobotType.SCOUT.strideRadius;
        		}
        		Clock.yield();
            	//}
            	//range = temp;
                /*else {
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
                /*if (trees.length > 0) {
                    for(int i = 0; i < 1; i++) {
                        Direction towardsTree = new Direction((float) Math.atan((trees[i].location.x-rc.getLocation().x)/(trees[i].location.y-rc.getLocation().y)));
                        tryMove(towardsTree);
                    }
                } else {*/
                   
               // }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                
            } catch (Exception e) {
                System.out.println("ScoutLsssssss: Exception");
                e.printStackTrace();
            }
        }
    }
}
