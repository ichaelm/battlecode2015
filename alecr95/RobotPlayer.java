package alecr95;

import java.util.*;
import battlecode.common.*;

public class RobotPlayer {
	//	****************** VARIABLES TO BE LATER DEFINED ******************
	static int beavers, minerFactories = 0, miners, supplyDepots, barracks = 0, soldiers, bashers, tankFactories, tanks, helipads, drones, aerospaceLab = 0, launchers;
	static int minerFactories_old, barracks_old, helipads_old, tankFactories_old, aerospaceLab_old;
	static int round, round_created;
	static Direction facing;
	static RobotController rc;
	static int myRange;
	static Team alliedTeam, enemyTeam;
	static MapLocation HQLocation, mapCenterLocation;
	static MapLocation[] alliedTowerLocations;
	static Random rand;
	static ArrayList<MapLocation> prevPath = new ArrayList<MapLocation>();

	//	****************** CONSTANT/DEFINED VARIABLES ********************** 
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

	//used in keeping track of unit construction
	static final int baseBroadcastOffset = 1, baseProgressBroadcastOffset = baseBroadcastOffset + 20;
	static final int minerFactoriesOffset = baseBroadcastOffset + 1, 
			barracksOffset = baseBroadcastOffset + 3,
			tankFactoriesOffset = baseBroadcastOffset + 4,
			helipadsOffset = baseBroadcastOffset + 5,
			aerospaceLabOffset = baseBroadcastOffset + 10;
	static final int minerFactoriesInProgressOffset = baseProgressBroadcastOffset,
			barracksInProgressOffset = baseProgressBroadcastOffset + 1,
			tankFactoriesInProgressOffset = baseProgressBroadcastOffset + 2,
			helipadsInProgressOffset = baseProgressBroadcastOffset + 3,
			tankFactoriesInProgessOffset = baseProgressBroadcastOffset + 4,
			aerospaceLabInProgressOffset = baseProgressBroadcastOffset + 5;
	static final int beaverOffset = baseBroadcastOffset,
			minerOffset = baseBroadcastOffset + 2,
			droneOffset = baseBroadcastOffset + 6,
			tankOffset = baseBroadcastOffset + 7,
			basherOffset = baseBroadcastOffset + 8,
			launcherOffset = baseBroadcastOffset + 9;

	//range from enemy units where you are considered "safe"
	static final int MAX_SAFE_RANGE = 3,
			BEAVER_BUILDING_SAFETY_RANGE = 50; 

	//number of each Building/Unit to construct
	static final int MAX_NUM_BARRACKS = 1, //1
			MAX_NUM_MINER_FACTORIES = 3, //3
			MAX_NUM_TANK_FACTORIES = 0, //7
			MAX_NUM_HELIPADS = 1,
			MAX_NUM_AEROSPACE_LABS = 1;
	static final int MAX_NUM_BEAVERS = 10, //10
			MAX_NUM_MINERS = 30, //30
			MAX_NUM_SOLDIERS = 0, 
			MAX_NUM_BASHERS = 0, //10
			MAX_NUM_TANKS = 0, //90
			MAX_NUM_DRONES = 2,
			MAX_NUM_LAUNCHERS = 10;

	//channels used for unit communication
	static final int BEAVER_CHANNEL = 100, 
			MINE_FACTORY_CHANNEL = 101, 
			BARRACKS_CHANNEL = 102,
			HELIPAD_CHANNEL = 103,
			TANK_FACTORY_CHANNEL = 104,
			MACRO_STRATEGY_CHANNEL = 105,
			SUPPLY_DEPOT_BEAVER = 106,
			AEROSPACE_LAB_CHANNEL = 107;

