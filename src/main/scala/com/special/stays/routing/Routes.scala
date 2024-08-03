package com.special.stays.routing

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import com.special.stays.database.Store
import com.special.stays.models.SpecialDeal
import com.special.stays.models.SpecialDeal._
import com.typesafe.scalalogging.Logger
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags._

import scalatags.Text
import scalatags.Text.TypedTag
import scalatags.Text.all._

import java.time.Instant

class Routes[F[_]: Sync, G[_]](store: Store[F, G]) extends Http4sDsl[F] {

  val logger = Logger(getClass)

  private def renderInstant(x: Instant): Text.TypedTag[String] = span(
    cls := "text-nowrap",
    x.toString()
  )

  private def renderUI(specialDeals: List[SpecialDeal]): Text.TypedTag[String] = {
    val specialRows =
      NonEmptyList
        .fromList(specialDeals).fold(NonEmptyList.one(tr(td(colspan := 8, em("No specials found."))))) { specialsNel =>
        specialsNel.map { special =>
          tr(
            td(special.id),
            td(special.description),
            td(special.hotelName),
            td(special.cityOfLocation),
            td(special.totalNights),
            td(special.discountPercentageOff),
            td(renderInstant(special.availableFrom.toInstant)),
            td(renderInstant(special.availableTo.toInstant)),
            td(button(cls := "check-availability", value := special.id, "Check Availability"))
          )
        }
      }

    html(
      PageUtils.mkHead("Special Hotel Deals"),
      body(
        div(
          cls := "container",
          h2("Welcome to Special Stays"),
          form(cls := "search", method := "GET", action := "/search",
            input(cls := "search-input", `type` := "text", name := "city")
          ),
          button(cls := "search", "Search"),
          List(
            h4("Special Deals"),
            table(
              cls := "table table-striped",
              thead(
                tr(
                  th("Id"),
                  th("Description"),
                  th("Hotel Name"),
                  th("City"),
                  th("Total Nights"),
                  th("Discount"),
                  th("From"),
                  th("Until"),
                  th("Book Deal")
                )
              ),
              tbody(
                specialRows.toList
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

        case (GET | HEAD) -> Root / "ping" =>
          Ok("Pong!")

        case GET -> Root / "special-deals" =>
          for {
            specials <- store.getAllSpecials()
            page <- Ok(renderUI(specials))
          } yield page

        case GET -> Root / "search" / city =>
          for {
            specials <- store.getAllSpecialsByCity(city)
            page <- Ok(renderUI(specials))
          } yield page

      }

  }

}
