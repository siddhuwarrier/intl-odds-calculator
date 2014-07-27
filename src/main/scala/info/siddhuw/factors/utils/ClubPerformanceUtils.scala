package info.siddhuw.factors.utils

import java.text.SimpleDateFormat

import com.github.tototoshi.csv.CSVReader
import info.siddhuw.Team.Team
import info.siddhuw._
import info.siddhuw.factors._

object ClubPerformanceUtils {

  val HOME_TEAM_FIELD = "HomeTeam"
  val AWAY_TEAM_FIELD = "AwayTeam"
  val DATE_FIELD = "Date"
  val DATE_FORMAT = new SimpleDateFormat("dd/MM/yy")
  val FT_HOME_GOALS_FIELD = "FTHG"
  val FT_AWAY_GOALS_FIELD = "FTAG"

  def getClubDraws(clubName: String, leagueName: String, numGamesToConsider: Int): Int = {
    val (lastHomeResults, lastAwayResults) = getLastHomeAndAwayResults(clubName, leagueName, numGamesToConsider)
    lastHomeResults.count(_.isDraw(FT_HOME_GOALS_FIELD, FT_AWAY_GOALS_FIELD)) +
      lastAwayResults.count(_.isDraw(FT_AWAY_GOALS_FIELD, FT_HOME_GOALS_FIELD))
  }

  def getClubWins(clubName: String, leagueName: String, numGamesToConsider: Int): Int = {
    val (lastHomeResults, lastAwayResults) = getLastHomeAndAwayResults(clubName, leagueName, numGamesToConsider)
    lastHomeResults.count(_.isWin(FT_HOME_GOALS_FIELD, FT_AWAY_GOALS_FIELD)) +
      lastAwayResults.count(_.isWin(FT_AWAY_GOALS_FIELD, FT_HOME_GOALS_FIELD))
  }

  def getClubLosses(clubName: String, leagueName: String, numGamesToConsider: Int): Int = {
    val (lastHomeResults, lastAwayResults) = getLastHomeAndAwayResults(clubName, leagueName, numGamesToConsider)
    lastHomeResults.count(_.isLoss(FT_HOME_GOALS_FIELD, FT_AWAY_GOALS_FIELD)) +
      lastAwayResults.count(_.isLoss(FT_AWAY_GOALS_FIELD, FT_HOME_GOALS_FIELD))
  }

  private def getLastHomeAndAwayResults(clubName: String, leagueName: String,
                                        numGamesToConsider: Int): (FixtureResults, FixtureResults) = {
    val allResults = CSVReader.open(getFileFromClasspath(leagueName + ".csv")).allWithHeaders()
    val lastHomeResults = allResults.filterBy(clubName, Team.HOME).takeRight(numGamesToConsider / 2)
    val lastAwayResults = allResults.filterBy(clubName, Team.AWAY).takeRight(numGamesToConsider / 2)

    (lastHomeResults, lastAwayResults)
  }

  implicit class ResultCalculator(result: FixtureResult) {
    def isWin(firstScoreField: String, secondScoreField: String): Boolean = {
      getScore(firstScoreField) > getScore(secondScoreField)
    }

    def isDraw(firstScoreField: String, secondScoreField: String): Boolean = {
      getScore(firstScoreField) == getScore(secondScoreField)
    }

    def isLoss(firstScoreField: String, secondScoreField: String): Boolean = {
      getScore(firstScoreField) < getScore(secondScoreField)
    }


    private def getScore(scoreField: String): Int = {
      val score = result.getOrElse(scoreField, 0).toString
      if (score.trim.size == 0) 0 else score.toInt
    }
  }

  implicit class ResultFilter(results: FixtureResults) {
    def filterBy(teamName: String, team: Team): FixtureResults = {
      val teamField = if (team == Team.HOME) HOME_TEAM_FIELD else AWAY_TEAM_FIELD
      results.filter(_.get(teamField).get == teamName)
    }
  }

}
