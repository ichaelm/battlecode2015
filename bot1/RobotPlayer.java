package bot1;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void run(RobotController myrc) {
		rc = myrc;
        rand = new Random(rc.getID());

		myRange = rc.getType().attackRadiusSquared;
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
        Direction lastDirection = null;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		int ARRAY_SIZE = 1000;
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
		
		while(true) {
            try {
                rc.setIndicatorString(0, "This is an indicator string.");
                rc.setIndicatorString(1, "I am a " + rc.getType());
            } catch (Exception e) {
                System.out.println("Unexpected exception");
                e.printStackTrace();
            }

			if (rc.getType() == RobotType.HQ) {
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
					int targetBarracks = 3;
					int targetTankFactories = 3;
					int targetHelipads = 3;
					int numBuildingBarracks = 0;
					int numBuildingTankFactories = 0;
					int numBuildingHelipads = 0;
					
					// beaver loop, check orders
					for (int i = 0; i < ARRAY_SIZE; i++) {
						RobotInfo r = myBeavers[i];
						if (r == null) {
							break;
						}
						int[] orders = recieveOrders(r.ID);
						if (orders != null) {
							RobotType buildOrder = numToRobotType(orders[0]);
							if (buildOrder != null) {
								switch (buildOrder) {
								case BARRACKS:
									numBuildingBarracks++;
								case TANKFACTORY:
									numBuildingTankFactories++;
								case HELIPAD:
									numBuildingHelipads++;
								}
							}
						}
					}
					
					rc.setIndicatorString(0, numBarracks + " barracks");
					rc.setIndicatorString(1, numBuildingBarracks + " building barracks");
					
					// beaver loop, send orders
					for (int i = 0; i < ARRAY_SIZE; i++) {
						RobotInfo r = myBeavers[i];
						if (r == null) {
							break;
						}
						int[] orders = recieveOrders(r.ID);
						if (orders == null) {
							if (targetBarracks > numBuildingBarracks + numBarracks) {
								sendOrders(r.ID, robotTypeToNum(RobotType.BARRACKS),0,0);
								numBuildingBarracks++;
							} else if (targetHelipads > numBuildingHelipads + numHelipads) {
								sendOrders(r.ID, robotTypeToNum(RobotType.HELIPAD),0,0);
								numBuildingHelipads++;
							} else if (targetTankFactories > numBuildingTankFactories + numTankFactories) {
								sendOrders(r.ID, robotTypeToNum(RobotType.TANKFACTORY),0,0);
								numBuildingTankFactories++;
							}
						} else {
							RobotType buildOrder = numToRobotType(orders[0]);
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
								}
							}
						}

					}
					
					if (rc.isWeaponReady()) {
						attackSomething();
					}

					if (rc.isCoreReady() && rc.getTeamOre() >= 350 && numBeavers < 10) {
						trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
					}
				} catch (Exception e) {
					System.out.println("HQ Exception");
                    e.printStackTrace();
				}
			}
			
            if (rc.getType() == RobotType.TOWER) {
                try {					
					if (rc.isWeaponReady()) {
						attackSomething();
					}
				} catch (Exception e) {
					System.out.println("Tower Exception");
                    e.printStackTrace();
				}
            }
			
			
			if (rc.getType() == RobotType.BASHER) {
                try {

					if (rc.isCoreReady()) {
						tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
					}
                } catch (Exception e) {
					System.out.println("Basher Exception");
					e.printStackTrace();
                }
            }
			
            if (rc.getType() == RobotType.SOLDIER) {
                try {
                    if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
					}
                } catch (Exception e) {
					System.out.println("Soldier Exception");
					e.printStackTrace();
                }
            }
            
            if (rc.getType() == RobotType.TANK) {
                try {
                    if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
					}
                } catch (Exception e) {
					System.out.println("Soldier Exception");
					e.printStackTrace();
                }
			}
            
            if (rc.getType() == RobotType.DRONE) {
                try {
                    if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
					}
                } catch (Exception e) {
					System.out.println("Drone Exception");
					e.printStackTrace();
                }
			}
			
			if (rc.getType() == RobotType.BEAVER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						int[] orders = recieveOrders(rc.getID());
						if (orders == null) {
							boolean fate = rand.nextBoolean();
							if (fate) {
								rc.mine();
							} else {
								tryMove(directions[rand.nextInt(8)]);
							}
						} else {
							RobotType buildOrder = numToRobotType(orders[0]);
							if (buildOrder == null) {
								boolean fate = rand.nextBoolean();
								if (fate) {
									rc.mine();
								} else {
									tryMove(directions[rand.nextInt(8)]);
								}
							} else {
								if (ordersMarked(rc.getID())) {
									sendOrders(rc.getID(), 0, 0, 0);
									boolean fate = rand.nextBoolean();
									if (fate) {
										rc.mine();
									} else {
										tryMove(directions[rand.nextInt(8)]);
									}
								} else {
									if (rc.getTeamOre() < buildOrder.oreCost) {
										boolean fate = rand.nextBoolean();
										if (fate) {
											rc.mine();
										} else {
											tryMove(directions[rand.nextInt(8)]);
										}
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
				} catch (Exception e) {
					System.out.println("Beaver Exception");
                    e.printStackTrace();
				}
			}

            if (rc.getType() == RobotType.BARRACKS) {
				try {
					
					if (rc.isCoreReady() && rc.getTeamOre() >= 600) {
						boolean fate = rand.nextBoolean();
						if (fate) {
							trySpawn(directions[rand.nextInt(8)],RobotType.SOLDIER);
						} else {
							trySpawn(directions[rand.nextInt(8)],RobotType.BASHER);
						}
						
					}
				} catch (Exception e) {
					System.out.println("Barracks Exception");
                    e.printStackTrace();
				}
			}
            
            if (rc.getType() == RobotType.TANKFACTORY) {
				try {
					if (rc.isCoreReady() && rc.getTeamOre() >= 600) {
							trySpawn(directions[rand.nextInt(8)],RobotType.TANK);
					}
				} catch (Exception e) {
					System.out.println("Tank Factory Exception");
                    e.printStackTrace();
				}
			}
            
            if (rc.getType() == RobotType.HELIPAD) {
				try {
					if (rc.isCoreReady() && rc.getTeamOre() >= 600) {
							trySpawn(directions[rand.nextInt(8)],RobotType.DRONE);
					}
				} catch (Exception e) {
					System.out.println("Helipad Exception");
                    e.printStackTrace();
				}
			}
			
			rc.yield();
		}
	}
	
	private static int robotTypeToNum(RobotType type) {
		int num;
		switch (type) {
		case BARRACKS:
			num = 1;
			break;
		case HELIPAD:
			num = 2;
			break;
		case TANKFACTORY:
			num = 3;
			break;
		default:
			num = 0;
			break;
		}
		return num;
	}
	
	private static RobotType numToRobotType(int num) {
		RobotType type;
		switch(num) {
		case 1:
			type = RobotType.BARRACKS;
			break;
		case 2:
			type = RobotType.HELIPAD;
			break;
		case 3:
			type = RobotType.TANKFACTORY;
			break;
		default:
			type = null;
			break;
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
}
