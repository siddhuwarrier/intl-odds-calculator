package info.siddhuw

import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.factors.{ClubPerformanceFactor, Probability, WorldCupPerformanceFactor}

/**
 * Created by siddhuwarrier on 27/07/2014.
 */
object OddsCalculator extends App with LazyLogging {
  val CLUB_FIELD = "Club"
  val LEAGUE_FIELD = "League"
  val WC_PERF_WEIGHT = 0.7d
  val CLUB_PERF_WEIGHT = 0.3d
  val NUM_WC_GAMES = 30

  val teamOneName = "England"
  val teamTwoName = "England"

  println("Calculating. Please wait...")

  val weightedFactors = Map(new WorldCupPerformanceFactor() -> 0.7, new ClubPerformanceFactor() -> 0.3)
  val weightedProbabilities = weightedFactors.map {
    case (factor, weight) =>
      factor.getProbabilities(teamOneName, teamTwoName) * weight
  }

  val summedProbability = weightedProbabilities.foldLeft(Probability(0d, 0d, 0d))((sum, probability) => sum + probability)
  val weightedProbability = summedProbability / weightedFactors.values.sum
  logger.debug("Weighted probability: " + weightedProbability)

  println("Decimal odds using two weighted factors: (a) World cup performance (0.7) and (b) performance of clubs the players belong to (0.3)")
  println(teamOneName + " winning: " + 1/weightedProbability.teamOneWin)
  println(teamTwoName + " winning: " + 1/weightedProbability.teamTwoWin)
  println("Draw: " + 1/weightedProbability.draw)
}
