package info.siddhuw

import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import info.siddhuw.Team.Team
import info.siddhuw.factors.HistoricResults

object WcPerformanceCalc extends LazyLogging {
  val TEAM1_FIELD = "Team1"
  val TEAM2_FIELD = "Team2"
  val SCORE1_FIELD = "Score1"
  val SCORE2_FIELD = "Score2"

  type FixtureResult = Map[String, String]
  type FixtureResults = List[Map[String, String]]

  def getWcWinsAndLosses(teamName: String, numGamesToConsider: Int = 30): HistoricResults = {
    val allWcResults = CSVReader.open(getFileFromClasspath(teamName + "_wc.csv")).allWithHeaders()
    val lastWcResults = allWcResults.takeRight(numGamesToConsider) //TODO do not assume sorted by date

    HistoricResults(teamName, getWcWins(lastWcResults, teamName), getWcLosses(lastWcResults, teamName), getWcDraws(lastWcResults, teamName))
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
