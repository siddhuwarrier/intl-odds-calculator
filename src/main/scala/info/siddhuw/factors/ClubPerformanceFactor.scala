package info.siddhuw.factors

import java.text.SimpleDateFormat

import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import info.siddhuw._
import info.siddhuw.factors.ResultType.ResultType
import info.siddhuw.factors.utils.ClubPerformanceUtils

/**
 * This factor works as follows.:
 *
 * It calculates the performance of the clubs that each of the team members belongs to, weighting the performance
 * by the number of team members who play for that particular club.
 *
 * This class expects:
 * * a teamsheet for each team that has the following fields: Player,Club,League (League has to be within parameters)
 * * a results file with the same name as the league, in the football-data.co.uk format, sorted by match date
 *
 * Created by siddhuwarrier on 27/07/2014.
 */
class ClubPerformanceFactor(numGamesToConsider: Int = 14) extends PerformanceFactor with LazyLogging {
  val CLUB_FIELD = "Club"
  val LEAGUE_FIELD = "League"

  val HOME_TEAM_FIELD = "HomeTeam"
  val AWAY_TEAM_FIELD = "AwayTeam"
  val DATE_FIELD = "Date"
  val DATE_FORMAT = new SimpleDateFormat("dd/MM/yy")
  val FT_HOME_GOALS_FIELD = "FTHG"
  val FT_AWAY_GOALS_FIELD = "FTAG"

  override def getProbabilities(teamOneName: String, teamTwoName: String): Probability = {
    val teamOneSquad = CSVReader.open(getFileFromClasspath(teamOneName + ".csv")).allWithHeaders()
    val teamTwoSquad = CSVReader.open(getFileFromClasspath(teamTwoName + ".csv")).allWithHeaders()
    val teamOneHistoricRes = calculateHistoricalResults(teamOneName, teamOneSquad)
    val teamTwoHistoricRes = calculateHistoricalResults(teamTwoName, teamTwoSquad)

    val prob = getFromHistoricalResults(teamOneHistoricRes, teamTwoHistoricRes, numGamesToConsider * 2)
    logger.debug("Probability given club performance: {}", prob)

    prob
  }

  private def calculateHistoricalResults(teamName: String, squad: List[Map[String, String]]): HistoricResults = {
    val playersPerClub = getPlayersPerClub(squad)
    logger.debug("For team " + teamName + ", players to club mapping: " + playersPerClub)
    val clubToLeagueMapping = getClubToLeagueMapping(squad)

    val numWins = getResultsFor(playersPerClub, clubToLeagueMapping, squad.size, ResultType.WIN)
    val numLosses = getResultsFor(playersPerClub, clubToLeagueMapping, squad.size, ResultType.LOSS)
    val numDraws = getResultsFor(playersPerClub, clubToLeagueMapping, squad.size, ResultType.DRAW)

    HistoricResults(teamName, numWins, numLosses, numDraws)
  }

  private def getResultsFor(playersPerClub: Map[String, Int], clubToLeagueMapping: Map[String, String],
                            squadSize: Int, resultType: ResultType): Double = {
    playersPerClub.map {
      case (club, numPlayers) =>
        val numResults = resultType match {
          case ResultType.WIN =>
            ClubPerformanceUtils.getClubWins(club, clubToLeagueMapping(club), numGamesToConsider)
          case ResultType.LOSS =>
            ClubPerformanceUtils.getClubLosses(club, clubToLeagueMapping(club), numGamesToConsider)
          case _ =>
            ClubPerformanceUtils.getClubDraws(club, clubToLeagueMapping(club), numGamesToConsider)
        }

        (numPlayers.toDouble / squadSize) * numResults.toDouble
    }.sum
  }


  private def getPlayersPerClub(squad: List[Map[String, String]]): Map[String, Int] = {
    squad.groupBy(_.get(CLUB_FIELD)).map {
      case (k, v) => k.get -> v.size
    }
  }

  private def getClubToLeagueMapping(squad: List[Map[String, String]]): Map[String, String] = {
    squad.groupBy(_.get(CLUB_FIELD)).map {
      case (k, v) => k.get -> v.map(_.get(LEAGUE_FIELD).get).head
    }
  }

}
