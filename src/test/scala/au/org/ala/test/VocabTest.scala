package au.org.ala.test

import org.scalatest.FunSuite
import au.org.ala.biocache.{StateCentrePoints, HabitatMap}

class VocabTest extends FunSuite {
  
  test("Expect non marine to match terrestrial"){
	  expect(true){HabitatMap.isCompatible("non-marine", "terrestrial").get}
  }

  test("Expect  marine to not match terrestrial"){
	  expect(false){HabitatMap.isCompatible("marine", "terrestrial").get}
  }

  test("Expect coordinates for QLD centre to match"){

    expect(false){StateCentrePoints.coordinatesMatchCentre("QLD", "12","12")}

    //   -20.9175738f, 142.7027956f

    expect(true){StateCentrePoints.coordinatesMatchCentre("QLD", "-20.9175738","142.7027956")}
    expect(true){StateCentrePoints.coordinatesMatchCentre("QLD", "-20.917573","142.702795")}
    expect(true){StateCentrePoints.coordinatesMatchCentre("QLD", "-20.917","142.702")}
  }
}