package mainbot_soldiers_contain_v0;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class Simulator {

	private static final double BEAVER_MINE_MAX = GameConstants.BEAVER_MINE_MAX;
	private static final double BEAVER_MINE_RATE = GameConstants.BEAVER_MINE_RATE;
	private static final double MINER_MINE_MAX = GameConstants.MINER_MINE_MAX;
	private static final double MINER_MINE_RATE = GameConstants.MINER_MINE_RATE;
	private static final double MINIMUM_MINE_AMOUNT = GameConstants.MINIMUM_MINE_AMOUNT;
	
	public static class OreTurns {
		public OreTurns(double ore, int turns) {
			this.ore = ore;
			this.turns = turns;
		}
		public int turns;
		public double ore;
	}
	
	public static OreTurns simulateSafeEarlyMining(RobotType type, double orePerSquare, int numSquares) {
		double baseIncome = 5;
		double teamOre = 500;
		double maxOre;
		int numUnits = 0;
		int turn = 0;
		double incomePerUnit;
		int unitBreakEvenTurns;
		if (type == RobotType.BEAVER) {
			incomePerUnit = beaverOreRateGivenNumMines(orePerSquare, beaverOptimalNumMines(orePerSquare));
			unitBreakEvenTurns = (int)Math.ceil((type.oreCost / incomePerUnit) + type.buildTurns);
			maxOre = (orePerSquare - beaverOreLeftAfterMining(orePerSquare, beaverOptimalNumMines(orePerSquare)))*numSquares + teamOre;
		} else if (type == RobotType.MINER) {
			numSquares--; // due to miner factory
			incomePerUnit = minerOreRateGivenNumMines(orePerSquare, minerOptimalNumMines(orePerSquare));
			unitBreakEvenTurns = (int)Math.ceil((type.oreCost / incomePerUnit) + type.buildTurns);
			maxOre = (orePerSquare - minerOreLeftAfterMining(orePerSquare, minerOptimalNumMines(orePerSquare)))*numSquares + teamOre;
			// build beaver
			teamOre -= RobotType.BEAVER.oreCost;
			turn += RobotType.BEAVER.buildTurns;
			teamOre += baseIncome * RobotType.BEAVER.buildTurns;
			// build minerfactory
			teamOre -= RobotType.MINERFACTORY.oreCost;
			turn += RobotType.MINERFACTORY.buildTurns;
			teamOre += baseIncome * RobotType.MINERFACTORY.buildTurns;
		} else {
			// error, should throw exception
			return null;
		}
		while (teamOre < maxOre) {
			// invariant: at beginning of loop, we can build a unit
			if ((int)Math.floor((maxOre - teamOre) / (baseIncome + ((numUnits + 1) * incomePerUnit))) > unitBreakEvenTurns) { // if i should build a unit
				if (teamOre >= type.oreCost) {
					teamOre -= type.oreCost;
					numUnits++;
					// advance by spawn delay
					double income = baseIncome + numUnits*incomePerUnit;
					int turnsToWait = type.buildTurns;
					if (teamOre + income*turnsToWait < maxOre) {
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					} else {
						// advance to endTurn
						turnsToWait = (int)Math.ceil((maxOre - teamOre)/income);
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					}
				} else {
					// advance by number of turns to get enough ore
					double income = baseIncome + numUnits*incomePerUnit;
					int turnsToWait = (int)Math.ceil((type.oreCost - teamOre) / income);
					if (teamOre + income*turnsToWait < maxOre) {
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					} else {
						// advance to endTurn
						turnsToWait = (int)Math.ceil((maxOre - teamOre)/income);
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					}
				}
			} else {
				// advance to endTurn
				double income = baseIncome + numUnits*incomePerUnit;
				int turnsToWait = (int)Math.ceil((maxOre - teamOre)/income);
				turn += turnsToWait;
				teamOre += income*turnsToWait;
			}
		}
		return new OreTurns(teamOre, turn);
	}
	
	public static double simulateMaximumEarlyMiningOre(RobotType type, double orePerSquare, int endTurn) {
		double baseIncome = 5;
		double teamOre = 500;
		int numUnits = 0;
		int turn = 0;
		double incomePerUnit;
		int unitBreakEvenTurns;
		if (type == RobotType.BEAVER) {
			incomePerUnit = beaverOreRateGivenNumMines(orePerSquare, beaverOptimalNumMines(orePerSquare));
			unitBreakEvenTurns = (int)Math.ceil((type.oreCost / incomePerUnit) + type.buildTurns);
		} else if (type == RobotType.MINER) {
			incomePerUnit = minerOreRateGivenNumMines(orePerSquare, minerOptimalNumMines(orePerSquare));
			unitBreakEvenTurns = (int)Math.ceil((type.oreCost / incomePerUnit) + type.buildTurns);
			// build beaver
			teamOre -= RobotType.BEAVER.oreCost;
			turn += RobotType.BEAVER.buildTurns;
			teamOre += baseIncome * RobotType.BEAVER.buildTurns;
			// build minerfactory
			teamOre -= RobotType.MINERFACTORY.oreCost;
			turn += RobotType.MINERFACTORY.buildTurns;
			teamOre += baseIncome * RobotType.MINERFACTORY.buildTurns;
		} else {
			// error, should throw exception
			return 0;
		}
		while (turn < endTurn) {
			// invariant: at beginning of loop, we can build a unit
			if (endTurn - turn > unitBreakEvenTurns) {
				if (teamOre >= type.oreCost) {
					teamOre -= type.oreCost;
					numUnits++;
					// advance by spawn delay
					double income = baseIncome + numUnits*incomePerUnit;
					int turnsToWait = type.buildTurns;
					if (turn+turnsToWait < endTurn) {
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					} else {
						// advance to endTurn
						turnsToWait = endTurn - turn;
						turn = endTurn;
						teamOre += income*turnsToWait;
					}
				} else {
					// advance by number of turns to get enough ore
					double income = baseIncome + numUnits*incomePerUnit;
					int turnsToWait = (int)Math.ceil((type.oreCost - teamOre) / income);
					if (turn+turnsToWait < endTurn) {
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					} else {
						// advance to endTurn
						turnsToWait = endTurn - turn;
						turn = endTurn;
						teamOre += income*turnsToWait;
					}
				}
			} else {
				// advance to endTurn
				double income = baseIncome + numUnits*incomePerUnit;
				int turnsToWait = endTurn - turn;
				turn = endTurn;
				teamOre += income*turnsToWait;
			}
		}
		return teamOre;
	}

	// parameters will change in future
	public static double simulateEarlyMiningOre(RobotType type, int maxNumUnits, double orePerSquare, int endTurn) {
		double baseIncome = 5;
		double teamOre = 500;
		int numUnits = 0;
		int turn = 0;
		double incomePerUnit;
		if (type == RobotType.BEAVER) {
			incomePerUnit = beaverOreRateGivenNumMines(orePerSquare, beaverOptimalNumMines(orePerSquare));
		} else if (type == RobotType.MINER) {
			incomePerUnit = minerOreRateGivenNumMines(orePerSquare, minerOptimalNumMines(orePerSquare));
			// build beaver
			teamOre -= RobotType.BEAVER.oreCost;
			turn += RobotType.BEAVER.buildTurns;
			teamOre += baseIncome * RobotType.BEAVER.buildTurns;
			// build minerfactory
			teamOre -= RobotType.MINERFACTORY.oreCost;
			turn += RobotType.MINERFACTORY.buildTurns;
			teamOre += baseIncome * RobotType.MINERFACTORY.buildTurns;
		} else {
			// error, should throw exception
			return 0;
		}
		while (turn < endTurn) {
			// invariant: at beginning of loop, we can build a unit
			if (numUnits < maxNumUnits) {
				if (teamOre >= type.oreCost) {
					teamOre -= type.oreCost;
					numUnits++;
					// advance by spawn delay
					double income = baseIncome + numUnits*incomePerUnit;
					int turnsToWait = type.buildTurns;
					if (turn+turnsToWait < endTurn) {
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					} else {
						// advance to endTurn
						turnsToWait = endTurn - turn;
						turn = endTurn;
						teamOre += income*turnsToWait;
					}
				} else {
					// advance by number of turns to get enough ore
					double income = baseIncome + numUnits*incomePerUnit;
					int turnsToWait = (int)Math.ceil((type.oreCost - teamOre) / income);
					if (turn+turnsToWait < endTurn) {
						turn += turnsToWait;
						teamOre += income*turnsToWait;
					} else {
						// advance to endTurn
						turnsToWait = endTurn - turn;
						turn = endTurn;
						teamOre += income*turnsToWait;
					}
				}
			} else {
				// advance to endTurn
				double income = baseIncome + numUnits*incomePerUnit;
				int turnsToWait = endTurn - turn;
				turn = endTurn;
				teamOre += income*turnsToWait;
			}
		}
		return teamOre;
	}

	public static double minerSkipBenefit(int travelDist, double oreA, double oreB) {
		// ySkip = max(rateB*(x-delaySkip),0)
		// yTake = rateA*x where x < delayTake, rateA*delayTake + rateB*(x-delayTake) otherwise
		int numMinesA = minerOptimalNumMines(oreA);
		int numMinesB = minerOptimalNumMines(oreB);
		double rateA = minerOreRateGivenNumMines(oreA, numMinesA);
		double rateB = minerOreRateGivenNumMines(oreB, numMinesB);
		int delaySkip = 2*travelDist;
		int delayTake = 2*(1+numMinesA)*travelDist;
		int breakEvenTurn = (int)(rateB*delaySkip / (rateB-rateA));
		double steadyStateDiff = rateA*delayTake - rateB*delayTake + rateB*delaySkip; // + means take was beneficial
		// (breakEvenTurn < delayTake) means skipping was beneficial
		if (breakEvenTurn < delayTake) {
			if (steadyStateDiff > 0) {
				// problem
				System.out.println("problem: broke even, but steadyStateDiff = " + steadyStateDiff);
				return -steadyStateDiff;
			} else {
				return -steadyStateDiff;
			}
		} else {
			if (steadyStateDiff >= 0) {
				return -steadyStateDiff;
			} else {
				// problem
				System.out.println("problem didn't break even, but: steadyStateDiff = " + steadyStateDiff);
				return -steadyStateDiff;
			}
		}

	}

	public static double beaverSkipBenefit(int travelDist, double oreA, double oreB) {
		// ySkip = max(rateB*(x-delaySkip),0)
		// yTake = rateA*x where x < delayTake, rateA*delayTake + rateB*(x-delayTake) otherwise
		int numMinesA = beaverOptimalNumMines(oreA);
		int numMinesB = beaverOptimalNumMines(oreB);
		double rateA = beaverOreRateGivenNumMines(oreA, numMinesA);
		double rateB = beaverOreRateGivenNumMines(oreB, numMinesB);
		int delaySkip = 2*travelDist;
		int delayTake = 2*(1+numMinesA)*travelDist;
		int breakEvenTurn = (int)(rateB*delaySkip / (rateB-rateA));
		double steadyStateDiff = rateA*delayTake - rateB*delayTake + rateB*delaySkip; // + means take was beneficial
		// (breakEvenTurn < delayTake) means skipping was beneficial
		if (breakEvenTurn < delayTake) {
			if (steadyStateDiff > 0) {
				// problem
				System.out.println("problem: broke even, but steadyStateDiff = " + steadyStateDiff);
				return -steadyStateDiff;
			} else {
				return -steadyStateDiff;
			}
		} else {
			if (steadyStateDiff >= 0) {
				return -steadyStateDiff;
			} else {
				// problem
				System.out.println("problem didn't break even, but: steadyStateDiff = " + steadyStateDiff);
				return -steadyStateDiff;
			}
		}

	}

	public static int minerOptimalNumMines(double startOre) {
		double inflectionPointOreTheoretical = MINER_MINE_RATE * MINER_MINE_MAX;
		int inflectionPointNumMines = Math.max((int)((startOre - inflectionPointOreTheoretical) / MINER_MINE_MAX), 0);
		double inflectionPointOre = startOre - (inflectionPointNumMines * MINER_MINE_MAX);
		int numMines = inflectionPointNumMines;
		int turns = numMines * 2 + 2; // account for movement and core delay;
		double ore = inflectionPointOre;
		double rate;
		double maxRate = (startOre - ore) / turns;
		int maxRateNumMines = numMines;
		while (ore > 0) {
			ore -= Math.min(Math.max(Math.min(ore/MINER_MINE_RATE,MINER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
			numMines++;
			turns += 2; // account for core delay
			rate = (startOre - ore) / turns;
			if (rate > maxRate) {
				maxRate = rate;
				maxRateNumMines = numMines;
			} else {
				break; // will not slope up again
			}
		}
		return maxRateNumMines;
	}

	public static int beaverOptimalNumMines(double startOre) {
		double inflectionPointOreTheoretical = BEAVER_MINE_RATE * BEAVER_MINE_MAX;
		int inflectionPointNumMines = Math.max((int)((startOre - inflectionPointOreTheoretical) / BEAVER_MINE_MAX), 0);
		double inflectionPointOre = startOre - (inflectionPointNumMines * BEAVER_MINE_MAX);
		int numMines = inflectionPointNumMines;
		int turns = numMines * 2 + 2; // account for movement and core delay;
		double ore = inflectionPointOre;
		double rate;
		double maxRate = (startOre - ore) / turns;
		int maxRateNumMines = numMines;
		while (ore > 0) {
			ore -= Math.min(Math.max(Math.min(ore/BEAVER_MINE_RATE,BEAVER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
			numMines++;
			turns += 2; // account for core delay
			rate = (startOre - ore) / turns;
			if (rate > maxRate) {
				maxRate = rate;
				maxRateNumMines = numMines;
			} else {
				break; // will not slope up again
			}
		}
		return maxRateNumMines;
	}

	public static double minerOreRateGivenNumMines(double startOre, int numMines) {
		double ore = startOre;
		for (int i = 0; i < numMines; i++) {
			ore -= Math.min(Math.max(Math.min(ore/MINER_MINE_RATE,MINER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
		}
		int turns = numMines*2 + 2;
		double oreRate = (startOre - ore) / turns;
		return oreRate;
	}

	public static double beaverOreRateGivenNumMines(double startOre, int numMines) {
		double ore = startOre;
		for (int i = 0; i < numMines; i++) {
			ore -= Math.min(Math.max(Math.min(ore/BEAVER_MINE_RATE,BEAVER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
		}
		int turns = numMines*2 + 2;
		double oreRate = (startOre - ore) / turns;
		return oreRate;
	}

	public static double minerOreLeftAfterMining(double ore, int num) {
		for (int i = 0; i < num; i++) {
			ore -= Math.min(Math.max(Math.min(ore/MINER_MINE_RATE,MINER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
		}
		return ore;
	}

	public static double minerOreLeftAfterMining(double ore) {
		return ore - Math.min(Math.max(Math.min(ore/MINER_MINE_RATE,MINER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
	}

	public static double beaverOreLeftAfterMining(double ore, int num) {
		for (int i = 0; i < num; i++) {
			ore -= Math.min(Math.max(Math.min(ore/BEAVER_MINE_RATE,BEAVER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
		}
		return ore;
	}

	public static double beaverOreLeftAfterMining(double ore) {
		return ore - Math.min(Math.max(Math.min(ore/BEAVER_MINE_RATE,BEAVER_MINE_MAX),MINIMUM_MINE_AMOUNT),ore);
	}

	public static int minerTurnsToMine(double start, double stop) {
		int i = 0;
		while (start > stop) {
			start -= Math.min(Math.max(Math.min(start/MINER_MINE_RATE,MINER_MINE_MAX),MINIMUM_MINE_AMOUNT),start);
			i++;
		}
		i = i*2;
		return i;
	}

	public static int beaverTurnsToMine(double start, double stop) {
		int i = 0;
		while (start > stop) {
			start -= Math.min(Math.max(Math.min(start/BEAVER_MINE_RATE,BEAVER_MINE_MAX),MINIMUM_MINE_AMOUNT),start);
			i++;
		}
		i = i*2;
		return i;
	}

	public static void main(String[] args) {
		/*
		for (double startOre = 0; startOre <= 100; startOre+= 10) {
			int numMines = 0;
			double oreLeft;
			double oreRate;
			do {
				oreRate = minerOreRateGivenNumMines(startOre, numMines);
				oreLeft = minerOreLeftAfterMining(startOre, numMines);
				System.out.println(startOre + " ore, " + numMines + " mines: rate = " + oreRate + " remaining = " + oreLeft);
				numMines++;
			} while (oreLeft > 0);
			int optimalNumMines = minerOptimalNumMines(startOre);
			System.out.println(startOre + " ore: optimal mines = " + optimalNumMines + " with remaining = " + minerOreLeftAfterMining(startOre, optimalNumMines));
		}
		double ore = 80;
		for (int i = 0; i < 20; i++) {
			int optimalNumMines = minerOptimalNumMines(ore);
			double oreLeft = minerOreLeftAfterMining(ore, optimalNumMines);
			System.out.println(ore + " ore: optimal mines = " + optimalNumMines + " with rate = " + minerOreRateGivenNumMines(ore, optimalNumMines) + " remaining = " + oreLeft);
			ore = oreLeft;
		}

		for (double startOre = 0; startOre <= 100; startOre+= 10) {
			int numMines = 0;
			double oreLeft;
			double oreRate;
			do {
				oreRate = beaverOreRateGivenNumMines(startOre, numMines);
				oreLeft = beaverOreLeftAfterMining(startOre, numMines);
				System.out.println(startOre + " ore, " + numMines + " mines: rate = " + oreRate + " remaining = " + oreLeft);
				numMines++;
			} while (oreLeft > 0);
			int optimalNumMines = beaverOptimalNumMines(startOre);
			System.out.println(startOre + " ore: optimal mines = " + optimalNumMines + " with remaining = " + beaverOreLeftAfterMining(startOre, optimalNumMines));
		}
		ore = 80;
		for (int i = 0; i < 20; i++) {
			int optimalNumMines = beaverOptimalNumMines(ore);
			double oreLeft = beaverOreLeftAfterMining(ore, optimalNumMines);
			System.out.println(ore + " ore: optimal mines = " + optimalNumMines + " with rate = " + beaverOreRateGivenNumMines(ore, optimalNumMines) + " remaining = " + oreLeft);
			ore = oreLeft;
		}
		*/
		/*
		for (int travelDist = 10; travelDist <= 50; travelDist+=10) {
			for (int oreB = 20; oreB <= 80; oreB+=20) {
				for (int oreA = 10; oreA < oreB; oreA+=10 ) {
					System.out.println("dist = " + travelDist + " oreA = " + oreA + " oreB = " + oreB + ": skipBenefit = " + minerSkipBenefit(travelDist, oreA, oreB));
				}
			}
		}
		*/
		
		/*
		for (double ore = 10; ore <= 80; ore+=10) {
			for (int numTurns = 0; numTurns <= 1000; numTurns+=50) {
				double beaverResult = simulateEarlyMiningOre(RobotType.BEAVER, 9999, ore, numTurns);
				double minerResult = simulateEarlyMiningOre(RobotType.MINER, 9999, ore, numTurns);
				boolean beaverWins = beaverResult > minerResult;
				double winPercent;
				if (beaverWins) {
					winPercent = 100*(beaverResult-minerResult)/minerResult;
				} else {
					winPercent = 100*(minerResult-beaverResult)/beaverResult;
				}
				System.out.println("ore = " + ore + " turns = " + numTurns + ": winner = " + (beaverWins?"beaver":"miner") + " by " + (int)winPercent + " percent");
			}
		}
		*/
		
		for (double ore = 10; ore <= 80; ore+=10) {
			for (int numSquares = 10; numSquares <= 1280; numSquares*=2) {
				OreTurns beaverResult = simulateSafeEarlyMining(RobotType.BEAVER, ore, numSquares);
				OreTurns minerResult = simulateSafeEarlyMining(RobotType.MINER, ore, numSquares);
				boolean beaverWins = beaverResult.ore > minerResult.ore;
				boolean beaverFaster = beaverResult.turns < minerResult.turns;
				double winPercent;
				double fasterPercent;
				if (beaverWins) {
					winPercent = 100*(beaverResult.ore-minerResult.ore)/minerResult.ore;
				} else {
					winPercent = 100*(minerResult.ore-beaverResult.ore)/beaverResult.ore;
				}
				if (beaverFaster) {
					fasterPercent = 100*(1/(double)beaverResult.turns - 1/(double)minerResult.turns)*minerResult.turns;
				} else {
					fasterPercent = 100*(1/(double)minerResult.turns - 1/(double)beaverResult.turns)*beaverResult.turns;
				}
				System.out.println("ore = " + ore + " squares = " + numSquares + ": winner = " + (beaverWins?"beaver":"miner") + " by " + (int)winPercent + " percent, faster = " + (beaverFaster?"beaver":"miner") + " by " + (int)fasterPercent + " percent");
			}
		}
	}

}
