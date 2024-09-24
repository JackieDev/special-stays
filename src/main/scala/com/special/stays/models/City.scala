package com.special.stays.models

import vulcan.Codec

case class City(name: String)

object City {
  def fromString(string: String): City = City(string.toLowerCase)
  def toString(city: City): String = city.name.toLowerCase

  implicit val codec: Codec[City] = Codec.string.imap(City(_))(toString)
}
