package com.special.stays.models

import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import vulcan.Codec
import vulcan.Codec._

import java.time.{ZoneId, ZonedDateTime}

case class SpecialDealBooking(specialDealId: String,
                              customerName: String,
                              startDate: ZonedDateTime,
                              endDate: ZonedDateTime)

object SpecialDealBooking {

  implicit val decode: Decoder[SpecialDealBooking] = deriveDecoder[SpecialDealBooking]
  implicit val encode: Encoder[SpecialDealBooking] = deriveEncoder[SpecialDealBooking]

  implicit val zonedDateTimeCodec: Codec[ZonedDateTime] =
    Codec.instant.imap(zdt => ZonedDateTime.ofInstant(zdt, ZoneId.of("Z")))(_.toInstant)

  implicit val codec: Codec[SpecialDealBooking] =
    Codec.record("SpecialDealBooking", "special-deal-booking", None) { f =>
      (
        f("specialDealId", _.specialDealId),
        f("customerName", _.customerName),
        f("startDate", _.startDate),
        f("endDate", _.endDate)
        ).mapN(SpecialDealBooking(_, _, _, _))
    }
}
