package info.siddhuw.factors

import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.Team._
import info.siddhuw._

class WorldCupPerformanceFactor(numGamesToConsider: Int = 30) extends PerformanceFactor with LazyLogging {
  val TEAM1_FIELD = "Team1"
  val TEAM2_FIELD = "Team2"
  val SCORE1_FIELD = "Score1"
  val SCORE2_FIELD = "Score2"

  //expects files named TeamName_wc.csv
  override def getProbabilities(teamOneName: String, teamTwoName: String): Probability = {
    val teamOneHistoricRes = calculateHistoricalResults(teamOneName)
    val teamTwoHistoricRes = calculateHistoricalResults(teamTwoName)

    val prob = getFromHistoricalResults(teamOneHistoricRes, teamTwoHistoricRes, numGamesToConsider * 2)
    logger.debug("Probability: {}", prob)
    prob
  }

  private def calculateHistoricalResults(teamName: String): HistoricResults = {
    val results = CSVReader
      .open(getFileFromClasspath(teamName + "_wc.csv"))
      .allWithHeaders()
      .takeRight(numGamesToConsider)
    HistoricResults(teamName, getWcWins(results, teamName),
      getWcLosses(results, teamName),
      getWcDraws(results, teamName))
  }


  private def getWcDraws(lastWcResults: FixtureResults, teamName: String): Int = {
    lastWcResults.filterBy(teamName, Team.HOME)
      .count(_.isDraw(SCORE1_FIELD, SCORE2_FIELD)) +
      lastWcResults.filterBy(teamName, Team.AWAY)
        .count(_.isDraw(SCORE2_FIELD, SCORE1_FIELD))
  }

  private def getWcWins(lastWcResults: FixtureResults, teamName: String): Int = {
    lastWcResults.filterBy(teamName, Team.HOME)
      .count(_.isWin(SCORE1_FIELD, SCORE2_FIELD)) +
      lastWcResults.filterBy(teamName, Team.AWAY)
        .count(_.isWin(SCORE2_FIELD, SCORE1_FIELD))
  }

  private def getWcLosses(lastWcResults: FixtureResults, teamName: String): Int = {
    lastWcResults.filterBy(teamName, Team.HOME)
      .count(_.isLoss(SCORE1_FIELD, SCORE2_FIELD)) +
      lastWcResults.filterBy(teamName, Team.AWAY)
        .count(_.isLoss(SCORE2_FIELD, SCORE1_FIELD))
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
      val teamField = if (team == Team.HOME) TEAM1_FIELD else TEAM2_FIELD
      results.groupBy(_.get(teamField)).get(Some(teamName)).get
    }
  }

}
