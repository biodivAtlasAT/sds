package au.org.ala.biocache

/**
 * Case class that encapsulates a canonical form and variants.
 */
case class Term (canonical:String, variants:Array[String])

/**
 * Case class that represents an error code for a occurrence record.
 */
case class ErrorCode(name:String, code:Int)

/**
 * A trait for a vocabulary. A vocabulary consists of a set
 * of Terms, each with string variants.
 */
trait Vocab {
  val all:Array[Term]
  /**
   * Match a term. Matches canonical form or variants in array
   * @param string2Match
   * @return
   */
  def matchTerm(string2Match:String) : Option[Term] = {
    if(string2Match!=null){
      //strip whitespace & strip quotes and fullstops & uppercase
      val stringToUse = string2Match.replaceAll("([.,-]*)?([\\s]*)?", "").toLowerCase
      for(term<-all){
        if(term.canonical.equalsIgnoreCase(stringToUse))
          return Some(term)
        if(term.variants.contains(stringToUse)){
          return Some(term)
        }
      }
    }
    None
  }
  /**
   * Retrieve all the terms defined in this vocab.
   * @return
   */
  def retrieveAll : Array[Term] = {
    val methods = this.getClass.getMethods
    for{
      method<-methods
      if(method.getReturnType.getName == "au.org.ala.biocache.Term")
    } yield (method.invoke(this).asInstanceOf[Term])
  }
}

/**
 * Quick state string matching implementation.
 */
object States extends Vocab {
  val act = new Term("Australian Capital Territory", Array("AustCapitalTerritory","AustCapitalTerrit","AusCap","AusCapTerrit","ACT"))
  val nsw = new Term("New South Wales", Array("nswales","nsw"))
  val nt = new Term("Northern Territory", Array("nterritory","nterrit","nt"))
  val qld = new Term("Queensland", Array("qland","qld"))
  val sa = new Term("South Australia", Array("sthaustralia","saustralia","saust","sa"))
  val tas = new Term("Tasmania", Array("tassie","tas"))
  val vic = new Term("Victoria", Array("vic","vict"))
  val wa = new Term("Western Australia", Array("waustralia","westaustralia","westaust","wa"))
  val all = retrieveAll
}

/**
 * Matching of coordinates for centre points for states.
 * This is for detecting auto-generated coordinates at very low accuracy.
 */
object StateCentrePoints {
  val map = Map(
    States.act -> (-35.4734679f, 149.0123679f),
    States.nsw -> (-31.2532183f, 146.921099f),
    States.nt -> (-19.4914108f, 132.5509603f),
    States.qld -> (-20.9175738f, 142.7027956f),
    States.sa -> (-30.0002315f, 136.2091547f),
    States.tas -> (-41.3650419f, 146.6284905f),
    States.vic -> (-37.4713077f, 144.7851531f),
    States.wa -> (-27.6728168f, 121.6283098f)
  )

  /**
   * Returns true if the supplied coordinates are the centre point for the supplied
   * state or territory
   */
  def coordinatesMatchCentre(state:String, decimalLatitude:String, decimalLongitude:String) : Boolean = {
    val matchedState = States.matchTerm(state)
    if(!matchedState.isEmpty){

      val coordinates = map.get(matchedState.get)

      //how many decimal places are the supplied coordinates
      try {
          val latitude = decimalLatitude.toFloat
          val longitude = decimalLongitude.toFloat

          val latDecPlaces = noOfDecimalPlace(latitude)
          val longDecPlaces = noOfDecimalPlace(longitude)

          //println("Decimal places: "+latDecPlaces +", "+longDecPlaces)
          //approximate the centre points appropriately
          val approximatedLat = round(coordinates.get._1,latDecPlaces)
          val approximatedLong = round(coordinates.get._2,longDecPlaces)

          //println("Rounded values: "+approximatedLat +", "+approximatedLong)

          if(approximatedLat == latitude && approximatedLong == longitude){
            true
          } else {
            false
          }
      } catch {
        case e:NumberFormatException => false
      }
    } else {
      false
    }
  }

  /**
   * Round to the supplied no of decimal places.
   */
  def round(number:Float, decimalPlaces:Int) : Float = {
    if(decimalPlaces>0){
      var x = 1
      for (i <- 0 until decimalPlaces) x = x * 10
      (((number * x).toInt).toFloat) / x
    } else {
      number.round
    }
  }

