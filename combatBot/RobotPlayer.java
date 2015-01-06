package combatBot;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer{
	
	static RobotController rc;
	static Direction[] directions = Direction.values();
	static Random rand;
	static Direction facing;
	
	public static void run(RobotController myrc){
		rc = myrc;
		while(true){
			try{
				
				if(rc.getType() == RobotType.DRONE){
					combatUnitAI();
				}
				rc.yield();
				
			} catch(GameActionException e){
				e.printStackTrace();
			}
		}
	}
	
	private static void combatUnitAI() throws GameActionException{
		attackNearestEnemy();
		if(Clock.getRoundNum() < 1000){
			moveRandomly();
		} else {
			stutterStep();
		}
	}

	private static void attackNearestEnemy() throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getLocation(), rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		if(nearbyEnemies.length > 0){
			int closest = rc.getType().attackRadiusSquared;
			RobotInfo closestEnemy = null;
			for(RobotInfo enemy: nearbyEnemies){
				if(rc.getLocation().distanceSquaredTo(enemy.location) < closest){
					closest = rc.getLocation().distanceSquaredTo(enemy.location);
					closestEnemy = enemy;
				}	
			}
			if(rc.isWeaponReady() && rc.canAttackLocation(closestEnemy.location))
				rc.attackLocation(closestEnemy.location);
		}
	}
	
	private static void moveRandomly() throws GameActionException {
		rand = new Random(rc.getID());
		facing = directions[(int)(rand.nextDouble() * 8)];
		if(rc.isCoreReady())
			tryMove(facing);
	}
	
	static void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0, 1, -1, 2, -2};
		int dirint = directionToInt(d);
		while(offsetIndex < 5 && !canMoveSafely(directions[(dirint + offsets[offsetIndex] + 8) % 8])){
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint + offsets[offsetIndex] + 8) % 8]);
		}
	}
	
	private static boolean canMoveSafely(Direction direction) {
		boolean isSafe = false;
		if(rc.canMove(direction)){
			MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			isSafe = true;
			if(enemyHQ.distanceSquaredTo(rc.getLocation().add(direction)) <= RobotType.HQ.attackRadiusSquared){
				isSafe = false;
			}
			for(MapLocation m: enemyTowers){
				if(m.distanceSquaredTo(rc.getLocation().add(direction)) <= RobotType.TOWER.attackRadiusSquared){
					isSafe = false;
					break;
				}
			}
		}
		return isSafe;
	}

	private static void stutterStep() {
		// TODO Auto-generated method stub
		
	}
	
	static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}
}