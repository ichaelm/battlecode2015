package mainbot_swarm2;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	
	private static final int ROUND_TO_BUILD_LAUNCHERS = 100;

	// AI parameters
	private static final int RUSH_TURNS_LEFT = 500;
	private static final int ALL_OUT_RUSH_TURNS_LEFT = 300;

	// Game parameters
	private static final int MAX_MAP_WIDTH = 120;
	private static final int MAX_MAP_HEIGHT = 120;

	// Broadcast channels
	private static final int NUM_CHANNELS = 65536;
	private static final int CENSUS_CHAN = 0;
	private static final int CENSUS_SIZE = 21;
	private static final int HQ_ROUNDNUM_CHAN = CENSUS_CHAN + CENSUS_SIZE;
	private static final int PROGRESS_TABLE_CHAN = HQ_ROUNDNUM_CHAN + 1;
	private static final int PROGRESS_TABLE_SIZE = 21;
	private static final int COMPLETED_TABLE_CHAN = PROGRESS_TABLE_CHAN + PROGRESS_TABLE_SIZE;
	private static final int COMPLETED_TABLE_SIZE = 21;
	private static final int BUILD_QUEUE_CHAN = COMPLETED_TABLE_CHAN + COMPLETED_TABLE_SIZE;
	private static final int BUILD_QUEUE_ROW_SIZE = 2;
	private static final int BUILD_QUEUE_NUM_ROWS = 50;
	private static final int BUILD_QUEUE_SIZE = BUILD_QUEUE_ROW_SIZE * BUILD_QUEUE_NUM_ROWS;
	private static final int MINING_TABLE_CHAN = BUILD_QUEUE_CHAN + BUILD_QUEUE_SIZE;
	private static final int MINING_TABLE_ROW_SIZE = 6;
	private static final int MINING_TABLE_NUM_ROWS = 20;
	private static final int MINING_TABLE_SIZE = MINING_TABLE_ROW_SIZE * MINING_TABLE_NUM_ROWS;
	private static final int MINING_TABLE_CURRENT_NUMROWS_CHAN = MINING_TABLE_CHAN + MINING_TABLE_SIZE;
	private static final int MONTE_CARLO_RESULTS_TABLE_CHAN = MINING_TABLE_CURRENT_NUMROWS_CHAN + 1;
	private static final int MONTE_CARLO_RESULTS_TABLE_ROW_SIZE = 3;
	private static final int MONTE_CARLO_RESULTS_TABLE_NUM_ROWS = 50;
	private static final int MONTE_CARLO_RESULTS_TABLE_SIZE = MONTE_CARLO_RESULTS_TABLE_ROW_SIZE * MONTE_CARLO_RESULTS_TABLE_NUM_ROWS;
	private static final int MONTE_CARLO_NUM_RESULTS_CHAN = MONTE_CARLO_RESULTS_TABLE_CHAN + MONTE_CARLO_RESULTS_TABLE_SIZE;
	private static final int MONTE_CARLO_MAX_FOUND_CHAN = MONTE_CARLO_NUM_RESULTS_CHAN + 1;
	private static final int NAVMAP_CHAN = MONTE_CARLO_MAX_FOUND_CHAN + 1;
	private static final int NAVMAP_SQUARE_SIZE = 4;
	private static final int NAVMAP_WIDTH = MAX_MAP_WIDTH;
	private static final int NAVMAP_HEIGHT = MAX_MAP_HEIGHT;
	private static final int NAVMAP_SIZE = NAVMAP_SQUARE_SIZE * NAVMAP_WIDTH * NAVMAP_HEIGHT;
	private static final int NAVQUEUE_CHAN = NAVMAP_CHAN + NAVMAP_SIZE;
	private static final int NAVQUEUE_QUEUE_SIZE = 500;
	private static final int NAVQUEUE_NUM_QUEUES = 7;
	private static final int NAVQUEUE_SIZE = NAVQUEUE_QUEUE_SIZE * NAVQUEUE_NUM_QUEUES;
	private static final int NAVQUEUE_STARTPTR_CHAN = NAVQUEUE_CHAN + NAVQUEUE_SIZE;
	private static final int NAVQUEUE_STARTPTR_SIZE = NAVQUEUE_NUM_QUEUES;
	private static final int NAVQUEUE_ENDPTR_CHAN = NAVQUEUE_STARTPTR_CHAN + NAVQUEUE_STARTPTR_SIZE;
	private static final int NAVQUEUE_ENDPTR_SIZE = NAVQUEUE_NUM_QUEUES;
	private static final int NAVMAP_NUM_WAYPOINTS_CHAN = NAVQUEUE_ENDPTR_CHAN + NAVQUEUE_ENDPTR_SIZE;
	private static final int NAVMAP_WAYPOINTS_CHAN = NAVMAP_NUM_WAYPOINTS_CHAN + 1;
	private static final int NAVMAP_WAYPOINTS_SIZE = NAVQUEUE_NUM_QUEUES;
	private static final int NAVMAP_STILL_PATHFINDING_CHAN = NAVMAP_WAYPOINTS_CHAN + NAVMAP_WAYPOINTS_SIZE;
	private static final int NAVMAP_STILL_PATHFINDING_SIZE = NAVQUEUE_NUM_QUEUES;
	private static final int NAVMAP_SYMMETRY_LOCS_CHAN = NAVMAP_STILL_PATHFINDING_CHAN + NAVMAP_STILL_PATHFINDING_SIZE;
	private static final int NAVMAP_SYMMETRY_LOCS_SIZE = NAVQUEUE_NUM_QUEUES;
	private static final int NAVMAP_SYMMETRY_COSTS_CHAN = NAVMAP_SYMMETRY_LOCS_CHAN + NAVMAP_SYMMETRY_LOCS_SIZE;
	private static final int NAVMAP_SYMMETRY_COSTS_SIZE = NAVQUEUE_NUM_QUEUES;
	private static final int NORTH_BOUND_CHAN = NAVMAP_SYMMETRY_COSTS_CHAN + NAVMAP_SYMMETRY_COSTS_SIZE;
	private static final int EAST_BOUND_CHAN = NORTH_BOUND_CHAN + 1;
	private static final int SOUTH_BOUND_CHAN = EAST_BOUND_CHAN + 1;
	private static final int WEST_BOUND_CHAN = SOUTH_BOUND_CHAN + 1;
	private static final int NORTH_FARTHEST_CHAN = WEST_BOUND_CHAN + 1;
	private static final int EAST_FARTHEST_CHAN = NORTH_FARTHEST_CHAN + 1;
	private static final int SOUTH_FARTHEST_CHAN = EAST_FARTHEST_CHAN + 1;
	private static final int WEST_FARTHEST_CHAN = SOUTH_FARTHEST_CHAN + 1;
	private static final int MINER_ORE_COUNTER_CHAN = WEST_FARTHEST_CHAN + 1;
	private static final int BEAVER_ORE_COUNTER_CHAN = MINER_ORE_COUNTER_CHAN + 1;
	private static final int BUILDER_BEAVER_COUNTER_CHAN = BEAVER_ORE_COUNTER_CHAN + 1;
	private static final int BUILDER_BEAVER_REQUEST_CHAN = BUILDER_BEAVER_COUNTER_CHAN + 1;
	private static final int UNIT_ORDER_CHAN = BUILDER_BEAVER_REQUEST_CHAN + 1;
	private static final int UNIT_TOWER_DEFENSE_CHAN = UNIT_ORDER_CHAN + 1;
	private static final int UNIT_NEEDS_SUPPLY_X_CHAN = UNIT_TOWER_DEFENSE_CHAN + 1;
	private static final int UNIT_NEEDS_SUPPLY_Y_CHAN = UNIT_NEEDS_SUPPLY_X_CHAN + 1;
	private static final int TOWER_ASSIGN_NUM_CHAN = UNIT_NEEDS_SUPPLY_Y_CHAN + 1;
	private static final int SYMMETRY_TYPE_CHAN = TOWER_ASSIGN_NUM_CHAN + 1;
	private static final int UNKNOWN_QUEUE_CHAN = SYMMETRY_TYPE_CHAN + 1;
	private static final int UNKNOWN_QUEUE_SIZE = 1000;
	private static final int UNKNOWN_QUEUE_STARTPTR_CHAN = UNKNOWN_QUEUE_CHAN + UNKNOWN_QUEUE_SIZE;
	private static final int UNKNOWN_QUEUE_ENDPTR_CHAN = UNKNOWN_QUEUE_STARTPTR_CHAN + 1;
	private static final int RALLY_POINT_NUM_CHAN = UNKNOWN_QUEUE_ENDPTR_CHAN + 1;
	private static final int RALLY_POINT_CHAN = RALLY_POINT_NUM_CHAN + 1;
	private static final int RALLY_POINT_SIZE = 2;
	private static final int SAFETY_RADIUS_SQ_CHAN = RALLY_POINT_CHAN + RALLY_POINT_SIZE;
	
	//Swarm estimates
	private static final int SWARM_ONE_LOCATION = SAFETY_RADIUS_SQ_CHAN + 1;
	private static final int SWARM_ONE_SOLDIERS = SWARM_ONE_LOCATION + 1;
	private static final int SWARM_ONE_BASHERS = SWARM_ONE_SOLDIERS + 1;
	private static final int SWARM_ONE_TANKS = SWARM_ONE_BASHERS + 1;
	private static final int SWARM_ONE_DRONES = SWARM_ONE_TANKS + 1;
	private static final int SWARM_ONE_LAUNCHERS = SWARM_ONE_DRONES + 1;
	private static final int SWARM_ONE_COMMANDER = SWARM_ONE_LAUNCHERS + 1;

	private static final int SWARM_TWO_LOCATION = SWARM_ONE_COMMANDER + 1;
	private static final int SWARM_TWO_SOLDIERS = SWARM_TWO_LOCATION + 1;
	private static final int SWARM_TWO_BASHERS = SWARM_TWO_SOLDIERS + 1;
	private static final int SWARM_TWO_TANKS = SWARM_TWO_BASHERS + 1;
	private static final int SWARM_TWO_DRONES = SWARM_TWO_TANKS + 1;
	private static final int SWARM_TWO_LAUNCHERS = SWARM_TWO_DRONES + 1;
	private static final int SWARM_TWO_COMMANDER = SWARM_TWO_LAUNCHERS + 1;
	
	private static final int SWARM_THREE_LOCATION = SWARM_TWO_COMMANDER + 1;
	private static final int SWARM_THREE_SOLDIERS = SWARM_THREE_LOCATION + 1;
	private static final int SWARM_THREE_BASHERS = SWARM_THREE_SOLDIERS + 1;
	private static final int SWARM_THREE_TANKS = SWARM_THREE_BASHERS + 1;
	private static final int SWARM_THREE_DRONES = SWARM_THREE_TANKS + 1;
	private static final int SWARM_THREE_LAUNCHERS = SWARM_THREE_DRONES + 1;
	private static final int SWARM_THREE_COMMANDER = SWARM_THREE_LAUNCHERS + 1;
	
	// Broadcast signaling constants
	private static final int NO_BOUND = 32000;
	private static final int UNIT_ORDER_ATTACK_TOWERS = 1;
	private static final int UNIT_ORDER_DEFEND = 2;
	private static final int UNIT_ORDER_RALLY = 3;
	private static final int UNIT_ORDER_ATTACK_VULNERABLE_TOWER = 4;
	private static final int SYMMETRY_TYPE_NONE = 0;
	private static final int SYMMETRY_TYPE_ROTATIONAL = 1;
	private static final int SYMMETRY_TYPE_HORIZONTAL = 2;
	private static final int SYMMETRY_TYPE_VERTICAL = 3;
	private static final int SYMMETRY_TYPE_DIAGONAL_POS = 4;
	private static final int SYMMETRY_TYPE_DIAGONAL_NEG = 5;

	// cached enums for brevity
	private static RobotType HQ;
	private static RobotType TOWER;
	private static RobotType SUPPLYDEPOT;
	private static RobotType TECHNOLOGYINSTITUTE;
	private static RobotType BARRACKS;
	private static RobotType HELIPAD;
	private static RobotType TRAININGFIELD;
	private static RobotType TANKFACTORY;
	private static RobotType MINERFACTORY;
	private static RobotType HANDWASHSTATION;
	private static RobotType AEROSPACELAB;
	private static RobotType BEAVER;
	private static RobotType COMPUTER;
	private static RobotType SOLDIER;
	private static RobotType BASHER;
	private static RobotType MINER;
	private static RobotType DRONE;
	private static RobotType TANK;
	private static RobotType COMMANDER;
	private static RobotType LAUNCHER;
	private static RobotType MISSILE;

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
	private static boolean mining;
	private static int mineCounter;
	private static RobotInfo[] enemyRobots;
	private static int[][] buildQueue;
	private static int row;
	private static int[] numRobotsByType;
	private static int[] numInProgressByType;
	private static int[] numCompletedByType;
	private static int buildingParity;
	private static int bugNavWinding;
	private static int bugNavFallTimes = 0;
	private static MapLocation navTargetLoc;
	private static MapLocation navSourceLoc;
	private static int navType;
	private static int navClosestDistSq;
	private static final int NAVTYPE_SIMPLE = 1;
	private static final int NAVTYPE_BUG = 2;
	private static final int NAVTYPE_PRECOMP = 3;
	private static int maxNumBytecodes = 0;
	private static MapLocation[] foundPath = null;
	private static int foundPathIndex = 0;
	private static double enemyHQHealth = 2000;
	private static MapLocation[] originalEnemyTowerLocs;
	private static double[] enemyTowerHealths = null;

	// should be final, but can't because set in run()
	private static Direction[] directions;
	private static RobotType[] robotTypes;
	private static double[] oreConsumptionByType;

	public static void run(RobotController myrc) {
		// Initialize cached game information
		System.out.println(SWARM_THREE_COMMANDER);
		rc = myrc;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		enemyHQLoc = rc.senseEnemyHQLocation();
		if (myrc.getType() == RobotType.MISSILE) { //hack for missiles to run faster
			runMissile();
		}
		robotTypes = RobotType.values();
		directions = Direction.values(); // happens to be {NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST, NONE, OMNI}
		HQ = RobotType.HQ;
		TOWER = RobotType.TOWER;
		SUPPLYDEPOT = RobotType.SUPPLYDEPOT;
		TECHNOLOGYINSTITUTE = RobotType.TECHNOLOGYINSTITUTE;
		BARRACKS = RobotType.BARRACKS;
		HELIPAD = RobotType.HELIPAD;
		TRAININGFIELD = RobotType.TRAININGFIELD;
		TANKFACTORY = RobotType.TANKFACTORY;
		MINERFACTORY = RobotType.MINERFACTORY;
		HANDWASHSTATION = RobotType.HANDWASHSTATION;
		AEROSPACELAB = RobotType.AEROSPACELAB;
		BEAVER = RobotType.BEAVER;
		COMPUTER = RobotType.COMPUTER;
		SOLDIER = RobotType.SOLDIER;
		BASHER = RobotType.BASHER;
		MINER = RobotType.MINER;
		DRONE = RobotType.DRONE;
		TANK = RobotType.TANK;
		COMMANDER = RobotType.COMMANDER;
		LAUNCHER = RobotType.LAUNCHER;
		MISSILE = RobotType.MISSILE;
		
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
		buildingParity = (((myHQLoc.x + myHQLoc.y) % 2) + 2) % 2;
		bugNavWinding = 0;
		leftHanded = rand.nextBoolean();
		navClosestDistSq = 0;

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
		
		boolean builtADrone = false;

		// information stored across rounds
		double[] oreMinedByTurn = new double[10];
		double[] oreMinedByTurnByMiners = new double[10];
		double[] oreMinedByTurnByBeavers = new double[10];
		int[] oldNumRobotsByType = new int[21];
		int selfSwarmTimer = 0;

		// turn 1 code
		try {
			// initialize bounds channels
			initBounds();
			
			// update locations
			updateLocations();
			
			enemyTowerHealths = new double[enemyTowerLocs.length];
			
			originalEnemyTowerLocs = new MapLocation[enemyTowerLocs.length];
			for (int i = 0; i < enemyTowerLocs.length; i++) {
				originalEnemyTowerLocs[i] = enemyTowerLocs[i];
			}
			
			for (int i = 0; i < myTowerLocs.length + 1; i++) {
				rc.broadcast(NAVMAP_SYMMETRY_LOCS_CHAN + i, packLocation(new MapLocation(NO_BOUND, NO_BOUND)));
			}
			
			initializePathfindingQueues();
			
			initializeRallyLocs();
			
			// TODO: better mining strategy picking
			//double orePerSquare = rc.senseOre(myHQLoc); // approximate
			//int turnToMaximize = 300 + 2*rushDist + 100*rc.senseTowerLocations().length;
			// forget about rush safety for now, just go long term
			
			
			
			//**********************************
			
			// decisions
			// determine symmetry
			boolean couldBeHorizontal = true;
			boolean couldBeVertical = true;
			boolean couldBeDiagonalPos = true;
			boolean couldBeDiagonalNeg = true;
			boolean couldBeRotational = true;
			int numPossible = 5;
			int rushX = enemyHQLoc.x - myHQLoc.x;
			int rushY = enemyHQLoc.y - myHQLoc.y;
			if (rushX != 0) {
				couldBeHorizontal = false;
				numPossible--;
			}
			if (rushY != 0) {
				couldBeVertical = false;
				numPossible--;
			}
			if (rushX != rushY) {
				couldBeDiagonalNeg = false;
				numPossible--;
			}
			if (rushX != -rushY) {
				couldBeDiagonalPos = false;
				numPossible--;
			}
			for (int i = 0; i < myTowerLocs.length; i++) {
				MapLocation myTowerLoc = myTowerLocs[i];
				int relX = myTowerLoc.x - myHQLoc.x;
				int relY = myTowerLoc.y - myHQLoc.y;
				MapLocation expectedLoc;
				if (couldBeHorizontal) {
					expectedLoc = new MapLocation(enemyHQLoc.x + relX, enemyHQLoc.y - relY);
					if (!isEnemyTowerLoc(expectedLoc)) {
						couldBeHorizontal = false;
						numPossible--;
					}
				}
				if (couldBeVertical) {
					expectedLoc = new MapLocation(enemyHQLoc.x - relX, enemyHQLoc.y + relY);
					if (!isEnemyTowerLoc(expectedLoc)) {
						couldBeVertical = false;
						numPossible--;
					}
				}
				if (couldBeDiagonalPos) {
					expectedLoc = new MapLocation(enemyHQLoc.x + relY, enemyHQLoc.y + relX);
					if (!isEnemyTowerLoc(expectedLoc)) {
						couldBeDiagonalPos = false;
						numPossible--;
					}
				}
				if (couldBeDiagonalNeg) {
					expectedLoc = new MapLocation(enemyHQLoc.x - relY, enemyHQLoc.y - relX);
					if (!isEnemyTowerLoc(expectedLoc)) {
						couldBeDiagonalNeg = false;
						numPossible--;
					}
				}
				if (couldBeRotational) {
					expectedLoc = new MapLocation(enemyHQLoc.x - relX, enemyHQLoc.y - relY);
					if (!isEnemyTowerLoc(expectedLoc)) {
						couldBeRotational = false;
						numPossible--;
					}
				}
				if (numPossible <= 1) {
					break;
				}
			}
			Direction symmetryDirection;
			if (couldBeRotational) {
				System.out.println("rotational symmetry");
				symmetryDirection = Direction.OMNI;
				rc.broadcast(SYMMETRY_TYPE_CHAN, SYMMETRY_TYPE_ROTATIONAL);
			} else if (couldBeHorizontal) {
				System.out.println("horizontal symmetry");
				symmetryDirection = Direction.EAST;
				rc.broadcast(SYMMETRY_TYPE_CHAN, SYMMETRY_TYPE_HORIZONTAL);
			} else if (couldBeVertical) {
				System.out.println("vertical symmetry");
				symmetryDirection = Direction.SOUTH;
				rc.broadcast(SYMMETRY_TYPE_CHAN, SYMMETRY_TYPE_VERTICAL);
			} else if (couldBeDiagonalPos) {
				System.out.println("diagonal pos symmetry");
				symmetryDirection = Direction.SOUTH_EAST;
				rc.broadcast(SYMMETRY_TYPE_CHAN, SYMMETRY_TYPE_DIAGONAL_POS);
			} else if (couldBeDiagonalNeg) {
				System.out.println("diagonal neg symmetry");
				symmetryDirection = Direction.NORTH_EAST;
				rc.broadcast(SYMMETRY_TYPE_CHAN, SYMMETRY_TYPE_DIAGONAL_NEG);
			} else {
				// never happen
				System.out.println("bad symmetry!");
				symmetryDirection = null;
			}
			
			
			//***********************************
			
			
			
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

				// look for map boundaries
				lookForBounds();

				bytecodes[2] = Clock.getBytecodeNum();

				// read unit census, progress table, and completed table
				numRobotsByType = readCensus();
				numInProgressByType = readProgressTable();
				numCompletedByType = readCompletedTable();
				int numInProgress = 0;
				for (int i = 21; --i >= 0;) {
					numInProgress += numInProgressByType[i];
				}

				bytecodes[3] = Clock.getBytecodeNum();

				//resetting the defense channel
				rc.broadcast(UNIT_TOWER_DEFENSE_CHAN, 0);

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
				if (numBuilderBeavers <= numInProgress) {
					buildQueue[row][0] = BEAVER.ordinal();
					buildQueue[row][1] = 1;
					row++;
					requestBuilderBeaver();
				}

				bytecodes[13] = Clock.getBytecodeNum();

				if (numRobotsByType[MINERFACTORY.ordinal()] + numInProgressByType[MINERFACTORY.ordinal()] < 1) {
					buildQueue[row][0] = MINERFACTORY.ordinal();
					buildQueue[row][1] = 1;
					row++;
				} else {
					//int breakEvenRounds = (int)Math.ceil(MINER.oreCost / oreMinedPerMiner) + MINER.buildTurns;
					if (numRobotsByType[MINER.ordinal()] < 30 && roundNum < 1000) {
						buildQueue[row][0] = MINER.ordinal();
						buildQueue[row][1] = 1;
						row++;
					} else if (numRobotsByType[MINER.ordinal()] < 15 && rc.getRoundLimit()-Clock.getRoundNum() > 500) {
						buildQueue[row][0] = MINER.ordinal();
						buildQueue[row][1] = 1;
						row++;
					}
				}

				bytecodes[14] = Clock.getBytecodeNum();

				if (totalSupplyUpkeep > totalSupplyGeneration) {
					buildQueue[row][0] = SUPPLYDEPOT.ordinal();
					buildQueue[row][1] = 1;
					row++;
				}

				bytecodes[15] = Clock.getBytecodeNum();

				//updating unit counts
				int numLaunchers = numRobotsByType[LAUNCHER.ordinal()] + numInProgressByType[LAUNCHER.ordinal()];
				int numDrones = numRobotsByType[DRONE.ordinal()] + numInProgressByType[DRONE.ordinal()];
				int numTanks = numRobotsByType[TANK.ordinal()] + numInProgressByType[TANK.ordinal()];
				int numSoldiers = numRobotsByType[SOLDIER.ordinal()] + numInProgressByType[SOLDIER.ordinal()];
				int numUnits = numLaunchers + numTanks + numDrones + numSoldiers;
				int numHelipad = numRobotsByType[HELIPAD.ordinal()] + numInProgressByType[HELIPAD.ordinal()];
				int numAeroLab = numRobotsByType[AEROSPACELAB.ordinal()] + numInProgressByType[AEROSPACELAB.ordinal()];
				
				// what units and what buildings to build in what order
				/* commented out because drones don't work
				if (numUnits > 15) {
					if (numRobotsByType[DRONE.ordinal()] + numInProgressByType[DRONE.ordinal()] < 1) {
						addToBuildQueue(DRONE, 1, 0);
					}
					if (numRobotsByType[HELIPAD.ordinal()] + numInProgressByType[HELIPAD.ordinal()] < 1) {
						addToBuildQueue(HELIPAD, 1, 0);
					}
				}
				 */
				/*
				if (numRobotsByType[BARRACKS.ordinal()] + numInProgressByType[BARRACKS.ordinal()] < 1) {
					buildQueue[row][0] = BARRACKS.ordinal();
					buildQueue[row][1] = 1;
					row++;
				}
				*/

				if (roundNum < ROUND_TO_BUILD_LAUNCHERS) {
					//addToBuildQueue(SOLDIER);
					//addToBuildQueue(BARRACKS);
				} else {
					if (numHelipad < 1) {
						addToBuildQueue(HELIPAD);
						//addToBuildQueue(SOLDIER);
					} else if (numRobotsByType[HELIPAD.ordinal()] < 1) {
						//addToBuildQueue(SOLDIER);
					} else if (numAeroLab < 1) {
						if (!builtADrone)
							addToBuildQueue(DRONE);
						addToBuildQueue(AEROSPACELAB);
						//addToBuildQueue(SOLDIER);
					} else if (numRobotsByType[AEROSPACELAB.ordinal()] < 1) {
						if (!builtADrone)
							addToBuildQueue(DRONE);
						//addToBuildQueue(SOLDIER);
					} else {
						if (!builtADrone)
							addToBuildQueue(DRONE);
						if (true) {
							addToBuildQueue(LAUNCHER);
							addToBuildQueue(AEROSPACELAB);
							addToBuildQueue(AEROSPACELAB);
							addToBuildQueue(AEROSPACELAB);
						} else {
							addToBuildQueue(SOLDIER);
							addToBuildQueue(BARRACKS);
							addToBuildQueue(LAUNCHER);
							addToBuildQueue(AEROSPACELAB);
							addToBuildQueue(AEROSPACELAB);
							addToBuildQueue(AEROSPACELAB);
						}
						
						
					}
				}
				
				if (numDrones > 0) {
					builtADrone = true;
				}


				bytecodes[16] = Clock.getBytecodeNum();
				bytecodes[17] = Clock.getBytecodeNum();

				writeBuildQueue(buildQueue);

				bytecodes[18] = Clock.getBytecodeNum();

				// telling units what to do
				if (selfSwarmTimer > 0) {
					selfSwarmTimer--;
				}

				bytecodes[19] = Clock.getBytecodeNum();

				//				// check if good time to swarm myself
				//				if (enemyTowerLocs.length < myTowerLocs.length) {
				//					if (areEnemyTowersVulnerable() && numSoldiers >= 50) {
				//						rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_ATTACK_VULNERABLE_TOWER);
				//					} else {
				//						selfSwarmTimer = 75;
				//						rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_DEFEND);
				//					}
				//					// check if good time to stop swarming myself
				//				} else if (selfSwarmTimer <= 0) {
				//					// check if good time to attack
				//					if (numSoldiers >= 50) {
				//						rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_ATTACK_TOWERS);
				//					} else { //check if a good time to retreat
				//						if (numSoldiers <= 30) {
				//							rc.broadcast(UNIT_ORDER_CHAN, UNIT_ORDER_RALLY);
				//						}
				//					}
				//				}

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

				bytecodes[25] = Clock.getBytecodeNum();

				// update mining table with results from monte carlo
				updateMiningTable();

				bytecodes[26] = Clock.getBytecodeNum();

				getAllMiningTargets();

				/*
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < 27; i++) {
					sb.append(i + ": ");
					sb.append((bytecodes[i] - bytecodes[i-1]) + " ");
				}

				rc.setIndicatorString(0, sb.toString());
				 */
				
				if (roundNum % 100 == 0) {
					debug_showNavMap(0);
				}

				// end round
				rc.broadcast(SWARM_ONE_SOLDIERS, 0);
				rc.broadcast(SWARM_ONE_BASHERS, 0);
				rc.broadcast(SWARM_ONE_TANKS, 0);
				rc.broadcast(SWARM_ONE_DRONES, 0);
				rc.broadcast(SWARM_ONE_LAUNCHERS, 0);
				rc.broadcast(SWARM_ONE_COMMANDER, 0);
				
				rc.broadcast(SWARM_TWO_SOLDIERS, 0);
				rc.broadcast(SWARM_TWO_BASHERS, 0);
				rc.broadcast(SWARM_TWO_TANKS, 0);
				rc.broadcast(SWARM_TWO_DRONES, 0);
				rc.broadcast(SWARM_TWO_LAUNCHERS, 0);
				rc.broadcast(SWARM_TWO_COMMANDER, 0);
				
				rc.broadcast(SWARM_THREE_SOLDIERS, 0);
				rc.broadcast(SWARM_THREE_BASHERS, 0);
				rc.broadcast(SWARM_THREE_TANKS, 0);
				rc.broadcast(SWARM_THREE_DRONES, 0);
				rc.broadcast(SWARM_THREE_LAUNCHERS, 0);
				rc.broadcast(SWARM_THREE_COMMANDER, 0);
				
				System.out.println(Clock.getRoundNum());
				precomputePathfindingAndYield(0);
			} catch (Exception e) {
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}
	}
	
	private static void addToBuildQueue(RobotType type) {
		buildQueue[row][0] = type.ordinal();
		buildQueue[row][1] = 1;
		row++;
	}

	private static void addToBuildQueue(RobotType type, double desired, int exist) {
		if (exist < desired) {
			buildQueue[row][0] = type.ordinal();
			buildQueue[row][1] = 1;
			row++;
		}
	}

	//returns true if there exists a tower that has less than 3 enemies around it
	private static boolean areEnemyTowersVulnerable() {
		for (MapLocation i: enemyTowerLocs) {
			if (rc.senseNearbyRobots(i, 20, enemyTeam).length < 3) {
				return true;
			}
		}
		return false;
	}

	//returns true if there exists a tower that has less than 3 enemies around it
	private static boolean isVulnerable(MapLocation loc) {
		if (rc.senseNearbyRobots(loc, 20, enemyTeam).length < 3) {
			return true;
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
		int myTowerNum = 0;
		try {
			myTowerNum = rc.readBroadcast(TOWER_ASSIGN_NUM_CHAN) + 1;
			rc.broadcast(TOWER_ASSIGN_NUM_CHAN, myTowerNum);
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Tower Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// attack
				if (rc.isWeaponReady()) {
					attackSomething();
				}
				
				// transfer supply
				transferSupply();

				// end round
				precomputePathfindingAndYield(myTowerNum);
			} catch (Exception e) {
				System.out.println("Tower Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runAerospaceLab() {
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Aerospace Lab Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Barracks Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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

				// look for map boundaries
				lookForBounds();

				// TODO: basher movement and combat code
				if (rc.isCoreReady()) {
					if (rc.getRoundLimit()-Clock.getRoundNum() < RUSH_TURNS_LEFT) {
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

				// look for map boundaries
				lookForBounds();

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
				
				rc.setIndicatorString(0, "leftHanded = " + leftHanded);
				if (rc.isCoreReady()) {
					if (rc.getRoundLimit()-Clock.getRoundNum() < ALL_OUT_RUSH_TURNS_LEFT) {
						if (enemyTowerLocs.length == 0) {
							tryMove(myLoc.directionTo(enemyHQLoc));
						} else {
							tryMove(myLoc.directionTo(closestLocation(mapCenter, enemyTowerLocs)));
						}
					} else {
						if (builderBeaver) {
							// follow spawn orders
							boolean moved = beaverMove();
							if (!moved) {
								Direction buildDir = bestBuildDir();
								if (buildDir != null) {
									thingIJustBuilt = beaverFollowOrders(buildDir);
								}
							}
						} else {
							// TODO: beaver mining
							Direction escapeDir = escapeCrowding();
							if (escapeDir != null) {
								tryMove(escapeDir);
							} else {
								//TODO: generalize mine and minebeaver
								mineBeaver();
							}
						}
					}
				}

				// transfer supply
				transferSupply();

				// run monte carlo ore finding system, storing results
				runMonteCarloOreFinder(999); // limited by bytecodes

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

				// look for map boundaries
				lookForBounds();

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

				// look for map boundaries
				lookForBounds();

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
		MapLocation allyUnitLoc = null;
		while (true) {
			try {
				// participate in census
				markCensus();

				// update locations
				updateLocations();

				// look for map boundaries
				lookForBounds();

				// TODO: drone attack code
				/* removed for supply running
				if (rc.isWeaponReady()) {
					MapLocation attackLoc = droneAttackLocation();
					if (attackLoc != null) {
						rc.attackLocation(attackLoc);
					}
				}
				 */

				// TODO: drone movement code
				if (rc.isCoreReady()) {
					/*
					// find units that need supply
					if (allyUnitLoc == null && rc.readBroadcast(UNIT_NEEDS_SUPPLY_X_CHAN) != 0) {
						allyUnitLoc = new MapLocation(rc.readBroadcast(UNIT_NEEDS_SUPPLY_X_CHAN), rc.readBroadcast(UNIT_NEEDS_SUPPLY_Y_CHAN));
						rc.setIndicatorString(0, allyUnitLoc.toString());
					}

					// If I have supply, go to said unit
					if (allyUnitLoc != null && rc.getSupplyLevel() > 1000) {
						if (myLoc.distanceSquaredTo(allyUnitLoc) >= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
							safeTryMove(myLoc.directionTo(allyUnitLoc));
						} else {
							//transfer nearly all supplies to unit
							if (rc.senseRobotAtLocation(allyUnitLoc) != null) { // TODO: hack: fix the root of the problem
								rc.transferSupplies((int)(rc.getSupplyLevel() - 100), allyUnitLoc);
							}
							allyUnitLoc = null;
							rc.broadcast(UNIT_NEEDS_SUPPLY_X_CHAN, 0);
							rc.broadcast(UNIT_NEEDS_SUPPLY_Y_CHAN, 0);
						}
					} else { //if I don't have supply, go to HQ and wait for enough supply
						safeTryMove(myLoc.directionTo(myHQLoc));
					}
					*/
					moveToSafely(enemyHQLoc);

				}

				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Drone Exception");
				e.printStackTrace();
			}
		}
	}
	private static void runHandwashStation() {
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Handwash Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Helipad Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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
		while (true) {
			try {
				// participate in census
				markCensus();

				// update locations
				updateLocations();

				// look for map boundaries
				lookForBounds();
				
				MapLocation nearestEnemy = nearestSensedEnemy();
				MapLocation baseTarget = closestLocation(myLoc, enemyTowerLocs);
				
				// attack
				if (rc.getMissileCount() > 0) {
					if (nearestEnemy != null) {
						tryLaunch(rc.getLocation().directionTo(nearestEnemy));
					} else if (baseTarget != null && myLoc.distanceSquaredTo(baseTarget) <= 36) {
						tryLaunch(rc.getLocation().directionTo(baseTarget));
					} else if (myLoc.distanceSquaredTo(enemyHQLoc) <= 36) {
						tryLaunch(rc.getLocation().directionTo(enemyHQLoc));
					}
				}
				
				// TODO: launcher movement code
				// move according to orders
				if (rc.isCoreReady()) {
					if (nearestEnemy != null && myLoc.distanceSquaredTo(nearestEnemy) <= 15) { // if i am too close
						safeTryMove(nearestEnemy.directionTo(myLoc));
					} else  if (nearestEnemy != null && myLoc.add(myLoc.directionTo(nearestEnemy)).distanceSquaredTo(nearestEnemy) <= 35) { // if i would move too close {
						// don't move
					} else {
						if (rc.getRoundLimit()-Clock.getRoundNum() > RUSH_TURNS_LEFT) {
							if(isVulnerable(baseTarget) && rc.senseNearbyRobots(baseTarget, 36, myTeam).length >= 10 ) {
								moveToSafely(baseTarget);
							} else {
								MapLocation invader = attackingEnemy();
								if (invader != null) {
									moveToSafely(invader);
								} else {
									rally();
								}
							}
						} else {
							MapLocation invader = attackingEnemy();
							if (invader != null) {
								safeTryMove(myLoc.directionTo(invader));
							} else {
								if (enemyTowerLocs.length == 0) {
									moveToSafely(enemyHQLoc);
								} else {
									moveToSafely(closestLocation(mapCenter, enemyTowerLocs));
								}
							}
						}
					}
				} else { // hack for bytecodes
					// transfer supply
					transferSupply();
				}
				
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

				// look for map boundaries
				lookForBounds();

				// attack
				if (rc.isWeaponReady()) {
					attackSomething();
				}

				// mine
				// TODO: miner code
				// TODO: make miners avoid enemy towers
				if (rc.isCoreReady()) {
					if (rc.getRoundLimit()-Clock.getRoundNum() < ALL_OUT_RUSH_TURNS_LEFT) {
						if (enemyTowerLocs.length == 0) {
							tryMove(myLoc.directionTo(enemyHQLoc));
						} else {
							tryMove(myLoc.directionTo(closestLocation(mapCenter, enemyTowerLocs)));
						}
					} else {
						mine();
					}
				} else { // hack for bytecodes
					// transfer supply
					transferSupply();
				}

				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Miner Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runMinerFactory() {
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Miner Factory Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

				// follow spawn orders
				if (rc.isCoreReady()) {
					buildingFollowOrders();
				}

				// transfer supply
				transferSupply();

				// end round
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
				// missile move and explode code
				if (rc.isCoreReady()) {
					myLoc = rc.getLocation();
					MapLocation target = fastNearestEnemy();
					if (target == null) {
						quickTryMove(myLoc.directionTo(enemyHQLoc));
						//rc.disintegrate();
					} else {
						if (myLoc.distanceSquaredTo(target) <= 2) { // if adjacent
							quickTryMove(myLoc.directionTo(target)); // not sure if this should be done
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
		while (true) {
			try {
				// participate in census
				markCensus();

				// update locations
				updateLocations();

				// look for map boundaries
				lookForBounds();

				//communincate supply need
				if (rc.getSupplyLevel() < 500) {
					broadcastNeedSupplyLocation();
				}

				// attack
				if (rc.isWeaponReady()) {
					focusAttackEnemies();
				}

				// move according to orders
				if (rc.isCoreReady()) {
					if (rc.getRoundLimit()-Clock.getRoundNum() > RUSH_TURNS_LEFT) {
						MapLocation target = closestLocation(myLoc, enemyTowerLocs);
						if(isVulnerable(target) && rc.senseNearbyRobots(target, 36, myTeam).length >= 10 ) {
							safeTryMove(myLoc.directionTo(target));
						} else {
							MapLocation invader = attackingEnemy();
							if (false) {
								safeTryMove(myLoc.directionTo(invader));
							} else {
								if (Clock.getRoundNum() < 0) {
									harass();
								} else {
									rally();
								}
							}
						}
					} else {
						MapLocation invader = attackingEnemy();
						if (false) {
							safeTryMove(myLoc.directionTo(invader));
						} else {
							if (enemyTowerLocs.length == 0) {
								safeTryMove(myLoc.directionTo(enemyHQLoc));
							} else {
								safeTryMove(myLoc.directionTo(closestLocation(mapCenter, enemyTowerLocs)));
							}
						}
					}
				} else { // hack for bytecodes
					// transfer supply
					transferSupply();
				}

				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Soldier Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runSupplyDepot() {
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Supply Depot Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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

				// look for map boundaries
				lookForBounds();

				//communincate supply need
				if (rc.getSupplyLevel() < 500) {
					broadcastNeedSupplyLocation();
				}

				// attack
				if (rc.isWeaponReady()) {
					focusAttackEnemies();
				}

				// move according to orders
				if (rc.isCoreReady()) {
					if (rc.getRoundLimit()-Clock.getRoundNum() < RUSH_TURNS_LEFT) {
						MapLocation target = closestLocation(myLoc, enemyTowerLocs);
						if(isVulnerable(target) && rc.senseNearbyRobots(target, 36, myTeam).length >= 10 ) {
							tryMove(myLoc.directionTo(target));
						} else {
							MapLocation invader = attackingEnemy();
							if (invader != null) {
								safeTryMove(myLoc.directionTo(invader));
							} else {
								harass();
							}
						}
					} else {
						if (enemyTowerLocs.length == 0) {
							tryMove(myLoc.directionTo(enemyHQLoc));
						} else {
							tryMove(myLoc.directionTo(closestLocation(mapCenter, enemyTowerLocs)));
						}
					}
				} else { // hack for bytecodes
					// transfer supply
					transferSupply();
				}

				// end round
				rc.yield();
			} catch (Exception e) {
				System.out.println("Tank Exception");
				e.printStackTrace();
			}
		}
	}

	private static void runTankFactory() {
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Tank Factory Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Technology Institute Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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
		try {
			// update locations
			updateLocations();
			
			// look for map boundaries
			lookForBounds();
		} catch (Exception e) {
			System.out.println("Training Field Exception");
			e.printStackTrace();
		}
		while (true) {
			try {
				// participate in census
				markCensus();

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

	private static void broadcastNeedSupplyLocation() throws GameActionException {
		rc.broadcast(UNIT_NEEDS_SUPPLY_X_CHAN, myLoc.x);
		rc.broadcast(UNIT_NEEDS_SUPPLY_Y_CHAN, myLoc.y);
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
		rc.broadcast(NORTH_FARTHEST_CHAN, myHQLoc.y);
		rc.broadcast(EAST_FARTHEST_CHAN, myHQLoc.x);
		rc.broadcast(SOUTH_FARTHEST_CHAN, myHQLoc.y);
		rc.broadcast(WEST_FARTHEST_CHAN, myHQLoc.x);
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
		if (myLoc.y - range < rc.readBroadcast(NORTH_FARTHEST_CHAN)) {
			rc.broadcast(NORTH_FARTHEST_CHAN, myLoc.y - range);
		}
		if (myLoc.y + range > rc.readBroadcast(SOUTH_FARTHEST_CHAN)) {
			rc.broadcast(SOUTH_FARTHEST_CHAN, myLoc.y + range);
		}
		if (myLoc.x - range < rc.readBroadcast(WEST_FARTHEST_CHAN)) {
			rc.broadcast(WEST_FARTHEST_CHAN, myLoc.x - range);
		}
		if (myLoc.x + range > rc.readBroadcast(EAST_FARTHEST_CHAN)) {
			rc.broadcast(EAST_FARTHEST_CHAN, myLoc.x + range);
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
		if (myLoc.y - range < rc.readBroadcast(NORTH_FARTHEST_CHAN)) {
			rc.broadcast(NORTH_FARTHEST_CHAN, myLoc.y - range);
		}
		if (myLoc.y + range > rc.readBroadcast(SOUTH_FARTHEST_CHAN)) {
			rc.broadcast(SOUTH_FARTHEST_CHAN, myLoc.y + range);
		}
		if (myLoc.x - range < rc.readBroadcast(WEST_FARTHEST_CHAN)) {
			rc.broadcast(WEST_FARTHEST_CHAN, myLoc.x - range);
		}
		if (myLoc.x + range > rc.readBroadcast(EAST_FARTHEST_CHAN)) {
			rc.broadcast(EAST_FARTHEST_CHAN, myLoc.x + range);
		}
	}

	private static int northBound() throws GameActionException {
		return rc.readBroadcast(NORTH_BOUND_CHAN);
	}

	private static int eastBound() throws GameActionException {
		return rc.readBroadcast(EAST_BOUND_CHAN);
	}

	private static int southBound() throws GameActionException {
		return rc.readBroadcast(SOUTH_BOUND_CHAN);
	}

	private static int westBound() throws GameActionException {
		return rc.readBroadcast(WEST_BOUND_CHAN);
	}

	private static int northFarthest() throws GameActionException {
		return rc.readBroadcast(NORTH_FARTHEST_CHAN);
	}

	private static int eastFarthest() throws GameActionException {
		return rc.readBroadcast(EAST_FARTHEST_CHAN);
	}

	private static int southFarthest() throws GameActionException {
		return rc.readBroadcast(SOUTH_FARTHEST_CHAN);
	}

	private static int westFarthest() throws GameActionException {
		return rc.readBroadcast(WEST_FARTHEST_CHAN);
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
		rc.broadcast(HQ_ROUNDNUM_CHAN, Clock.getRoundNum());
		return census;
	}

	private static void markCensus() throws GameActionException {
		if (rc.readBroadcast(HQ_ROUNDNUM_CHAN) == Clock.getRoundNum()) {
			int typeNum = myType.ordinal();
			rc.broadcast(CENSUS_CHAN + typeNum, rc.readBroadcast(CENSUS_CHAN + typeNum) + 1);
		}
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
		if (rc.readBroadcast(HQ_ROUNDNUM_CHAN) == Clock.getRoundNum()) {
			int typeNum = type.ordinal();
			rc.broadcast(PROGRESS_TABLE_CHAN + typeNum, rc.readBroadcast(PROGRESS_TABLE_CHAN + typeNum) + 1);
		}
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
		if (rc.readBroadcast(HQ_ROUNDNUM_CHAN) == Clock.getRoundNum()) {
			int typeNum = type.ordinal();
			rc.broadcast(COMPLETED_TABLE_CHAN + typeNum, rc.readBroadcast(COMPLETED_TABLE_CHAN + typeNum) + 1);
		}
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
		if (rc.readBroadcast(HQ_ROUNDNUM_CHAN) == Clock.getRoundNum()) {
			float oldOre = Float.intBitsToFloat(rc.readBroadcast(MINER_ORE_COUNTER_CHAN));
			rc.broadcast(MINER_ORE_COUNTER_CHAN, Float.floatToIntBits(oldOre + (float)ore));
		}
	}

	private static void markBeaverOreCounter(double ore) throws GameActionException {
		if (rc.readBroadcast(HQ_ROUNDNUM_CHAN) == Clock.getRoundNum()) {
			float oldOre = Float.intBitsToFloat(rc.readBroadcast(BEAVER_ORE_COUNTER_CHAN));
			rc.broadcast(BEAVER_ORE_COUNTER_CHAN, Float.floatToIntBits(oldOre + (float)ore));
		}
	}

	private static int readBuilderBeaverCounter() throws GameActionException {
		int numBuilderBeavers = rc.readBroadcast(BUILDER_BEAVER_COUNTER_CHAN);
		rc.broadcast(BUILDER_BEAVER_COUNTER_CHAN, 0);
		return numBuilderBeavers;
	}

	private static void markBuilderBeaverCounter() throws GameActionException {
		if (rc.readBroadcast(HQ_ROUNDNUM_CHAN) == Clock.getRoundNum()) {
			rc.broadcast(BUILDER_BEAVER_COUNTER_CHAN, rc.readBroadcast(BUILDER_BEAVER_COUNTER_CHAN) + 1);
		}
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
		return beaverFollowOrders(directions[rand.nextInt(8)]);
	}
	
	private static RobotType beaverFollowOrders(Direction dir) throws GameActionException {
		RobotType buildOrder = getMyBuildOrder();
		if (buildOrder != null) {
			boolean success = tryBuild(dir,buildOrder);
			if (success) {
				markProgressTable(buildOrder);
			} else {
				System.out.println("failed build");
				buildOrder = null;
			}
		}
		return buildOrder;
	}

	// ore, size, x, y, numRobots, badness
	private static void writeMiningTable(int[][] miningTable) throws GameActionException {
		rc.broadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN, miningTable.length);
		for (int row = 0; row < miningTable.length; row++) {
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 0, miningTable[row][0]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 1, miningTable[row][1]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 2, miningTable[row][2]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 3, miningTable[row][3]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 4, miningTable[row][4]);
			rc.broadcast(MINING_TABLE_CHAN + row*MINING_TABLE_ROW_SIZE + 5, 0);
			if (miningTable[row][0] == 0) { // must be at end to ensure 0 is written
				rc.broadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN, row);
				break;
			}
		}
	}

	private static void markBadMiningTable(MapLocation myLoc) throws GameActionException {
		int numLocs = rc.readBroadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN);
		int x;
		int y;
		MapLocation loc;
		for (int i = 0; i < numLocs; i++) {
			x = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 2);
			y = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 3);
			loc = new MapLocation(x, y);
			if (myLoc.equals(loc)) {
				rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5, 1);
				break;
			}
		}
	}

	private static MapLocation[] getAllMiningTargets() throws GameActionException {
		int numLocs = rc.readBroadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN);
		MapLocation[] locs = new MapLocation[numLocs];
		int x;
		int y;
		for (int i = 0; i < numLocs; i++) {
			if (rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5) == 0) {
				x = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 2);
				y = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 3);
				locs[i] = new MapLocation(x, y);
				rc.setIndicatorDot(locs[i], 0, 255, 255);
			}
		}
		return locs;
	}

	private static MapLocation[] getAllMiningTargetsBetterThan(double ore) throws GameActionException {
		// assumes entire table has same ore
		MapLocation[] locs;
		if (getBestOre() > ore) {
			int numLocs = rc.readBroadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN);
			locs = new MapLocation[numLocs];
			int x;
			int y;
			for (int i = 0; i < numLocs; i++) {
				if (rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5) == 0) {
					x = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 2);
					y = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 3);
					locs[i] = new MapLocation(x, y);
				}
			}
		} else {
			locs = new MapLocation[0];
		}
		return locs;
	}

	private static MapLocation getClosestMiningTargetBetterThan(MapLocation myLoc, double ore) throws GameActionException {
		// assumes entire table has same ore
		MapLocation bestLoc = null;
		if (getBestOre() > ore) {
			int numLocs = rc.readBroadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN);
			int x;
			int y;
			int distSq;
			MapLocation loc;
			int bestDistSq = 999999;
			for (int i = 0; i < numLocs; i++) {
				if (rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5) == 0) {
					x = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 2);
					y = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 3);
					loc = new MapLocation(x, y);
					distSq = myLoc.distanceSquaredTo(loc);
					if (distSq < bestDistSq) {
						bestLoc = loc;
						bestDistSq = distSq;
					}
				}
			}
		}
		return bestLoc;
	}

	private static double getBestOre() throws GameActionException {
		int numRows = rc.readBroadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN);
		for (int i = 0; i < numRows; i++) {
			if (rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5) == 0) { // if good
				return Float.intBitsToFloat(rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 0));
			}
		}
		return 0;
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

	private static void updateMiningTable() throws GameActionException {

		int indexInMonteCarlo = 0;
		int monteCarloNumResults = rc.readBroadcast(MONTE_CARLO_NUM_RESULTS_CHAN);
		int numMiningTableRows = rc.readBroadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN);
		double miningTableMinOre = getBestOre();
		double monteCarloMaxOre = Float.intBitsToFloat(rc.readBroadcast(MONTE_CARLO_MAX_FOUND_CHAN));
		boolean rebuild = (monteCarloMaxOre > miningTableMinOre);
		double minOre = Math.max(miningTableMinOre, monteCarloMaxOre);
		int lastMiningTableRowWritten = rebuild ? -1 : numMiningTableRows-1;
		boolean endOfMonteCarlo = false;
		// i = row in mining table
		for (int i = 0; i < MINING_TABLE_NUM_ROWS; i++) {
			if (!endOfMonteCarlo && (rebuild || i >= numMiningTableRows || rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5) == 1)) { // if row doesn't exist or is marked as bad
				// get an entry from monte carlo
				double newOre = 0;
				int x = NO_BOUND;
				int y = NO_BOUND;
				while (newOre < minOre && indexInMonteCarlo < monteCarloNumResults) {
					newOre = Float.intBitsToFloat(rc.readBroadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + indexInMonteCarlo*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 0));
					x = rc.readBroadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + indexInMonteCarlo*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 1);
					y = rc.readBroadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + indexInMonteCarlo*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 2);
					indexInMonteCarlo++;
				}
				if (x != NO_BOUND && newOre >= minOre) {
					rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 0, Float.floatToIntBits((float)newOre));
					//rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 1, );
					rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 2, x);
					rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 3, y);
					//rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 4, );
					rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5, 0);
					if (i > lastMiningTableRowWritten) {
						lastMiningTableRowWritten = i;
					}
					//rc.setIndicatorDot(newLoc, 0, 0, 255);
				} else {
					// end of monte carlo
					endOfMonteCarlo = true;
				}
			} else {
				if (i < numMiningTableRows) {
					int x = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 2);
					int y = rc.readBroadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 3);
					double sensedOre = rc.senseOre(new MapLocation(x, y));
					if (sensedOre < minOre) {
						rc.broadcast(MINING_TABLE_CHAN + i*MINING_TABLE_ROW_SIZE + 5, 1);
					}
				}
			}
		}
		rc.broadcast(MINING_TABLE_CURRENT_NUMROWS_CHAN, lastMiningTableRowWritten + 1);
		rc.broadcast(MONTE_CARLO_NUM_RESULTS_CHAN, 0);
	}
	
	private static void pruneMiningTable() {
		
	}

	private static void runMonteCarloOreFinder(int times) throws GameActionException {
		int minx = myHQLoc.x-119;
		int maxx = myHQLoc.x+119;
		int miny = myHQLoc.y-119;
		int maxy = myHQLoc.y+119;
		int westFarthest = westFarthest();
		int eastFarthest = eastFarthest();
		int northFarthest = northFarthest();
		int southFarthest = southFarthest();
		if (westFarthest != NO_BOUND) minx = Math.max(minx, westFarthest);
		if (eastFarthest != NO_BOUND) maxx = Math.min(maxx, eastFarthest);
		if (northFarthest != NO_BOUND) miny = Math.max(miny, northFarthest);
		if (southFarthest != NO_BOUND) maxy = Math.min(maxy, southFarthest);
		double minOre = getBestOre();
		int x;
		int y;
		MapLocation loc;
		double ore;
		double maxFound = 0;
		int row = 0; //rc.readBroadcast(MONTE_CARLO_NUM_RESULTS_CHAN);
		for (int i = times; --i >= 0;) {
			x = rand.nextInt(maxx-minx+1) + minx;
			y = rand.nextInt(maxy-miny+1) + miny;
			loc = new MapLocation(x,y);
			ore = rc.senseOre(loc);
			if (ore >= minOre && (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc)) && !inEnemyBuildingRange(loc)) {
				rc.broadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + row*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 0, Float.floatToIntBits((float)ore));
				rc.broadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + row*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 1, x);
				rc.broadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + row*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 2, y);
				row++;
				if (ore > maxFound) {
					maxFound = ore;
				}
				if (row >= MONTE_CARLO_RESULTS_TABLE_NUM_ROWS) {
					break;
				}
			}
			if (Clock.getBytecodesLeft() < 1000) {
				break;
			}
		}
		rc.broadcast(MONTE_CARLO_NUM_RESULTS_CHAN, row);
		rc.broadcast(MONTE_CARLO_MAX_FOUND_CHAN, Float.floatToIntBits((float)maxFound));
	}

	private static MapLocation[] readMonteCarloResults() throws GameActionException {
		int numLocs = rc.readBroadcast(MONTE_CARLO_NUM_RESULTS_CHAN);
		MapLocation[] results = new MapLocation[numLocs];
		int x;
		int y;
		for (int i = numLocs; --i >= 0;) {
			x = rc.readBroadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + i*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 1);
			y = rc.readBroadcast(MONTE_CARLO_RESULTS_TABLE_CHAN + i*MONTE_CARLO_RESULTS_TABLE_ROW_SIZE + 2);
			results[i] = new MapLocation(x, y);
		}
		rc.broadcast(MONTE_CARLO_NUM_RESULTS_CHAN, 0);
		return results;
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
		if (rc.getType() != HQ) {
			if(r.type == BEAVER || r.type == COMPUTER || r.type == COMMANDER || r.type == SOLDIER ||
					r.type == BASHER || r.type == TANK || r.type == LAUNCHER || r.type == MINER)
				return true;
		} else {
			if(r.type == BEAVER || r.type == COMPUTER || r.type == COMMANDER || r.type == SOLDIER || 
					r.type == BASHER || r.type == TANK || r.type == LAUNCHER || r.type == MINER || r.type == DRONE)
				return true;
		}
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

		int round = Clock.getRoundNum();

		int[] bytecodes = new int[50];
		bytecodes[0] = Clock.getBytecodeNum();

		double myOre = rc.senseOre(myLoc);
		rc.setIndicatorString(2, "myOre = " + myOre);

		bytecodes[1] = Clock.getBytecodeNum();

		if (mining && mineCounter > 0) {
			rc.setIndicatorString(1,"branch1");
			mineCounter--;
			double oreMined = Math.min(Math.max(Math.min(myOre/GameConstants.MINER_MINE_RATE,GameConstants.MINER_MINE_MAX),GameConstants.MINIMUM_MINE_AMOUNT),myOre);

			bytecodes[2] = Clock.getBytecodeNum();

			markMinerOreCounter(oreMined);

			bytecodes[3] = Clock.getBytecodeNum();

			rc.mine();

			bytecodes[4] = Clock.getBytecodeNum();
			bytecodes[5] = Clock.getBytecodeNum();
			bytecodes[6] = Clock.getBytecodeNum();
			bytecodes[7] = Clock.getBytecodeNum();
			bytecodes[8] = Clock.getBytecodeNum();
		} else {
			if (mining) {
				rc.setIndicatorString(1,"branch2");
				mining = false;
			}
			rc.setIndicatorString(1,"branch3");

			bytecodes[2] = Clock.getBytecodeNum();

			// if my location has tied for the most ore in sensor range, mine here
			// else move towards that location without mining
			// break ties by closeness to top of the table

			// TODO: make this take less time
			MapLocation targetLoc = getClosestMiningTargetBetterThan(myLoc, 0);

			bytecodes[3] = Clock.getBytecodeNum();

			if (myLoc.equals(targetLoc) && myOre > 0) {
				bytecodes[4] = Clock.getBytecodeNum();
				bytecodes[5] = Clock.getBytecodeNum();
				bytecodes[6] = Clock.getBytecodeNum();
				bytecodes[7] = Clock.getBytecodeNum();
				bytecodes[8] = Clock.getBytecodeNum();
				rc.setIndicatorString(1,"branch6");
				mineCounter = Simulator.minerOptimalNumMines(myOre);
				double oreMined = Math.min(Math.max(Math.min(myOre/GameConstants.MINER_MINE_RATE,GameConstants.MINER_MINE_MAX),GameConstants.MINIMUM_MINE_AMOUNT),myOre);
				markMinerOreCounter(oreMined);
				mining = true;
				markBadMiningTable(targetLoc);
				rc.mine();
			} else {
				double bestEverOre = getBestOre();
				
				boolean noTarget = false;
				if (targetLoc == null) {
					noTarget = true;
					targetLoc = myHQLoc;
				} else {
					if (myLoc.distanceSquaredTo(targetLoc) <= mySensorRangeSq) {
						// TODO: make this take less time
						if (rc.senseOre(targetLoc) < bestEverOre || !rc.isPathable(MINER, targetLoc)) {
							markBadMiningTable(targetLoc);
						}
					}
				}
				rc.setIndicatorString(2,"bestore:" + bestEverOre);
				if (bestEverOre <= myOre) {
					noTarget = true;
					targetLoc = myHQLoc;
				}

				bytecodes[4] = Clock.getBytecodeNum();

				double bestOre = myOre;
				int bestTargetDistSq = myLoc.distanceSquaredTo(targetLoc);
				MapLocation bestLoc = null;

				bytecodes[5] = Clock.getBytecodeNum();

				//MapLocation[] nearbyLocs = MapLocation.getAllMapLocationsWithinRadiusSq(myLoc, mySensorRangeSq);

				MapLocation[] nearbyLocs = new MapLocation[]{
						myLoc.add(Direction.NORTH),
						myLoc.add(Direction.NORTH_EAST),
						myLoc.add(Direction.EAST),
						myLoc.add(Direction.SOUTH_EAST),
						myLoc.add(Direction.SOUTH),
						myLoc.add(Direction.SOUTH_WEST),
						myLoc.add(Direction.WEST),
						myLoc.add(Direction.NORTH_WEST)
				};

				bytecodes[6] = Clock.getBytecodeNum();

				// TODO: make this take less time
				for (int i = 0; i < nearbyLocs.length; i++) {
					MapLocation loc = nearbyLocs[i];
					double ore = rc.senseOre(loc);
					int targetDistSq = loc.distanceSquaredTo(targetLoc);
					if (!loc.equals(myLoc) && ore > myOre && !rc.isLocationOccupied(loc)) {
						if (ore > bestOre) {
							bestLoc = loc;
							bestOre = ore;
							bestTargetDistSq = targetDistSq;
						} else if (ore >= bestOre && targetDistSq < bestTargetDistSq) {
							bestLoc = loc;
							bestOre = ore;
							bestTargetDistSq = targetDistSq;
						}
					}
				}

				bytecodes[7] = Clock.getBytecodeNum();

				if (bestLoc == null) {
					if (myOre > bestEverOre / 2) {
						rc.setIndicatorString(1,"branch4");
						mineCounter = Simulator.minerOptimalNumMines(myOre);
						double oreMined = Math.min(Math.max(Math.min(myOre/GameConstants.MINER_MINE_RATE,GameConstants.MINER_MINE_MAX),GameConstants.MINIMUM_MINE_AMOUNT),myOre);
						markMinerOreCounter(oreMined);
						mining = true;
						markBadMiningTable(myLoc); // hack
						rc.mine();
					} else if (noTarget) {
						rc.setIndicatorString(1,"branch8");
						safeTryMove(directions[rand.nextInt(8)]);
					} else {
						rc.setIndicatorString(1,"branch7 " + targetLoc.x + " " + targetLoc.y);
						moveToSafely(targetLoc);
					}
				} else {
					if (bestOre > bestEverOre / 2) {
						rc.setIndicatorString(1,"branch5");
						moveToSafely(bestLoc);
					} else {
						rc.setIndicatorString(1,"branch9");
						moveToSafely(targetLoc);
					}

				}

				bytecodes[8] = Clock.getBytecodeNum();
			}

		}

		if (Clock.getRoundNum() > round) {
			System.out.println("miners exceed bytecodes!!");
		}

	}

	// TODO: join duplicate miner and beaver mining code
	private static void mineBeaver() throws GameActionException {
		double myOre = rc.senseOre(myLoc);
		if (mining && mineCounter > 0) {
			mineCounter--;
			double oreMined = Math.min(Math.max(Math.min(myOre/GameConstants.BEAVER_MINE_RATE,GameConstants.BEAVER_MINE_MAX),GameConstants.MINIMUM_MINE_AMOUNT),myOre);
			markBeaverOreCounter(oreMined);
			rc.mine();
		} else {
			if (mining) {
				mining = false;
			}
			// if my location has tied for the most ore in sensor range, mine here
			// else move towards that location without mining
			// break ties by closeness to top of the table
			MapLocation targetLoc = getClosestMiningTargetBetterThan(myLoc, myOre);
			if (targetLoc == null) {
				targetLoc = myHQLoc;
			} else {
				if (myLoc.distanceSquaredTo(targetLoc) <= mySensorRangeSq) {
					if (rc.senseOre(targetLoc) < getBestOre()) {
						markBadMiningTable(targetLoc);
					}
				}
			}
			double bestOre = myOre;
			int bestTargetDistSq = myLoc.distanceSquaredTo(targetLoc);
			MapLocation bestLoc = null;
			MapLocation[] nearbyLocs = MapLocation.getAllMapLocationsWithinRadiusSq(myLoc, mySensorRangeSq);
			for (int i = 0; i < nearbyLocs.length; i++) {
				MapLocation loc = nearbyLocs[i];
				double ore = rc.senseOre(loc);
				int targetDistSq = myLoc.distanceSquaredTo(targetLoc);
				if (!loc.equals(myLoc) && ore > myOre) {
					if (ore > bestOre) {
						bestLoc = loc;
						bestOre = ore;
						bestTargetDistSq = targetDistSq;
					} else if (ore >= bestOre && targetDistSq > bestTargetDistSq) {
						bestLoc = loc;
						bestOre = ore;
						bestTargetDistSq = targetDistSq;
					}
				}
			}
			if (bestLoc == null) {
				mineCounter = Simulator.beaverOptimalNumMines(myOre);
				double oreMined = Math.min(Math.max(Math.min(myOre/GameConstants.BEAVER_MINE_RATE,GameConstants.BEAVER_MINE_MAX),GameConstants.MINIMUM_MINE_AMOUNT),myOre);
				markBeaverOreCounter(oreMined);
				rc.mine();
			} else {
				tryMove(myLoc.directionTo(bestLoc));
			}
		}
	}

	private static void rally() throws GameActionException {
		MapLocation invaderLoc = attackingEnemy();
		MapLocation enemy = nearestAttackableEnemyAll();
		if (enemy != null) {
			tryMove(myLoc.directionTo(enemy));
		} else {
			if (invaderLoc != null) {
				tryMove(myLoc.directionTo(invaderLoc));
			} else {
				moveToSafely(getClosestRallyPoint());
			}
		}
	}

	// TODO: replace harass with alec or josh's drone micro
	private static void harass() throws GameActionException {
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
			} else if (type == LAUNCHER) { 
				int rangeSq = 8;
				int damage = 20;
				if (damage > 0 && rangeSq > 0) {
					for (int sourceX = -1; sourceX <= 1; sourceX++) {
						for (int sourceY = -1; sourceY <= 1; sourceY++) {
							distX = targetX - sourceX;
							distY = targetY - sourceY;
							distSq = (distX*distX) + (distY*distY);
							if (distSq <= myAttackRangeSq) { // myRange
								canAttackGrid[sourceX+1][sourceY+1] = true;
							}
						}
					}
				}
			} else if (type == COMMANDER && rc.senseNearbyRobots(9, myTeam).length > 3) {
				tryMove(rc.getLocation().directionTo(ri.location));
			} else {

				int rangeSq = type.attackRadiusSquared;
				int damage = (int)type.attackPower;
				if (damage > 0 && rangeSq > 0) {
					for (int sourceX = -1; sourceX <= 1; sourceX++) {
						for (int sourceY = -1; sourceY <= 1; sourceY++) {
							distX = targetX - sourceX;
							distY = targetY - sourceY;
							distSq = (distX*distX) + (distY*distY);
						}
					}
				}
				if (type != COMMANDER && type != TANK) {
					// can attack safely
					for (int sourceX = -1; sourceX <= 1; sourceX++) {
						for (int sourceY = -1; sourceY <= 1; sourceY++) {
							distX = targetX - sourceX;
							distY = targetY - sourceY;
							distSq = (distX*distX) + (distY*distY);
							if (distSq <= myAttackRangeSq) { // myRange
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
	
	private static boolean fitsCheckerboard (MapLocation loc) {
		return ((((loc.x + loc.y) % 2) + 2) % 2 == buildingParity);
	}
	
	private static boolean goodBuildLoc (MapLocation loc) {
		return fitsCheckerboard(loc) && rc.isPathable(BEAVER, loc);
	}
	
	private static boolean goodPlaceForBeaver (MapLocation loc) {
		if (isLocationNextToVoid(loc)) {
			return false;
		}
		if (loc.equals(myLoc) || rc.isPathable(BEAVER, loc)) {
			if (fitsCheckerboard(loc)) {
				if (goodBuildLoc(loc.add(Direction.NORTH_EAST))
						|| goodBuildLoc(loc.add(Direction.SOUTH_EAST))
						|| goodBuildLoc(loc.add(Direction.SOUTH_WEST))
						|| goodBuildLoc(loc.add(Direction.NORTH_WEST))) {
					return true;
				}
			} else {
				if (goodBuildLoc(loc.add(Direction.NORTH))
						|| goodBuildLoc(loc.add(Direction.EAST))
						|| goodBuildLoc(loc.add(Direction.SOUTH))
						|| goodBuildLoc(loc.add(Direction.WEST))) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static Direction bestBuildDir() {
		Direction dir = myLoc.directionTo(myHQLoc);
		if (!fitsCheckerboard(myLoc.add(dir))) {
			dir = dir.rotateLeft();
		}
		int i = 0;
		while (i < 4 && !rc.canMove(dir)) {
			dir = dir.rotateLeft().rotateLeft();
			i++;
		}
		if (i < 4) {
			return dir;
		}
		return null;
	}
	
	private static boolean beaverMove() throws GameActionException {
		if (goodPlaceForBeaver(myLoc)) {
			return false;
		}
		Direction dir = myLoc.directionTo(myHQLoc);
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3};
		int dirint = directionToInt(dir);
		while (offsetIndex < 7 && !(rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) && goodPlaceForBeaver(myLoc.add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < 7) {
			dir = directions[(dirint+offsets[offsetIndex]+8)%8];
			rc.move(dir);
			return true;
		}
		return tryMove(myHQLoc.directionTo(myLoc));
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
		int centroidX = 0;
		int centroidY = 0;
		int soldiers = 0;
		int bashers = 0;
		int tanks = 0;
		int drones = 0;
		int launchers = 0;
		int commander = 0;	
		for (RobotInfo r : enemies) {
			if (r.type != MISSILE) {
				MapLocation enemyLoc = r.location;
				int dist = myLoc.distanceSquaredTo(enemyLoc);
				if (dist < closestDist) {
					closestDist = dist;
					closestRobot = r;
				}
				centroidX += r.location.x;
				centroidY += r.location.y;
				if(r.type == SOLDIER){
					soldiers++;
				}
				if(r.type == BASHER){
					bashers++;
				}
				if(r.type == TANK){
					tanks++;
				}
				if(r.type == DRONE){
					drones++;
				}
				if(r.type == LAUNCHER){
					launchers++;
				}
				if(r.type == COMMANDER){
					commander = 1;
				}
			}
		}
		if(enemies.length > 3){
			centroidX /= enemies.length;
			centroidY /= enemies.length;
			rc.setIndicatorString(0, "Centroid is:"+ centroidX +", "+ centroidY);
			MapLocation swarmLocation = new MapLocation(centroidX,centroidY);
			MapLocation swarm1 = unpackLocation(rc.readBroadcast(SWARM_ONE_LOCATION));
			MapLocation swarm2 = unpackLocation(rc.readBroadcast(SWARM_TWO_LOCATION));
			MapLocation swarm3 = unpackLocation(rc.readBroadcast(SWARM_THREE_LOCATION));
			//initialize a swarm
			if(swarm1.x == NO_BOUND){
				rc.setIndicatorString(1, "broadcasting initial for swarm 1");
				rc.broadcast(SWARM_ONE_LOCATION, packLocation(swarmLocation));
				rc.broadcast(SWARM_ONE_SOLDIERS, soldiers);
				rc.broadcast(SWARM_ONE_BASHERS, bashers);
				rc.broadcast(SWARM_ONE_TANKS, tanks);
				rc.broadcast(SWARM_ONE_DRONES, drones);
				rc.broadcast(SWARM_ONE_LAUNCHERS, launchers);
				rc.broadcast(SWARM_ONE_COMMANDER, commander);
			}
			else{
				//if its the same swarm as swarm 1
				if(distance(swarmLocation, swarm1) < 100){
					rc.setIndicatorString(1, "broadcasting updated for swarm 1");
					MapLocation meanCenter = new MapLocation((swarmLocation.x + swarm1.x)/2, (swarmLocation.y + swarm1.y)/2);
					rc.broadcast(SWARM_ONE_LOCATION, packLocation(meanCenter));
					if(soldiers > rc.readBroadcast(SWARM_ONE_SOLDIERS)){
						rc.broadcast(SWARM_ONE_SOLDIERS, soldiers);
					}
					if(bashers > rc.readBroadcast(SWARM_ONE_BASHERS)){
						rc.broadcast(SWARM_ONE_BASHERS, bashers);
					}
					if(tanks > rc.readBroadcast(SWARM_ONE_TANKS)){
						rc.broadcast(SWARM_ONE_TANKS, tanks);
					}
					if(drones > rc.readBroadcast(SWARM_ONE_DRONES)){
						rc.broadcast(SWARM_ONE_DRONES, drones);
					}
					if(soldiers > rc.readBroadcast(SWARM_ONE_LAUNCHERS)){
						rc.broadcast(SWARM_ONE_LAUNCHERS, launchers);
					}
					if(commander == 1){ 
						rc.broadcast(SWARM_ONE_COMMANDER, 1);
					}
				}
				else if(swarm2.x == NO_BOUND){
					rc.setIndicatorString(1, "broadcasting initial for swarm 2");
					rc.broadcast(SWARM_TWO_LOCATION, packLocation(swarmLocation));
					rc.broadcast(SWARM_TWO_SOLDIERS, soldiers);
					rc.broadcast(SWARM_TWO_BASHERS, bashers);
					rc.broadcast(SWARM_TWO_TANKS, tanks);
					rc.broadcast(SWARM_TWO_DRONES, drones);
					rc.broadcast(SWARM_TWO_LAUNCHERS, launchers);
					rc.broadcast(SWARM_TWO_COMMANDER, commander);
				}
				else if(distance(swarmLocation, swarm2) < 100){
					rc.setIndicatorString(1, "broadcasting updated for swarm 1");
					MapLocation meanCenter = new MapLocation((swarmLocation.x + swarm1.x)/2, (swarmLocation.y + swarm1.y)/2);
					rc.broadcast(SWARM_TWO_LOCATION, packLocation(meanCenter));
					if(soldiers > rc.readBroadcast(SWARM_TWO_SOLDIERS)){
						rc.broadcast(SWARM_TWO_SOLDIERS, soldiers);
					}
					if(bashers > rc.readBroadcast(SWARM_TWO_BASHERS)){
						rc.broadcast(SWARM_TWO_BASHERS, bashers);
					}
					if(tanks > rc.readBroadcast(SWARM_TWO_TANKS)){
						rc.broadcast(SWARM_TWO_TANKS, tanks);
					}
					if(drones > rc.readBroadcast(SWARM_TWO_DRONES)){
						rc.broadcast(SWARM_TWO_DRONES, drones);
					}
					if(soldiers > rc.readBroadcast(SWARM_TWO_LAUNCHERS)){
						rc.broadcast(SWARM_TWO_LAUNCHERS, launchers);
					}
					if((commander == 1 )){ 
						rc.broadcast(SWARM_TWO_COMMANDER, 1);
					}
				}
				else if(swarm3.x == NO_BOUND){
					rc.setIndicatorString(1, "broadcasting initial for swarm 3");
					rc.broadcast(SWARM_THREE_LOCATION, packLocation(swarmLocation));
					rc.broadcast(SWARM_THREE_SOLDIERS, soldiers);
					rc.broadcast(SWARM_THREE_BASHERS, bashers);
					rc.broadcast(SWARM_THREE_TANKS, tanks);
					rc.broadcast(SWARM_THREE_DRONES, drones);
					rc.broadcast(SWARM_THREE_LAUNCHERS, launchers);
					rc.broadcast(SWARM_THREE_COMMANDER, commander);
				}
				else if(distance(swarmLocation, swarm3) < 100){
					rc.setIndicatorString(1, "broadcasting update for swarm 3");
					MapLocation meanCenter = new MapLocation((swarmLocation.x + swarm1.x)/2, (swarmLocation.y + swarm1.y)/2);
					rc.broadcast(SWARM_THREE_LOCATION, packLocation(meanCenter));
					if(soldiers > rc.readBroadcast(SWARM_THREE_SOLDIERS)){
						rc.broadcast(SWARM_THREE_SOLDIERS, soldiers);
					}
					if(bashers > rc.readBroadcast(SWARM_THREE_BASHERS)){
						rc.broadcast(SWARM_THREE_BASHERS, bashers);
					}
					if(tanks > rc.readBroadcast(SWARM_THREE_TANKS)){
						rc.broadcast(SWARM_THREE_TANKS, tanks);
					}
					if(drones > rc.readBroadcast(SWARM_THREE_DRONES)){
						rc.broadcast(SWARM_THREE_DRONES, drones);
					}
					if(soldiers > rc.readBroadcast(SWARM_THREE_LAUNCHERS)){
						rc.broadcast(SWARM_THREE_LAUNCHERS, launchers);
					}
					if(commander == 1){ 
						rc.broadcast(SWARM_THREE_COMMANDER, 1);
					}
				
				}
				else{
					System.out.print("WTF! more than 3 swarms!?");
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
		RobotInfo[] enemies = rc.senseNearbyRobots(24, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		int i = 0;
		for (RobotInfo r : enemies) {
			if (r.type != RobotType.MISSILE) {
				MapLocation enemyLoc = r.location;
				int dist = myLoc.distanceSquaredTo(enemyLoc);
				if (dist < closestDist) {
					closestDist = dist;
					closestRobot = r;
				}
			}
			i++;
			if (i >= 3) {
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
		RobotInfo[] enemies = rc.senseNearbyRobots(myHQLoc, rc.readBroadcast(SAFETY_RADIUS_SQ_CHAN), enemyTeam);
		int closestDist = 9999999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type != MISSILE && r.type != MINER && r.type != BEAVER) {
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
	// TODO: fix this!
	private static void focusAttackEnemies() throws GameActionException {
		// attack the unit with the least health
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, enemyTeam);
		if (enemies.length == 0) {
			return;
		}

		RobotInfo targetEnemy = enemies[0];
		for (RobotInfo i: enemies) {
			if (i.type == TOWER || i.type == LAUNCHER) {
				if (i.health < targetEnemy.health) {
					targetEnemy = i;
					break;
				}
			} else {
				if (i.health < targetEnemy.health) {
					targetEnemy = i;
				}
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
	// this method is optimized to take about 53 bytecodes. rotateLeft and rotateRight appear to be free.
	private static void quickTryMove(Direction d) throws GameActionException {
		if (rc.canMove(d)) {
			rc.move(d);
		} else if (rc.canMove(d.rotateLeft())) {
			rc.move(d.rotateLeft());
		} else if (rc.canMove(d.rotateRight())) {
			rc.move(d.rotateRight());
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

	//TODO: rename launcherTryMove
	//TODO: make more efficient
	private static boolean safeTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		while (offsetIndex < 5 && (!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || inEnemyBuildingRange(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	private static boolean launcherTryMoveTo(MapLocation targetLoc) throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation enemy = nearestSensedEnemy();
		Direction d = myLoc.directionTo(targetLoc);
		if (enemy != null) {
			if (myLoc.distanceSquaredTo(enemy) <= 15) { // if i am too close
				d = d.opposite(); // move away
			} else if (myLoc.add(d).distanceSquaredTo(enemy) <= 15) { // if i would move too close
				return false; // don't move
			}
		}
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			Direction dir = directions[(dirint+offsets[offsetIndex]+8)%8];
			if (!inEnemyBuildingRange(myLoc.add(dir))) {
				rc.move(dir);
				return true;
			}
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
	
	private static boolean moveTo(MapLocation targetLoc) throws GameActionException {
		Direction dir = null;
		if (targetLoc.equals(navTargetLoc)) {
			if (navType == NAVTYPE_BUG && !pathExists(myLoc, targetLoc)) {
				dir = bugNavDirection(targetLoc);
			} else {
				dir = pathFindingDirection(targetLoc);
				if (dir == null) {
					navType = 0;
					dir = bugNavDirection(targetLoc);
				}
			}
		} else {
			navType = 0;
			dir = pathFindingDirection(targetLoc);
			if (dir == null) {
				dir = bugNavDirection(targetLoc);
			}
		}
		if (dir == null)
			return false;
		rc.move(dir);
		return true;
	}
	
	private static boolean moveToSafely(MapLocation targetLoc) throws GameActionException {
		Direction dir = null;
		if (targetLoc.equals(navTargetLoc)) {
			if (navType == NAVTYPE_BUG && !pathExists(myLoc, targetLoc)) {
				rc.setIndicatorString(0, "trying to bug again");
				dir = safeBugNavDirection(targetLoc);
			} else {
				rc.setIndicatorString(0, "trying to path again");
				dir = safePathFindingDirection(targetLoc);
				if (dir == null) {
					rc.setIndicatorString(0, "failed path again, trying to bug again");
					navType = 0;
					dir = safeBugNavDirection(targetLoc);
				}
			}
		} else {
			navType = 0;
			rc.setIndicatorString(0, "trying to path first");
			dir = safePathFindingDirection(targetLoc);
			if (dir == null) {
				rc.setIndicatorString(0, "failed path first, trying to bug first");
				dir = safeBugNavDirection(targetLoc);
			}
		}
		if (dir == null) {
			rc.setIndicatorString(1, "epic fail");
			return false;
		}
		rc.move(dir);
		return true;
	}
	
	private static boolean canMoveSafely(Direction dir) {
		return !inEnemyBuildingRange(myLoc.add(dir));
	}
	
	private static boolean canMove(Direction dir, boolean safety) {
		return (safety ? (rc.canMove(dir) && canMoveSafely(dir)) : rc.canMove(dir));
	}
	
	private static Direction bugNavDirection(MapLocation targetLoc) throws GameActionException {
		return bugNavDirection(targetLoc, false);
	}
	
	private static Direction safeBugNavDirection(MapLocation targetLoc) throws GameActionException {
		return bugNavDirection(targetLoc, true);
	}

	private static Direction bugNavDirection(MapLocation targetLoc, boolean safety) throws GameActionException {
		if (!targetLoc.equals(navTargetLoc) || navType != NAVTYPE_BUG) {
			// new bugnav session
			navTargetLoc = targetLoc;
			navType = NAVTYPE_BUG;
			bugNavWinding = 0;
			navClosestDistSq = myLoc.distanceSquaredTo(targetLoc);
		}
		Direction targetDir = myLoc.directionTo(targetLoc);
		int targetDirNum = targetDir.ordinal();
		int currentDirNum = (((targetDirNum + bugNavWinding) % 8) + 8) % 8;
		Direction currentDir = directions[currentDirNum];
		int countWinds = 0;
		// invariant: currentDir = directions[currentDirNum]
		// invariant: currentDirNum - targetDirNum = bugNavWinding in mod 8
		if (leftHanded) {
			// move condition for left handed: (bugNavWinding == 0 && rc.canMove(currentDir)) || (!rc.canMove(currentDir.rotateRight()) && rc.canMove(currentDir))
			while (!((bugNavWinding == 0 && canMove(currentDir, safety)) || (!canMove(currentDir.rotateRight(), safety) && canMove(currentDir, safety)))) { // loop if the move condition is false
				// known: bugNavWinding != 0 || !rc.canMove(currentDir)
				// known: rc.canMove(currentDir.rotateRight()) || !rc.canMove(currentDir)
				if (canMove(currentDir.rotateRight(), safety) && bugNavWinding != 0) {
					bugNavWinding++;
				} else {
					// known: !rc.canMove(currentDir)
					bugNavWinding--;
					countWinds++;
				}
				// update invariants
				currentDirNum = (((targetDirNum + bugNavWinding) % 8) + 8) % 8;
				currentDir = directions[currentDirNum];
				// watchdog for totally blocked
				if (countWinds >= 8) {
					bugNavWinding += 8;
					return null;
				}
			}
		} else {
			// move condition for right handed: (bugNavWinding == 0 && rc.canMove(currentDir)) || (!rc.canMove(currentDir.rotateLeft()) && rc.canMove(currentDir))
			while (!((bugNavWinding == 0 && canMove(currentDir, safety)) || (!canMove(currentDir.rotateLeft(), safety) && canMove(currentDir, safety)))) { // loop if the move condition is false
				// known: bugNavWinding != 0 || !rc.canMove(currentDir)
				// known: rc.canMove(currentDir.rotateLeft()) || !rc.canMove(currentDir)
				if (canMove(currentDir.rotateLeft(), safety) && bugNavWinding != 0) {
					bugNavWinding--;
				} else {
					// known: !rc.canMove(currentDir)
					bugNavWinding++;
					countWinds++;
				}
				// update invariants
				currentDirNum = (((targetDirNum + bugNavWinding) % 8) + 8) % 8;
				currentDir = directions[currentDirNum];
				// watchdog for totally blocked
				if (countWinds >= 8) {
					bugNavWinding -= 8;
					return null;
				}
			}
		}
		int currentDistSq = myLoc.distanceSquaredTo(targetLoc);
		if (currentDistSq < navClosestDistSq) {
			navClosestDistSq = currentDistSq;
		} else if (Math.sqrt(currentDistSq) > Math.sqrt(navClosestDistSq)*(Math.pow(2, bugNavFallTimes+1)) + 10) {
			leftHanded = !leftHanded;
			bugNavWinding = 0;
			navClosestDistSq = currentDistSq;
			bugNavFallTimes++;
		}
		return currentDir;
	}
	
	private static Direction pathFindingDirection(MapLocation targetLoc) throws GameActionException {
		return pathFindingDirection(targetLoc, false);
	}
	
	private static Direction safePathFindingDirection(MapLocation targetLoc) throws GameActionException {
		return pathFindingDirection(targetLoc, true);
	}
	
	private static Direction pathFindingDirection(MapLocation targetLoc, boolean safety) throws GameActionException {
		if (!targetLoc.equals(navTargetLoc) || navType != NAVTYPE_PRECOMP || navSourceLoc.distanceSquaredTo(myLoc) > 2) {
			navTargetLoc = targetLoc;
			navSourceLoc = myLoc;
			navType = NAVTYPE_PRECOMP;
			foundPath = pathFind(myLoc, navTargetLoc);
			foundPathIndex = 0;
		}
		if (foundPath == null || foundPath[0] == null) {
			return null;
		}

		MapLocation currentLoc = foundPath[foundPathIndex];
		while (currentLoc != null && currentLoc.distanceSquaredTo(myLoc) <= 8) {
			foundPathIndex++;
			currentLoc = foundPath[foundPathIndex];
		}
		if (currentLoc == null) {
			return null;
		}
		Direction d = myLoc.directionTo(currentLoc);
		navSourceLoc = myLoc.add(d);
		if (canMove(d, safety)) {
			return d;
		} else if (canMove(d.rotateLeft(), safety)) {
			return d.rotateLeft();
		} else if (canMove(d.rotateRight(), safety)) {
			return d.rotateRight();
		}else if (canMove(d.rotateLeft().rotateLeft(), safety)) {
			return d.rotateLeft().rotateLeft();
		} else if (canMove(d.rotateRight().rotateRight(), safety)) {
			return d.rotateRight().rotateRight();
		}
		return null;
	}
	
	private static boolean leftSideOfMap() {
		int rushVecX = enemyHQLoc.x - myHQLoc.x;
		int rushVecY = enemyHQLoc.y - myHQLoc.y;
		int myVecX = myLoc.x - myHQLoc.x;
		int myVecY = myLoc.y - myHQLoc.y;
		int crossProduct = (rushVecX * myVecY) - (rushVecY * myVecX);
		return crossProduct < 0;
	}
	
	private static boolean isLocationNextToVoid(MapLocation loc) {
		for (int i = 0; i < 8; i++) {
			if (rc.senseTerrainTile(loc.add(directions[i])) != TerrainTile.NORMAL) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isLocationBlocking(MapLocation loc) {
		int risingEdges = 0;
		int fallingEdges = 0;
		boolean lastDirBlocked = !rc.isPathable(BEAVER, loc.add(directions[0]));
		for (int i = 1; i < 8; i++) {
			Direction dir = directions[i];
			if (rc.isPathable(BEAVER, loc.add(directions[i]))) {
				if (lastDirBlocked) {
					fallingEdges++;
				}
			} else {
				if (!lastDirBlocked) {
					risingEdges++;
				}
			}
		}
		int connectivity = Math.max(risingEdges, fallingEdges);
		return connectivity > 1;
	}
	
	private static int getNavMapSquareChannel(MapLocation loc) {
		int x = ((loc.x - myHQLoc.x) + NAVMAP_WIDTH) % NAVMAP_WIDTH;
		int y = ((loc.y - myHQLoc.y) + NAVMAP_HEIGHT) % NAVMAP_HEIGHT;
		return NAVMAP_CHAN + NAVMAP_SQUARE_SIZE * ((NAVMAP_WIDTH * y) + x);
	}
	
	private static short readNavMapBits(MapLocation loc, int waypoint) throws GameActionException {
		int channel = getNavMapSquareChannel(loc) + (waypoint / 2);
		int dataInt = rc.readBroadcast(channel);
		short dataShort = (waypoint % 2 == 0) ? (short)(dataInt >>> 16) : (short)(dataInt & 0xFFFF);
		return dataShort;
	}
	
	private static void writeNavMapBits(MapLocation loc, int waypoint, short bits) throws GameActionException {
		int channel = getNavMapSquareChannel(loc) + (waypoint / 2);
		int dataInt = rc.readBroadcast(channel);
		dataInt = (waypoint % 2 == 0) ? ((bits << 16) | (dataInt & 0xFFFF)) : ((dataInt & 0xFFFF0000) | (bits & 0xFFFF));
		rc.broadcast(channel, dataInt);
	}
	
	private static boolean isLocationMarkedUnknown(MapLocation loc) throws GameActionException {
		return readNavMapBits(loc, 7) == 1;
	}
	
	private static void markLocationUnknown(MapLocation loc) throws GameActionException {
		writeNavMapBits(loc, 7, (short)1);
	}
	
	private static int packLocation(MapLocation loc) {
		return (((short)loc.x) << 16) | (((short)loc.y) & 0xFFFF);
	}
	
	private static MapLocation unpackLocation(int packedLoc) {
		return new MapLocation((short)(packedLoc >>> 16), (short)(packedLoc & 0xFFFF));
	}
	
	private static void addToNavQueue(int waypoint, MapLocation loc) throws GameActionException {
		int startPtr = rc.readBroadcast(NAVQUEUE_STARTPTR_CHAN + waypoint);
		int endPtr = rc.readBroadcast(NAVQUEUE_ENDPTR_CHAN + waypoint);
		int size = ((endPtr - startPtr) + NAVQUEUE_QUEUE_SIZE) % NAVQUEUE_QUEUE_SIZE;
		if (size >= NAVQUEUE_QUEUE_SIZE - 1) {
			System.out.println("Nav queue full!");
			return;
		}
		int packedLoc = packLocation(loc);
		rc.broadcast(NAVQUEUE_CHAN + (waypoint * NAVQUEUE_QUEUE_SIZE) + endPtr, packedLoc);
		endPtr = (endPtr + 1) % NAVQUEUE_QUEUE_SIZE;
		rc.broadcast(NAVQUEUE_ENDPTR_CHAN + waypoint, endPtr);
	}
	
	private static MapLocation popFromNavQueue(int waypoint) throws GameActionException {
		int startPtr = rc.readBroadcast(NAVQUEUE_STARTPTR_CHAN + waypoint);
		int endPtr = rc.readBroadcast(NAVQUEUE_ENDPTR_CHAN + waypoint);
		int size = ((endPtr - startPtr) + NAVQUEUE_QUEUE_SIZE) % NAVQUEUE_QUEUE_SIZE;
		if (size <= 0) { // queue empty
			return null;
		}
		int packedLoc = rc.readBroadcast(NAVQUEUE_CHAN + (waypoint * NAVQUEUE_QUEUE_SIZE) + startPtr);
		startPtr = (startPtr + 1) % NAVQUEUE_QUEUE_SIZE;
		rc.broadcast(NAVQUEUE_STARTPTR_CHAN + waypoint, startPtr);
		return unpackLocation(packedLoc);
	}
	
	private static void addToUnknownQueue(MapLocation loc) throws GameActionException {
		int startPtr = rc.readBroadcast(UNKNOWN_QUEUE_STARTPTR_CHAN);
		int endPtr = rc.readBroadcast(UNKNOWN_QUEUE_ENDPTR_CHAN);
		int size = ((endPtr - startPtr) + UNKNOWN_QUEUE_SIZE) % UNKNOWN_QUEUE_SIZE;
		if (size >= UNKNOWN_QUEUE_SIZE - 1) {
			System.out.println("Unknown queue full!");
			return;
		}
		int packedLoc = packLocation(loc);
		rc.broadcast(UNKNOWN_QUEUE_CHAN + endPtr, packedLoc);
		endPtr = (endPtr + 1) % UNKNOWN_QUEUE_SIZE;
		rc.broadcast(UNKNOWN_QUEUE_ENDPTR_CHAN, endPtr);
	}
	
	private static MapLocation popFromUnknownQueue() throws GameActionException {
		int startPtr = rc.readBroadcast(UNKNOWN_QUEUE_STARTPTR_CHAN);
		int endPtr = rc.readBroadcast(UNKNOWN_QUEUE_ENDPTR_CHAN);
		int size = ((endPtr - startPtr) + UNKNOWN_QUEUE_SIZE) % UNKNOWN_QUEUE_SIZE;
		if (size <= 0) { // queue empty
			return null;
		}
		int packedLoc = rc.readBroadcast(UNKNOWN_QUEUE_CHAN + startPtr);
		startPtr = (startPtr + 1) % UNKNOWN_QUEUE_SIZE;
		rc.broadcast(UNKNOWN_QUEUE_STARTPTR_CHAN, startPtr);
		return unpackLocation(packedLoc);
	}
	
	private static void initializePathfindingQueues() throws GameActionException {
		MapLocation[] towerLocs = rc.senseTowerLocations();
		int numTowers = towerLocs.length;
		// broadcast waypoint info
		rc.broadcast(NAVMAP_NUM_WAYPOINTS_CHAN, numTowers + 1);
		short rootBits = -0x8000;
		short adjBits = 0x4000;
		
		// for hq
		rc.broadcast(NAVMAP_WAYPOINTS_CHAN, packLocation(myHQLoc));
		writeNavMapBits(myHQLoc, 0, rootBits);
		for (int dirNum = 0; dirNum < 8; dirNum++) {
			Direction dir = directions[dirNum];
			MapLocation adjLoc = myHQLoc.add(dir);
			TerrainTile adjTile = rc.senseTerrainTile(adjLoc);
			if (adjTile == TerrainTile.NORMAL || adjTile == TerrainTile.UNKNOWN) {
				writeNavMapBits(adjLoc, 0, adjBits);
				addToNavQueue(0, adjLoc);
			}
		}
		rc.broadcast(NAVMAP_STILL_PATHFINDING_CHAN, 1);
		
		// for towers
		for (int i = numTowers; --i >= 0;) {
			MapLocation towerLoc = towerLocs[i];
			rc.broadcast(NAVMAP_WAYPOINTS_CHAN + 1 + i, packLocation(towerLoc));
			writeNavMapBits(towerLoc, i+1, rootBits);
			for (int dirNum = 0; dirNum < 8; dirNum++) {
				Direction dir = directions[dirNum];
				MapLocation adjLoc = towerLoc.add(dir);
				TerrainTile adjTile = rc.senseTerrainTile(adjLoc);
				if (adjTile == TerrainTile.NORMAL || adjTile == TerrainTile.UNKNOWN) {
					writeNavMapBits(adjLoc, i+1, adjBits);
					addToNavQueue(i+1, adjLoc);
				}
			}
			rc.broadcast(NAVMAP_STILL_PATHFINDING_CHAN + 1 + i, 1);
		}
		
		// unknown queue is initialized by default
	}
	
	// TODO: prioritize pathfinding
	private static void precomputePathfindingAndYield() throws GameActionException {
		precomputePathfindingAndYield(0);
	}
	
	private static void precomputePathfindingAndYield(int waypoint) throws GameActionException {
		if (rc.readBroadcast(NAVMAP_STILL_PATHFINDING_CHAN + waypoint) == 1) {
			int roundNum = Clock.getRoundNum();
			while (Clock.getRoundNum() == roundNum) {
				MapLocation loc = popFromNavQueue(waypoint);
				if (loc == null) {
					// finished
					traverseUnknownQueueAndYield();
					return;
				}
				TerrainTile tile = rc.senseTerrainTile(loc);
				MapLocation symmetricLoc = symmetricLocation(loc);
				TerrainTile symmetricTile = (symmetricLoc != null) ? rc.senseTerrainTile(symmetricLoc) : null;
				if (tile == TerrainTile.NORMAL || symmetricTile == TerrainTile.NORMAL) {
					int bestCost = 99999999; // never negative
					Direction bestDir = null;
					for (int dirNum = 8; --dirNum >= 0;) {
						Direction dir = directions[dirNum];
						MapLocation adjLoc = loc.add(dir);
						TerrainTile adjTile = rc.senseTerrainTile(adjLoc);
						MapLocation symmetricAdjLoc = symmetricLocation(adjLoc);
						TerrainTile symmetricAdjTile = (symmetricAdjLoc != null) ? rc.senseTerrainTile(symmetricAdjLoc) : null;
						if (adjTile == TerrainTile.NORMAL || symmetricAdjTile == TerrainTile.NORMAL) {
							short adjBits = readNavMapBits(adjLoc, waypoint);
							if ((adjBits & -0x8000) == -0x8000) { // if done
								int newCost = (adjBits & 0x07ff) + (dir.isDiagonal() ? 7 : 5); // move cost function
								if (newCost < bestCost || (newCost <= bestCost && !dir.isDiagonal())) { // if lowest cost found yet
									bestCost = newCost;
									bestDir = dir;
								}
							} else if ((adjBits & 0x4000) == 0x0000) { // if not queued
								writeNavMapBits(adjLoc, waypoint, (short)0x4000); // mark as queued
								addToNavQueue(waypoint, adjLoc);
							}
						} else if (adjTile == TerrainTile.UNKNOWN && symmetricAdjTile == TerrainTile.UNKNOWN) {
							short adjBits = readNavMapBits(adjLoc, waypoint);
							if ((adjBits & 0x4000) == 0x0000) { // if not queued
								writeNavMapBits(adjLoc, waypoint, (short)0x4000); // mark as queued
								markLocationUnknown(adjLoc);
								addToUnknownQueue(adjLoc);
							} else {
							}
						}
					}
					if (bestDir == null) {
						System.out.println("pathfinding error: bestDir = null");
					} else {
						short bits = (short)(-0x8000 | (bestDir.ordinal() << 11) | (bestCost & 0x07ff));
						writeNavMapBits(loc, waypoint, bits);
						if (isOnSymmetryBoundary(loc)) {
							int bestSymmetryCost = rc.readBroadcast(NAVMAP_SYMMETRY_COSTS_CHAN + waypoint);
							if (bestCost < bestSymmetryCost) {
								rc.broadcast(NAVMAP_SYMMETRY_LOCS_CHAN + waypoint, packLocation(loc));
								rc.broadcast(NAVMAP_SYMMETRY_COSTS_CHAN + waypoint, bestCost);
							}
						}
					}
				} else if (tile == TerrainTile.UNKNOWN && symmetricTile == TerrainTile.UNKNOWN) {
					markLocationUnknown(loc);
					addToUnknownQueue(loc);
				}
			}
			
		}
	}
	
	private static void traverseUnknownQueueAndYield() throws GameActionException {
		int roundNum = Clock.getRoundNum();
		int numWaypoints = rc.readBroadcast(NAVMAP_NUM_WAYPOINTS_CHAN);
		while (Clock.getRoundNum() == roundNum) {
			MapLocation loc = popFromUnknownQueue();
			if (loc == null) {
				// finished
				rc.yield();
				return;
			}
			TerrainTile tile = rc.senseTerrainTile(loc);
			MapLocation symmetricLoc = symmetricLocation(loc);
			TerrainTile symmetricTile = (symmetricLoc != null) ? rc.senseTerrainTile(symmetricLoc) : null;
			if (tile == TerrainTile.NORMAL || symmetricTile == TerrainTile.NORMAL) {
				for (int waypoint = 0; waypoint < numWaypoints; waypoint++) {
					short bits = readNavMapBits(loc, waypoint);
					if ((bits & 0x4000) != 0) { // if queued by this waypoint
						addToNavQueue(waypoint, loc);
					}
				}
			} else if (tile == TerrainTile.UNKNOWN && symmetricTile == TerrainTile.UNKNOWN) {
				addToUnknownQueue(loc);
			}
		}
	}
	
	private static boolean pathExists(MapLocation startLoc, MapLocation endLoc) throws GameActionException {
		MapLocation symmetricStartLoc = symmetricLocation(startLoc);
		MapLocation symmetricEndLoc = symmetricLocation(endLoc);
		final int waypoint = 0;
		short startBits = readNavMapBits(startLoc, waypoint);
		short endBits = readNavMapBits(endLoc, waypoint);
		short symmetricStartBits = readNavMapBits(symmetricStartLoc, waypoint);
		short symmetricEndBits = readNavMapBits(symmetricEndLoc, waypoint);
		boolean startDone = ((startBits & -0x8000) != 0);
		boolean endDone = ((endBits & -0x8000) != 0);
		boolean symmetricStartDone = ((symmetricStartBits & -0x8000) != 0);
		boolean symmetricEndDone = ((symmetricEndBits & -0x8000) != 0);
		if (startDone && endDone) {
			return true;
		} else if (symmetricStartDone && symmetricEndDone) {
			return true;
		}
		return false;
	}
	
	//TODO: better drawstring on pathfinding
	private static MapLocation[] pathFind(MapLocation startLoc, MapLocation endLoc) throws GameActionException {
		int numWaypoints = rc.readBroadcast(NAVMAP_NUM_WAYPOINTS_CHAN);
		MapLocation symmetricStartLoc = symmetricLocation(startLoc);
		MapLocation symmetricEndLoc = symmetricLocation(endLoc);
		int bestCost = 9999999;
		int bestWaypoint = -1;
		boolean bestSymmetric = false;
		for (int waypoint = 0; waypoint < numWaypoints; waypoint++) {
			short startBits = readNavMapBits(startLoc, waypoint);
			short endBits = readNavMapBits(endLoc, waypoint);
			short symmetricStartBits = readNavMapBits(symmetricStartLoc, waypoint);
			short symmetricEndBits = readNavMapBits(symmetricEndLoc, waypoint);
			boolean startDone = ((startBits & -0x8000) != 0);
			boolean endDone = ((endBits & -0x8000) != 0);
			boolean symmetricStartDone = ((symmetricStartBits & -0x8000) != 0);
			boolean symmetricEndDone = ((symmetricEndBits & -0x8000) != 0);
			if (startDone && endDone) {
				int cost = (startBits & 0x07FF) + (endBits & 0x07FF);
				if (cost < bestCost) {
					bestCost = cost;
					bestWaypoint = waypoint;
					bestSymmetric = false;
				}
			} else if (symmetricStartDone && symmetricEndDone) {
				int cost = (symmetricStartBits & 0x07FF) + (symmetricEndBits & 0x07FF);
				if (cost < bestCost) {
					bestCost = cost;
					bestWaypoint = waypoint;
					bestSymmetric = true;
				}
			}

		}
		if (bestWaypoint < 0) {
			return null;
		}
		MapLocation[] startPath = new MapLocation[1000];
		int startPathSize = 0;
		MapLocation[] endPath = new MapLocation[1000];
		int endPathSize = 0;
		if (bestSymmetric) {
			startLoc = symmetricLocation(startLoc);
			endLoc = symmetricLocation(endLoc);
		}
		MapLocation waypointLoc = unpackLocation(rc.readBroadcast(NAVMAP_WAYPOINTS_CHAN + bestWaypoint));
		MapLocation currentLoc = startLoc;
		while (!currentLoc.equals(waypointLoc)) {
			short bits = readNavMapBits(currentLoc, bestWaypoint);
			int dirNum = (bits & 0x3800) >>> 11;
			Direction dir = directions[dirNum];
			currentLoc = currentLoc.add(dir);
			if (bestSymmetric) {
				startPath[startPathSize] = symmetricLocation(currentLoc);
			} else {
				startPath[startPathSize] = currentLoc;
			}
			startPathSize++;
		}
		currentLoc = endLoc;
		while (!currentLoc.equals(waypointLoc)) {
			short bits = readNavMapBits(currentLoc, bestWaypoint);
			int dirNum = (bits & 0x3800) >>> 11;
			Direction dir = directions[dirNum];
			currentLoc = currentLoc.add(dir);
			if (bestSymmetric) {
				endPath[endPathSize] = symmetricLocation(currentLoc);
			} else {
				endPath[endPathSize] = currentLoc;
			}
			endPathSize++;
		}
		// drawstring
		while (startPathSize > 2 && endPathSize > 2) {
			MapLocation currentStartLoc = startPath[startPathSize - 1];
			MapLocation prevStartLoc = startPath[startPathSize - 2];
			MapLocation currentEndLoc = endPath[endPathSize - 1];
			MapLocation prevEndLoc = endPath[endPathSize - 2];
			if (isPathSimple(prevStartLoc, prevEndLoc)) {
				int currentCost = straightPathCost(currentStartLoc, currentEndLoc);
				int prevCost = straightPathCost(prevStartLoc, prevEndLoc);
				currentCost += prevStartLoc.directionTo(currentStartLoc).isDiagonal() ? 7 : 5;
				currentCost += prevEndLoc.directionTo(currentEndLoc).isDiagonal() ? 7 : 5;
				if (prevCost <= currentCost) {
					startPathSize--;
					startPath[startPathSize] = null;
					endPathSize--;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		for (; --endPathSize >= 0; startPathSize++) {
			startPath[startPathSize] = endPath[endPathSize];
		}
		return startPath;
	}
	
	private static boolean isPathSimple(MapLocation startLoc, MapLocation endLoc) {
		MapLocation currentLoc = startLoc;
		while (currentLoc.distanceSquaredTo(endLoc) > 2) {
			currentLoc = currentLoc.add(currentLoc.directionTo(endLoc));
			if (rc.senseTerrainTile(currentLoc) != TerrainTile.NORMAL) {
				return false;
			}
		}
		return true;
	}

	private static int straightPathCost(MapLocation startLoc, MapLocation endLoc) {
		int x = Math.abs(endLoc.x - startLoc.x);
		int y = Math.abs(endLoc.y - startLoc.y);
		int diagonals = Math.min(x, y);
		int cardinals = Math.max(x, y) - diagonals;
		return (cardinals * 5) + (diagonals * 7);
	}
	
	private static boolean isOnSymmetryBoundary(MapLocation loc) throws GameActionException {
		MapLocation symmetricLoc = symmetricLocation(loc);
		if (loc.distanceSquaredTo(symmetricLoc) <= 2) {
			return true;
		} else {
			return false;
		}
	}
	
	private static MapLocation symmetricLocation(MapLocation loc) throws GameActionException {
		int relX = loc.x - myHQLoc.x;
		int relY = loc.y - myHQLoc.y;
		MapLocation symmetricLoc = null;
		int symmetryType = rc.readBroadcast(SYMMETRY_TYPE_CHAN);
		switch (symmetryType) {
		case (SYMMETRY_TYPE_HORIZONTAL):
			symmetricLoc = new MapLocation(enemyHQLoc.x + relX, enemyHQLoc.y - relY);
			break;
		case (SYMMETRY_TYPE_VERTICAL):
			symmetricLoc = new MapLocation(enemyHQLoc.x - relX, enemyHQLoc.y + relY);
			break;
		case (SYMMETRY_TYPE_DIAGONAL_POS):
			symmetricLoc = new MapLocation(enemyHQLoc.x + relY, enemyHQLoc.y + relX);
			break;
		case (SYMMETRY_TYPE_DIAGONAL_NEG):
			symmetricLoc = new MapLocation(enemyHQLoc.x - relY, enemyHQLoc.y - relX);
			break;
		case (SYMMETRY_TYPE_ROTATIONAL):
			symmetricLoc = new MapLocation(enemyHQLoc.x - relX, enemyHQLoc.y - relY);
			break;
		}
		return symmetricLoc;
	}
	
	private static Direction symmetricDirection(Direction dir) throws GameActionException {
		Direction symmetricDir = null;
		int symmetryType = rc.readBroadcast(SYMMETRY_TYPE_CHAN);
		switch (symmetryType) {
		case (SYMMETRY_TYPE_HORIZONTAL):
			symmetricDir = diffsToDir(dir.dx, -dir.dy);
		break;
		case (SYMMETRY_TYPE_VERTICAL):
			symmetricDir = diffsToDir(-dir.dx, dir.dy);
		break;
		case (SYMMETRY_TYPE_DIAGONAL_POS):
			symmetricDir = diffsToDir(dir.dy, dir.dx);
		break;
		case (SYMMETRY_TYPE_DIAGONAL_NEG):
			symmetricDir = diffsToDir(-dir.dy, -dir.dx);
		break;
		case (SYMMETRY_TYPE_ROTATIONAL):
			symmetricDir = diffsToDir(-dir.dx, -dir.dy);
		break;
		}
		return symmetricDir;
	}
	
	private static Direction diffsToDir(int dx, int dy) {
		Direction dir = null;
		switch (dx) {
		case -1:
			switch (dy) {
			case -1: return Direction.NORTH_WEST;
			case 0: return Direction.WEST;
			case 1: return Direction.SOUTH_WEST;
			}
			break;
		case 0:
			switch (dy) {
			case -1: return Direction.NORTH;
			case 1: return Direction.SOUTH;
			}
			break;
		case 1:
			switch (dy) {
			case -1: return Direction.NORTH_EAST;
			case 0: return Direction.EAST;
			case 1: return Direction.SOUTH_EAST;
			}
			break;
		}
		return null;
	}
	
	private static void debug_showNavMap(int waypoint) throws GameActionException {
		int xmin = westFarthest();
		int xmax = eastFarthest();
		int ymin = northFarthest();
		int ymax = southFarthest();
		if (xmin != NO_BOUND && xmax != NO_BOUND && ymin != NO_BOUND && ymax != NO_BOUND) {
			for (int i = xmin; i <= xmax; i++) {
				for (int j = ymin; j <= ymax; j++) {
					MapLocation loc = new MapLocation(i, j);
					short bits = readNavMapBits(loc, waypoint);
					if ((bits & -0x8000) != 0) { // if done
						int dirNum = (bits & 0x3800) >> 11;
						Direction dir = directions[dirNum];
						rc.setIndicatorLine(loc, loc.add(dir), 0, 255, 0);
						
					}
				}
			}
		}
	}
	
	private static boolean isEnemyTowerLoc(MapLocation loc) {
		for (MapLocation towerLoc : enemyTowerLocs) {
			if (towerLoc.equals(loc)) {
				return true;
			}
		}
		return false;
	}
	
	private static void initializeRallyLocs() throws GameActionException {
		MapLocation bestLoc = null;
		int bestDistSq = 9999999;
		MapLocation secBestLoc = null;
		int secBestDistSq = 9999999;
		for (MapLocation loc : myTowerLocs) {
			int distSq = loc.distanceSquaredTo(mapCenter);
			if (distSq < bestDistSq) {
				secBestLoc = bestLoc;
				secBestDistSq = bestDistSq;
				bestLoc = loc;
				bestDistSq = distSq;
			} else if (distSq < secBestDistSq) {
				secBestLoc = loc;
				secBestDistSq = distSq;
			}
		}
		rc.broadcast(RALLY_POINT_NUM_CHAN, secBestLoc == null ? 1 : 2);
		rc.broadcast(RALLY_POINT_CHAN, packLocation(bestLoc));
		if (secBestLoc != null)
			rc.broadcast(RALLY_POINT_CHAN + 1, packLocation(secBestLoc));
		rc.broadcast(SAFETY_RADIUS_SQ_CHAN, (int)Math.pow(Math.sqrt(myHQLoc.distanceSquaredTo(bestLoc)) + 8, 2));
	}
	
	private static MapLocation getClosestRallyPoint() throws GameActionException {
		MapLocation rallyPoint = unpackLocation(rc.readBroadcast(RALLY_POINT_CHAN));
		if (rc.readBroadcast(RALLY_POINT_NUM_CHAN) > 1) {
			MapLocation otherRallyPoint = unpackLocation(rc.readBroadcast(RALLY_POINT_CHAN + 1));
			if (otherRallyPoint.distanceSquaredTo(myLoc) < rallyPoint.distanceSquaredTo(myLoc)) {
				rallyPoint = otherRallyPoint;
			}
		}
		return rallyPoint;
	}
	
	private static void updateEnemyHQHealth() throws GameActionException {
		if (rc.canSenseLocation(enemyHQLoc))
			enemyHQHealth = rc.senseRobotAtLocation(enemyHQLoc).health;
	}
	
	private static void updateEnemyTowerHealth() throws GameActionException {
		for (int i = 0; i < originalEnemyTowerLocs.length; i++) {
			MapLocation loc = originalEnemyTowerLocs[i];
			if (rc.canSenseLocation(loc)) {
				RobotInfo ri = rc.senseRobotAtLocation(loc);
				if (ri != null) {
					enemyTowerHealths[i] = ri.health;
				} else {
					enemyTowerHealths[i] = 0;
				}
			}
		}
		
	}
	
	private static boolean winning() throws GameActionException {
		if (myTowerLocs.length > enemyTowerLocs.length) {
			return true;
		} else if (myTowerLocs.length < enemyTowerLocs.length) {
			return false;
		} else {
			if (rc.getHealth() > enemyHQHealth) {
				return true;
			} else if (rc.getHealth() < enemyHQHealth) {
				return false;
			} else {
				double totalEnemyTowerHealth = 0;
				for (int i = 0; i < enemyTowerHealths.length; i++) {
					totalEnemyTowerHealth += enemyTowerHealths[i];
				}
				double totalMyTowerHealth = 0;
				for (int i = 0; i < myTowerLocs.length; i++) {
					MapLocation loc = myTowerLocs[i];
					if (rc.canSenseLocation(loc)) {
						RobotInfo ri = rc.senseRobotAtLocation(loc);
						if (ri != null) {
							totalMyTowerHealth += ri.health;
						} else {
							totalMyTowerHealth += 0;
						}
					}
				}
				if (totalMyTowerHealth > totalEnemyTowerHealth) {
					return true;
				} else if (totalMyTowerHealth < totalEnemyTowerHealth) {
					return false;
				} else {
					return false; // always build more handwash stations
				}
			}
		}
	}
	
	private static double distance(MapLocation l1, MapLocation l2){
		return Math.sqrt((l1.x - l2.x)*(l1.x - l2.x)+(l1.y - l2.y)*(l1.y - l2.y));
	}
}
