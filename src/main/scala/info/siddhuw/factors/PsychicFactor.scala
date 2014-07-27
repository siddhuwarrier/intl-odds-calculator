package info.siddhuw.factors

import com.typesafe.scalalogging.LazyLogging

import scala.compat.Platform
import scala.util.Random


/**
 * I could just return 0.3333.. *3 and still get the long term trend of the late Paul the octopus's
 * predictions.;-)
 *
 * Created by siddhuwarrier on 27/07/2014.
 */
class PsychicFactor extends PerformanceFactor with LazyLogging {
  override def getProbabilities(teamOneName: String, teamTwoName: String): Probability = {
    val random = new Random(Platform.currentTime)
    val probTeamOneWin = random.nextDouble() * 0.49d
    val probTeamTwoWin = random.nextDouble() * 0.49d
    val probDraw = 1 - (probTeamOneWin + probTeamTwoWin)

    val prob = Probability(probTeamOneWin, probTeamTwoWin, probDraw)
    logger.debug("Probability given belief in psychics: {}", prob)

    prob
  }
}
