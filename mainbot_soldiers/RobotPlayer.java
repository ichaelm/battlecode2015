package mainbot_soldiers;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {

	// AI parameters
	private static final int RUSH_TURN = 1200;
	
	// Game parameters
	private static final int MAX_MAP_WIDTH = 120;
	private static final int MAX_MAP_HEIGHT = 120;
	
	// Broadcast channels
	private static final int NUM_CHANNELS = 65536;
	private static final int CENSUS_CHAN = 0;
	private static final int CENSUS_SIZE = 21;
	private static final int PROGRESS_TABLE_CHAN = CENSUS_CHAN + CENSUS_SIZE;
	private static final int PROGRESS_TABLE_SIZE = 21;
	private static final int COMPLETED_TABLE_CHAN = PROGRESS_TABLE_CHAN + PROGRESS_TABLE_SIZE;
	private static final int COMPLETED_TABLE_SIZE = 21;
	private static final int BUILD_QUEUE_CHAN = COMPLETED_TABLE_CHAN + COMPLETED_TABLE_SIZE;
	private static final int BUILD_QUEUE_ROW_SIZE = 2;
	private static final int BUILD_QUEUE_NUM_ROWS = 50;
	private static final int BUILD_QUEUE_SIZE = BUILD_QUEUE_ROW_SIZE * BUILD_QUEUE_NUM_ROWS;
	private static final int MINING_TABLE_CHAN = BUILD_QUEUE_CHAN + BUILD_QUEUE_SIZE;
	private static final int MINING_TABLE_ROW_SIZE = 5;
	private static final int MINING_TABLE_NUM_ROWS = 50;
	private static final int MINING_TABLE_SIZE = MINING_TABLE_ROW_SIZE * MINING_TABLE_NUM_ROWS;
	private static final int NAVMAP_CHAN = MINING_TABLE_CHAN + MINING_TABLE_SIZE;
	private static final int NAVMAP_SQUARE_SIZE = 3;
	private static final int NAVMAP_WIDTH = MAX_MAP_WIDTH;
	private static final int NAVMAP_HEIGHT = MAX_MAP_HEIGHT;
	private static final int NAVMAP_SIZE = NAVMAP_SQUARE_SIZE * NAVMAP_WIDTH * NAVMAP_HEIGHT;
	private static final int NORTH_BOUND_CHAN = NAVMAP_CHAN + NAVMAP_SIZE;
	private static final int EAST_BOUND_CHAN = NORTH_BOUND_CHAN + 1;
	private static final int SOUTH_BOUND_CHAN = EAST_BOUND_CHAN + 1;
	private static final int WEST_BOUND_CHAN = SOUTH_BOUND_CHAN + 1;
	private static final int MINER_ORE_COUNTER_CHAN = WEST_BOUND_CHAN + 1;
	private static final int BEAVER_ORE_COUNTER_CHAN = MINER_ORE_COUNTER_CHAN + 1;
	private static final int BUILDER_BEAVER_COUNTER_CHAN = BEAVER_ORE_COUNTER_CHAN + 1;
	private static final int BUILDER_BEAVER_REQUEST_CHAN = BUILDER_BEAVER_COUNTER_CHAN + 1;
	private static final int UNIT_ORDER_CHAN = BUILDER_BEAVER_REQUEST_CHAN + 1;
	private static final int UNIT_TOWER_DEFENSE_CHAN = UNIT_ORDER_CHAN + 1;
	private static final int LAUNCHER_TOWER_DEFENSE_CHAN = UNIT_TOWER_DEFENSE_CHAN + 1;
	
	// Broadcast signaling constants
	private static final int NO_BOUND = 99999;
	private static final int UNIT_ORDER_ATTACK_TOWERS = 1;
	private static final int UNIT_ORDER_DEFEND = 2;
	private static final int UNIT_ORDER_RALLY = 3;
	private static final int UNIT_ORDER_ATTACK_VULNERABLE_TOWER = 4;
	
	// cached enums for brevity
	private static final RobotType HQ = RobotType.HQ;
	private static final RobotType TOWER = RobotType.TOWER;
	private static final RobotType SUPPLYDEPOT = RobotType.SUPPLYDEPOT;
	private static final RobotType TECHNOLOGYINSTITUTE = RobotType.TECHNOLOGYINSTITUTE;
	private static final RobotType BARRACKS = RobotType.BARRACKS;
	private static final RobotType HELIPAD = RobotType.HELIPAD;
	private static final RobotType TRAININGFIELD = RobotType.TRAININGFIELD;
	private static final RobotType TANKFACTORY = RobotType.TANKFACTORY;
	private static final RobotType MINERFACTORY = RobotType.MINERFACTORY;
	private static final RobotType HANDWASHSTATION = RobotType.HANDWASHSTATION;
	private static final RobotType AEROSPACELAB = RobotType.AEROSPACELAB;
	private static final RobotType BEAVER = RobotType.BEAVER;
	private static final RobotType COMPUTER = RobotType.COMPUTER;
	private static final RobotType SOLDIER = RobotType.SOLDIER;
	private static final RobotType BASHER = RobotType.BASHER;
	private static final RobotType MINER = RobotType.MINER;
	private static final RobotType DRONE = RobotType.DRONE;
	private static final RobotType TANK = RobotType.TANK;
	private static final RobotType COMMANDER = RobotType.COMMANDER;
	private static final RobotType LAUNCHER = RobotType.LAUNCHER;
	private static final RobotType MISSILE = RobotType.MISSILE;
	
	// Cached game information
	private static RobotController rc;
	private static Team myTeam;
	private static Team enemyTeam;
	private static RobotType myType;
	private static int myAttackRangeSq;
	private static int mySensorRangeSq;
	private static Random rand;
	private static MapLocation myHQLoc;
	private static MapLocation enemyHQLoc;
	private static MapLocation mapCenter;
	private static int rushDistSq;
	private static int rushDist;
	private static MapLocation myLoc;
	private static MapLocation minerTarget;
	private static boolean leftHanded;
	private static MapLocation[] enemyTowerLocs;
	private static MapLocation[] myTowerLocs;
	private static boolean mineWithBeavers;
	private static boolean mining;
	private static int mineCounter;
	private static RobotInfo[] enemyRobots;
	private static int[] numRobotsByType;
	private static int[] numInProgressByType;
	private static int[] numCompletedByType;
	private static int[][] buildQueue;
	private static int row;

	// should be final, but can't because set in run()
	private static Direction[] directions;
	private static RobotType[] robotTypes;
	private static int[] offsets;
	private static double[] oreConsumptionByType;
	
	public static void run(RobotController myrc) {
		// Initialize cached game information
		rc = myrc;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		robotTypes = RobotType.values();
		offsets = new int[] {0,1,-1,2,-2};
		enemyHQLoc = rc.senseEnemyHQLocation();
		directions = new Direction[]{
				Direction.NORTH,
				Direction.NORTH_EAST,
				Direction.EAST,
				Direction.SOUTH_EAST,
				Direction.SOUTH,
				Direction.SOUTH_WEST,
				Direction.WEST,
				Direction.NORTH_WEST};
		if (myrc.getType() == MISSILE) { //hack for missiles to run faster
			rc.setIndicatorString(1, "Bytecodes Used: " + Clock.getBytecodeNum() + " Left: " + Clock.getBytecodesLeft());
			runMissile();
		}
		myType = rc.getType();
		myAttackRangeSq = myType.attackRadiusSquared;
		mySensorRangeSq = myType.sensorRadiusSquared;
		rand = new Random(rc.getID());
		myHQLoc = rc.senseHQLocation();
		mapCenter = new MapLocation(myHQLoc.x + (enemyHQLoc.x - myHQLoc.x)/2, myHQLoc.y + (enemyHQLoc.y - myHQLoc.y)/2);
		rushDistSq = myHQLoc.distanceSquaredTo(enemyHQLoc);
		rushDist = (int)Math.sqrt(rushDistSq);
		oreConsumptionByType = new double[]{5, 0, 0, 0.4, 4, 25/6, 1.25, 5, 2.5, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		mining = false;
		mineCounter = 0;

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
		
		// information stored across rounds
		double[] oreMinedByTurn = new double[10];
		double[] oreMinedByTurnByMiners = new double[10];
		double[] oreMinedByTurnByBeavers = new double[10];
		int[] oldNumRobotsByType = new int[21];
		int selfSwarmTimer = 0;
		boolean defendPriority = false;
		
		// turn 1 code
		try {
			// initialize bounds channels
			initBounds();
			
			// TODO: better mining strategy picking
			double orePerSquare = rc.senseOre(myHQLoc); // approximate
			int turnToMaximize = 300 + 2*rushDist + 100*rc.senseTowerLocations().length;
			// forget about rush safety for now, just go long term
			double beaverEarlyOre = Simulator.simulateMaximumEarlyMiningOre(BEAVER, orePerSquare, turnToMaximize);
			double minerEarlyOre = Simulator.simulateMaximumEarlyMiningOre(MINER, orePerSquare, turnToMaximize);
			if (beaverEarlyOre > minerEarlyOre) {
				mineWithBeavers = true;
				//idealOreGenerationPerUnit = Simulator.beaverOreRateGivenNumMines(orePerSquare, Simulator.beaverOptimalNumMines(orePerSquare));
				//minOre = Simulator.beaverOreLeftAfterMining(orePerSquare, Simulator.beaverOptimalNumMines(orePerSquare)) + 0.1;
			} else {
				mineWithBeavers = false;
				//idealOreGenerationPerUnit = Simulator.minerOreRateGivenNumMines(orePerSquare, Simulator.minerOptimalNumMines(orePerSquare));
				//minOre = Simulator.beaverOreLeftAfterMining(orePerSquare, Simulator.beaverOptimalNumMines(orePerSquare)) + 0.1;
			}
		} catch (Exception e) {
			System.out.println("HQ Exception");
			e.printStackTrace();
		}

		while (true) {
			try {
				int[] bytecodes = new int[50];
				bytecodes[0] = Clock.getBytecodeNum();
				
				// participate in census
				markCensus();
				
				bytecodes[1] = Clock.getBytecodeNum();
				
				// update locations
				updateLocations();
				
				bytecodes[2] = Clock.getBytecodeNum();
				
				// read unit census, progress table, and completed table
				numRobotsByType = readCensus();
				numInProgressByType = readProgressTable();
				numCompletedByType = readCompletedTable();
				
				bytecodes[3] = Clock.getBytecodeNum();
				
				//resetting the defense channel
				rc.broadcast(UNIT_TOWER_DEFENSE_CHAN, 0);
				rc.broadcast(LAUNCHER_TOWER_DEFENSE_CHAN, 0);
				
				/*
				rc.setIndicatorString(0, numRobotsByType[HQ.ordinal()] + " " + numRobotsByType[BEAVER.ordinal()] + " " + numRobotsByType[MINERFACTORY.ordinal()] + " " + numRobotsByType[MINER.ordinal()] + " " + numRobotsByType[BARRACKS.ordinal()] + " " + numRobotsByType[SOLDIER.ordinal()]);
				rc.setIndicatorString(1, numInProgressByType[HQ.ordinal()] + " " + numInProgressByType[BEAVER.ordinal()] + " " + numInProgressByType[MINERFACTORY.ordinal()] + " " + numInProgressByType[MINER.ordinal()] + " " + numInProgressByType[BARRACKS.ordinal()] + " " + numInProgressByType[SOLDIER.ordinal()]);
				rc.setIndicatorString(2, numCompletedByType[HQ.ordinal()] + " " + numCompletedByType[BEAVER.ordinal()] + " " + numCompletedByType[MINERFACTORY.ordinal()] + " " + numCompletedByType[MINER.ordinal()] + " " + numCompletedByType[BARRACKS.ordinal()] + " " + numCompletedByType[SOLDIER.ordinal()]);
				*/
				
				bytecodes[4] = Clock.getBytecodeNum();
				
				// sensing all enemy robots
				enemyRobots = rc.senseNearbyRobots(999999, enemyTeam);
				
				bytecodes[5] = Clock.getBytecodeNum();
				
				// calculate destroyed robots
				int[] numDestroyedByType = new int[21];
				for (int i = 21; --i >= 0;) {
					int diff = numRobotsByType[i] - oldNumRobotsByType[i];
					int expectedDiff = numCompletedByType[i];
					numDestroyedByType[i] = expectedDiff - diff;
					// error checking
					if (diff > expectedDiff) {
//						System.out.println("error with counting destroyed robots");
					}
				}
				
				bytecodes[6] = Clock.getBytecodeNum();
				
				// save round number to ensure I don't go over bytecode limit
				int roundNum = Clock.getRoundNum();
				
				bytecodes[7] = Clock.getBytecodeNum();
				
				// read ore counters
				double oreMinedLastTurnByMiners = readMinerOreCounter();
				double oreMinedLastTurnByBeavers = readBeaverOreCounter();
				double oreMinedLastTurn = oreMinedLastTurnByMiners + oreMinedLastTurnByBeavers;
				
				bytecodes[8] = Clock.getBytecodeNum();
				
				// read builder beaver counter
				int numBuilderBeavers = readBuilderBeaverCounter();
				
				bytecodes[9] = Clock.getBytecodeNum();
				
				// calculate average mining rate and ore income rate for past 10 turns
				oreMinedByTurn[roundNum%10] = oreMinedLastTurn;
				double miningRate = 0;
				for (int i = 0; i < oreMinedByTurn.length; i++) {
					miningRate += oreMinedByTurn[i];
				}
				miningRate = miningRate / 10;
				double oreIncomeRate = miningRate + GameConstants.HQ_ORE_INCOME;
				// for miners
				oreMinedByTurnByMiners[roundNum%10] = oreMinedLastTurnByMiners;
				double minerMiningRate = 0;
				for (int i = 0; i < oreMinedByTurnByMiners.length; i++) {
					minerMiningRate += oreMinedByTurnByMiners[i];
				}
				minerMiningRate = minerMiningRate / 10;
				double oreMinedPerMiner = minerMiningRate / numRobotsByType[MINER.ordinal()];
				// for beavers
				oreMinedByTurnByBeavers[roundNum%10] = oreMinedLastTurnByBeavers;
				double beaverMiningRate = 0;
				for (int i = 0; i < oreMinedByTurnByBeavers.length; i++) {
					beaverMiningRate += oreMinedByTurnByBeavers[i];
				}
				beaverMiningRate = beaverMiningRate / 10;
				double oreMinedPerBeaver = beaverMiningRate / (numRobotsByType[BEAVER.ordinal()] - numBuilderBeavers); // ignores the fact that builder beavers can mine
				
				bytecodes[10] = Clock.getBytecodeNum();
				
				// calculate supply upkeep
				int totalSupplyUpkeep = 0;
				for (int i = 21; --i >= 0;) {
					RobotType type = robotTypes[i];
					int numRobots = numRobotsByType[i];
					int supplyUpkeepPerRobot = type.supplyUpkeep;
					if (supplyUpkeepPerRobot > 0) {
						supplyUpkeepPerRobot += 8; // for safety, adjust later
					}
					totalSupplyUpkeep += numRobots * supplyUpkeepPerRobot;
				}

				
				bytecodes[11] = Clock.getBytecodeNum();
				
				// calculate supply generation
				int totalSupplyGeneration = (int)(100*(2+Math.pow(numRobotsByType[SUPPLYDEPOT.ordinal()],0.6)));
				
				bytecodes[12] = Clock.getBytecodeNum();
				
				// calculate build queue
				buildQueue = new int[BUILD_QUEUE_NUM_ROWS][2];
				row = 0;
				if (numBuilderBeavers < 2) { // HACK, CALCULATE REAL NUMBER
					buildQueue[row][0] = BEAVER.ordinal();
					buildQueue[row][1] = 1;
					row++;
					requestBuilderBeaver();
				}
				
				bytecodes[13] = Clock.getBytecodeNum();
				
				if (mineWithBeavers) {
					if (numRobotsByType[BEAVER.ordinal()] - numBuilderBeavers < 2) {
						buildQueue[row][0] = BEAVER.ordinal();
						buildQueue[row][1] = 1;
						row++;
					} else {
						int breakEvenRounds = (int)Math.ceil(BEAVER.oreCost / oreMinedPerBeaver) + BEAVER.buildTurns;
						if (roundNum + breakEvenRounds < 2000 - rushDist) {
							buildQueue[row][0] = BEAVER.ordinal();
							buildQueue[row][1] = 1;
							row++;
						}
					}
				} else {
					if (numRobotsByType[MINERFACTORY.ordinal()] + numInProgressByType[MINERFACTORY.ordinal()] < 1) {
						buildQueue[row][0] = MINERFACTORY.ordinal();
						buildQueue[row][1] = 1;
						row++;
					} else {
						int breakEvenRounds = (int)Math.ceil(MINER.oreCost / oreMinedPerMiner) + MINER.buildTurns;
						if (roundNum + breakEvenRounds < 2000 - rushDist) {
							buildQueue[row][0] = MINER.ordinal();
							buildQueue[row][1] = 1;
							row++;
						}
					}
				}
				
				bytecodes[14] = Clock.getBytecodeNum();
				if (!defendPriority) { // if defense is a huge priority, stop building supply depots, we need all the resources we can get
					if (totalSupplyUpkeep > totalSupplyGeneration) {
						buildQueue[row][0] = SUPPLYDEPOT.ordinal();
						buildQueue[row][1] = 1;
						row++;
					}
				} else {
					if (totalSupplyUpkeep*.75 > totalSupplyGeneration) {
						buildQueue[row][0] = SUPPLYDEPOT.ordinal();
						buildQueue[row][1] = 1;
						row++;
					}
				}
				
				bytecodes[15] = Clock.getBytecodeNum();
				
				//updating unit counts
				int numSoldiers = numRobotsByType[SOLDIER.ordinal()] + numInProgressByType[SOLDIER.ordinal()];
				int numTanks = numRobotsByType[TANK.ordinal()] + numInProgressByType[TANK.ordinal()];
				int numDrones = numRobotsByType[DRONE.ordinal()] + numInProgressByType[DRONE.ordinal()];
				int numLaunchers = numRobotsByType[LAUNCHER.ordinal()] + numInProgressByType[LAUNCHER.ordinal()];
				
				// what units and what buildings to build in what order
				int maxSoldiers = 100;
				int maxTanks = 30;
				int maxLaunchers = 6;
				
				if (Clock.getRoundNum() < 500 && rc.senseNearbyRobots(9999, enemyTeam).length > 0) {
					defendPriority = true;
				}
				
				if (defendPriority) {	
					
					addToBuildQueue(LAUNCHER, maxLaunchers, numLaunchers);
					addToBuildQueue(HELIPAD, 1, numRobotsByType[HELIPAD.ordinal()] + numInProgressByType[HELIPAD.ordinal()]);			
					addToBuildQueue(AEROSPACELAB, maxLaunchers*.2, numRobotsByType[AEROSPACELAB.ordinal()] + numInProgressByType[AEROSPACELAB.ordinal()]);			

					addToBuildQueue(SOLDIER, maxSoldiers*.1, numSoldiers);
					addToBuildQueue(BARRACKS, maxSoldiers*.01, numRobotsByType[BARRACKS.ordinal()] + numInProgressByType[BARRACKS.ordinal()]);
					
					addToBuildQueue(TANK, maxTanks, numTanks);
					addToBuildQueue(TANKFACTORY, maxTanks*.1, numRobotsByType[TANKFACTORY.ordinal()] + numInProgressByType[TANKFACTORY.ordinal()]);			
					
					addToBuildQueue(SOLDIER, maxSoldiers, numSoldiers);
				} else {
					//build a scout, send to center, then build soldiers
					addToBuildQueue(DRONE, 1, numDrones);
					addToBuildQueue(HELIPAD, 1, numRobotsByType[HELIPAD.ordinal()] + numInProgressByType[HELIPAD.ordinal()]);		
					
					addToBuildQueue(SOLDIER, maxSoldiers*.3, numSoldiers);
					addToBuildQueue(BARRACKS, maxSoldiers*.03, numRobotsByType[BARRACKS.ordinal()] + numInProgressByType[BARRACKS.ordinal()]);			
					
					addToBuildQueue(TANK, maxTanks*.3, numTanks);
					addToBuildQueue(TANKFACTORY, maxTanks*.03, numRobotsByType[TANKFACTORY.ordinal()] + numInProgressByType[TANKFACTORY.ordinal()]);			
					
					addToBuildQueue(SOLDIER, maxSoldiers*.7, numSoldiers);
					addToBuildQueue(BARRACKS, maxSoldiers*.07, numRobotsByType[BARRACKS.ordinal()] + numInProgressByType[BARRACKS.ordinal()]);
					
					addToBuildQueue(TANK, maxTanks, numTanks);
					addToBuildQueue(TANKFACTORY, maxTanks*.1, numRobotsByType[TANKFACTORY.ordinal()] + numInProgressByType[TANKFACTORY.ordinal()]);			
					
					addToBuildQueue(SOLDIER, maxSoldiers, numSoldiers);
				}
				
				// if you get through everything else, just build tanks
				addToBuildQueue(TANK, 99999, numTanks);
				
				bytecodes[16] = Clock.getBytecodeNum();
				bytecodes[17] = Clock.getBytecodeNum();
				
				writeBuildQueue(buildQueue);
				
				bytecodes[18] = Clock.getBytecodeNum();
				
				// telling units what to do
				if (selfSwarmTimer > 0) {
					selfSwarmTimer--;
				}
				
				bytecodes[19] = Clock.getBytecodeNum();
				
				rc.setIndicatorString(0, "NumSoldiers: " + numSoldiers);
				
				// check if good time to swarm myself
				if (enemyTowerLocs.length < myTowerLocs.length) {
					if (areEnemyTowersVulnerable() && numSoldiers >= 50) {
						rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_ATTACK_VULNERABLE_TOWER);
					} else {
						selfSwarmTimer = 75;
						rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_DEFEND);
					}
					// check if good time to stop swarming myself
				} else if (selfSwarmTimer <= 0) {
					// check if good time to attack
					if (numSoldiers >= 50) {
						rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_ATTACK_TOWERS);
					} else { //check if a good time to retreat
//						if (rc.senseNearbyRobots(9999, enemyTeam).length > 1) {
//							selfSwarmTimer = 75;
//							rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_DEFEND);
//						} else {
//							rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_RALLY);
//						}
						if (numSoldiers <= 30) {
							rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_RALLY);
						} else {
							rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_DEFEND);
						}
					}
				}
				
				bytecodes[20] = Clock.getBytecodeNum();
				
				// attack
				if (rc.isWeaponReady()) {
					HQAttackSomething();
				}
				
				bytecodes[21] = Clock.getBytecodeNum();
				
				// spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				bytecodes[22] = Clock.getBytecodeNum();
				
				// transfer supply
				transferSupply();
				
				bytecodes[23] = Clock.getBytecodeNum();
				
				// store old values
				oldNumRobotsByType = numRobotsByType;
				
				bytecodes[24] = Clock.getBytecodeNum();
				
				/*
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < 25; i++) {
					sb.append(i + ": ");
					sb.append((bytecodes[i] - bytecodes[i-1]) + " ");
				}
				
				rc.setIndicatorString(0, sb.toString());
				*/
				
				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}
	}
	
	private static void addToBuildQueue(RobotType type, double desired, int exist) {
		if (exist < desired) {
			buildQueue[row][0] = type.ordinal();
			buildQueue[row][1] = 1;
			row++;
		}
	}
	
	//returns true if there exists a tower thathas less than 3 enemies around it
	private static boolean areEnemyTowersVulnerable() {
		for (MapLocation i: enemyTowerLocs) {
			if (rc.senseNearbyRobots(i, 20, enemyTeam).length < 3) {
				return true;
			}
		}
		return false;
	}
	
	private static MapLocation getEnemyVulnerableTower() {
		for (MapLocation i: enemyTowerLocs) {
			if (rc.senseNearbyRobots(i, 20, enemyTeam).length < 3) {
				return i;
			}
		}
		return null;
	}

	private static void runTower() {
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// attack
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// TODO: basher movement and combat code
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						rally();
					} else {
						MapLocation enemyLoc = nearestSensedEnemy();
						if (enemyLoc == null) {
							tryMove(rc.getLocation().directionTo(enemyHQLoc));
						} else {
							tryMove(rc.getLocation().directionTo(enemyLoc));
						}
					}
				}
				
				// transfer supply
				transferSupply();
				
				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Basher Exception");
				e.printStackTrace();
			}
		}
	}
	
	// TODO: escape crowding
	// TODO: make builder beaver build in good locations: checkerboard, near hq
	private static void runBeaver() {
		// on first turn, determine if I am a builder beaver or a mining beaver
		boolean builderBeaver = false;
		RobotType thingIJustBuilt = null;
		try {
			builderBeaver = amIABuilderBeaver();
		} catch (Exception e) {
			System.out.println("Beaver Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// mark builder beaver
				if (builderBeaver) {
					markBuilderBeaverCounter();
				}
				
				// progress
				if (rc.isBuildingSomething()) {
					markProgressTable(thingIJustBuilt);
				} else if (thingIJustBuilt != null) {
					markCompletedTable(thingIJustBuilt);
					thingIJustBuilt = null;
				}
				
				// attack
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				
				if (rc.isCoreReady()) {
					if (builderBeaver) {
						// follow spawn orders
						Direction escapeDir = escapeCrowding(); // hack, better place to put this
						if (escapeDir != null) {
							tryMove(escapeDir);
						} else {
							thingIJustBuilt = beaverFollowOrders();
						}
					} else {
						// TODO: beaver mining
						Direction escapeDir = escapeCrowding();
						if (escapeDir != null) {
							tryMove(escapeDir);
						} else {
							mine();
						}
					}
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// attack
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				
				// TODO: commander movement code
				
				// transfer supply
				transferSupply();
				
				// yield
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// look for map boundaries
				lookForBounds();
				
				// TODO: drone attack code
				if (rc.isWeaponReady()) {
					MapLocation attackLoc = droneAttackLocation();
					if (attackLoc != null) {
						rc.attackLocation(attackLoc);
					}
				}
				
				// TODO: drone movement code
				if (rc.isCoreReady()) {
					launcherTryMove(myLoc.directionTo(mapCenter));
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participare in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}

				// transfer supply
				transferSupply();
				
				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Helipad Exception");
				e.printStackTrace();
			}
		}
	}
	
	// TODO: make launcher able to shoot at enemies out of sensor range using memory of recent enemy positions, so they can't run away
	private static void runLauncher() {
		boolean retreated = false;
		boolean fireBurst = false;
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// attack
				MapLocation target = nearestSensedEnemy();
				
				if (target != null ) {
					if (myLoc.distanceSquaredTo(target) <= 25) {
						if (rc.getMissileCount() > 4) {
							fireBurst = true;
						}
						else {
							if (rc.getMissileCount() <= 1) {
								fireBurst = false;
							}
						}
						if (fireBurst) {
							tryLaunch(rc.getLocation().directionTo(target));
						} 
						continue;
					}
				}

				if (rc.isCoreReady()) {
					//have all units move to HQ before going to posts
					if (!retreated) {
						launcherTryMove(rc.getLocation().directionTo(myHQLoc));
					} else {
						// make some code to evenly distribute soldiers between towers,
						MapLocation destination = getDefenseTower();
												
						// if already close to a tower, sit closer to the enemy so they attack soldiers before the tower
						if (myLoc.distanceSquaredTo(destination) < 3) {
							launcherTryMove(rc.getLocation().directionTo(enemyHQLoc));
						} else {
							launcherTryMove(rc.getLocation().directionTo(destination));
						}
					}
					if (myLoc.distanceSquaredTo(myHQLoc) < 40) {
						retreated = true;
					}
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// attack
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				
				// mine
				// TODO: miner code
				// TODO: make miners avoid enemy towers
				if (rc.isCoreReady()) {
					Direction escapeDir = escapeCrowding();
					if (escapeDir != null) {
						tryMove(escapeDir);
					} else {
						mine();
					}
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				// transfer supply
				transferSupply();
				
				// end turn
				rc.yield();
			} catch (Exception e) {
				System.out.println("Miner Factory Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runMissile() {
		try {
			while (true) {
				//update locations
				updateLocations();
				
				// missile move and explode code
				if (rc.isCoreReady()) {
					MapLocation target = fastNearestEnemy();
					if (target == null) {
						quickTryMove(myLoc.directionTo(enemyHQLoc));
					} else {
						if (myLoc.distanceSquaredTo(target) <= 2) { // if adjacent
							quickTryMove(myLoc.directionTo(target));
							rc.explode();
						} else {
							quickTryMove(myLoc.directionTo(target));
						}
					}
				}
				// end turn
				rc.yield();
			}
		}  
		catch (Exception e) {
			System.out.println("Missile Exception");
			e.printStackTrace();
		}
	}

	private static void runSoldier() {
		boolean retreated = false;
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// attack
				if (rc.isWeaponReady()) {
					focusAttackEnemies();
				}
				
				// move according to orders
				if (rc.isCoreReady()) {
					int order = rc.readBroadcast(UNIT_ORDER_CHAN);
					switch (order) {
					case UNIT_ORDER_ATTACK_TOWERS:
						retreated = false;
						tryMove(rc.getLocation().directionTo(closestLocation(mapCenter, enemyTowerLocs)));
						break;
						
					case UNIT_ORDER_DEFEND:
						//have all units move to HQ before going to posts
						if (!retreated) {
							launcherTryMove(rc.getLocation().directionTo(myHQLoc));
						} else {
							// make some code to evenly distribute soldiers between towers,
							MapLocation destination = getDefenseTower();
													
							// if already close to a tower, sit closer to the enemy so they attack soldiers before the tower
							if (myLoc.distanceSquaredTo(destination) < 3) {
								launcherTryMove(rc.getLocation().directionTo(enemyHQLoc));
							} else {
								launcherTryMove(rc.getLocation().directionTo(destination));
							}
						}
						if (myLoc.distanceSquaredTo(myHQLoc) < 40) {
							retreated = true;
						}
						
						break;
					case UNIT_ORDER_RALLY:
						retreated = false;
						rally();
						break;
					case UNIT_ORDER_ATTACK_VULNERABLE_TOWER:
						retreated = false;
						tryMove(rc.getLocation().directionTo(getEnemyVulnerableTower()));
						break;
					
					}
				}
				
				// transfer supply
				transferSupply();
				
				// end round
				rc.yield();
			} catch (Exception e) {
				//System.out.println("Soldier Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runSupplyDepot() {
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// transfer supply
				transferSupply();
				
				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Supply Depot Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTank() {
		MapLocation destination;
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// attack
				if (rc.isWeaponReady()) {
					focusAttackEnemies();
				}
				
				// move according to orders
				if (rc.isCoreReady()) {
					int order = rc.readBroadcast(UNIT_ORDER_CHAN);
					switch (order) {
					case UNIT_ORDER_ATTACK_TOWERS:
						destination = closestLocation(mapCenter, enemyTowerLocs);
						if (myLoc.distanceSquaredTo(destination) <= 30) {
							if (rc.senseNearbyRobots(7, myTeam).length > 2) {
								tryMove(rc.getLocation().directionTo(destination));
							}
							// if not enough of my team nearby, wait for them before moving in for the kill
						} else {
							tryMove(rc.getLocation().directionTo(destination));
						}
						break;
						
					case UNIT_ORDER_DEFEND:
						// make some code to evenly distribute soldiers between towers,
						destination = getDefenseTower();
												
						// if already close to a tower, sit closer to the enemy so they attack soldiers before the tower
						if (myLoc.distanceSquaredTo(destination) < 10) {
							launcherTryMove(rc.getLocation().directionTo(enemyHQLoc));
						} else {
							launcherTryMove(rc.getLocation().directionTo(destination));
						}
						break;
						
					case UNIT_ORDER_RALLY:
						rally();
						break;
						
					case UNIT_ORDER_ATTACK_VULNERABLE_TOWER:
						tryMove(rc.getLocation().directionTo(getEnemyVulnerableTower()));
						break;
					}
				}
				
				// transfer supply
				transferSupply();
				
				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTankFactory() {
		while (true) {
			try {
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				// transfer supply
				transferSupply();
				
				// end round
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
				// participate in census
				markCensus();
				
				// update locations
				updateLocations();
				
				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}
				
				// transfer supply
				transferSupply();
				
				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Training Field Exception");
				e.printStackTrace();
			}
		}
	}
	
	private static int numEnemiesSwarmingBase() {
		int sum = 0;
		for (RobotInfo ri : enemyRobots) {
			if (ri.location.distanceSquaredTo(myHQLoc) <= Math.pow(Math.sqrt(RobotType.HQ.sensorRadiusSquared) + 5, 2)) {
				sum++;
			} else {
				for (MapLocation towerLoc : myTowerLocs) {
					if (ri.location.distanceSquaredTo(towerLoc) <= Math.pow(Math.sqrt(RobotType.TOWER.sensorRadiusSquared) + 5, 2)) {
						sum++;
						break;
					}
				}
			}
		}
		return sum;
	}
	
	private static MapLocation getDefenseTower() throws GameActionException {
		int currentTower = rc.readBroadcast(UNIT_TOWER_DEFENSE_CHAN);
		rc.broadcast(UNIT_TOWER_DEFENSE_CHAN, currentTower + 1);
		
		if (myTowerLocs.length == 0) {
			return myHQLoc;
		}
		
		rc.setIndicatorString(0, "" + currentTower%myTowerLocs.length);
		
		return myTowerLocs[currentTower%myTowerLocs.length];
	}
	
	private static MapLocation getLauncherDefenseTower() throws GameActionException {
		int currentTower = rc.readBroadcast(LAUNCHER_TOWER_DEFENSE_CHAN);
		rc.broadcast(LAUNCHER_TOWER_DEFENSE_CHAN, currentTower + 1);
		
		if (myTowerLocs.length == 0) {
			return myHQLoc;
		}
		
		rc.setIndicatorString(0, "" + currentTower%myTowerLocs.length);
		
		return myTowerLocs[currentTower%myTowerLocs.length];
	}
	
	private static MapLocation closestLocation(MapLocation center, MapLocation[] locations) {
		MapLocation closestTower = null;
		int closestDistance = 100000;
		int testDistance;
		for (MapLocation i: locations) {
			testDistance = center.distanceSquaredTo(i);
			if (testDistance < closestDistance) {
				closestTower = i;
				closestDistance = testDistance;
			}
		}
		return closestTower;
	}

	// this method initializes the map bounds stored in the broadcast array to indicate none have been found yet
	private static void initBounds() throws GameActionException {
		rc.broadcast(NORTH_BOUND_CHAN, NO_BOUND);
		rc.broadcast(EAST_BOUND_CHAN, NO_BOUND);
		rc.broadcast(SOUTH_BOUND_CHAN, NO_BOUND);
		rc.broadcast(WEST_BOUND_CHAN, NO_BOUND);
	}

	private static int[] readCensus() throws GameActionException {
		int[] census = new int[21];
		int channel = CENSUS_CHAN + 21;
		for (int i = 21; --i >= 0;) {
			channel--;
			census[i] = rc.readBroadcast(channel);
			if (census[i] != 0) {
				rc.broadcast(channel, 0);
			}
		}
		return census;
	}

	private static void markCensus() throws GameActionException {
		int typeNum = myType.ordinal();
		rc.broadcast(CENSUS_CHAN + typeNum, rc.readBroadcast(CENSUS_CHAN + typeNum) + 1);
	}

	private static int[] readProgressTable() throws GameActionException {
		int[] progress = new int[21];
		int channel = PROGRESS_TABLE_CHAN + 21;
		for (int i = 21; --i >= 0;) {
			channel--;
			progress[i] = rc.readBroadcast(channel);
			if (progress[i] != 0) {
				rc.broadcast(channel, 0);
			}
		}
		return progress;
	}

	private static int readProgressTable(RobotType type) throws GameActionException {
		int typeNum = type.ordinal();	
		return rc.readBroadcast(PROGRESS_TABLE_CHAN + typeNum);
	}

	private static void markProgressTable(RobotType type) throws GameActionException {
		int typeNum = type.ordinal();
		rc.broadcast(PROGRESS_TABLE_CHAN + typeNum, rc.readBroadcast(PROGRESS_TABLE_CHAN + typeNum) + 1);
	}

	private static int[] readCompletedTable() throws GameActionException {
		int[] completed = new int[21];
		int channel = COMPLETED_TABLE_CHAN + 21;
		for (int i = 21; --i >= 0;) {
			channel--;
			completed[i] = rc.readBroadcast(channel);
			if (completed[i] != 0) {
				rc.broadcast(channel, 0);
			}
		}
		return completed;
	}

	private static int readCompletedTable(RobotType type) throws GameActionException {
		int typeNum = type.ordinal();	
		return rc.readBroadcast(COMPLETED_TABLE_CHAN + typeNum);
	}

	private static void markCompletedTable(RobotType type) throws GameActionException {
		int typeNum = type.ordinal();
		rc.broadcast(COMPLETED_TABLE_CHAN + typeNum, rc.readBroadcast(COMPLETED_TABLE_CHAN + typeNum) + 1);
	}

	private static double readMinerOreCounter() throws GameActionException {
		double ore = Float.intBitsToFloat(rc.readBroadcast(MINER_ORE_COUNTER_CHAN));
		rc.broadcast(MINER_ORE_COUNTER_CHAN, Float.floatToIntBits(0));
		return ore;
	}

	private static double readBeaverOreCounter() throws GameActionException {
		double ore = Float.intBitsToFloat(rc.readBroadcast(BEAVER_ORE_COUNTER_CHAN));
		rc.broadcast(BEAVER_ORE_COUNTER_CHAN, Float.floatToIntBits(0));
		return ore;
	}

	private static void markMinerOreCounter(double ore) throws GameActionException {
		float oldOre = Float.intBitsToFloat(rc.readBroadcast(MINER_ORE_COUNTER_CHAN));
		rc.broadcast(MINER_ORE_COUNTER_CHAN, Float.floatToIntBits(oldOre + (float)ore));
	}

	private static void markBeaverOreCounter(double ore) throws GameActionException {
		float oldOre = Float.intBitsToFloat(rc.readBroadcast(BEAVER_ORE_COUNTER_CHAN));
		rc.broadcast(BEAVER_ORE_COUNTER_CHAN, Float.floatToIntBits(oldOre + (float)ore));
	}

	private static int readBuilderBeaverCounter() throws GameActionException {
		int numBuilderBeavers = rc.readBroadcast(BUILDER_BEAVER_COUNTER_CHAN);
		rc.broadcast(BUILDER_BEAVER_COUNTER_CHAN, 0);
		return numBuilderBeavers;
	}

	private static void markBuilderBeaverCounter() throws GameActionException {
		rc.broadcast(BUILDER_BEAVER_COUNTER_CHAN, rc.readBroadcast(BUILDER_BEAVER_COUNTER_CHAN) + 1);
	}

	private static void requestBuilderBeaver() throws GameActionException {
		rc.broadcast(BUILDER_BEAVER_REQUEST_CHAN, 1);
	}

	private static boolean amIABuilderBeaver() throws GameActionException {
		if (rc.readBroadcast(BUILDER_BEAVER_REQUEST_CHAN) > 0) {
			rc.broadcast(BUILDER_BEAVER_REQUEST_CHAN, 0);
			return true;
		}
		return false;
	}

	// columns: typeNum to build, number to build, number filled
	private static void writeBuildQueue(int[][] buildQueue) throws GameActionException {
		for (int row = 0; row < BUILD_QUEUE_NUM_ROWS; row++) {
			rc.broadcast(BUILD_QUEUE_CHAN + row*BUILD_QUEUE_ROW_SIZE + 0, buildQueue[row][0]);
			rc.broadcast(BUILD_QUEUE_CHAN + row*BUILD_QUEUE_ROW_SIZE + 1, buildQueue[row][1]);
			if (buildQueue[row][0] == 0) { // must be at end to ensure 0 is written
				break;
			}
		}
	}

	/*
	private static int getPositionInBuildQueue(RobotType type) throws GameActionException {
		int cumOre = 0;
		for (int row = 0; row < BUILD_QUEUE_NUM_ROWS; row++) {
			int typeNum = rc.readBroadcast(BUILD_QUEUE_CHAN + row*BUILD_QUEUE_ROW_SIZE + 0);
			int number = rc.readBroadcast(BUILD_QUEUE_CHAN + row*BUILD_QUEUE_ROW_SIZE + 1);
			if (typeNum == 0) {
				break;
			}
			RobotType foundType = robotTypes[typeNum];
			if (foundType == type && number > readCompletedTable(type) + readProgressTable(type)) {
				return cumOre;
			} else {
				cumOre += foundType.oreCost * number;
			}
		}
		return -1; // special value to indicate not found
	}
	*/
	
	private static RobotType getMyBuildOrder() throws GameActionException {
		RobotType myType = rc.getType();
		int teamOre = (int)rc.getTeamOre();
		int cumOre = 0;
		for (int row = 0; row < BUILD_QUEUE_NUM_ROWS && cumOre <= teamOre; row++) {
			int typeNum = rc.readBroadcast(BUILD_QUEUE_CHAN + row*BUILD_QUEUE_ROW_SIZE + 0);
			int number = rc.readBroadcast(BUILD_QUEUE_CHAN + row*BUILD_QUEUE_ROW_SIZE + 1);
			if (typeNum == 0) {
				break;
			}
			RobotType foundType = robotTypes[typeNum];
			if (builtBy(foundType) == myType
				&& number > readCompletedTable(foundType) + readProgressTable(foundType)
				&& cumOre + foundType.oreCost <= teamOre)
			{
				return foundType;
			} else {
				cumOre += foundType.oreCost * number;
			}
		}
		return null; // special value to indicate not found
	}

	private static void buildingFollowOrders() throws GameActionException {
		RobotType buildOrder = getMyBuildOrder();
		if (buildOrder != null) {
			boolean success = trySpawn(directions[rand.nextInt(8)],buildOrder);
			if (success) {
				markCompletedTable(buildOrder);
			} else {
				System.out.println("failed spawn");
			}
		}
	}

	private static RobotType beaverFollowOrders() throws GameActionException {
		RobotType buildOrder = getMyBuildOrder();
		if (buildOrder != null) {
			boolean success = tryBuild(directions[rand.nextInt(8)],buildOrder);
			if (success) {
				markProgressTable(buildOrder);
			} else {
				System.out.println("failed build");
			}
		}
		return buildOrder;
	}

	private static void writeMiningTable(int[][] miningTable) throws GameActionException {
		for (int row = 0; row < MINING_TABLE_NUM_ROWS; row++) {
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 0, miningTable[row][0]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 1, miningTable[row][1]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 2, miningTable[row][2]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 3, miningTable[row][3]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 4, miningTable[row][4]);
			if (miningTable[row][0] == 0) { // must be at end to ensure 0 is written
				break;
			}
		}
	}

	private static MapLocation getTopMiningTarget() throws GameActionException {
		int x = rc.readBroadcast(MINING_TABLE_CHAN + 2);
		int y = rc.readBroadcast(MINING_TABLE_CHAN + 3);
		return new MapLocation(x,y);
	}

	private static MapLocation getBetterMiningTarget(double ore) throws GameActionException {
		double topOre = Float.intBitsToFloat(rc.readBroadcast(MINING_TABLE_CHAN + 0));
		if (topOre > ore) {
			int x = rc.readBroadcast(MINING_TABLE_CHAN + 2);
			int y = rc.readBroadcast(MINING_TABLE_CHAN + 3);
			return new MapLocation(x,y);
		}
		return null;
	}

	private static void updateLocations() {
		myLoc = rc.getLocation();
		enemyTowerLocs = rc.senseEnemyTowerLocations();
		myTowerLocs = rc.senseTowerLocations();
	}

	private static RobotType builtBy(RobotType target) {
		switch (target) {
		case HQ: return null;
		case TOWER: return null;
		case AEROSPACELAB: return BEAVER;
		case BARRACKS: return BEAVER;
		case BASHER: return BARRACKS;
		case BEAVER: return HQ;
		case COMMANDER: return TRAININGFIELD;
		case COMPUTER: return TECHNOLOGYINSTITUTE;
		case DRONE: return HELIPAD;
		case HANDWASHSTATION: return BEAVER;
		case HELIPAD: return BEAVER;
		case LAUNCHER: return AEROSPACELAB;
		case MINER: return MINERFACTORY;
		case MINERFACTORY: return BEAVER;
		case MISSILE: return LAUNCHER;
		case SOLDIER: return BARRACKS;
		case SUPPLYDEPOT: return BEAVER;
		case TANK: return TANKFACTORY;
		case TANKFACTORY: return BEAVER;
		case TECHNOLOGYINSTITUTE: return BEAVER;
		case TRAININGFIELD: return BEAVER;
		default: return null;
		}
	}
	
	// TODO: better supply transfer code
	// TODO: supply runners
	private static void transferSupply() throws GameActionException {
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
		double mySupply = rc.getSupplyLevel();
		double lowestSupply = mySupply;
		RobotInfo lowestRobotNeedingSupply = null;
		for (RobotInfo r : nearbyAllies) {
			if (needsSupply(r) && r.supplyLevel < lowestSupply) {
				lowestSupply = r.supplyLevel;
				lowestRobotNeedingSupply = r;
			}
		}
		if (lowestRobotNeedingSupply != null) {
			if(needsSupply()){
				rc.transferSupplies((int)((mySupply-lowestSupply)/2), lowestRobotNeedingSupply.location);
			}else{
				rc.transferSupplies((int)(mySupply), lowestRobotNeedingSupply.location);
			}
		}
	}
	
	// TODO: replace needsSupply with intelligent supply upkeep calculations
	private static boolean needsSupply(){
		if(rc.getType() == BEAVER || rc.getType() == COMPUTER || 
				rc.getType() == COMMANDER || rc.getType() == SOLDIER || 
				rc.getType() == BASHER || rc.getType() == TANK || 
				rc.getType() == DRONE || rc.getType() == LAUNCHER || 
				rc.getType() == MINER)
			return true;
		return false;
	}
	
	private static boolean needsSupply(RobotInfo r){
		if(r.type == BEAVER || r.type == COMPUTER || 
				r.type == COMMANDER || r.type == SOLDIER || 
				r.type == BASHER || r.type == TANK || 
				r.type == DRONE || r.type == LAUNCHER || 
				r.type == MINER)
			return true;
		return false;
	}
	
	// TODO: replace with alec's code for beavers, may keep this for miners, two separate problems
	private static Direction escapeCrowding() {
		RobotInfo[] myRobots = rc.senseNearbyRobots(2);
		if (myRobots.length >= 3) {
			boolean[] blockedDirs = {false, false, false, false, false, false, false, false};
			for (int i = 0; i < myRobots.length; i++) {
				MapLocation move = myRobots[i].location;
				Direction d = myLoc.directionTo(move);
				blockedDirs[directionToInt(d)] = true;
			}
			Direction[] validMoves = new Direction[8-3];
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

	// TODO: mining code
	private static void mine() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		double myOre = rc.senseOre(myLoc);
		if (myOre > 5) {
			rc.mine();
		} else {
			Direction[] bestDirs = new Direction[8];
			int numBestDirs = 0;
			double bestOre = 0;
			for (int i = 0; i < 8; i++) {
				Direction d = intToDirection(i);
				double dirOre = rc.senseOre(myLoc.add(d));
				if (dirOre > bestOre && rc.canMove(d)) {
					bestDirs[0] = d;
					numBestDirs = 1;
					bestOre = dirOre;
				} else if (dirOre >= bestOre && rc.canMove(d)) {
					bestDirs[numBestDirs] = d;
					numBestDirs++;
				}
			}
			if (numBestDirs > 0) {
				if (bestOre > myOre) {
					int choice = rand.nextInt(numBestDirs);
					rc.move(bestDirs[choice]);
				} else {
					rc.mine();
				}
			} else {
				if (myOre > 0) {
					rc.mine();
				} else {
					if (myLoc.distanceSquaredTo(myHQLoc) > (rushDist / 4)) {
						tryMove(myLoc.directionTo(myHQLoc));
					} else {
						tryMove(directions[rand.nextInt(8)]);
					}
				}
			}
		}
	}
	
	private static void rally() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation invaderLoc = attackingEnemy();
		MapLocation enemy = nearestAttackableEnemyAll();
		if (enemy != null) {
			tryMove(myLoc.directionTo(enemy));
		} else {
			if (invaderLoc != null) {
				tryMove(myLoc.directionTo(invaderLoc));
			} else {
				launcherTryMove(myLoc.directionTo(mapCenter));
			}
		}
	}
	
	// TODO: replace harass with alec or josh's drone micro
	private static void harass() throws GameActionException {
		if (rc.getSupplyLevel() <= 0) {
			return;
		}
		// setup
		//System.out.println("Start with: " + Clock.getBytecodesLeft());
		//System.out.println("supply = " + rc.getSupplyLevel());
		int bytecodes = Clock.getBytecodeNum();
		int newBytecodes;
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(25, enemyTeam);
		int numEnemyTowers = enemyTowerLocs.length;
		int enemyHQRange = 24;
		if (numEnemyTowers >= 2) {
			enemyHQRange = 35;
		}
		int enemyTowerRange = 24;
		int enemyHQDamage = 24;
		if (numEnemyTowers >= 3) {
			enemyHQDamage = enemyHQDamage * 3 / 2;
		}
		if (numEnemyTowers >= 6) {
			enemyHQDamage = enemyHQDamage * 10;
		}
		int enemyTowerDamage = 8;
//		if (brandNew) {
//			leftHanded = rand.nextBoolean();
//		}
		int distX;
		int distY;
		int distSq;
		int[][] damageGrid = new int[3][3];
		boolean[][] canAttackGrid = new boolean[3][3];
		// populate grids with hq and towers
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("setup: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		MapLocation[] newEnemyTowerLocs = new MapLocation[6];
		int newNumEnemyTowers = 0;
		for (MapLocation towerLoc : enemyTowerLocs) {
			if (towerLoc.distanceSquaredTo(myLoc) <= 36) {
				newEnemyTowerLocs[newNumEnemyTowers] = towerLoc;
				newNumEnemyTowers++;
			}
		}
		for (int sourceX = -1; sourceX <= 1; sourceX++) {
			for (int sourceY = -1; sourceY <= 1; sourceY++) {
				int absSourceX = myLoc.x + sourceX;
				int absSourceY = myLoc.y + sourceY;
				if (inEnemyHQRange(new MapLocation(absSourceX, absSourceY))) {
					damageGrid[sourceX+1][sourceY+1] += enemyHQDamage;
				}
				for (int i = 0; i < newNumEnemyTowers; i++) {
					MapLocation towerLoc = newEnemyTowerLocs[i];
					distX = towerLoc.x - absSourceX;
					distY = towerLoc.y - absSourceY;
					distSq = (distX*distX) + (distY*distY);
					if (distSq <= enemyTowerRange) {
						damageGrid[sourceX+1][sourceY+1] += enemyTowerDamage;
					}
				}
			}
		}
		// populate grids with other units
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("populate with towers+hq: : " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		int counter = 0;
		for (RobotInfo ri : nearbyEnemies) {
			int targetX = ri.location.x - myLoc.x;
			int targetY = ri.location.y - myLoc.y;
			RobotType type = ri.type;
			if (type == MISSILE) {
				for (int sourceX = -1; sourceX <= 1; sourceX++) {
					for (int sourceY = -1; sourceY <= 1; sourceY++) {
						distX = targetX - sourceX;
						distY = targetY - sourceY;
						distSq = (distX*distX) + (distY*distY);
						if (distSq <= 2) {
							damageGrid[sourceX+1][sourceY+1] += 20;
						} else if (distSq <= 8) {
							damageGrid[sourceX+1][sourceY+1] += 4; // approx
						}
						if (sourceX*sourceX+sourceY*sourceY > 1) { // if non-cardinal direction
							damageGrid[sourceX+1][sourceY+1] += 4; // approx
						}
					}
				}
			} else if (type == LAUNCHER) { 
				int rangeSq = 8;
				int damage = 20;
				if (damage > 0 && rangeSq > 0) {
					for (int sourceX = -1; sourceX <= 1; sourceX++) {
						for (int sourceY = -1; sourceY <= 1; sourceY++) {
							distX = targetX - sourceX;
							distY = targetY - sourceY;
							distSq = (distX*distX) + (distY*distY);
							if (distSq <= rangeSq) {
								damageGrid[sourceX+1][sourceY+1] += damage;
							}
							if (distSq <= 10) { // myRange
								canAttackGrid[sourceX+1][sourceY+1] = true;
							}
						}
					}
				}
			} else {
				int rangeSq = type.attackRadiusSquared;
				int damage = (int)type.attackPower;
				if (damage > 0 && rangeSq > 0) {
					for (int sourceX = -1; sourceX <= 1; sourceX++) {
						for (int sourceY = -1; sourceY <= 1; sourceY++) {
							distX = targetX - sourceX;
							distY = targetY - sourceY;
							distSq = (distX*distX) + (distY*distY);
							if (distSq <= rangeSq) {
								damageGrid[sourceX+1][sourceY+1] += damage;
							}
						}
					}
				}
				if (type != COMMANDER && type != TANK && type != LAUNCHER) {
					// can attack safely
					for (int sourceX = -1; sourceX <= 1; sourceX++) {
						for (int sourceY = -1; sourceY <= 1; sourceY++) {
							distX = targetX - sourceX;
							distY = targetY - sourceY;
							distSq = (distX*distX) + (distY*distY);
							if (distSq <= 10) { // myRange
								canAttackGrid[sourceX+1][sourceY+1] = true;
							}
						}
					}
				}
			}
			counter++;
			if (Clock.getBytecodesLeft() < 2500) {
				//System.out.println(counter);
				break;
			}
		}
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("populate with " + counter + " other units: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		// select possible moves
		int[][] possibleCoords = new int[9][2];
		possibleCoords[0][0] = 0;
		possibleCoords[0][1] = 0;
		int numPossibleCoords = 1;
		for (int dirNum = 0; dirNum < 8; dirNum++) {
			Direction dir = directions[dirNum];
			if (rc.canMove(dir)) {
				possibleCoords[numPossibleCoords][0] = dir.dx;
				possibleCoords[numPossibleCoords][1] = dir.dy;
				numPossibleCoords++;
			}
		}
		/*
		for (int i = 0; i < numPossibleCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + possibleCoords[i][0], myLoc.y + possibleCoords[i][1]), 255, 0, 0);
		}
		*/
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("select possible moves: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		if (numPossibleCoords <= 1) {
			return;
		}
		// select lowest damage moves
		int[][] safestCoords = new int[9][2];
		int numSafestCoords = 0;
		int lowestDamage = Integer.MAX_VALUE;
		for (int i = 0; i < numPossibleCoords; i++) {
			int x = possibleCoords[i][0];
			int y = possibleCoords[i][1];
				int damage = damageGrid[x+1][y+1];
				if (x == 1 && y == -1) {
					rc.setIndicatorString(1,"x = " + x + " y = " + y + " damage = " + damage);
				}
				if (damage < lowestDamage) {
					safestCoords[0][0] = x;
					safestCoords[0][1] = y;
					numSafestCoords = 1;
					lowestDamage = damage;
				} else if (damage <= lowestDamage) {
					safestCoords[numSafestCoords][0] = x;
					safestCoords[numSafestCoords][1] = y;
					numSafestCoords++;
				}
		}
		/*
		for (int i = 0; i < numSafestCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + safestCoords[i][0], myLoc.y + safestCoords[i][1]), 0, 255, 0);
		}
		*/
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("select lowest damage moves: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		// select non-border moves
		int[][] betterCoords = new int[9][2];
		int numBetterCoords = 0;
		/*
		int myBorderBuffer = HARASS_BORDER_BUFFER;
		int myDistToBounds = locDistToBounds(myLoc);
		if (myDistToBounds < HARASS_BORDER_BUFFER) {
			myBorderBuffer = myDistToBounds + 1;
		}
		for (int i = 0; i < numSafestCoords; i++) {
			MapLocation loc = new MapLocation(myLoc.x + safestCoords[i][0], myLoc.y + safestCoords[i][1]);
			int distToBounds = locDistToBounds(loc);
			if (distToBounds >= myBorderBuffer) {
				betterCoords[numBetterCoords][0] = safestCoords[i][0];
				betterCoords[numBetterCoords][1] = safestCoords[i][1];
				numBetterCoords++;
			}
		}
		*/
		if (numBetterCoords <= 0) {
			betterCoords = safestCoords;
			numBetterCoords = numSafestCoords;
		}
		/*
		for (int i = 0; i < numBetterCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + betterCoords[i][0], myLoc.y + betterCoords[i][1]), 0, 0, 255);
		}
		*/
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("select non-border moves: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		// select attacking moves
		int[][] bestCoords = new int[9][2];
		int numBestCoords = 0;
		for (int i = 0; i < numBetterCoords; i++) {
			if (canAttackGrid[betterCoords[i][0]+1][betterCoords[i][1]+1]) {
				bestCoords[numBestCoords][0] = betterCoords[i][0];
				bestCoords[numBestCoords][1] = betterCoords[i][1];
				numBestCoords++;
			}
		}
		if (numBestCoords <= 0) {
			bestCoords = betterCoords;
			numBestCoords = numBetterCoords;
		}
		/*
		for (int i = 0; i < numBestCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + bestCoords[i][0], myLoc.y + bestCoords[i][1]), 255, 255, 0);
		}
		*/
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("select attacking moves: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		/*
		int[][] bestestCoords = new int[9][2];
		int numBestestCoords = 0;
		int minEnemyHQDistSq = 9999999;
		for (int i = 0; i < numBestCoords; i++) {
			MapLocation loc = new MapLocation(myLoc.x + bestCoords[i][0], myLoc.y + bestCoords[i][1]);
			int enemyHQDistSq = loc.distanceSquaredTo(enemyHQLoc);
			if (enemyHQDistSq < minEnemyHQDistSq) {
				bestestCoords[0][0] = bestCoords[i][0];
				bestestCoords[0][1] = bestCoords[i][1];
				numBestestCoords = 1;
				minEnemyHQDistSq = enemyHQDistSq;
			} else if (enemyHQDistSq <= minEnemyHQDistSq) {
				bestestCoords[numBestestCoords][0] = bestCoords[i][0];
				bestestCoords[numBestestCoords][1] = bestCoords[i][1];
				numBestestCoords++;
			}
		}
		rc.setIndicatorString(0, "here8");
		for (int i = 0; i < numBestestCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + bestestCoords[i][0], myLoc.y + bestestCoords[i][1]), 255, 0, 255);
		}
		//rc.setIndicatorString(1, "numBestestCoords = " + numBestestCoords);
		rc.setIndicatorString(2, "bestestCoords = " + bestestCoords[0][0] + " " + bestestCoords[0][1] + " " + bestestCoords[1][0] + " " + bestestCoords[1][1]);
		// guarantee numBestestCoords > 0
		int selection = rand.nextInt(numBestestCoords);
		int x = bestestCoords[selection][0];
		int y = bestestCoords[selection][1];
		Direction dir;
		if (x == -1) {
			if (y == -1) {
				dir = Direction.NORTH_WEST;
			} else if (y == 0) {
				dir = Direction.WEST;
			} else if (y == 1) {
				dir = Direction.SOUTH_WEST;
			} else {
				System.out.println("errormsg2"); return;
			}
		} else if (x == 0) {
			if (y == -1) {
				dir = Direction.NORTH;
			} else if (y == 0) {
				dir = Direction.NONE;
			} else if (y == 1) {
				dir = Direction.SOUTH;
			} else {
				System.out.println("errormsg3"); return;
			}
		} else if (x == 1) {
			if (y == -1) {
				dir = Direction.NORTH_EAST;
			} else if (y == 0) {
				dir = Direction.EAST;
			} else if (y == 1) {
				dir = Direction.SOUTH_EAST;
			} else {
				System.out.println("errormsg4"); return;
			}
		} else {
			System.out.println("errormsg1"); return;
		}
		rc.setIndicatorString(0, "here9");
		if (dir == Direction.NONE) {
			return;
		}
		rc.move(dir);
		*/
		// select moves towards target
		Direction dirToEnemyHQ = myLoc.directionTo(enemyHQLoc);
		int[] leftOffsets = {0, -1, -2, -3, -4, 1, 2, 3};
		int[] rightOffsets = {0, 1, 2, 3, 4, -1, -2, -3};
		int[] myOffsets;
		if (leftHanded) {
			myOffsets = leftOffsets;
		} else {
			myOffsets = rightOffsets;
		}
		int dirNum = directionToInt(dirToEnemyHQ);
		Direction testDir = dirToEnemyHQ;
		int i;
		for (i = 0; i < 8; i++) {
			int testDirNum = (dirNum + myOffsets[i] + 8) % 8;
			testDir = directions[testDirNum];
			int x = testDir.dx;
			int y = testDir.dy;
			boolean found = false;
			for (int j = 0; j < numBestCoords; j++) {
				if (x == bestCoords[j][0] && y == bestCoords[j][1]) {
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("select moves towards target moves: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
		if (i < 8) {
			rc.move(testDir);;
		}
		if (i > 3) {
			leftHanded = !leftHanded;
		}
//		brandNew = false;
		newBytecodes = Clock.getBytecodeNum();
		//System.out.println("move and cleanup: " + (newBytecodes - bytecodes));
		bytecodes = newBytecodes;
	}
	
	// TODO: generalize inMyHQRange, inEnemyHQRange, and inEnemyBuildingRange to reduce duplicate code
	private static boolean inMyHQRange(MapLocation loc) {
		MapLocation myHQ = myHQLoc;
		int numTowers = myTowerLocs.length;
		int HQRange = 24;
		if (numTowers >= 2) {
			HQRange = 35;
		}
		int dist = loc.distanceSquaredTo(myHQ);
		if (dist <= HQRange) {
			return true;
		}
		if (numTowers >= 5) {
			if (dist <= 48) {
				return true;
			} else if (dist <= 52) {
				int x = Math.abs(myHQ.x - loc.x);
				int y = Math.abs(myHQ.y - loc.y);
				if (y < x) {
					int temp = x;
					x = y;
					y = temp;
				}
				if (x > 2) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean inEnemyHQRange(MapLocation loc) {
		MapLocation enemyHQ = enemyHQLoc;
		int numTowers = enemyTowerLocs.length;
		int HQRange = 24;
		if (numTowers >= 2) {
			HQRange = 35;
		}
		int dist = loc.distanceSquaredTo(enemyHQ);
		if (dist <= HQRange) {
			return true;
		}
		if (numTowers >= 5) {
			if (dist <= 48) {
				return true;
			} else if (dist <= 52) {
				int x = Math.abs(enemyHQ.x - loc.x);
				int y = Math.abs(enemyHQ.y - loc.y);
				if (y < x) {
					int temp = x;
					x = y;
					y = temp;
				}
				if (x > 2) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean inEnemyBuildingRange(MapLocation loc) {
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		MapLocation enemyHQ = enemyHQLoc;
		int numTowers = enemyTowers.length;
		int HQRange = 24;
		if (numTowers >= 2) {
			HQRange = 35;
		}
		int towerRange = 24;
		// hq:
		int dist = loc.distanceSquaredTo(enemyHQ);
		if (dist <= HQRange) {
			return true;
		}
		if (numTowers >= 5) {
			if (dist <= 48) {
				return true;
			} else if (dist <= 52) {
				int x = Math.abs(enemyHQ.x - loc.x);
				int y = Math.abs(enemyHQ.y - loc.y);
				if (y < x) {
					int temp = x;
					x = y;
					y = temp;
				}
				if (x > 2) {
					return true;
				}
			}
		}
		for (MapLocation towerLoc : enemyTowers) {
			if (loc.distanceSquaredTo(towerLoc) <= towerRange) {
				return true;
			}
		}
		return false;
	}
	
	// TODO: replace shouldIAttack with alec's stuff
	private static boolean shouldIAttack(){
		RobotInfo[] nearbyUnits = rc.senseNearbyRobots(myType.sensorRadiusSquared);
		double sumHealth = 0;
		for(RobotInfo r: nearbyUnits){
			if(r.type == DRONE){
				if(r.team == myTeam)
					sumHealth += r.health;
				else
					sumHealth -= r.health;
			}
		}
		if(sumHealth >= 0)
			return true;
		return false;
	}

	private static MapLocation nearestSensedEnemy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(mySensorRangeSq, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type != MISSILE) {
				MapLocation enemyLoc = r.location;
				int dist = myLoc.distanceSquaredTo(enemyLoc);
				if (dist < closestDist) {
					closestDist = dist;
					closestRobot = r;
				}
			}
		}
		if (closestRobot != null) {
			return closestRobot.location;
		}
		return null;
	}
	
	private static MapLocation nearestAttackableEnemyAll() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myAttackRangeSq, enemyTeam);
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
	
	private static MapLocation fastNearestEnemy() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(24, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		int i = 0;
		for (RobotInfo r : enemies) {
			if (r.type != MISSILE) {
				MapLocation enemyLoc = r.location;
				int dist = myLoc.distanceSquaredTo(enemyLoc);
				if (dist < closestDist) {
					closestDist = dist;
					closestRobot = r;
				}
			}
			i++;
			if (i >= 1) {
				break;
			}
		}
		if (closestRobot != null) {
			return closestRobot.location;
		}
		return null;
	}
	
	// TODO: should sense all invading enemies and let units attack the closest
	private static MapLocation attackingEnemy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myHQLoc, 9999999, enemyTeam);
		int closestDist = 9999999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type != MISSILE && r.type != MINER && r.type != BEAVER) {
				MapLocation enemyLoc = r.location;
				int dist = myHQLoc.distanceSquaredTo(enemyLoc);
				if (dist < closestDist) {
					closestDist = dist;
					closestRobot = r;
				}
			}
		}
		if (closestRobot != null) {
			return closestRobot.location;
		}
		return null;
	}
	
	// combine both versions of lookForBounds to eliminate duplicated code
	private static void lookForBounds(MapLocation myLoc, int rangeSq) throws GameActionException {
		int range = (int)Math.sqrt(rangeSq);
		Direction[] myDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		int[] knownBounds = {
			rc.readBroadcast(NORTH_BOUND_CHAN),
			rc.readBroadcast(EAST_BOUND_CHAN),
			rc.readBroadcast(SOUTH_BOUND_CHAN),
			rc.readBroadcast(WEST_BOUND_CHAN)
		};
		for (int dirNum = 0; dirNum < 4; dirNum++) {
			Direction dir = myDirections[dirNum];
			int bound = knownBounds[dirNum];
			if (bound == NO_BOUND) {
				MapLocation testLoc = myLoc.add(dir, range);
				if (rc.senseTerrainTile(testLoc) == TerrainTile.OFF_MAP) {
					do {
						testLoc = testLoc.add(dir.opposite());
					} while (rc.senseTerrainTile(testLoc) == TerrainTile.OFF_MAP && !testLoc.equals(myLoc));
					if (dirNum == 0) {
						// y direction
						rc.broadcast(NORTH_BOUND_CHAN, testLoc.y);
					} else if (dirNum == 1) {
						// x direction
						rc.broadcast(EAST_BOUND_CHAN, testLoc.x);
					} else if (dirNum == 2) {
						// y direction
						rc.broadcast(SOUTH_BOUND_CHAN, testLoc.y);
					} else if (dirNum == 3) {
						// x direction
						rc.broadcast(WEST_BOUND_CHAN, testLoc.x);
					}
				}
			}
		}
	}
	
	private static void lookForBounds() throws GameActionException {
		int range = (int)Math.sqrt(myType.sensorRadiusSquared);
		Direction[] myDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		int[] knownBounds = {
			rc.readBroadcast(NORTH_BOUND_CHAN),
			rc.readBroadcast(EAST_BOUND_CHAN),
			rc.readBroadcast(SOUTH_BOUND_CHAN),
			rc.readBroadcast(WEST_BOUND_CHAN)
		};
		for (int dirNum = 0; dirNum < 4; dirNum++) {
			Direction dir = myDirections[dirNum];
			int bound = knownBounds[dirNum];
			if (bound == NO_BOUND) {
				MapLocation testLoc = myLoc.add(dir, range);
				if (rc.senseTerrainTile(testLoc) == TerrainTile.OFF_MAP) {
					do {
						testLoc = testLoc.add(dir.opposite());
					} while (rc.senseTerrainTile(testLoc) == TerrainTile.OFF_MAP && !testLoc.equals(myLoc));
					if (dirNum == 0) {
						// y direction
						rc.broadcast(NORTH_BOUND_CHAN, testLoc.y);
					} else if (dirNum == 1) {
						// x direction
						rc.broadcast(EAST_BOUND_CHAN, testLoc.x);
					} else if (dirNum == 2) {
						// y direction
						rc.broadcast(SOUTH_BOUND_CHAN, testLoc.y);
					} else if (dirNum == 3) {
						// x direction
						rc.broadcast(WEST_BOUND_CHAN, testLoc.x);
					}
				}
			}
		}
	}
	
	// TODO: replace with josh's drone micro
	private static MapLocation droneAttackLocation() {
		RobotInfo[] myEnemies = rc.senseNearbyRobots(myAttackRangeSq, enemyTeam);
		boolean foundLauncher = false;
		boolean foundMissile = false;
		RobotInfo bestEnemyTarget = null;
		int minDistSq = 9999999;
		for (RobotInfo ri : myEnemies) {
			RobotType type = ri.type;
			int distSq = ri.location.distanceSquaredTo(myLoc);
			if (foundLauncher) {
				if (type == LAUNCHER) {
					if (distSq < minDistSq) {
						bestEnemyTarget = ri;
						minDistSq = distSq;
					}
				}
			} else if (foundMissile) {
				if (type == LAUNCHER) {
					foundLauncher = true;
					bestEnemyTarget = ri;
					minDistSq = distSq;
				} else if (type == MISSILE) {
					if (distSq < minDistSq) {
						bestEnemyTarget = ri;
						minDistSq = distSq;
					}
				}
			} else {
				if (type == LAUNCHER) {
					foundLauncher = true;
					bestEnemyTarget = ri;
					minDistSq = distSq;
				} else if (type == MISSILE) {
					foundMissile = true;
					bestEnemyTarget = ri;
					minDistSq = distSq;
				} else {
					if (distSq < minDistSq) {
						bestEnemyTarget = ri;
						minDistSq = distSq;
					}
				}
			}
		}
		if (bestEnemyTarget != null) {
			return bestEnemyTarget.location;
		} else {
			return null;
		}
	}

	
	//attacks a nearby enemy with the least health
	private static void focusAttackEnemies() throws GameActionException {
		// attack the unit with the least health
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, enemyTeam);
		if (enemies.length == 0) {
			return;
		}

		RobotInfo targetEnemy = enemies[0];
		for (RobotInfo i: enemies) {
			if (i.type == RobotType.TOWER)
				if (i.health < targetEnemy.health) {
					targetEnemy = i;
				}
		}
		rc.attackLocation(targetEnemy.location);
	}
		
	private static void attackSomething() throws GameActionException {
		MapLocation enemy = nearestAttackableEnemyAll();
		if (enemy != null) {
			rc.attackLocation(enemy);
		}
		
	}
	
	private static void HQAttackSomething() throws GameActionException {
		int HQRange = 24;
		if (myTowerLocs.length >= 2) {
			HQRange = 35;
		}
		RobotInfo[] enemies = rc.senseNearbyRobots(52, enemyTeam);
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
			if (closestDist <= HQRange) {
				rc.attackLocation(closestRobot.location);
			} else if (inMyHQRange(closestRobot.location)) {
				rc.attackLocation(closestRobot.location.add(closestRobot.location.directionTo(myLoc)));
			}
			
		}
		
	}
	
	// TODO: rewrite tryMove, quickTryMove and LauncherTryMove to be faster, better names
	private static void quickTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int dirint = directionToInt(d);
		while (offsetIndex < 3 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 3) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}
	
	private static boolean tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	private static boolean launcherTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			Direction dir = directions[(dirint+offsets[offsetIndex]+8)%8];
			if (!inEnemyBuildingRange(rc.getLocation().add(dir))) {
				rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			}
			return true;
		}
		return false;
	}
	
	private static boolean limitedTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1};
		int dirint = directionToInt(d);
		while (offsetIndex < 3 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 3) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	private static boolean tryMoveLeft(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,-1,-2,-3};
		int dirint = directionToInt(d);
		while (offsetIndex < 4 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 4) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	private static boolean tryMoveRight(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,2,3};
		int dirint = directionToInt(d);
		while (offsetIndex < 4 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 4) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	private static boolean tryLaunch(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		while (offsetIndex < 5 && !rc.canLaunch(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.launchMissile(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	private static boolean trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
			return true;
		}
		return false;
	}
	
	private static boolean tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
			return true;
		}
		return false;
	}
	
	private static MapLocation[] findTowerChain() {
		// does not consider hq
		MapLocation[] startingFoundTowers = new MapLocation[myTowerLocs.length + 2];
		MapLocation[] foundTowers;
		for (MapLocation startTower : myTowerLocs) {
			MapLocation[] possibleEdges = MapLocation.getAllMapLocationsWithinRadiusSq(startTower, 35);
			boolean bordersEdge = false;
			for (int i = 0; i < possibleEdges.length; i++) {
				if (rc.senseTerrainTile((possibleEdges[i])) == TerrainTile.OFF_MAP) {
					bordersEdge = true;
					break;
				}
			}
			if (bordersEdge) {
				startingFoundTowers[0] = startTower;
				foundTowers = findTowerChainStartingAt(startingFoundTowers, 1);
				if (foundTowers != null) {
					for (int numFoundTowers = 1; numFoundTowers < foundTowers.length; numFoundTowers++) {
						if (foundTowers[numFoundTowers] == null) {
							possibleEdges = MapLocation.getAllMapLocationsWithinRadiusSq(foundTowers[numFoundTowers-1], 35);
							bordersEdge = false;
							for (int i = 0; i < possibleEdges.length; i++) {
								if (rc.senseTerrainTile((possibleEdges[i])) == TerrainTile.OFF_MAP) {
									bordersEdge = true;
									break;
								}
							}
							if (bordersEdge) {
								return foundTowers;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private static MapLocation[] findTowerChainStartingAt(MapLocation[] foundTowers, int numFoundTowers) {
		for (MapLocation tower : myTowerLocs) {
			// ensure not already found
			boolean alreadyFound = false;
			for (int i = 0; i < numFoundTowers; i++) {
				if (tower == foundTowers[i]) {
					alreadyFound = true;
				}
			}
			if (!alreadyFound) {
				MapLocation prevTower;
				if (numFoundTowers > 0) {
					prevTower = foundTowers[numFoundTowers - 1];
				} else {
					// should not happen!
					prevTower = myTowerLocs[0];
				}
				if (prevTower.distanceSquaredTo(tower) <= 80) { // i think?
					foundTowers[numFoundTowers] = tower;
					numFoundTowers++;
					return findTowerChainStartingAt(foundTowers, numFoundTowers);
				}
			}
		}
		return null;
	}
	
	private static int HQEffectiveRangeSq() { // approximate for splash
		int numTowers = rc.senseTowerLocations().length;
		int HQRange = 24;
		if (numTowers >= 2) {
			HQRange = 35;
		} else if (numTowers >= 5) {
			HQRange = 52;
		}
		return HQRange;
	}

	private static int directionToInt(Direction d) {
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

	private static Direction intToDirection(int num) {
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