  def noOfDecimalPlace(number:Float) : Int = {
    val numberString = number.toString
    val decimalPointLoc = numberString.indexOf(".")
    if(decimalPointLoc<0) {
      0
    } else {
       numberString.substring(decimalPointLoc+1).length
    }
  }
}

/**
 * Vocabulary matcher for basis of record values.
 */
object BasisOfRecord extends Vocab {
  val specimen = new Term("PreservedSpecimen", Array("specimen","s", "spec", "sp"))
  val observation = new Term("HumanObservation", Array("observation","o","obs"))
  val fossil = new Term("FossilSpecimen", Array("fossil","f", "fos"))
  val living = new Term("LivingSpecimen", Array("living","l"))
  val all = retrieveAll
}

/**
 * Vocabulary matcher for type status values.
 */
object TypeStatus extends Vocab {
  val allolectotype = new Term("allolectotype", Array[String]())
  val alloneotype = new Term("alloneotype", Array[String]())
  val allotype = new Term("allotype", Array[String]())
  val cotype = new Term("cotype", Array[String]())
  val epitype = new Term("epitype", Array[String]())
  val exepitype = new Term("exepitype", Array[String]())
  val exholotype = new Term("exholotype", Array("ex holotype"))
  val exisotype = new Term("exisotype", Array[String]())
  val exlectotype = new Term("exlectotype", Array[String]())
  val exneotype = new Term("exneotype", Array[String]())
  val exparatype = new Term("exparatype", Array[String]())
  val exsyntype = new Term("exsyntype", Array[String]())
  val extype = new Term("extype", Array[String]())
  val hapantotype = new Term("hapantotype", Array[String]())
  val holotype = new Term("holotype", Array("holo type"))
  val iconotype = new Term("iconotype", Array[String]())
  val isolectotype = new Term("isolectotype", Array[String]())
  val isoneotype = new Term("isoneotype", Array[String]())
  val isosyntype = new Term("isosyntype", Array[String]())
  val isotype = new Term("isotype", Array[String]("iso type"))
  val lectotype = new Term("lectotype", Array[String]())
  val neotype = new Term("neotype", Array[String]("neo type"))
  val notatype = new Term("notatype", Array("not a type"))  //should this be removed??
  val paralectotype = new Term("paralectotype", Array[String]())
  val paraneotype = new Term("paraneotype", Array[String]())
  val paratype = new Term("paratype", Array[String]())
  val plastoholotype = new Term("plastoholotype", Array[String]())
  val plastoisotype = new Term("plastoisotype", Array[String]())
  val plastolectotype = new Term("plastolectotype", Array[String]())
  val plastoneotype = new Term("plastoneotype", Array[String]())
  val plastoparatype = new Term("plastoparatype", Array[String]())
  val plastosyntype = new Term("plastosyntype", Array[String]())
  val plastotype = new Term("plastotype", Array[String]())
  val secondarytype = new Term("secondarytype", Array[String]())
  val supplementarytype = new Term("supplementarytype", Array[String]())
  val syntype = new Term("syntype", Array[String]())
  val topotype = new Term("topotype", Array[String]())
  val typee = new Term("type", Array[String]())
  val all = retrieveAll
}

trait VocabMaps {

  /** The map of terms to query against */
  val termMap:Map[String, Array[String]]

  /**
   * Compares the supplied term to an array of options
   * for compatibility.
   *
   * @param term
   * @param terms
   * @return
   */
  def areTermsCompatible(term:String, terms:Array[String]) : Option[Boolean] = {
    var weTested:Option[Boolean] = None
    for(matchingTerm<-terms){
      val matches = isCompatible(term, matchingTerm)
      if(!matches.isEmpty){
        //term is recognised
        if(matches.get){
          //it matches
          return Some(true)
        } else {
          weTested = Some(false)
        }
      }
    }
    weTested
  }

  /**
   * Returns None if the term wasnt recognised. If it was recognised, then we can test it.
   *
   * @param term1
   * @param term2
   * @return
   */
  def isCompatible (term1:String, term2:String) : Option[Boolean] = {
    if(term1!=null && term2!=null){
      if(term1.toUpperCase == term2.toUpperCase){
        //same term, return true
        Some(true)
      } else {
        val mapped = termMap.get(term1.toUpperCase)
        if(mapped.isEmpty){
          // if the term isnt mapped, return no decision
          None
        } else {
          //it is mapped, so return if its compatible
          Some(mapped.get.contains(term2.toUpperCase))
        }
      }
    } else {
      None
    }
  }
}

