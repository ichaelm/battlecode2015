package minerbot_maxoremindist_picky_savedmarkedminer2_4ore;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {

	// AI parameters
	private static final int ARRAY_SIZE = 200;
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
	private static int rushDist;
	private static MapLocation minerTarget;
	private static boolean brandNew;
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
	/*
	hq	5
	miner factory	2.5
	tech institute	0.4
	barracks	4
	helipad	4.16666
	tank factory	5
	training field	1.25
	aerospace lab	4
	*/
	private static double[] oreConsumptionByType = {5, 0, 0, 0.4, 4, 25/6, 1.25, 5, 2.5, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
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
		rushDist = HQLoc.distanceSquaredTo(enemyHQLoc);
		brandNew = true;

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
	
	static RobotType[][] newBuildOrders;
	static int numNewBuildOrders;

	private static void runHQ() {
		// Information stored across rounds
		RobotInfo[] myRobots = null;
		RobotInfo[][] myRobotsByType;
		RobotInfo[][] myFreeRobotsByType;
		int[] numRobotsByType; // zeros
		int[] numFreeRobotsByType; // zeros
		int[] progressRobotsByType; //zeros

		while (true) {
			try {
				myRobots = rc.senseNearbyRobots(999999, myTeam);
				myRobotsByType = new RobotInfo[21][ARRAY_SIZE];
				myFreeRobotsByType = new RobotInfo[21][ARRAY_SIZE];
				numRobotsByType = new int[21]; // zeros
				numFreeRobotsByType = new int[21]; // zeros
				progressRobotsByType = new int[21]; //zeros
				
				int totalSupplyUpkeep = 0;
				double estimatedOreGeneration = 5;
				double estimatedOreConsumption = 5; //  because hq is not included in myrobots
				
				// loop to count units, and get orders, and calculate total supply upkeep and ore generation and consumption
				for (RobotInfo r : myRobots) {
					// count units
					RobotType type = r.type;
					int typeNum = robotTypeToNum(type);
					myRobotsByType[typeNum][numRobotsByType[typeNum]] = r;
					numRobotsByType[typeNum]++;
					// get orders
					RobotType buildOrder = recieveBuildOrders(r.ID);
					if (buildOrder == null) {
						myFreeRobotsByType[typeNum][numFreeRobotsByType[typeNum]] = r;
						numFreeRobotsByType[typeNum]++;
						if (type == RobotType.BEAVER) {
							estimatedOreGeneration += 2;
						}
					} else {
						int buildOrderTypeNum = robotTypeToNum(buildOrder);
						progressRobotsByType[buildOrderTypeNum]++;
					}
					totalSupplyUpkeep += type.supplyUpkeep;
					if (type == RobotType.MINER) {
						estimatedOreGeneration += 3;
					}
					estimatedOreConsumption += oreConsumptionByType[robotTypeToNum(type)];
				}
				// extra loop for hq
				{
					RobotInfo rHQ = rc.senseRobotAtLocation(HQLoc);
					int typeNum = robotTypeToNum(RobotType.HQ);
					myRobotsByType[typeNum][0] = rHQ;
					numRobotsByType[typeNum] = 1;
					RobotType buildOrder = recieveBuildOrders(rHQ.ID);
					if (buildOrder == null) {
						myFreeRobotsByType[typeNum][0] = rHQ;
						numFreeRobotsByType[typeNum] = 1;
					} else {
						int buildOrderTypeNum = robotTypeToNum(buildOrder);
						progressRobotsByType[buildOrderTypeNum]++;
					}
				}
				int totalSupplyGeneration = (int)(100*(2+Math.pow(numRobotsByType[robotTypeToNum(RobotType.SUPPLYDEPOT)],0.7)));
				
				// calculate macro build orders for all free units
				newBuildOrders = new RobotType[ARRAY_SIZE][2];
				numNewBuildOrders = 0;
				int teamOre = (int)rc.getTeamOre();
				int plannedTeamOre = teamOre;
				
				
				if (true) {
					// goal: build more miners
					if (numRobotsByType[robotTypeToNum(RobotType.MINERFACTORY)] + progressRobotsByType[robotTypeToNum(RobotType.MINERFACTORY)] < 1) {
						// goal: build a miner factory
						if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
							// goal: build a beaver
							order(RobotType.HQ, RobotType.BEAVER);
							plannedTeamOre -= RobotType.BEAVER.oreCost;
						} else {
							// goal: build a miner factory
							order(RobotType.BEAVER, RobotType.MINERFACTORY);
							plannedTeamOre -= RobotType.MINERFACTORY.oreCost;
						}
					} else {
						// goal: build a miner
						if (numFreeRobotsByType[robotTypeToNum(RobotType.MINERFACTORY)] < 1) {
							// wait
						} else {
							order(RobotType.MINERFACTORY, RobotType.MINER);
							plannedTeamOre -= RobotType.MINER.oreCost;
						}
					}
				}
				if (totalSupplyUpkeep * 1.1 >= totalSupplyGeneration) {
					// goal: build a supplier if not already being built
					if (progressRobotsByType[robotTypeToNum(RobotType.SUPPLYDEPOT)] < 1) { // if no supplier is being built
						// goal: build a supplier
						if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
							// goal: build a beaver
							order(RobotType.HQ, RobotType.BEAVER);
							plannedTeamOre -= RobotType.BEAVER.oreCost;
						} else {
							// goal: build a supplier
							order(RobotType.BEAVER, RobotType.SUPPLYDEPOT);
							plannedTeamOre -= RobotType.SUPPLYDEPOT.oreCost;
						}
					}
				}
				
				// send orders loop
				for (int i = 0; i < numNewBuildOrders; i++) {
					RobotType source = newBuildOrders[i][0];
					RobotType target = newBuildOrders[i][1];
					teamOre -= target.oreCost;
					if (teamOre >= 0) {
						for (int j = 0; j < numFreeRobotsByType[robotTypeToNum(source)]; j++) {
							int ID = myFreeRobotsByType[robotTypeToNum(source)][j].ID;
							if (recieveBuildOrders(ID) == null) {
								sendOrders(ID, robotTypeToNum(target), 0, 0);
								break;
							}
						}
					} else {
						break;
					}
				}
				
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady()) {
					RobotType buildOrder = recieveBuildOrders(rc.getID());
					if (buildOrder != null) {
						if (ordersMarked(rc.getID())) {
							sendOrders(rc.getID(), -1, 0, 0);
						} else {
							if (rc.getTeamOre() >= buildOrder.oreCost) {
								boolean success = trySpawn(directions[rand.nextInt(8)],buildOrder);
								if (success) {
									markOrders(rc.getID());
								}
							}
						}
					}
				}
				rc.yield();
				transferSupply();
				rc.yield();
			} catch (Exception e) {
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}
	}

	private static void order(RobotType source, RobotType target) {
		newBuildOrders[numNewBuildOrders][0] = source;
		newBuildOrders[numNewBuildOrders][1] = target;
		numNewBuildOrders++;
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
				if (rc.isCoreReady()) {
					RobotType buildOrder = recieveBuildOrders(rc.getID());
					if (buildOrder != null) {
						if (ordersMarked(rc.getID())) {
							sendOrders(rc.getID(), -1, 0, 0);
						} else {
							if (rc.getTeamOre() >= buildOrder.oreCost) {
								boolean success = trySpawn(directions[rand.nextInt(8)],buildOrder);
								if (success) {
									markOrders(rc.getID());
								}
							}
						}
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
				if (Clock.getBytecodesLeft() > 1000) {
					transferSupply();
				}
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
				if (rc.isCoreReady()) {
					RobotType buildOrder = recieveBuildOrders(rc.getID());
					if (buildOrder != null) {
						if (ordersMarked(rc.getID())) {
							sendOrders(rc.getID(), -1, 0, 0);
						} else {
							if (rc.getTeamOre() >= buildOrder.oreCost) {
								boolean success = trySpawn(directions[rand.nextInt(8)],buildOrder);
								if (success) {
									markOrders(rc.getID());
								}
							}
						}
					}
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
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				if (rc.isCoreReady()) {
					Direction escapeDir = escapeCrowding();
					if (escapeDir != null) {
						tryMove(escapeDir);
					} else {
						mine();
					}
				}
				if (Clock.getBytecodesLeft() > 1000) {
					transferSupply();
				}
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
				if (rc.isCoreReady()) {
					RobotType buildOrder = recieveBuildOrders(rc.getID());
					if (buildOrder != null) {
						if (ordersMarked(rc.getID())) {
							sendOrders(rc.getID(), -1, 0, 0);
						} else {
							if (rc.getTeamOre() >= buildOrder.oreCost) {
								boolean success = trySpawn(directions[rand.nextInt(8)],buildOrder);
								if (success) {
									markOrders(rc.getID());
								}
							}
						}
					}
				}
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
				System.out.println("Tank Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTankFactory() {
		while (true) {
			try {
				if (rc.isCoreReady()) {
					RobotType buildOrder = recieveBuildOrders(rc.getID());
					if (buildOrder != null) {
						if (ordersMarked(rc.getID())) {
							sendOrders(rc.getID(), -1, 0, 0);
						} else {
							if (rc.getTeamOre() >= buildOrder.oreCost) {
								boolean success = trySpawn(directions[rand.nextInt(8)],buildOrder);
								if (success) {
									markOrders(rc.getID());
								}
							}
						}
					}
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
	
	private static RobotType builtBy(RobotType target) {
		switch (target) {
		case HQ: return null;
		case TOWER: return null;
		case AEROSPACELAB: return RobotType.BEAVER;
		case BARRACKS: return RobotType.BEAVER;
		case BASHER: return RobotType.BARRACKS;
		case BEAVER: return RobotType.HQ;
		case COMMANDER: return RobotType.TRAININGFIELD;
		case COMPUTER: return RobotType.TECHNOLOGYINSTITUTE;
		case DRONE: return RobotType.HELIPAD;
		case HANDWASHSTATION: return RobotType.BEAVER;
		case HELIPAD: return RobotType.BEAVER;
		case LAUNCHER: return RobotType.AEROSPACELAB;
		case MINER: return RobotType.MINERFACTORY;
		case MINERFACTORY: return RobotType.BEAVER;
		case MISSILE: return RobotType.LAUNCHER;
		case SOLDIER: return RobotType.BARRACKS;
		case SUPPLYDEPOT: return RobotType.BEAVER;
		case TANK: return RobotType.TANKFACTORY;
		case TANKFACTORY: return RobotType.BEAVER;
		case TECHNOLOGYINSTITUTE: return RobotType.BEAVER;
		case TRAININGFIELD: return RobotType.BEAVER;
		default: return null;
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
			if (numValidMoves > 0) {
				int choice = rand.nextInt(numValidMoves);
				return validMoves[choice];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private static void mine() throws GameActionException {
		int minOre = 4;
		MapLocation myLoc = rc.getLocation();
		double myOre = rc.senseOre(myLoc);
		if (myOre > minOre) {
			rc.mine();
			markMining(rc.getID());
		} else {
			Direction[] bestDirs = new Direction[8];
			int numBestDirs = 0;
			double bestOre = 0;
			int bestHQDistSq = 9999999;
			for (int i = 0; i < 8; i++) {
				Direction d = intToDirection(i);
				double dirOre = rc.senseOre(myLoc.add(d));
				int dirHQDistSq = myLoc.add(d).distanceSquaredTo(HQLoc);
				if (rc.canMove(d) && dirOre > 0) {
					if (dirOre > bestOre) {
						bestDirs[0] = d;
						numBestDirs = 1;
						bestOre = dirOre;
						bestHQDistSq = dirHQDistSq;
					} else if (dirOre >= bestOre) {
						if (dirHQDistSq < bestHQDistSq) {
							bestDirs[0] = d;
							numBestDirs = 1;
							bestOre = dirOre;
							bestHQDistSq = dirHQDistSq;
						} else if (dirHQDistSq <= bestHQDistSq) {
							bestDirs[numBestDirs] = d;
							numBestDirs++;
						}
					}
				}
			}
			if (bestOre > minOre) {
				int choice = rand.nextInt(numBestDirs);
				rc.move(bestDirs[choice]);
				unmarkMining(rc.getID());
			} else {
				// seek better location
				if (brandNew) {
					// initial location finding
					minerTarget = findNearestMarkedMiner();
					if (minerTarget == null) {
						tryMove(directions[rand.nextInt(8)]);
					} else {
						tryMove(myLoc.directionTo(minerTarget));
					}
				} else {
					if (minerTarget != null) {
						// continue initial location finding
						if (myLoc.isAdjacentTo(minerTarget) || myLoc.equals(minerTarget)) {
							// reached target, use secondary location finding
							minerTarget = null;
							tryMove(myLoc.directionTo(HQLoc).opposite());
						} else {
							// not reached target, continue initial location finding
							boolean success = limitedTryMove(myLoc.directionTo(minerTarget));
							if (!success) {
								minerTarget = null;
								limitedTryMove(myLoc.directionTo(HQLoc).opposite());
							}
						}
					} else {
						// use secondary location finding
						minerTarget = findNearestMarkedMiner();
						if (minerTarget == null) {
							tryMove(directions[rand.nextInt(8)]);
						} else {
							boolean success = limitedTryMove(myLoc.directionTo(minerTarget));
							if (!success) {
								minerTarget = findNearestMarkedMiner();
								if (minerTarget == null) {
									limitedTryMove(directions[rand.nextInt(8)]);
								} else {
									limitedTryMove(myLoc.directionTo(minerTarget));
								}
							}
						}
					}
				}
				unmarkMining(rc.getID());
			}
		}
		brandNew = false;
	}
	
	private static MapLocation findNearestMarkedMiner() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] myRobots = rc.senseNearbyRobots(9999999, myTeam);
		MapLocation nearest = null;
		int nearestDist = 9999999;
		for (RobotInfo ri : myRobots) {
			if (ri.type == RobotType.MINER) {
				if (ordersMining(ri.ID)) {
					MapLocation minerLoc = ri.location;
					int dist = myLoc.distanceSquaredTo(minerLoc);
					if (dist < nearestDist) {
						nearestDist = dist;
						nearest = minerLoc;
					}
				}
			}
		}
		if (nearest != null) {
			rc.setIndicatorLine(myLoc, nearest, 255,0,0);
			rc.setIndicatorDot(myLoc, 255,0,0);
		}
		return nearest;
	}
	
	private static void rally() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation invaderLoc = attackingEnemy();
		if (invaderLoc != null) {
			tryMove(myLoc.directionTo(invaderLoc));
		} else {
			if (myLoc.distanceSquaredTo(HQLoc) > (rushDist / 9)) {
				tryMove(myLoc.directionTo(HQLoc));
			} else {
				MapLocation enemy = nearestEnemy();
				if (enemy == null) {
					tryMove(directions[rand.nextInt(8)]);
				} else {
					tryMove(myLoc.directionTo(enemy));
				}
				
			}
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
	
	private static MapLocation attackingEnemy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(HQLoc, (rushDist / 9), enemyTeam);
		int closestDist = 9999999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			MapLocation enemyLoc = r.location;
			int dist = HQLoc.distanceSquaredTo(enemyLoc);
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
	
	private static final int MSG_SPACE = 65536;
	private static final int MSG_LEN = 6; // ID, order, x, y, marked, mining
	private static final int NUM_MSG = MSG_SPACE / MSG_LEN;

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
		} else {
			rc.broadcast(hash * MSG_LEN, ID);
			rc.broadcast(hash * MSG_LEN + 1, -1);
			rc.broadcast(hash * MSG_LEN + 4, 1);
		}
	}
	
	private static void unmarkOrders(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == ID) {
			rc.broadcast(hash * MSG_LEN + 4, 0);
		} else {
			rc.broadcast(hash * MSG_LEN, ID);
			rc.broadcast(hash * MSG_LEN + 1, -1);
			rc.broadcast(hash * MSG_LEN + 4, 0);
		}
	}
	
	private static void markMining(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == ID) {
			rc.broadcast(hash * MSG_LEN + 5, 1);
		} else {
			rc.broadcast(hash * MSG_LEN, ID);
			rc.broadcast(hash * MSG_LEN + 1, -1);
			rc.broadcast(hash * MSG_LEN + 5, 1);
		}
	}
	
	private static void unmarkMining(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == ID) {
			rc.broadcast(hash * MSG_LEN + 5, 0);
		} else {
			rc.broadcast(hash * MSG_LEN, ID);
			rc.broadcast(hash * MSG_LEN + 1, -1);
			rc.broadcast(hash * MSG_LEN + 5, 0);
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
	
	private static boolean ordersMining(int ID) throws GameActionException {
		int hash = hashID(ID);
		int foundID = rc.readBroadcast(hash * MSG_LEN);
		while (foundID != ID && foundID != 0) {
			hash = hash + 1;
			hash = hash % NUM_MSG;
			foundID = rc.readBroadcast(hash * MSG_LEN);
		}
		if (foundID == ID) {
			return (1 == rc.readBroadcast(hash * MSG_LEN + 5));
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
	static boolean tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	static boolean limitedTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 3 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 3) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	static boolean tryMoveLeft(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,-1,1,-2,-3};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	static boolean tryMoveRight(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,3};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}

	// This method will attempt to spawn in the given direction (or as close to it as possible)
	static boolean trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
			return true;
		}
		return false;
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
