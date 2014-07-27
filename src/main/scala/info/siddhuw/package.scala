package info

import java.io.File

import org.springframework.core.io.ClassPathResource

/**
 * Created by siddhuwarrier on 27/07/2014.
 */
package object siddhuw {
  def getFileFromClasspath(filename: String): File = {
    if (new File(filename).exists()) {
      new File(filename)
    }
    else if (new ClassPathResource(filename).exists()) {
      new File(getClass.getClassLoader.getResource(filename).toURI)
    }
    else {
      throw new IllegalArgumentException(String.format("File %s does not exist", filename))
    }
  }

  object Team extends Enumeration {
    type Team = Value
    val HOME, AWAY = Value
  }
}