object HabitatMap extends VocabMaps {
  val termMap = Map(
    "MARINE" -> Array("MARINE"),
    "NON-MARINE" -> Array("NON-MARINE", "TERRESTRIAL", "LIMNETIC"),
    "TERRESTRIAL" -> Array("NON-MARINE", "TERRESTRIAL", "LIMNETIC"),
    "LIMNETIC" -> Array("NON-MARINE", "TERRESTRIAL", "LIMNETIC")
  )
}

object AssertionCodes {

  val GEOSPATIAL_NEGATED_LATITUDE = ErrorCode("qaNegatedLatitude",1)
  val GEOSPATIAL_NEGATED_LONGITUDE = ErrorCode("qaNegatedLongitude",2)
  val GEOSPATIAL_INVERTED_COORDINATES = ErrorCode("qaInvertedCoordinates",3)
  val GEOSPATIAL_ZERO_COORDINATES = ErrorCode("qaZeroCoordinates",4)
  val GEOSPATIAL_COORDINATES_OUT_OF_RANGE = ErrorCode("qaCoordinatesOutOfRange",5)

  val GEOSPATIAL_UNKNOWN_COUNTRY_NAME = ErrorCode("qaUnknownCountry",7)
  val GEOSPATIAL_ALTITUDE_OUT_OF_RANGE = ErrorCode("qaAltitudeOutOfRange",8)
  val GEOSPATIAL_ERRONOUS_ALTITUDE = ErrorCode("qaErroneousAltitude",9)
  val GEOSPATIAL_MIN_MAX_ALTITUDE_REVERSED = ErrorCode("qaMinMaxAltitudeReversed",10)
  val GEOSPATIAL_DEPTH_IN_FEET = ErrorCode("qaDepthInFeet",11)
  val GEOSPATIAL_DEPTH_OUT_OF_RANGE = ErrorCode("qaDepthOutOfRange",12)
  val GEOSPATIAL_MIN_MAX_DEPTH_REVERSED = ErrorCode("qaMinMaxDepthReversed",13)
  val GEOSPATIAL_ALTITUDE_IN_FEET = ErrorCode("qaAltitudeInFeet",14)
  val GEOSPATIAL_ALTITUDE_NON_NUMERIC = ErrorCode("qaAltitudeNonNumeric",15)
  val GEOSPATIAL_DEPTH_NON_NUMERIC = ErrorCode("qaDepthNonNumeric",16)

  val GEOSPATIAL_COUNTRY_COORDINATE_MISMATCH = ErrorCode("qaCountryCoordinateMismatch",6)
  val GEOSPATIAL_STATE_COORDINATE_MISMATCH = ErrorCode("qaStateCoordinateMismatch",17)
  val COORDINATE_HABITAT_MISMATCH = ErrorCode("qaHabitatMismatch",18)
  val STATE_CENTRE_COORDINATES = ErrorCode("qaStateCentreCoordinates",19)

  val TAXONOMIC_INVALID_SCIENTIFIC_NAME = ErrorCode("qaInvalidScientificName",1001)
  val TAXONOMIC_UNKNOWN_KINGDOM = ErrorCode("qaUnknownKingdom",1002)
  val TAXONOMIC_AMBIGUOUS_NAME = ErrorCode("qaAmbiguousName",1003)
  val TAXONOMIC_NAME_NOTRECOGNISED = ErrorCode("qaNameNotRecognised",1004)
  val TAXONOMIC_HOMONYM_ISSUE = ErrorCode("qaHomonymIssue",1005)


  val OTHER_MISSING_BASIS_OF_RECORD = ErrorCode("qaMissingBasisOfRecord",2001)
  val OTHER_BADLY_FORMED_BASIS_OF_RECORD = ErrorCode("qaBadlyFormedBasisOfRecord",2002)
  val OTHER_INVALID_DATE = ErrorCode("qaInvalidDate",2003)
  val OTHER_COUNTRY_INFERRED_FROM_COORDINATES = ErrorCode("qaCountryInferredByCoordinates",2004)
  val OTHER_UNRECOGNISED_TYPESTATUS = ErrorCode("qaUnrecognisedTypeStatus",2006)
  val OTHER_UNRECOGNISED_COLLECTIONCODE = ErrorCode("qaUnrecognisedCollectionCode",2007)
} 