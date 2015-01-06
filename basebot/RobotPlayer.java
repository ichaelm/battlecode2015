package basebot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {

	// AI parameters
	private static final int ARRAY_SIZE = 1000;
	private static final int RUSH_TURN = 1500;

	// Cached game information
	private static RobotController rc;
	private static Team myTeam;
	private static Team enemyTeam;
	private static RobotType myType;
	private static int myRange;
	private static Random rand;
	private static MapLocation HQLoc;
	private static MapLocation enemyHQLoc;
	private static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private static RobotType[] robotTypes = {
		RobotType.HQ,
		RobotType.TOWER,
		RobotType.SUPPLYDEPOT,
		RobotType.TECHNOLOGYINSTITUTE,
		RobotType.BARRACKS,
		RobotType.HELIPAD,
		RobotType.TRAININGFIELD,
		RobotType.TANKFACTORY,
		RobotType.MINERFACTORY,
		RobotType.HANDWASHSTATION,
		RobotType.AEROSPACELAB,
		RobotType.BEAVER,
		RobotType.COMPUTER,
		RobotType.SOLDIER,
		RobotType.BASHER,
		RobotType.MINER,
		RobotType.DRONE,
		RobotType.TANK,
		RobotType.COMMANDER,
		RobotType.LAUNCHER,
		RobotType.MISSILE,
	};
	
	public static void run(RobotController myrc) {
		// Initialize cached game information
		rc = myrc;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		myType = rc.getType();
		myRange = myType.attackRadiusSquared;
		rand = new Random(rc.getID());
		HQLoc = rc.senseHQLocation();
		enemyHQLoc = rc.senseEnemyHQLocation();

		switch (myType) {
		case HQ: runHQ(); break;
		case TOWER: runTower(); break;
		case AEROSPACELAB: runAerospaceLab(); break;
		case BARRACKS: runBarracks(); break;
		case BASHER: runBasher(); break;
		case BEAVER: runBeaver(); break;
		case COMMANDER: runCommander(); break;
		case COMPUTER: runComputer(); break;
		case DRONE: runDrone(); break;
		case HANDWASHSTATION: runHandwashStation(); break;
		case HELIPAD: runHelipad(); break;
		case LAUNCHER: runLauncher(); break;
		case MINER: runMiner(); break;
		case MINERFACTORY: runMinerFactory(); break;
		case MISSILE: runMissile(); break;
		case SOLDIER: runSoldier(); break;
		case SUPPLYDEPOT: runSupplyDepot(); break;
		case TANK: runTank(); break;
		case TANKFACTORY: runTankFactory(); break;
		case TECHNOLOGYINSTITUTE: runTechnologyInstitute(); break;
		case TRAININGFIELD: runTrainingField(); break;
		default: break;
		}
	}

	private static void runHQ() {
		// Information stored across rounds
		RobotInfo[] myRobots = null;
		RobotInfo[] mySupplyDepots = null;
		RobotInfo[] myMinerFactories = null;
		RobotInfo[] myTechnologyInstitutes = null;
		RobotInfo[] myBarracks = null;
		RobotInfo[] myHelipads = null;
		RobotInfo[] myTrainingFields = null;
		RobotInfo[] myTankFactories = null;
		RobotInfo[] myAerospaceLabs = null;
		RobotInfo[] myHandwashStations = null;
		RobotInfo[] myBeavers = null;
		RobotInfo[] myMiners = null;
		RobotInfo[] myComputers = null;
		RobotInfo[] mySoldiers = null;
		RobotInfo[] myBashers = null;
		RobotInfo[] myDrones = null;
		RobotInfo[] myTanks = null;
		RobotInfo[] myCommanders = null;
		RobotInfo[] myLaunchers = null;
		RobotInfo[] myMissiles = null;
		RobotInfo[] myTowers = null;

		while (true) {
			try {
				myRobots = rc.senseNearbyRobots(999999, myTeam);
				mySupplyDepots = new RobotInfo[ARRAY_SIZE];
				myMinerFactories = new RobotInfo[ARRAY_SIZE];
				myTechnologyInstitutes = new RobotInfo[ARRAY_SIZE];
				myBarracks = new RobotInfo[ARRAY_SIZE];
				myHelipads = new RobotInfo[ARRAY_SIZE];
				myTrainingFields = new RobotInfo[ARRAY_SIZE];
				myTankFactories = new RobotInfo[ARRAY_SIZE];
				myAerospaceLabs = new RobotInfo[ARRAY_SIZE];
				myHandwashStations = new RobotInfo[ARRAY_SIZE];
				myBeavers = new RobotInfo[ARRAY_SIZE];
				myMiners = new RobotInfo[ARRAY_SIZE];
				myComputers = new RobotInfo[ARRAY_SIZE];
				mySoldiers = new RobotInfo[ARRAY_SIZE];
				myBashers = new RobotInfo[ARRAY_SIZE];
				myDrones = new RobotInfo[ARRAY_SIZE];
				myTanks = new RobotInfo[ARRAY_SIZE];
				myCommanders = new RobotInfo[ARRAY_SIZE];
				myLaunchers = new RobotInfo[ARRAY_SIZE];
				myMissiles = new RobotInfo[ARRAY_SIZE];
				myTowers = new RobotInfo[ARRAY_SIZE];
				int numSupplyDepots = 0;
				int numMinerFactories = 0;
				int numTechnologyInstitutes = 0;
				int numBarracks = 0;
				int numHelipads = 0;
				int numTrainingFields = 0;
				int numTankFactories = 0;
				int numAerospaceLabs = 0;
				int numHandwashStations = 0;
				int numBeavers = 0;
				int numMiners = 0;
				int numComputers = 0;
				int numSoldiers = 0;
				int numBashers = 0;
				int numDrones = 0;
				int numTanks = 0;
				int numCommanders = 0;
				int numLaunchers = 0;
				int numMissiles = 0;
				int numTowers = 0;

				for (RobotInfo r : myRobots) {
					RobotType type = r.type;
					switch (type) {
					case SUPPLYDEPOT:
						mySupplyDepots[numSupplyDepots] = r;
						numSupplyDepots++;
						break;
					case MINERFACTORY:
						myMinerFactories[numMinerFactories] = r;
						numMinerFactories++;
						break;
					case TECHNOLOGYINSTITUTE:
						myTechnologyInstitutes[numTechnologyInstitutes] = r;
						numTechnologyInstitutes++;
						break;
					case BARRACKS:
						myBarracks[numBarracks] = r;
						numBarracks++;
						break;
					case HELIPAD:
						myHelipads[numHelipads] = r;
						numHelipads++;
						break;
					case TRAININGFIELD:
						myTrainingFields[numTrainingFields] = r;
						numTrainingFields++;
						break;
					case TANKFACTORY:
						myTankFactories[numTankFactories] = r;
						numTankFactories++;
						break;
					case AEROSPACELAB:
						myAerospaceLabs[numAerospaceLabs] = r;
						numAerospaceLabs++;
						break;
					case HANDWASHSTATION:
						myHandwashStations[numHandwashStations] = r;
						numHandwashStations++;
						break;
					case BEAVER:
						myBeavers[numBeavers] = r;
						numBeavers++;
						break;
					case MINER:
						myMiners[numMiners] = r;
						numMiners++;
						break;
					case COMPUTER:
						myComputers[numComputers] = r;
						numComputers++;
						break;
					case SOLDIER:
						mySoldiers[numSoldiers] = r;
						numSoldiers++;
						break;
					case BASHER:
						myBashers[numBashers] = r;
						numBashers++;
						break;
					case DRONE:
						myDrones[numDrones] = r;
						numDrones++;
						break;
					case TANK:
						myTanks[numTanks] = r;
						numTanks++;
						break;
					case COMMANDER:
						myCommanders[numCommanders] = r;
						numCommanders++;
						break;
					case LAUNCHER:
						myLaunchers[numLaunchers] = r;
						numLaunchers++;
						break;
					case MISSILE:
						myMissiles[numMissiles] = r;
						numMissiles++;
						break;
					case TOWER:
						myTowers[numTowers] = r;
						numTowers++;
						break;
					case HQ:
						break;
					}
				}
				int targetBarracks = 5;
				int targetTankFactories = 0;
				int targetHelipads = 0;
				int targetSupplyDepots = 15;
				int numBuildingBarracks = 0;
				int numBuildingTankFactories = 0;
				int numBuildingHelipads = 0;
				int numBuildingSupplyDepots = 0;

				// beaver loop, check orders
				for (int i = 0; i < ARRAY_SIZE; i++) {
					RobotInfo r = myBeavers[i];
					if (r == null) {
						break;
					}
					RobotType buildOrder = recieveBuildOrders(r.ID);
					if (buildOrder != null) {
						switch (buildOrder) {
						case BARRACKS:
							numBuildingBarracks++;
						case TANKFACTORY:
							numBuildingTankFactories++;
						case HELIPAD:
							numBuildingHelipads++;
						case SUPPLYDEPOT:
							numBuildingSupplyDepots++;
						}
					}
				}

				rc.setIndicatorString(0, numBarracks + " barracks");
				rc.setIndicatorString(1, numBuildingBarracks + " building barracks");

				// beaver loop, send orders
				for (int i = numBeavers; --i >= 0;) {
					RobotInfo r = myBeavers[i];
					if (r == null) {
						break;
					}
					RobotType buildOrder = recieveBuildOrders(r.ID);
					if (buildOrder == null) {
						if (targetBarracks > numBuildingBarracks + numBarracks) {
							sendOrders(r.ID, robotTypeToNum(RobotType.BARRACKS),0,0);
							numBuildingBarracks++;
						} else if (targetHelipads > numBuildingHelipads + numHelipads) {
							sendOrders(r.ID, robotTypeToNum(RobotType.HELIPAD),0,0);
							numBuildingHelipads++;
						} else if (targetTankFactories > numBuildingTankFactories + numTankFactories) {
							sendOrders(r.ID, robotTypeToNum(RobotType.TANKFACTORY),0,0);
							numBuildingTankFactories++;
						} else if (targetSupplyDepots > numBuildingSupplyDepots + numSupplyDepots) {
							sendOrders(r.ID, robotTypeToNum(RobotType.SUPPLYDEPOT),0,0);
							numBuildingSupplyDepots++;
						}
					}
				}
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady() && rc.getTeamOre() >= 350 && numBeavers < 30) {
					trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTower() {
		while (true) {
			try {					
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Tower Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runAerospaceLab() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Aerospace Lab Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runBarracks() {
		while (true) {
			try {
				if (rc.isCoreReady() && rc.getTeamOre() >= 600) {
					boolean fate = rand.nextBoolean();
					if (fate) {
						trySpawn(directions[rand.nextInt(8)],RobotType.SOLDIER);
					} else {
						trySpawn(directions[rand.nextInt(8)],RobotType.BASHER);
					}

				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Barracks Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runBasher() {
		while (true) {
			try {
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						rally();
					} else {
						MapLocation enemyLoc = nearestEnemy();
						if (enemyLoc == null) {
							tryMove(rc.getLocation().directionTo(enemyHQLoc));
						} else {
							tryMove(rc.getLocation().directionTo(enemyLoc));
						}
					}
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Basher Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runBeaver() {
		while (true) {
			try {
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady()) {
					if (rc.getLocation().distanceSquaredTo(HQLoc) <= 2) {
						tryMove(rc.getLocation().directionTo(HQLoc).opposite());
					} else {
						Direction escapeDir = escapeCrowding();
						if (escapeDir != null) {
							tryMove(escapeDir);
						} else {
							RobotType buildOrder = recieveBuildOrders(rc.getID());
							if (buildOrder == null) {
								mine();
							} else {
								if (ordersMarked(rc.getID())) {
									sendOrders(rc.getID(), -1, 0, 0);
									mine();
								} else {
									if (rc.getTeamOre() < buildOrder.oreCost) {
										mine();
									} else {
										boolean success = tryBuild(directions[rand.nextInt(8)],buildOrder);
										if (success) {
											markOrders(rc.getID());
										}
									}
								}
							}
						}
					}
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Beaver Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runCommander() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Commander Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runComputer() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Computer Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runDrone() {
		while (true) {
			try {
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						rally();
					} else {
						tryMove(rc.getLocation().directionTo(enemyHQLoc));
					}
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Drone Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runHandwashStation() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Handwash Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runHelipad() {
		while (true) {
			try {
				if (rc.isCoreReady() && rc.getTeamOre() >= 600) {
					trySpawn(directions[rand.nextInt(8)],RobotType.DRONE);
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Helipad Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runLauncher() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Launcher Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runMiner() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Miner Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runMinerFactory() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Miner Factory Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runMissile() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Missile Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runSoldier() {
		while (true) {
			try {
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						rally();
					} else {
						tryMove(rc.getLocation().directionTo(enemyHQLoc));
					}
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runSupplyDepot() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Supply Depot Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTank() {
		while (true) {
			try {
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						rally();
					} else {
						tryMove(rc.getLocation().directionTo(enemyHQLoc));
					}
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Tank Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTankFactory() {
		while (true) {
			try {
				if (rc.isCoreReady() && rc.getTeamOre() >= 600) {
					trySpawn(directions[rand.nextInt(8)],RobotType.TANK);
				}
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Tank Factory Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTechnologyInstitute() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Technology Institute Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTrainingField() {
		while (true) {
			try {
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("Training Field Exception");
				e.printStackTrace();
			}
		}
	}

	private static void transferSupply() throws GameActionException {
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
		double mySupply = rc.getSupplyLevel();
		double lowestSupply = mySupply;
		RobotInfo lowestRobot = null;
		for (RobotInfo r : nearbyAllies) {
			if (r.supplyLevel < lowestSupply) {
				lowestSupply = r.supplyLevel;
				lowestRobot = r;
			}
		}
		if (lowestRobot != null) {
			rc.transferSupplies((int)((mySupply-lowestSupply)/2), lowestRobot.location);
		}
	}
	
	private static Direction escapeCrowding() {
		RobotInfo[] myRobots = rc.senseNearbyRobots(2);
		MapLocation myLoc = rc.getLocation();
		if (myRobots.length >= 6) {
			boolean[] blockedDirs = {false, false, false, false, false, false, false, false};
			for (int i = 0; i < myRobots.length; i++) {
				MapLocation move = myRobots[i].location;
				Direction d = myLoc.directionTo(move);
				blockedDirs[directionToInt(d)] = true;
			}
			Direction[] validMoves = {null, null};
			int numValidMoves = 0;
			for (int i = 0; i < 8; i++) {
				if (blockedDirs[i] == false) {
					validMoves[numValidMoves] = intToDirection(i);
					numValidMoves++;
				}
			}
			int choice = rand.nextInt(numValidMoves);
			return validMoves[choice];
		} else {
			return null;
		}
	}

	private static void mine() throws GameActionException {
		MapLocation loc = rc.getLocation();
		double ore = rc.senseOre(loc);
		if (ore > 40) {
			rc.mine();
		} else {
			for (int i = 0; i < 8; i++) {
				Direction d = intToDirection(i);
				if (rc.senseOre(loc.add(d)) > 40 && rc.canMove(d)) {
					rc.move(d);
					break;
				}
			}
		}
	}
	
	private static void rally() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		if (myLoc.distanceSquaredTo(HQLoc) > 300) {
			tryMove(myLoc.directionTo(HQLoc));
		} else {
			tryMove(directions[rand.nextInt(8)]);
		}
	}
	
	private static MapLocation nearestEnemy() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(24, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			MapLocation enemyLoc = r.location;
			int dist = myLoc.distanceSquaredTo(enemyLoc);
			if (dist < closestDist) {
				closestDist = dist;
				closestRobot = r;
			}
		}
		if (closestRobot != null) {
			return closestRobot.location;
		}
		return null;
	}

	private static int robotTypeToNum(RobotType type) {
		int num;
		switch (type) {
		case HQ: num = 0; break;
		case TOWER: num = 1; break;
		case SUPPLYDEPOT: num = 2; break;
		case TECHNOLOGYINSTITUTE: num = 3; break;
		case BARRACKS: num = 4; break;
		case HELIPAD: num = 5; break;
		case TRAININGFIELD: num = 6; break;
		case TANKFACTORY: num = 7; break;
		case MINERFACTORY: num = 8; break;
		case HANDWASHSTATION: num = 9; break;
		case AEROSPACELAB: num = 10; break;
		case BEAVER: num = 11; break;
		case COMPUTER: num = 12; break;
		case SOLDIER: num = 13; break;
		case BASHER: num = 14; break;
		case MINER: num = 15; break;
		case DRONE: num = 16; break;
		case TANK: num = 17; break;
		case COMMANDER: num = 18; break;
		case LAUNCHER: num = 19; break;
		case MISSILE: num = 20; break;
		default: num = -1; break;
		}
		return num;
	}

	private static RobotType numToRobotType(int num) {
		RobotType type;
		if (num >= 0 && num < 21) {
			type = robotTypes[num];
		} else {
			type = null;
		}
		return type;
	}

	private static final int MSG_LEN = 5;
	private static final int NUM_MSG = 65536 / MSG_LEN;

	private static void sendOrders(int ID, int order, int x, int y) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		rc.broadcast(hash * MSG_LEN, ID);
		rc.broadcast(hash * MSG_LEN + 1, order);
		rc.broadcast(hash * MSG_LEN + 2, x);
		rc.broadcast(hash * MSG_LEN + 3, y);
		rc.broadcast(hash * MSG_LEN + 4, 0);
	}

	private static int[] recieveOrders(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == 0) {
			return null;
		}
		int[] result = new int[3];
		result[0] = rc.readBroadcast(hash * MSG_LEN + 1);
		result[1] = rc.readBroadcast(hash * MSG_LEN + 2);
		result[2] = rc.readBroadcast(hash * MSG_LEN + 3);
		return result;
	}

	private static RobotType recieveBuildOrders(int ID) throws GameActionException {
		int[] orders = recieveOrders(ID);
		if (orders != null) {
			RobotType buildOrders = numToRobotType(orders[0]);
			if (buildOrders != null) {
				return buildOrders;
			}
		}
		return null;
	}

	private static void markOrders(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == ID) {
			rc.broadcast(hash * MSG_LEN + 4, 1);
		}
	}

	private static boolean ordersMarked(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == ID) {
			return (1 == rc.readBroadcast(hash * MSG_LEN + 4));
		}
		return false;
	}

	private static int hashID(int ID) {
		return ID % NUM_MSG;
	}

	// This method will attack an enemy in sight, if there is one
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

	// This method will attempt to move in Direction d (or as close to it as possible)
	static void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}

	// This method will attempt to spawn in the given direction (or as close to it as possible)
	static void trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}

	// This method will attempt to build in the given direction (or as close to it as possible)
	static boolean tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
			return true;
		}
		return false;
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

	static Direction intToDirection(int num) {
		switch(num) {
		case 0:
			return Direction.NORTH;
		case 1:
			return Direction.NORTH_EAST;
		case 2:
			return Direction.EAST;
		case 3:
			return Direction.SOUTH_EAST;
		case 4:
			return Direction.SOUTH;
		case 5:
			return Direction.SOUTH_WEST;
		case 6:
			return Direction.WEST;
		case 7:
			return Direction.NORTH_WEST;
		default:
			return Direction.NONE;
		}
	}
}