	//  ********************************************************************
	//  ***************** BEGIN OF RUN LOOP ********************************
	//  ********************************************************************
	public static void run(RobotController thisRobot) {
		//initializing lots of variables that shouldn't be changing every round
		rc = thisRobot;
		rand = new Random(rc.getID());
		myRange = rc.getType().attackRadiusSquared;
		alliedTeam = rc.getTeam();
		enemyTeam = alliedTeam.opponent();
		HQLocation = rc.senseHQLocation();
		facing = randDirection();
		round_created = Clock.getRoundNum();
		mapCenterLocation = new MapLocation(HQLocation.x + (rc.senseEnemyHQLocation().x - HQLocation.x)/2, HQLocation.y + (rc.senseEnemyHQLocation().y - HQLocation.y)/2);
		for (int i = 0; i < 3; i++) {
			prevPath.add(new MapLocation(-1, -1));
		}

		while(true) {
			round = Clock.getRoundNum();
			alliedTowerLocations = rc.senseTowerLocations();
			try {
				switch (rc.getType()) {
				case HQ:

					attackSomething();

					//resupply nearby robots
					resupplyAlliedBots();

					//getting all the number of units to decide if it needs to build anything
					updateUnitCounts();

					//set all of the beaver actions, with a defined order of operations this is pretty much build order for beavers
					// mine factories first
					if (minerFactories + rc.readBroadcast(minerFactoriesInProgressOffset) < MAX_NUM_MINER_FACTORIES) {
						rc.broadcast(BEAVER_CHANNEL, 1); //100th spot is for beaver mode, 1 means build mine factories
					} else { 
						//then build barracks
						if(barracks + rc.readBroadcast(barracksInProgressOffset) < MAX_NUM_BARRACKS) {
							rc.broadcast(BEAVER_CHANNEL, 2); // 2 means construct barracks
						} else {
							//then helipads
							if(helipads + rc.readBroadcast(helipadsInProgressOffset) < MAX_NUM_HELIPADS) {
								rc.broadcast(BEAVER_CHANNEL, 4); // 4 means construct heilpads
							} else {
								//then build tank factories
								if (barracks >= 1 && tankFactories + rc.readBroadcast(tankFactoriesInProgressOffset) < MAX_NUM_TANK_FACTORIES) {
									rc.broadcast(BEAVER_CHANNEL, 3); // 3 means construct tank factories
								} else {
									//then build aerospace labs
									rc.setIndicatorString(0, "aero labs: " + aerospaceLab + " in progress: " + rc.readBroadcast(aerospaceLabInProgressOffset));
									if (helipads >= 1 && aerospaceLab + rc.readBroadcast(aerospaceLabInProgressOffset) < MAX_NUM_AEROSPACE_LABS) {
										rc.broadcast(BEAVER_CHANNEL, 5); // 5 means construct aerospace labs
									} else {
										rc.broadcast(BEAVER_CHANNEL, 0);
									}
								}
							}
						}
					}

					//					*********************************
					//					*** MESSAGES TO ALL BUILDINGS ***
					//					*********************************

					//message all mine factories, telling them to create or not create miners
					if (miners < MAX_NUM_MINERS) {
						rc.broadcast(MINE_FACTORY_CHANNEL, 1); // 1 means build miners
					} else {
						rc.broadcast(MINE_FACTORY_CHANNEL, 0); // 0 means don't build miners
					}

					//message all helipads, telling them to create or not create drones
					if (drones < MAX_NUM_DRONES) {
						rc.broadcast(HELIPAD_CHANNEL, 1); // 1 means build drones
					} else {
						rc.broadcast(HELIPAD_CHANNEL, 0); // 0 means don't build drones
					}

					//messaging barracks to build units
					if (soldiers < MAX_NUM_SOLDIERS) {
						rc.broadcast(BARRACKS_CHANNEL, 1); //1 means build soldiers
					} else {
						if (bashers < MAX_NUM_BASHERS) {
							rc.broadcast(BARRACKS_CHANNEL, 2); //2 means build bashers
						} else {
							rc.broadcast(BARRACKS_CHANNEL, 0); //0 means don't build anything
						}
					}

					//message all tank factories, telling them to create or not create tanks
					if (tanks < MAX_NUM_TANKS) {
						rc.broadcast(TANK_FACTORY_CHANNEL, 1); // 1 means build tanks
					} else {
						rc.broadcast(TANK_FACTORY_CHANNEL, 0); // 0 means don't build tanks
					}

					//message all aerospace labs, telling them to create or not create launchers
					if (launchers < MAX_NUM_LAUNCHERS) {
						rc.broadcast(AEROSPACE_LAB_CHANNEL, 1); // 1 means build launchers
					} else {
						rc.broadcast(AEROSPACE_LAB_CHANNEL, 0); // 0 means don't build launchers
					}

					//actions/modes are based on round number; army units formation orders are as follows
					// NOTE: These don't apply as much for drones; drones go where they please, kind of like Mundo
					// 0 - do nothing
					// 1 - move to allied towers
					// 2 - swarm map center, attack all in way of moving
					// 3 - attack enemy towers (if any)
					// 4 - attack enemy HQ
					// 5 - fall back to allied HQ
					if (round < 800) {

						// just protect allied towers
						rc.broadcast(MACRO_STRATEGY_CHANNEL, 1);

						//only spawn beavers until round 800
						if (beavers < MAX_NUM_BEAVERS) {
							spawnUnit(RobotType.BEAVER);
						}
					} else {
						if (round < 1650 && rc.senseTowerLocations().length > 0) {
							// swarm map center, get ready for mass invade
							rc.broadcast(MACRO_STRATEGY_CHANNEL, 1);
						} else {
							if ( rc.senseTowerLocations().length == 0 && rc.senseNearbyRobots(500, enemyTeam).length > 5) { 
								rc.broadcast(MACRO_STRATEGY_CHANNEL, 5);
							} 
							else {
								if (rc.senseEnemyTowerLocations().length == 0) {
									rc.broadcast(MACRO_STRATEGY_CHANNEL, 4); // FINISH THEM!
								} else {
									rc.broadcast(MACRO_STRATEGY_CHANNEL, 3);
								}
							}
							// //begin invasion, start with nearest tower and all units in way, leave a few units for defense (based on round creation number)
							// if (round < 1000 || (soldiers + bashers + drones) == (MAX_NUM_SOLDIERS + MAX_NUM_BASHERS + MAX_NUM_DRONES)) {
							// 	rc.broadcast(MACRO_STRATEGY_CHANNEL, 3);
							// } else {
							// 	//if sustained heavy casualties, just defend for rest of game TODO calculate team strength more effectively to decide this
							// 	if((soldiers + bashers + drones) < (MAX_NUM_SOLDIERS + MAX_NUM_BASHERS + MAX_NUM_DRONES)/4) {
							// 		rc.broadcast(MACRO_STRATEGY_CHANNEL, 1);
							// 	} else {
							// 		if ((soldiers + bashers + drones) > (MAX_NUM_SOLDIERS + MAX_NUM_BASHERS + MAX_NUM_DRONES)/2) {
							// 			if (rc.senseEnemyTowerLocations().length == 0) {
							// 				rc.broadcast(MACRO_STRATEGY_CHANNEL, 4); // FINISH THEM!
							// 			} else {
							// 				rc.broadcast(MACRO_STRATEGY_CHANNEL, 3);
							// 			}
							// 		}
							// 	}
							// }
						}
					}
					break;

				case BEAVER:
					rc.broadcast(baseBroadcastOffset, rc.readBroadcast(baseBroadcastOffset) + 1);

					//sensing and resupplying nearby allies if they have lower supply level
					resupplyAlliedBots();

					attackSomething();

					if (rc.readBroadcast(SUPPLY_DEPOT_BEAVER) == 0 || rc.readBroadcast(SUPPLY_DEPOT_BEAVER) == rc.getID()) {
						rc.broadcast(SUPPLY_DEPOT_BEAVER, rc.getID());
						//This beaver has now become in charge of creating a supply depot chain to the middle of the map
						RobotInfo[] nearbyBuildings = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, alliedTeam);
						boolean nearbySupplyDepot = false;
						for (RobotInfo i: nearbyBuildings) {
							if (i.type == RobotType.SUPPLYDEPOT) {
								nearbySupplyDepot = true;
								break;
							}
						}
						if (nearbySupplyDepot) {
							moveUnitSafely(rc.getLocation().directionTo(mapCenterLocation));
						} else {
							constructBuilding(RobotType.SUPPLYDEPOT);
						}
					} else {
						switch (rc.readBroadcast(BEAVER_CHANNEL)) {
						case 0:
							mineMove();
							break;
						case 1:
							//if mode is expand and defend, move away from HQ in some direction, then build a mine factory
							if (rc.getLocation().distanceSquaredTo(HQLocation) < 8 && congesting(3)) {
								facing = moveUnitSafely(facing, BEAVER_BUILDING_SAFETY_RANGE);
							} else {
								if(!constructBuilding(RobotType.MINERFACTORY)) {
									mineMove();
								}
							}

							break;
						case 2:
							//if mode is expand and defend, move away from HQ in some direction, then build a barracks
							if (rc.getLocation().distanceSquaredTo(HQLocation) < 8 && congesting(3)) {
								facing = moveUnitSafely(facing, BEAVER_BUILDING_SAFETY_RANGE);
							} else {
								if(!constructBuilding(RobotType.BARRACKS)) {
									mineMove();
								}
							}
							break;
						case 3:
							//if mode is expand and defend, move away from HQ in some direction, then build a tank factory
							if (rc.getLocation().distanceSquaredTo(HQLocation) < 8 && congesting(3)) {
								facing = moveUnitSafely(facing, BEAVER_BUILDING_SAFETY_RANGE);
							} else {
								if(!constructBuilding(RobotType.TANKFACTORY)) {
									mineMove();
								}
							}
							break;
						case 4:
							//if mode is expand and defend, move away from HQ in some direction, then build a tank factory
							if (rc.getLocation().distanceSquaredTo(HQLocation) < 8 && congesting(3)) {
								facing = moveUnitSafely(facing, BEAVER_BUILDING_SAFETY_RANGE);
							} else {
								if(!constructBuilding(RobotType.HELIPAD)) {
									mineMove();
								}
							}
							break;
						case 5:
							//if mode is expand and defend, move away from HQ in some direction, then build an aerospace lab
							if (rc.getLocation().distanceSquaredTo(HQLocation) < 8 && congesting(3)) {
								facing = moveUnitSafely(facing, BEAVER_BUILDING_SAFETY_RANGE);
							} else {
								if(!constructBuilding(RobotType.AEROSPACELAB)) {
									mineMove();
								}
							}
							break;
						default:
							break;
						}
					}
					break;

				case MINERFACTORY:
					// telling HQ that it is alive
					rc.broadcast(minerFactoriesOffset, rc.readBroadcast(minerFactoriesOffset) + 1);

					//if something is in range of attack, destroy the mothafucka
					attackSomething();

					//build miners if the HQ commands it
					switch (rc.readBroadcast(MINE_FACTORY_CHANNEL)) {
					case 0:
						//do nothing
						break;
					case 1: //create miners
						spawnUnit(RobotType.MINER);
						break;
					default:
						break;
					}


					break;

				case BARRACKS:
					// telling HQ that it is alive
					rc.broadcast(barracksOffset, rc.readBroadcast(barracksOffset) + 1);

					//resupply allies
					resupplyAlliedBots();

					//if something is in range of attack, destroy the mothafucka
					attackSomething();

					//build things if the HQ commands it
					switch (rc.readBroadcast(BARRACKS_CHANNEL)) {
					case 0:
						// do nothing
						break;
					case 1: //create soldiers
						spawnUnit(RobotType.SOLDIER);
						break;
					case 2: //create bashers
						spawnUnit(RobotType.BASHER);
						break;
					default:
						break;
					}

					break;

				case HELIPAD:
					// telling HQ that it is alive
					rc.broadcast(helipadsOffset, rc.readBroadcast(helipadsOffset) + 1);

					//resupply allies
					resupplyAlliedBots();

					//if something is in range of attack, destroy the mothafucka
					attackSomething();

					//build things if the HQ commands it
					switch (rc.readBroadcast(HELIPAD_CHANNEL)) {
					case 0:
						// do nothing
						break;
					case 1: //create drones
						spawnUnit(RobotType.DRONE);
						break;
					default:
						break;
					}

					break;

				case TANKFACTORY:
					// telling HQ that it is alive
					rc.broadcast(tankFactoriesOffset, rc.readBroadcast(tankFactoriesOffset) + 1);

					//resupply allies
					resupplyAlliedBots();

					//if something is in range of attack, destroy the mothafucka
					attackSomething();

					//build things if the HQ commands it
					switch (rc.readBroadcast(TANK_FACTORY_CHANNEL)) {
					case 0:
						// do nothing
						break;
					case 1: //create tanks
						spawnUnit(RobotType.TANK);
						break;
					default:
						break;
					}

					break;

				case AEROSPACELAB:
					// telling HQ that it is alive
					rc.broadcast(aerospaceLabOffset, rc.readBroadcast(aerospaceLabOffset) + 1);

					//resupply allies
					resupplyAlliedBots();

					//if something is in range of attack, destroy the mothafucka
					attackSomething();

					//build things if the HQ commands it
					switch (rc.readBroadcast(AEROSPACE_LAB_CHANNEL)) {
					case 0:
						// do nothing
						break;
					case 1: //create launcher
						spawnUnit(RobotType.LAUNCHER);
						break;
					default:
						break;
					}

					break;

				case TOWER:
					attackSomething();

					//resupply allies
					resupplyAlliedBots();

					break;

				case MINER:
					// telling HQ that it is alive
					rc.broadcast(minerOffset, rc.readBroadcast(minerOffset) + 1);

					//resupply allies
					resupplyAlliedBots();

					attackSomething();

					//normal mine and move
					mineMove();
					break;

				case DRONE:
					// telling HQ that it is alive
					rc.broadcast(droneOffset, rc.readBroadcast(droneOffset) + 1);

					attackSomething();

					// // Strategy Modes
					// // 0 - do nothing
					// // 1 - move to allied towers
					// // 2 - swarm map center, attack all in way of moving
					// // 3 - attack enemy towers (if any)
					// // 4 - attack enemy HQ
					// // 5 - fall back to allied HQ
					switch (rc.readBroadcast(MACRO_STRATEGY_CHANNEL)) {
					// case 0:
					// case 1:
					// case 2:
					// 	// roamAndKite();
					// 	break;
					case 3:
					// 	attackEnemyTowers();
					// 	break;
					// case 4:
						attackEnemyHQ();
						break;
					case 5:
						protectBase();
						break;
					default:
						break;
					}

					break;

				case LAUNCHER:
					rc.broadcast(launcherOffset, rc.readBroadcast(launcherOffset) + 1);

					// code to recognize nearby units and attack them with launchers
					RobotInfo closestEnemyInRange = closestUnit(rc.senseNearbyRobots(GameConstants.MISSILE_LIFESPAN, enemyTeam));
					if (closestEnemyInRange != null) {
						rc.setIndicatorString(0, "enemies location: " + closestEnemyInRange.location.toString());
						attack(closestEnemyInRange.location);
					}

					//resupply allies
					resupplyAlliedBots();

					// Strategy Modes
					// 0 - do nothing
					// 1 - move to allied towers
					// 2 - swarm map center, attack all in way of moving
					// 3 - attack enemy towers (if any)
					// 4 - attack enemy HQ
					// 5 - fall back to allied HQ
					switch (rc.readBroadcast(MACRO_STRATEGY_CHANNEL)) {
					case 0:
						// break;
					case 1:
						moveToAlliedTowers();
						break;
					case 2:
						moveToMapCenter();
						break;
					case 3:
						attackEnemyTowers();
						break;
					case 4:
						attackEnemyHQ();
						break;
					case 5:
						protectBase();
						break;
					default:
						break;
					}

					break;

				case MISSILE:
					//if units in blast radius are all enemies, explode
					while (arrayContains(rc.senseNearbyRobots(GameConstants.MISSILE_RADIUS_SQUARED), alliedTeam)) {
						moveUnit(rc.getLocation().directionTo(closestUnit(rc.senseNearbyRobots(GameConstants.MISSILE_RADIUS_SQUARED, enemyTeam)).location));
						rc.yield();
					}
					rc.explode();

					break;

				case SOLDIER:
				case BASHER:
				case TANK:
					if(rc.getType() == RobotType.TANK) {
						// telling HQ that it is alive
						rc.broadcast(tankOffset, rc.readBroadcast(tankOffset) + 1);
					}

					//attack things, yay
					attackSomething();

					//resupply allies
					resupplyAlliedBots();

					// Strategy Modes
					// 0 - do nothing
					// 1 - move to allied towers
					// 2 - swarm map center, attack all in way of moving
					// 3 - attack enemy towers (if any)
					// 4 - attack enemy HQ
					// 5 - fall back to allied HQ
					switch (rc.readBroadcast(MACRO_STRATEGY_CHANNEL)) {
					case 0:
						// break;
					case 1:
						moveToAlliedTowers();
						break;
					case 2:
						moveToMapCenter();
						break;
					case 3:
						attackEnemyTowers();
						break;
					case 4:
						attackEnemyHQ();
						break;
					case 5:
						protectBase();
						break;
					default:
						break;
					}

					break;

				default:
					break;

				}
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			rc.yield();
		}
	}

