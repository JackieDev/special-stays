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

  def mkHead(title: String): TypedTag[String] = head(
    tag("title")(title),
    link(
      href := "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css",
      rel  := "stylesheet"
    ),
    link(
      href        := "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css",
      rel         := "stylesheet"
    )
  )

  private def renderInstant(x: Instant): Text.TypedTag[String] = span(
    cls := "text-nowrap",
    x.toString()
  )

  private def renderUI(specialDeals: List[SpecialDeal]): Text.TypedTag[String] = {
    val specialRows =
      NonEmptyList
        .fromList(specialDeals).fold(NonEmptyList.one(tr(td(colspan := 6, em("No specials found."))))) { specialsNel =>
        specialsNel.map { special =>
          tr(
            td(special.id),
            td(special.description),
            td(special.hotelName),
            td(special.cityOfLocation),
            td(special.totalNights),
            td(special.discountPercentageOff),
            td(renderInstant(special.availableFrom.toInstant)),
            td(renderInstant(special.availableTo.toInstant))
          )
        }
      }

    html(
      mkHead("Special Hotel Deals"),
      body(
        div(
          cls := "container",
          h2(a(href := "/debug", cls := "link-secondary", "Welcome to Special Stays")),
          input(cls := "search-input"),
          button(cls := "search", value := "Search"),
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
                  th("Until")
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

      }

  }

}
