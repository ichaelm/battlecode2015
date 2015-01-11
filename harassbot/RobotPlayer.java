package harassbot;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {

	// AI parameters
	private static final int RUSH_TURN = 1800;
	private static final int HARASS_BORDER_BUFFER = 3;
	private static final int ARRAY_SIZE = 200;
	
	private static final int MSG_SPACE = 65532; // minus 4
	private static final int MSG_LEN = 6; // ID, order, x, y, marked, mining
	private static final int NUM_MSG = MSG_SPACE / MSG_LEN;
	private static final int NORTH_BOUND_CHAN = 65532;
	private static final int EAST_BOUND_CHAN = 65533;
	private static final int SOUTH_BOUND_CHAN = 65534;
	private static final int WEST_BOUND_CHAN = 65535;
	private static final int NO_BOUND = 99999;

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
	private static int lastNumMiners;
	private static int harassCooldown;
	private static boolean launched;
	private static boolean leftHanded;
	private static int startTimer;
	private static final Direction[] directions = Direction.values(); //{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private static final RobotType[] robotTypes = RobotType.values();
	private static int[] offsets = {0,1,-1,2,-2};
	

	public static void run(RobotController myrc) {
		// Initialize cached game information
		rc = myrc;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		if (myrc.getType() == RobotType.MISSILE) { //hack for missiles to run faster
			rc.setIndicatorString(1, "Bytecodes Used: " + Clock.getBytecodeNum() + " Left: " + Clock.getBytecodesLeft());
			runMissile();
		}
		myType = rc.getType();
		myRange = myType.attackRadiusSquared;
		rand = new Random(rc.getID());
		HQLoc = rc.senseHQLocation();
		enemyHQLoc = rc.senseEnemyHQLocation();
		rushDist = HQLoc.distanceSquaredTo(enemyHQLoc);
		minerTarget = null;
		brandNew = true;
		lastNumMiners = 0;
		harassCooldown = 0;
		launched = false;
		leftHanded = false;
		startTimer = 0;

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
		double[] oreConsumptionByType = {5, 0, 0, 0.4, 4, 25/6, 1.25, 5, 2.5, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
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
				
				if (brandNew) {
					rc.broadcast(NORTH_BOUND_CHAN, NO_BOUND);
					rc.broadcast(EAST_BOUND_CHAN, NO_BOUND);
					rc.broadcast(SOUTH_BOUND_CHAN, NO_BOUND);
					rc.broadcast(WEST_BOUND_CHAN, NO_BOUND);
				}

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
				
				if (harassCooldown > 0) {
					harassCooldown--;
				}
				int numMiners = numRobotsByType[robotTypeToNum(RobotType.MINER)];
				if (numMiners < lastNumMiners) {
					harassCooldown = 15;
				}
				lastNumMiners = numMiners; // update
				
				// calculate macro build orders for all free units
				newBuildOrders = new RobotType[ARRAY_SIZE][2];
				numNewBuildOrders = 0;
				int teamOre = (int)rc.getTeamOre();
				int plannedTeamOre = teamOre;


				if (harassCooldown <= 0 && Clock.getRoundNum() < RUSH_TURN + 100 && Clock.getRoundNum() > 250 && (plannedTeamOre < 600 || estimatedOreConsumption * 1.3 >= estimatedOreGeneration)) {
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
				int numHelipads = numRobotsByType[robotTypeToNum(RobotType.HELIPAD)] + progressRobotsByType[robotTypeToNum(RobotType.HELIPAD)];
				int numAerospaceLabs = numRobotsByType[robotTypeToNum(RobotType.AEROSPACELAB)] + progressRobotsByType[robotTypeToNum(RobotType.AEROSPACELAB)];
				int numDrones = numRobotsByType[robotTypeToNum(RobotType.DRONE)] + progressRobotsByType[robotTypeToNum(RobotType.DRONE)];
				int numLaunchers = numRobotsByType[robotTypeToNum(RobotType.LAUNCHER)] + progressRobotsByType[robotTypeToNum(RobotType.LAUNCHER)];
				if (plannedTeamOre >= 1300 && estimatedOreConsumption < estimatedOreGeneration) {
					if (true) { // hack: used to be numDrones < 10 && Clock.getRoundNum() < 600
						// goal: build a helipad
						if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
							// goal: build a beaver
							order(RobotType.HQ, RobotType.BEAVER);
							plannedTeamOre -= RobotType.BEAVER.oreCost;
						} else {
							// goal: build a helipad
							order(RobotType.BEAVER, RobotType.HELIPAD);
							plannedTeamOre -= RobotType.HELIPAD.oreCost;
						}
					} else {
						// goal: build an aerospace lab
						if (numHelipads < 1) {
							// goal: build a helipad
							if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
								// goal: build a beaver
								order(RobotType.HQ, RobotType.BEAVER);
								plannedTeamOre -= RobotType.BEAVER.oreCost;
							} else {
								// goal: build a helipad
								order(RobotType.BEAVER, RobotType.HELIPAD);
								plannedTeamOre -= RobotType.HELIPAD.oreCost;
							}
						} else {
							if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
								// goal: build a beaver
								order(RobotType.HQ, RobotType.BEAVER);
								plannedTeamOre -= RobotType.BEAVER.oreCost;
							} else {
								// goal: build an aerospace lab
								order(RobotType.BEAVER, RobotType.AEROSPACELAB);
								plannedTeamOre -= RobotType.AEROSPACELAB.oreCost;
							}
						}
					}
				}
				if (plannedTeamOre >= 600 && estimatedOreConsumption * 0.5 < estimatedOreGeneration) {
					// goal: build more drones or launchers
					if (true) { // hack: used to be numDrones < 10 && Clock.getRoundNum() < 600
						// goal: build a drone
						if (numHelipads < 1) {
							// goal: build a helipad
							if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
								// goal: build a beaver
								order(RobotType.HQ, RobotType.BEAVER);
								plannedTeamOre -= RobotType.BEAVER.oreCost;
							} else {
								// goal: build a helipad
								order(RobotType.BEAVER, RobotType.HELIPAD);
								plannedTeamOre -= RobotType.HELIPAD.oreCost;
							}
						} else {
							// goal: build a drone
							if (numFreeRobotsByType[robotTypeToNum(RobotType.HELIPAD)] < 1) {
								// wait
							} else {
								// goal: build a launcher
								order(RobotType.HELIPAD, RobotType.DRONE);
								plannedTeamOre -= RobotType.DRONE.oreCost;
							}
						}
					} else {
						// goal: build a launcher
						if (numAerospaceLabs < 1) {
							// goal: build an aerospace lab
							if (numHelipads < 1) {
								// goal: build a helipad
								if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
									// goal: build a beaver
									order(RobotType.HQ, RobotType.BEAVER);
									plannedTeamOre -= RobotType.BEAVER.oreCost;
								} else {
									// goal: build a helipad
									order(RobotType.BEAVER, RobotType.HELIPAD);
									plannedTeamOre -= RobotType.HELIPAD.oreCost;
								}
							} else {
								if (numFreeRobotsByType[robotTypeToNum(RobotType.BEAVER)] < 1) {
									// goal: build a beaver
									order(RobotType.HQ, RobotType.BEAVER);
									plannedTeamOre -= RobotType.BEAVER.oreCost;
								} else {
									// goal: build an aerospace lab
									order(RobotType.BEAVER, RobotType.AEROSPACELAB);
									plannedTeamOre -= RobotType.AEROSPACELAB.oreCost;
								}
							}
						} else {
							// goal: build a launcher
							if (numFreeRobotsByType[robotTypeToNum(RobotType.AEROSPACELAB)] < 1) {
								// wait
							} else {
								// goal: build a launcher
								order(RobotType.AEROSPACELAB, RobotType.LAUNCHER);
								plannedTeamOre -= RobotType.LAUNCHER.oreCost;
							}
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
				brandNew = false;
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
							//mine();
							tryMove(directions[rand.nextInt(8)]);
						} else {
							if (ordersMarked(rc.getID())) {
								sendOrders(rc.getID(), -1, 0, 0);
								//mine();
								tryMove(directions[rand.nextInt(8)]);
							} else {
								if (rc.getTeamOre() < buildOrder.oreCost) {
									//mine();
									tryMove(directions[rand.nextInt(8)]);
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
				lookForBounds();
				if (rc.isWeaponReady()) {
					MapLocation attackLoc = droneAttackLocation();
					if (attackLoc != null) {
						rc.attackLocation(attackLoc);
					}
				}
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						harass();
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
				return; //temp
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
				launched = false;
				if (rc.getMissileCount() > 0) {
					MapLocation target = nearestEnemy();
					if (target != null) {
						launched = tryLaunch(rc.getLocation().directionTo(target));
					}
				}
				if (rc.isCoreReady()) {
					if (Clock.getRoundNum() < RUSH_TURN) {
						launcherRally();
					} else {
						MapLocation enemyLoc = nearestEnemy();
						if (enemyLoc == null) {
							launcherTryMove(rc.getLocation().directionTo(enemyHQLoc));
						} else {
							//tryMove(rc.getLocation().directionTo(enemyLoc));
						}
					}
				}
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
		try {
			while (true) {
				if (rc.isCoreReady()) {
					MapLocation myLoc = rc.getLocation();
					MapLocation target = fastNearestEnemy();
					if (target == null) {
						rc.disintegrate();
					} else {
						if (myLoc.distanceSquaredTo(target) <= 2) { // if adjacent
							quickTryMove(myLoc.directionTo(target));
							rc.explode();
						} else {
							quickTryMove(myLoc.directionTo(target));
						}
					}
				}
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
				} else { // hack to fix over-bytecode bug
					transferSupply();
				}
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
	
	private static int minOre = 5;

	private static void mine() throws GameActionException {
		int minOre = 5;
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
		return nearest;
	}

	private static void rally() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation invaderLoc = attackingEnemy();
		MapLocation enemy = nearestEnemy();
		if (enemy != null) {
			tryMove(myLoc.directionTo(enemy));
		} else {
			if (invaderLoc != null) {
				tryMove(myLoc.directionTo(invaderLoc));
			} else {
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
							boolean success = tryMove(myLoc.directionTo(minerTarget));
							if (!success) {
								minerTarget = null;
								tryMove(myLoc.directionTo(HQLoc).opposite());
							}
						}
					} else {
						double myOre = rc.senseOre(myLoc);
						if (myOre > 5) {
							tryMove(myLoc.directionTo(HQLoc));
						} else {
							tryMove(myLoc.directionTo(HQLoc).opposite());
						}
					}
				}
			}
		}
		brandNew = false;
	}
	
	private static void launcherRally() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation invaderLoc = attackingEnemy();
		MapLocation enemy = nearestEnemy();
		if (enemy != null) {
			//launcherTryMove(myLoc.directionTo(enemy));
		} else {
			if (invaderLoc != null) {
				launcherTryMove(myLoc.directionTo(invaderLoc));
			} else {
				if (brandNew) {
					// initial location finding
					minerTarget = findNearestMarkedMiner();
					if (minerTarget == null) {
						launcherTryMove(directions[rand.nextInt(8)]);
					} else {
						launcherTryMove(myLoc.directionTo(minerTarget));
					}
				} else {
					if (minerTarget != null) {
						// continue initial location finding
						if (myLoc.isAdjacentTo(minerTarget) || myLoc.equals(minerTarget)) {
							// reached target, use secondary location finding
							minerTarget = null;
							launcherTryMove(myLoc.directionTo(HQLoc).opposite());
						} else {
							// not reached target, continue initial location finding
							boolean success = launcherTryMove(myLoc.directionTo(minerTarget));
							if (!success) {
								minerTarget = null;
								launcherTryMove(myLoc.directionTo(HQLoc).opposite());
							}
						}
					} else {
						double myOre = rc.senseOre(myLoc);
						if (myOre > 5) {
							launcherTryMove(myLoc.directionTo(HQLoc));
						} else {
							launcherTryMove(myLoc.directionTo(HQLoc).opposite());
						}
					}
				}
			}
		}
		brandNew = false;
	}
	
	private static void harass() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(25, enemyTeam);
		MapLocation[] enemyTowerLocs = rc.senseEnemyTowerLocations();
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
		if (brandNew) {
			leftHanded = rand.nextBoolean();
		}
		int distX;
		int distY;
		int distSq;
		int[][] damageGrid = new int[3][3];
		boolean[][] canAttackGrid = new boolean[3][3];
		rc.setIndicatorString(0, "here1");
		for (int sourceX = -1; sourceX <= 1; sourceX++) {
			for (int sourceY = -1; sourceY <= 1; sourceY++) {
				distX = enemyHQLoc.x - (myLoc.x + sourceX);
				distY = enemyHQLoc.y - (myLoc.y + sourceY);
				distSq = (distX*distX) + (distY*distY);
				if (distSq <= enemyHQRange) {
					damageGrid[sourceX+1][sourceY+1] += enemyHQDamage;
				}
				for (MapLocation towerLoc : enemyTowerLocs) {
					distX = towerLoc.x - (myLoc.x + sourceX);
					distY = towerLoc.y - (myLoc.y + sourceY);
					distSq = (distX*distX) + (distY*distY);
					if (distSq <= enemyTowerRange) {
						damageGrid[sourceX+1][sourceY+1] += enemyTowerDamage;
					}
				}
			}
		}
		rc.setIndicatorString(0, "here2");
		for (RobotInfo ri : nearbyEnemies) {
			int targetX = ri.location.x - myLoc.x;
			int targetY = ri.location.y - myLoc.y;
			RobotType type = ri.type;
			if (type == RobotType.MISSILE) {
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
					}
				}
			} else if (type == RobotType.LAUNCHER) { 
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
				if (type != RobotType.COMMANDER && type != RobotType.DRONE && type != RobotType.TANK && type != RobotType.LAUNCHER) {
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
			// clock timeout here
		}
		rc.setIndicatorString(0, "here3");
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
		rc.setIndicatorString(0, "here4");
		for (int i = 0; i < numPossibleCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + possibleCoords[i][0], myLoc.y + possibleCoords[i][1]), 255, 0, 0);
		}
		if (numPossibleCoords <= 1) {
			return;
		}
		// guarantee numPossibleCoords > 1
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
		rc.setIndicatorString(0, "here5");
		for (int i = 0; i < numSafestCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + safestCoords[i][0], myLoc.y + safestCoords[i][1]), 0, 255, 0);
		}
		// guarantee numSafestCoords > 0
		int[][] betterCoords = new int[9][2];
		int numBetterCoords = 0;
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
		rc.setIndicatorString(0, "here6");
		if (numBetterCoords <= 0) {
			betterCoords = safestCoords;
			numBetterCoords = numSafestCoords;
		}
		for (int i = 0; i < numBetterCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + betterCoords[i][0], myLoc.y + betterCoords[i][1]), 0, 0, 255);
		}
		// guarantee numBetterCoords > 0
		int[][] bestCoords = new int[9][2];
		int numBestCoords = 0;
		for (int i = 0; i < numBetterCoords; i++) {
			if (canAttackGrid[betterCoords[i][0]+1][betterCoords[i][1]+1]) {
				bestCoords[numBestCoords][0] = betterCoords[i][0];
				bestCoords[numBestCoords][1] = betterCoords[i][1];
				numBestCoords++;
			}
		}
		rc.setIndicatorString(0, "here7");
		if (numBestCoords <= 0) {
			bestCoords = betterCoords;
			numBestCoords = numBetterCoords;
		}
		for (int i = 0; i < numBestCoords; i++) {
			rc.setIndicatorDot(new MapLocation(myLoc.x + bestCoords[i][0], myLoc.y + bestCoords[i][1]), 255, 255, 0);
		}
		// guarantee numBestCoords > 0
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
		rc.setIndicatorString(2, "i = " + i);
		if (i < 8) {
			rc.move(testDir);;
		}
		if (i > 4) {
			leftHanded = !leftHanded;
		}
		
		brandNew = false;
	}
	
	private static boolean inEnemyRange(MapLocation loc) {
		MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
		MapLocation enemyHQ = enemyHQLoc;
		int numTowers = enemyTowers.length;
		int HQRange = 24;
		if (numTowers >= 2) {
			HQRange = 35;
		}
		int towerRange = 24;
		if (loc.distanceSquaredTo(enemyHQ) <= HQRange) {
			return true;
		}
		for (MapLocation towerLoc : enemyTowers) {
			if (loc.distanceSquaredTo(towerLoc) <= towerRange) {
				return true;
			}
		}
		return false;
	}

	private static MapLocation nearestEnemy() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type != RobotType.MISSILE) {
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
	
	private static MapLocation nearestEnemyAll() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
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
	
	private static MapLocation nearestCivilian() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(24, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type == RobotType.MINER || r.type == RobotType.BEAVER) {
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
	
	private static MapLocation nearestMissile() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(24, enemyTeam);
		int closestDist = 9999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type == RobotType.MISSILE) {
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
	
	private static MapLocation fastNearestEnemy() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
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
			if (i >= 1) {
				break;
			}
		}
		if (closestRobot != null) {
			return closestRobot.location;
		}
		return null;
	}

	private static MapLocation attackingEnemy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(HQLoc, 9999999, enemyTeam);
		int closestDist = 9999999;
		RobotInfo closestRobot = null;
		for (RobotInfo r : enemies) {
			if (r.type != RobotType.MISSILE && r.type != RobotType.MINER && r.type != RobotType.BEAVER) {
				MapLocation enemyLoc = r.location;
				int dist = HQLoc.distanceSquaredTo(enemyLoc);
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
	
	private static void lookForBounds() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		int range = (int)Math.sqrt(myRange);
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
	
	private static boolean isMoveTooCloseToBounds(Direction moveDir, int depth) throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation moveLoc = myLoc.add(moveDir);
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
			int myDistToBounds = locDistToBounds(myLoc);
			int moveDistToBounds = locDistToBounds(moveLoc);
			if (bound != NO_BOUND) {
				if (myDistToBounds < depth) {
					if (moveDistToBounds < myDistToBounds) {
						return true;
					}
				} else {
					if (moveDistToBounds < depth) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static int locDistToBounds(MapLocation loc) throws GameActionException {
		Direction[] myDirections = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		int[] knownBounds = {
			rc.readBroadcast(NORTH_BOUND_CHAN),
			rc.readBroadcast(EAST_BOUND_CHAN),
			rc.readBroadcast(SOUTH_BOUND_CHAN),
			rc.readBroadcast(WEST_BOUND_CHAN)
		};
		int leastDist = Integer.MAX_VALUE;
		for (int dirNum = 0; dirNum < 4; dirNum++) {
			int bound = knownBounds[dirNum];
			if (bound != NO_BOUND) {
				int dist = Integer.MAX_VALUE; // value will never be used, just suppresses warning
				switch (dirNum) {
				case 0:
					dist = loc.y - knownBounds[dirNum];
					break;
				case 1:
					dist = knownBounds[dirNum] - loc.x;
					break;
				case 2:
					dist = knownBounds[dirNum] - loc.y;
					break;
				case 3:
					dist = loc.x - knownBounds[dirNum];
					break;
				}
				if (dist < leastDist) {
					leastDist = dist;
				}
			}
		}
		return leastDist;
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

	private static MapLocation droneAttackLocation() {
		MapLocation myLoc = rc.getLocation();
		RobotInfo[] myEnemies = rc.senseNearbyRobots(myRange, enemyTeam);
		boolean foundLauncher = false;
		boolean foundMissile = false;
		RobotInfo bestEnemyTarget = null;
		int minDistSq = 9999999;
		for (RobotInfo ri : myEnemies) {
			RobotType type = ri.type;
			int distSq = ri.location.distanceSquaredTo(myLoc);
			if (foundLauncher) {
				if (type == RobotType.LAUNCHER) {
					if (distSq < minDistSq) {
						bestEnemyTarget = ri;
						minDistSq = distSq;
					}
				}
			} else if (foundMissile) {
				if (type == RobotType.LAUNCHER) {
					foundLauncher = true;
					bestEnemyTarget = ri;
					minDistSq = distSq;
				} else if (type == RobotType.MISSILE) {
					if (distSq < minDistSq) {
						bestEnemyTarget = ri;
						minDistSq = distSq;
					}
				}
			} else {
				if (type == RobotType.LAUNCHER) {
					foundLauncher = true;
					bestEnemyTarget = ri;
					minDistSq = distSq;
				} else if (type == RobotType.MISSILE) {
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

	// This method will attack an enemy in sight, if there is one
	static void attackSomething() throws GameActionException {
		MapLocation enemy = nearestEnemyAll();
		if (enemy != null) {
			rc.attackLocation(enemy);
		}
		
	}

	static void quickTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 3 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 3) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
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
	
	static boolean launcherTryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			Direction dir = directions[(dirint+offsets[offsetIndex]+8)%8];
			if (!inEnemyRange(rc.getLocation().add(dir))) {
				rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			} else if (!launched) {
				tryLaunch(dir);
			}
			return true;
		}
		return false;
	}
	
	static boolean harasserMoveTo(MapLocation targetLoc) throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		Direction d = myLoc.directionTo(targetLoc);
		int kiteDistSq = 0;
		if (rc.canSenseLocation(targetLoc)) {
			RobotInfo targetInfo = rc.senseRobotAtLocation(targetLoc);
			if (targetInfo.type.attackRadiusSquared < RobotType.DRONE.attackRadiusSquared) {
				kiteDistSq = targetInfo.type.attackRadiusSquared;
			}
		}
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int[] leftOffsets = {-1,-2,0,1,2,-3,3,4};
		int[] rightOffsets = {1,2,0,-1,-2,3,-3,4};
		if (startTimer < 15) {
			startTimer++;
			if (leftHanded) {
				offsets = leftOffsets;
			} else {
				offsets = rightOffsets;
			}
		}
		int dirint = directionToInt(d);
		for (int offsetIndex = 0; offsetIndex < 8; offsetIndex++) {
			Direction dir = directions[(dirint+offsets[offsetIndex]+8)%8];
			if (rc.canMove(dir) && !inEnemyRange(myLoc.add(dir)) && !isMoveTooCloseToBounds(dir, 6) && myLoc.distanceSquaredTo(targetLoc) > kiteDistSq) {
				rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
				return true;
			}
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
		int[] offsets = {0,-1,-2,-3};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 4 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 4) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	static boolean tryMoveRight(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,2,3};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 4 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 4) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
			return true;
		}
		return false;
	}
	
	static boolean tryLaunch(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canLaunch(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.launchMissile(directions[(dirint+offsets[offsetIndex]+8)%8]);
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