	// just searches an array of RobotInfo (returned by senseNearbyRobots) for a closest robot
	private static RobotInfo closestUnit(RobotInfo[] robots) {
		MapLocation currentLocation = rc.getLocation();
		if (robots.length > 0) {
			RobotInfo closestUnit = robots[0];
			for (RobotInfo i: robots) {
				if (currentLocation.distanceSquaredTo(i.location) < currentLocation.distanceSquaredTo(closestUnit.location)) {
					closestUnit = i;
				}
			}
			return closestUnit;
		}
		return null;
	}

	// just searches an array of RobotInfo (returned by senseNearbyRobots) for a type
	private static boolean arrayContains(RobotInfo[] robots, RobotType type) {
		for (RobotInfo i: robots) {
			if (i.type == type) {
				return true;
			}
		}
		return false;
	}

	// just searches an array of RobotInfo (returned by senseNearbyRobots) for a team
	private static boolean arrayContains(RobotInfo[] robots, Team t) {
		for (RobotInfo i: robots) {
			if (i.team == t) {
				return true;
			}
		}
		return false;
	}

	//specifically for drones, this has them seek out the nearest miner, and then kite them
	private static void roamAndKite() throws GameActionException {
		moveUnitSafely(rc.getLocation().directionTo(rc.senseEnemyHQLocation()), 10);

		RobotInfo[] allEnemyRobots = rc.senseNearbyRobots(RobotType.DRONE.sensorRadiusSquared, enemyTeam);
		RobotInfo targetMiner = null;
		for (RobotInfo i: allEnemyRobots) {
			if(i.type == RobotType.MINER) {
				targetMiner = i;
			}
		}
		if (targetMiner == null) {
			allEnemyRobots = rc.senseNearbyRobots(99999, enemyTeam);
			for (RobotInfo i: allEnemyRobots) {
				if(i.type == RobotType.MINER) {
					targetMiner = i;
				}
			}
		} 
		if (targetMiner != null) {
			// there is now a target; check that target is "safe" distance of 5+ away
			if(rc.getLocation().distanceSquaredTo(targetMiner.location) <= 7) {
				moveUnit(rc.getLocation().directionTo(targetMiner.location).opposite());
			} else {
				attack(targetMiner.location);
			}
		}
	}
	
