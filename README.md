# What is this?

This is a simplistic proof-of-concept architectural framework for calculating odds for World Cup football matches.

Given the program knows, for any given pair of teams:

* the squad composition for each team, i.e., the players, the clubs they play for, and the league the clubs are in,
* the historical results in world cup finals for each of the teams, and
* the results of the clubs that each of the players on the teams play for in the 2013/14 season

it will calculate the odds for each team's victory.

> This is a spike. This was not developed using TDD, and there are no tests written in after the fact.  

# Usage

Clone the application:

    git clone https://github.com/siddhuwarrier/intl-odds-calculator
    
Build a JAR using Maven:

    mvn package
    
Run the generated assembly:

    target/intl-odds-calculator/intl-odds-calculator-assembly/bin/calc-odds.sh [-p] England|Uruguay|Germany|France England|Uruguay|Germany|France
     
The ``-p`` option allows you to add Paul the psychic octopus's expert predictions in as a factor with a weight of ``0.05``.

## Logs

Logs are saved to ``/tmp/intl-odds-calculator.log``.

# Architecture

This program determines odds by:

 * calculating the probability of a team's victory given its performance in past world cups.
 * calculating the probability of a team's victory given the performance of the clubs the players in the team normally play for.
    * The results of any given club are weighted by the number of players on the team that play for that club.
 * weighting the probabilities calculated by applying a weight of 0.7 to the first of the two factors, and 0.3 to the second.

This architecture is extensible because you can add more (or more accurate/sensible/irrational) factors in by implementing the ``PerformanceFactor`` trait. 
For example, to add Paul the Psychic Octopus (or a similarly unscientific factor) in:

Add a class PsychicFactor in with the following content:

        class PsychicFactor extends PerformanceFactor with LazyLogging {
          override def getProbabilities(teamOneName: String, teamTwoName: String): Probability = {
            val random = new Random(Platform.currentTime)
            val probTeamOneWin = random.nextDouble() * 0.49d
            val probTeamTwoWin = random.nextDouble() * 0.49d
            val probDraw = 1 - (probTeamOneWin + probTeamTwoWin)
        
            Probability(probTeamOneWin, probTeamTwoWin, probDraw)
          }
        }
        
Then, add this into the list of factors you want considered in ``OddsCalculator.scala`` (make sure the weights add up to 1.0):

    val weightedFactors = Map(new WorldCupPerformanceFactor() -> 0.7, new ClubPerformanceFactor() -> 0.25, new PsychicFactor() -> 0.05)

# Why is this implementation unlikely to be accurate?

This implementation is by no means complete or accurate. This is merely a proof-of-concept to illustrate how we can weight
multiple factors to calculate the odds of a given team triumphing over another in a world cup competition (as long as
the teams are England, Germany, France, or Uruguay).

The inaccuracies stem from:
* the lack of data (I retrieved some data from (http://openfootball.github.io/)[http://openfootball.github.io/) and some
 from (http://www.football-data.co.uk)[http://www.football-data.co.uk/germanym.php], and
* the lack of time (I just spent a few hours on a quiet Sunday on this)
* the limited number of factors I take into consideration.


## Illustration
I doubt you will make any money betting using these odds (not to mention the fact that the World Cup is done and dusted). For instance, the factors I use to calculate odds here would
indicate that England were far likelier to beat Uruguay in their group game than the other way around. And we all know how that turned out...

The reason this program would suggest that England are likelier to beat Uruguay is because:

* we weight historical world cup performances from 1982 to 2010 where England have performed better than Uruguay (England: R16, QF, SF, DNQ, R16, QF, QF, R16; Uruguay: DNQ, R16, R16, DNQ, DNQ, Group, DNQ, SF)
* 12 of England's players squad play for clubs that finished in the top 4 of their leagues, compared to only 8 of the Uruguay squad.
* Some of Uruguay's players play for clubs outside of Europe that I could not retrieve data for. So, I had to disregard them.
* No weight is assigned for individual players' form.
* No World Cup qualification data or head-to-head data was used (as I could not retreive the data).

Note: According to this historical Odds portal site, the real bookies got the odds far more right: http://www.oddsportal.com/soccer/world/world-cup-2014/results/

## Then Why?

Et pourquoi pas?

As mentioned earlier, this is purely to serve a quicky and dirty PoC implementation of a possible architecture
 to calculate odds using a multitude of factors.
 
# Known issues

* This is a spike, and there aren't any tests.
* This application will fail if you try to calculate the odds for two teams at least one of which has played fewer than 30 WC games.
* This application will fail if you try to calculate the odds for two teams, at least one of whom has players who don't play in the 
top English, Scottish, German, Spanish, Italian, or Portuguese leagues (sorry, Netherlands!) 

# Possible Improvements

* Use [football.db](http://openfootball.github.io/) properly rather than exporting data into CSV. That should also make it easier to make it a web app.