package info.siddhuw

import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.factors.{ClubPerformanceFactor, Probability, PsychicFactor, WorldCupPerformanceFactor}
import org.clapper.argot._

/**
 * Created by siddhuwarrier on 27/07/2014.
 */
object OddsCalculator extends App with LazyLogging {
  val CLUB_FIELD = "Club"
  val LEAGUE_FIELD = "League"
  val WC_PERF_WEIGHT = 0.7d
  val CLUB_PERF_WEIGHT = 0.3d
  val NUM_WC_GAMES = 30
  val VALID_TEAM_NAME = List("England", "France", "Germany", "Uruguay")

  val (parser, psychicOpt, teamOneOpt, teamTwoOpt) = createParser
  parser.parse(args)
  if (teamOneOpt.value.get == teamTwoOpt.value.get) {
    parser.usage("The two teams cannot be the same")
  }
  val usePsychic = psychicOpt.value.getOrElse(false)
  val weightedFactors = usePsychic match {
    case true =>
      Map(new WorldCupPerformanceFactor() -> 0.7, new ClubPerformanceFactor() -> 0.25, new PsychicFactor() -> 0.05)
    case _ =>
      Map(new WorldCupPerformanceFactor() -> 0.7, new ClubPerformanceFactor() -> 0.3)
  }
  calculateOdds(teamOneOpt.value.get, teamTwoOpt.value.get)

  private def calculateOdds(teamOneName: String, teamTwoName: String) {
    printInfoMsg(teamOneName, teamTwoName, usePsychic)

    val weightedProbabilities = weightedFactors.map {
      case (factor, weight) =>
        factor.getProbabilities(teamOneName, teamTwoName) * weight
    }

    val summedProbability = weightedProbabilities.foldLeft(Probability(0d, 0d, 0d))((sum, probability) => sum + probability)
    val weightedProbability = summedProbability / weightedFactors.values.sum
    logger.debug("Weighted probability: " + weightedProbability)


    println("Decimal odds using weighted factors: (a) World cup performance, (b) performance of clubs the players belong to (0.3)")

    println(teamOneName + " winning: " + 1 / weightedProbability.teamOneWin)
    println(teamTwoName + " winning: " + 1 / weightedProbability.teamTwoWin)
    println("Draw: " + 1 / weightedProbability.draw)
  }

  private def printInfoMsg(teamOneName: String, teamTwoName: String, usePsychic: Boolean) {
    println("Calculating odds for " + teamOneName + " vs " + teamTwoName + ".")
    val factors = "Using weighted factors: (a) World cup performance, (b) performance of clubs players belong to"
    if (usePsychic)
      println(factors + ", (c) Paul the Psychic Octopus (back from the dead)")
    else
      println(factors)
  }

  private def createParser: (ArgotParser, FlagOption[Boolean], SingleValueParameter[String], SingleValueParameter[String]) = {
    val parser = new ArgotParser("calc-odds.sh", preUsage = Some("Simplistic proof-of-concept architectural framework for calculating odds for World Cup football matches."))

    val psychicOpt = parser.flag[Boolean](List("p", "psychic"),"Use Paul the psychic octopus.")(ArgotConverters.convertFlag)
    val teamOneOpt = parser.parameter[String]("teamOne", "One of [England|France|Germany|Uruguay]", optional = false) {
      (teamName, opt) =>
        isValidTeam(parser, teamName)
    }
    val teamTwoOpt = parser.parameter[String]("teamTwo", "One of [England|France|Germany|Uruguay]", optional = false) {
      (teamName, opt) =>
        isValidTeam(parser, teamName)
    }


    (parser, psychicOpt, teamOneOpt, teamTwoOpt)
  }

  private def isValidTeam(parser: ArgotParser, teamName: String): String = {
    if (!VALID_TEAM_NAME.contains(teamName)) {
      parser.usage("Team name \"" + teamName + "\" is not supported.")
    }

    teamName
  }
}