	private static void protectBase() throws GameActionException {
		if (alliedTowerLocations.length > 0) {
			MapLocation closestTower = alliedTowerLocations[0];
			for (MapLocation i: alliedTowerLocations) {
				if (HQLocation.distanceSquaredTo(i) < HQLocation.distanceSquaredTo(closestTower)) {
					closestTower = i;
				}
			}
			if (rc.getLocation().distanceSquaredTo(closestTower) > 4) {
				//now, move there
				facing = rc.getLocation().directionTo(closestTower);
				moveUnit(facing);
			}
		}

	}

	private static void attackEnemyHQ() throws GameActionException {
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		if (rc.getLocation().distanceSquaredTo(enemyHQ) > 8) {
			//now, move there
			facing = rc.getLocation().directionTo(enemyHQ);
			moveUnit(facing);
		} else {
			//attack the tower
			attack(enemyHQ);
		}

	}

	private static void attackEnemyTowers() throws GameActionException {
		// if there are existing towers, go to the one greatest distance away from HQ
		MapLocation[] enemyTowerLocations = rc.senseEnemyTowerLocations();
		if (enemyTowerLocations.length > 0) {
			MapLocation closestTower = enemyTowerLocations[0];
			for (MapLocation i: enemyTowerLocations) {
				if (mapCenterLocation.distanceSquaredTo(i) < mapCenterLocation.distanceSquaredTo(closestTower)) {
					closestTower = i;
				}
			}
			if (rc.getLocation().distanceSquaredTo(closestTower) > 8) {
				//now, move there
				facing = rc.getLocation().directionTo(closestTower);
				moveUnit(facing);
			} else {
				//attack the tower
				attack(closestTower);
				// //if youre a drone, move again, because why not?
				// if (rc.getType() == RobotType.DRONE) {
				// 	facing = rc.getLocation().directionTo(closestTower);
				// 	moveUnit(facing);
				// }
			}
		}
	}

