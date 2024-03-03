package com.special.stays.models

import cats.Show
import cats.implicits._

import java.time.{ZoneId, ZonedDateTime}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import vulcan.Codec
import vulcan.Codec._

case class SpecialDeal(id: String,
                       description: String,
                       totalNights: Option[Int], // only use if deal is for fixed number of nights such as Valentines and MayBankHoilday
                       discountPercentageOff: Double,
                       availableFrom: ZonedDateTime,
                       availableTo: ZonedDateTime)

object SpecialDeal {

  implicit val decode: Decoder[SpecialDeal] = deriveDecoder[SpecialDeal]
  implicit val encode: Encoder[SpecialDeal] = deriveEncoder[SpecialDeal]

  implicit val zonedDateTimeCodec: Codec[ZonedDateTime] =
    Codec.instant.imap(zdt => ZonedDateTime.ofInstant(zdt, ZoneId.of("Z")))(_.toInstant)

  implicit val codec: Codec[SpecialDeal] =
    Codec.record("SpecialDeal", "special-deal", None) { f =>
      (
        f("id", _.id),
        f("description", _.description),
        f("totalNights", _.totalNights),
        f("discountPercentageOff", _.discountPercentageOff),
        f("availableFrom", _.availableFrom),
        f("availableTo", _.availableTo)
        ).mapN(SpecialDeal(_, _, _, _, _, _))
    }

  implicit val showSpecial: Show[SpecialDeal] = Show.show(special => s"${special.id}, ${special.description}")

}
