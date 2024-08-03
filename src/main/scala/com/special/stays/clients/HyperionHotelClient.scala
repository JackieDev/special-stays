package com.special.stays.clients

import cats.effect.{Async, ConcurrentEffect, Resource}
import cats.implicits._
import com.special.stays.models.{AvailableRooms, CheckAvailability}
import io.circe.syntax.EncoderOps
import org.http4s.circe.jsonOf
import org.http4s.{EntityDecoder, Method, Request, Status, Uri}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

trait HyperionHotelClient[F[_]] {
  def checkHotel(): F[Unit]
  def checkHotelIsThere(specialId: String): F[String]
  def checkAvailability(checkAvailability: CheckAvailability): F[Option[AvailableRooms]]
}

object HyperionHotelClient {
  def apply[F[_]: Async: ConcurrentEffect](ec: ExecutionContext)(httpClient: Client[F], hyperionHotelBaseUri: Uri): HyperionHotelClient[F] = new HyperionHotelClient[F] {

    implicit val availableRoomsDecoder: EntityDecoder[F, Option[AvailableRooms]] = jsonOf[F, Option[AvailableRooms]]

    def checkHotel(): F[Unit] = {
      val uri: Uri = Uri.unsafeFromString("http://localhost:8080/check")

      val request: Request[F] = Request[F](
        method = Method.GET,
        uri = uri
      )

      httpClient
        .run(request)
        .use { response =>
          response.status match {
            case Status.Ok =>
              println(s"------------------ Status Ok received from the hyperion hotel during check").pure[F]
            case other =>
              println(s"------------------ Status $other received from the hyperion hotel during check").pure[F]
          }
        }
    }

    def checkHotelIsThere(specialId: String): F[String] = {
      val uri: Uri = (hyperionHotelBaseUri / "check" / specialId)

      val request: Request[F] = Request[F](
        method = Method.GET,
        uri = uri
      )

      httpClient
        .run(request)
        .use { response =>
          response.status match {
            case Status.Ok =>
              response.as[String]
            case Status.NotFound =>
              ("Not Found ....").pure[F]
            case other =>
              (s"Response status: $other").pure[F]
          }
        }
    }

    def checkAvailability(checkAvailability: CheckAvailability): F[Option[AvailableRooms]] = {
      val uri: Uri = (hyperionHotelBaseUri / "check-special-availability")

      val json = checkAvailability.asJson

      val request: Request[F] = Request[F](
        method = Method.POST,
        uri = uri
      ).withEntity(json)

      httpClient
        .run(request)
        .use { response =>
          response.status match {
            case Status.Ok =>
              response.as[Option[AvailableRooms]]
            case Status.NotFound =>
              none[AvailableRooms].pure[F]
            case other =>
              none[AvailableRooms].pure[F]
          }
        }

    }

  }

  def resource[F[_]: Async: ConcurrentEffect](ec: ExecutionContext)(baseUrl: Uri): Resource[F, HyperionHotelClient[F]] =
    BlazeClientBuilder[F](ec)
      .withRequestTimeout(30.seconds)
      .resource
      .map(apply(ec)(_, baseUrl))

}
