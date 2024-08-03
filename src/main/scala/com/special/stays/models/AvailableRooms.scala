package com.special.stays.models

import io.circe.{Decoder, Encoder, HCursor }
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class AvailableRoom(roomTypeName: String,
                         currentlyAvailable: Int,
                         discountedPricePerNight: Option[Double])

case class AvailableRooms(availableRooms: List[AvailableRoom])

object AvailableRooms {

  implicit val decodeAR: Decoder[AvailableRoom] = deriveDecoder[AvailableRoom]
  implicit val encodeAR: Encoder[AvailableRoom] = deriveEncoder[AvailableRoom]

  implicit val decodeARs: Decoder[AvailableRooms] = new Decoder[AvailableRooms] {
    final def apply(c: HCursor): Decoder.Result[AvailableRooms] =
      for {
        availableRooms <- c.downField("availableRooms").as[List[AvailableRoom]]
      } yield {
        new AvailableRooms(availableRooms)
      }
  }

  implicit val encodeARs: Encoder[AvailableRooms] = deriveEncoder[AvailableRooms]
}
