package info.siddhuw.factors

/**
 * A trait for one of the many factors that should be considered when calculating odds for an international match
 * between two teams
 *
 * Created by siddhuwarrier on 27/07/2014.
 */
trait PerformanceFactor {
  def getProbabilities(teamOneName: String, teamTwoName: String): Probability

  protected def getFromHistoricalResults(teamOneHistoricRes: HistoricResults,
                                         teamTwoHistoricRes: HistoricResults, totalNumGames: Int): Probability = {
    val teamOneWinProb = (teamOneHistoricRes.wins + teamTwoHistoricRes.losses) / totalNumGames
    val teamTwoWinProb = (teamOneHistoricRes.losses + teamTwoHistoricRes.wins) / totalNumGames
    val drawProb = (teamOneHistoricRes.draws + teamTwoHistoricRes.draws) / totalNumGames

    Probability(teamOneWinProb, teamTwoWinProb, drawProb)
  }
}
