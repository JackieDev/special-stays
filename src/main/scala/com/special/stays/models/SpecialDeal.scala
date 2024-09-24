package com.special.stays.models

import cats.Show
import cats.implicits._

import java.time.{ZoneId, ZonedDateTime}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, HCursor}
import vulcan.Codec
import vulcan.Codec._

case class SpecialDeal(id: String,
                       description: String,
                       hotelName: String,
                       cityOfLocation: City,
                       totalNights: Option[Int], // only use if deal is for fixed number of nights such as Valentines and MayBankHoliday
                       discountPercentageOff: Double,
                       availableFrom: ZonedDateTime,
                       availableTo: ZonedDateTime)

object SpecialDeal {

  implicit val cityDecode: Decoder[City] = deriveDecoder[City]
  implicit val cityEncode: Encoder[City] = deriveEncoder[City]

  implicit val encode: Encoder[SpecialDeal] = deriveEncoder[SpecialDeal]

  implicit val decodeSD: Decoder[SpecialDeal] =  new Decoder[SpecialDeal] {
    final def apply(c: HCursor): Decoder.Result[SpecialDeal] = {
      println(s"------------- do we get inside the special deal decoder?")
      for {
        id <- c.downField("special_id").as[String]
        description <- c.downField("description").as[String]
        name <- c.downField("hotel_name").as[String]
        city <- c.downField("city").as[City]
        nights <- c.downField("total_nights").as[Option[Int]]
        discount <- c.downField("discount_percentage_off").as[Double]
        from <- c.downField("available_from").as[ZonedDateTime]
        to <- c.downField("available_to").as[ZonedDateTime]
      } yield {
        val sd = new SpecialDeal(
          id,
          description,
          name,
          city,
          nights,
          discount,
          from,
          to
        )

        println(s"------------------------- sd: $sd")
        sd
      }
    }
  }

  implicit val zonedDateTimeCodec: Codec[ZonedDateTime] =
    Codec.instant.imap(zdt => ZonedDateTime.ofInstant(zdt, ZoneId.of("Z")))(_.toInstant)

  implicit val codec: Codec[SpecialDeal] =
    Codec.record("SpecialDeal", "special-deal", None) { f =>
      (
        f("id", _.id),
        f("description", _.description),
        f("hotelName", _.hotelName),
        f("cityOfLocation", _.cityOfLocation),
        f("totalNights", _.totalNights),
        f("discountPercentageOff", _.discountPercentageOff),
        f("availableFrom", _.availableFrom),
        f("availableTo", _.availableTo)
        ).mapN(SpecialDeal(_, _, _, _, _, _, _, _))
    }

  implicit val showSpecial: Show[SpecialDeal] = Show.show(special => s"${special.id}, ${special.description}")

}