	private static void attack(MapLocation target) throws GameActionException{
		if(target == null) {
			rc.setIndicatorString(1, "null target in attack. " + rc.getID());
			return;
		}
		switch (rc.getType()) {
			case LAUNCHER:
				//
				facing = rc.getLocation().directionTo(target);
				if (rc.getLocation().distanceSquaredTo(target) < GameConstants.MISSILE_LIFESPAN && rc.canLaunch(facing)) {
					rc.setIndicatorString(1, "can launch. " + rc.getID());
					rc.launchMissile(facing);
				} else {
					rc.setIndicatorString(1, "can't launch " + rc.getID());
					moveRetreat();
				}
				break;
			default:
				attack(target);
		}
		
	}

	private static void moveToMapCenter() throws GameActionException {
		if (rc.getLocation().distanceSquaredTo(mapCenterLocation) > 3 && rc.isPathable(RobotType.SOLDIER, mapCenterLocation)) {
			//now, move there
			facing = rc.getLocation().directionTo(mapCenterLocation);
			moveUnit(facing);
		}
	}

	private static void moveToAlliedTowers() throws GameActionException {
		// if there are existing towers, go to the one greatest distance away from HQ
		if (alliedTowerLocations.length > 0) {
			MapLocation furthestTower = alliedTowerLocations[0];
			for (MapLocation i: alliedTowerLocations) {
				if (mapCenterLocation.distanceSquaredTo(i) < mapCenterLocation.distanceSquaredTo(furthestTower)) {
					furthestTower = i;
				}
			}
			if (rc.getLocation().distanceSquaredTo(furthestTower) > 3) {
				//now, move there
				facing = rc.getLocation().directionTo(furthestTower);
				moveUnit(facing);
			}
		}
	}

