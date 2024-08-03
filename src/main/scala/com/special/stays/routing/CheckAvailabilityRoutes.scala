package com.special.stays.routing

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import com.special.stays.clients.HyperionHotelClient
import com.special.stays.models.{AvailableRooms, CheckAvailability}
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags._
import scalatags.Text
import scalatags.Text.all._

import java.time.ZonedDateTime.now

class CheckAvailabilityRoutes[F[_]: Sync](hyperionHotelClient: HyperionHotelClient[F]) extends Http4sDsl[F] {

  private def renderUI(specialDealId: String, rooms: Option[AvailableRooms]): Text.TypedTag[String] = {
    val noRooms = NonEmptyList.one(tr(td(colspan := 4, em("No rooms found."))))
    val roomRows: NonEmptyList[Text.TypedTag[String]] =
      rooms match {
        case Some(availableRooms) =>
          NonEmptyList
            .fromList(availableRooms.availableRooms)
            .fold(noRooms) { roomsNEL =>
              roomsNEL.map { room =>
                tr(
                  td(room.roomTypeName),
                  td(room.currentlyAvailable),
                  td(room.discountedPricePerNight),
                  td(button(cls := "book-special-deal", value := specialDealId, "Book Deal"))
                )
              }
            }
        case None =>
          noRooms
      }

    html(
      PageUtils.mkHead("Special Hotel Deals"),
      body(
        div(
          cls := "container",
          h2("Welcome to Special Stays"),
          List(
            h4("Rooms"),
            table(
              cls := "table table-striped",
              thead(
                tr(
                  th("Room type"),
                  th("How many left")
                )
              ),
              tbody(
                roomRows.toList
              )
            )
          )
        )
      )
    )
  }


  val routes: HttpRoutes[F] = {

    HttpRoutes
      .of[F] {
        case GET -> Root / "check-availability" / specialDealId => {
          println(s"-------------------check-availability was hit with $specialDealId ")
          val checkAvailability = CheckAvailability(specialDealId, now, now.plusDays(7)) //TODO come up with some kind of a date picker
          for {
            hotelCheck <- hyperionHotelClient.checkHotel()
            _ = println(s"----------------- hotelCheck: $hotelCheck")
            availability <- hyperionHotelClient.checkAvailability(checkAvailability)
            page <- Ok(renderUI(specialDealId, availability))
          } yield page
        }

        case GET -> Root / "check-hyperion" / specialDealId =>
          for {
            res <- Ok(hyperionHotelClient.checkHotelIsThere(specialDealId))
          } yield res

        case GET -> Root / "check-hyperion"  =>
          for {
            res <- Ok(hyperionHotelClient.checkHotel())
          } yield res

      }

  }

}
