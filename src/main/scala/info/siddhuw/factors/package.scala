package info.siddhuw

/**
 * Created by siddhuwarrier on 27/07/2014.
 */
package object factors {
  type FixtureResult = Map[String, String]
  type FixtureResults = List[Map[String, String]]

  case class Probability(teamOneWin: Double, teamTwoWin: Double, draw: Double) {
    def *(multiplicand: Double): Probability = {
      Probability(teamOneWin * multiplicand, teamTwoWin * multiplicand, draw * multiplicand)
    }

    def +(that: Probability) = {
      Probability(teamOneWin + that.teamOneWin, teamTwoWin + that.teamTwoWin, draw + that.draw)
    }
    
    def /(divisor: Double): Probability = {
      Probability(teamOneWin / divisor, teamTwoWin / divisor, draw / divisor)
    }
  }

  case class HistoricResults(teamName: String, wins: Double, losses: Double, draws: Double)

  object ResultType extends Enumeration {
    type ResultType = Value
    val WIN, LOSS, DRAW = Value
  }
}