	private static void resupplyAlliedBots() throws GameActionException {
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(), GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, alliedTeam);
		RobotInfo lowestAlly;
		if (nearbyAllies.length > 0) {
			lowestAlly = nearbyAllies[0];
		} else {
			return;
		}
		for (RobotInfo i: nearbyAllies) {
			if (i.supplyLevel < lowestAlly.supplyLevel) {
				lowestAlly = i;
			}
		}
		if (rc.getSupplyLevel() - lowestAlly.supplyLevel > 1) {
			rc.transferSupplies((int) ((rc.getSupplyLevel()-lowestAlly.supplyLevel)/2), lowestAlly.location);
		}
	}

	//determines if building a structure would congest traffic flow (only useful for trench-type maps)
	private static boolean congesting(int radius) {
		MapLocation[] locations = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), radius);
		int immovableSpaces = 0;
		for( MapLocation i: locations) {
			if(rc.isPathable(RobotType.BEAVER, i)) {
				immovableSpaces++;
			}
		}
		if (immovableSpaces < radius + 1) {
			return false;
		} else {
			return true;
		}
	}

	//used to make buildings
	private static boolean constructBuilding(RobotType type) throws GameActionException {
		for(int i = 0; i < 8; i++) {
			if (rc.isCoreReady() && rc.canBuild(facing, type)) {
				//determine what channel to increment
				int broadcastOffset = 0;
				switch (type) {
				case MINERFACTORY:
					broadcastOffset = minerFactoriesInProgressOffset;
					break;
				case BARRACKS:
					broadcastOffset = barracksInProgressOffset;
					break;
				case HELIPAD:
					broadcastOffset = helipadsInProgressOffset;
					break;
				default:
					break;
				}

				//increment channel to indicate building progress, build, decrement channel to indicate completion
				rc.broadcast(broadcastOffset, rc.readBroadcast(broadcastOffset) + 1);
				rc.build(facing, type);
				return true;
			} else {
				facing = facing.rotateLeft();
			}
		}
		return false;
	}

	//method specifically for mining, will search available ore, and "smartly" mine
	private static void mineMove() throws GameActionException {
		MapLocation currentLocation = rc.getLocation();
		Direction tempDir = facing;
		for (int i = 0; i < 9; i++) {
			if (congesting(1)) {
				if (rc.senseOre(currentLocation) > 0 && rc.isCoreReady() && rc.canMine()){
					rc.mine();
					return;
				} else {
					//move towards ore
					if (rc.senseOre(currentLocation.add(tempDir)) <= 10) {
						if (rand.nextBoolean()) {
							tempDir = tempDir.rotateLeft();
						} else {
							tempDir = tempDir.rotateRight();
						}
					} else {
						facing = moveUnitSafely(tempDir);
						return;
					}
				}
			}
		}
		facing = moveUnitSafely(facing);
	}

	//default range of 30
	private static Direction moveUnitSafely(Direction dir) throws GameActionException {
		return moveUnitSafely(dir, MAX_SAFE_RANGE);
	}

	private static Direction moveUnitSafely(Direction dir, int range) throws GameActionException {
		if (!isEnemyUnitInRange(range) && !isEnemyTowerInRange()) {
			return moveUnit(dir);
		} else {
			return moveRetreat();
		}
	}

	private static boolean isEnemyTowerInRange() throws GameActionException {
		MapLocation[] nearbyEnemyTowers = rc.senseEnemyTowerLocations();
		for (MapLocation i: nearbyEnemyTowers) {
			if( i.distanceSquaredTo(rc.getLocation()) < 30) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEnemyUnitInRange(int range) throws GameActionException {
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(range, enemyTeam);
		if( nearbyEnemies.length > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	private static void updateUnitCounts() throws GameActionException {
		//beavers
		beavers = rc.readBroadcast(beaverOffset);
		rc.broadcast(beaverOffset, 0);

		//mine factories
		int progressTemp = rc.readBroadcast(minerFactoriesInProgressOffset);
		minerFactories_old = minerFactories;
		rc.broadcast(minerFactoriesInProgressOffset, progressTemp - (minerFactories - minerFactories_old));
		rc.broadcast(minerFactoriesOffset, 0);

		//miners
		miners = rc.readBroadcast(minerOffset);
		rc.broadcast(minerOffset, 0);

		//drones
		drones = rc.readBroadcast(droneOffset);
		rc.broadcast(droneOffset, 0);

		//tanks
		tanks = rc.readBroadcast(tankOffset);
		rc.broadcast(tankOffset, 0);

		//bashers
		bashers = rc.readBroadcast(basherOffset);
		rc.broadcast(basherOffset, 0);

		//launchers
		launchers = rc.readBroadcast(launcherOffset);
		rc.broadcast(launcherOffset, 0);

		//barracks
		progressTemp = rc.readBroadcast(barracksInProgressOffset);
		barracks_old = barracks;
		barracks = rc.readBroadcast(barracksOffset);		
		rc.broadcast(barracksInProgressOffset, progressTemp - (barracks - barracks_old));
		rc.broadcast(barracksOffset, 0);

		//tank factories
		progressTemp = rc.readBroadcast(tankFactoriesInProgressOffset);
		tankFactories_old = tankFactories;
		tankFactories = rc.readBroadcast(tankFactoriesOffset);
		rc.broadcast(tankFactoriesInProgressOffset, progressTemp - (tankFactories - tankFactories_old));
		rc.broadcast(tankFactoriesOffset, 0);

		//helipad
		progressTemp = rc.readBroadcast(helipadsInProgressOffset);
		helipads_old = helipads;
		helipads = rc.readBroadcast(helipadsOffset);
		rc.broadcast(helipadsInProgressOffset, progressTemp - (helipads - helipads_old));
		rc.broadcast(helipadsOffset, 0);

		//aerospace lab
		progressTemp = rc.readBroadcast(aerospaceLabInProgressOffset);
		aerospaceLab_old = aerospaceLab;
		aerospaceLab = rc.readBroadcast(aerospaceLabOffset);
		rc.broadcast(aerospaceLabInProgressOffset, progressTemp - (aerospaceLab - aerospaceLab_old));
		rc.broadcast(aerospaceLabOffset, 0);

		//...

	}

	// This method will attack an enemy in sight, if there is one; don't attack missles if other things are in attack range.
	private static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		RobotInfo target;
		if (enemies.length > 0) {
			target = enemies[0];
		} else {
			return;
		}
		for (RobotInfo i: enemies) {
			if (i.type != RobotType.MISSILE) {
				target = i;
				break;
			}
		}
		if (rc.isWeaponReady() && rc.canAttackLocation(target.location )) {
			rc.attackLocation(target.location);
		}
	}

	private static boolean spawnUnit(RobotType type) throws GameActionException {
		for(int i = 0; i < 8; i++) {
			if (rc.isCoreReady() && rc.canSpawn(facing, type)) {
				rc.spawn(facing, type);
				return true;
			} else {
				facing = facing.rotateLeft();
			}
		}
		return false;
	}

	private static Direction moveUnit(Direction direction) throws GameActionException {
		prevPath.remove(0);
		prevPath.add(rc.getLocation());
		boolean rotationDirection = rand.nextBoolean();
		for(int i = 0; i < 8; i++) {
			if (rc.isCoreReady() && rc.canMove(direction) && !prevPath.contains(rc.getLocation().add(direction))) {
				rc.move(direction);
				break;
			} else {
				if (rotationDirection) {
					direction = direction.rotateLeft();
				} else {
					direction = direction.rotateRight();
				}
			}
		}
		return direction;
	}

	private static Direction moveRetreat() throws GameActionException {
		return moveUnit(rc.getLocation().directionTo(rc.senseHQLocation()));
	}

	private static Direction randDirection() {
		return directions[rand.nextInt(8)];
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
