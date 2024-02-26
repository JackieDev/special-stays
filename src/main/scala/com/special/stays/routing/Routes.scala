package com.special.stays.routing

import cats.effect._
import cats.implicits._

import com.typesafe.scalalogging.Logger
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class Routes[F[_]: Sync]() extends Http4sDsl[F] {

  val logger = Logger(getClass)

  val routes: HttpRoutes[F] = {

    HttpRoutes
      .of[F] {

        case (GET | HEAD) -> Root / "ping" =>
          Ok("Pong!")

        case GET -> Root / "special-deals" =>
//          for {
//            specials <- store.getAllSpecials
//            res <- specials match {
//              case Nil => Ok(s"There are currently no specials available")
//              case _ => Ok(s"These are the specials we currently have available: ${specials.mkString_("\n")}")
//            }
//          } yield res

          Ok(s"There are currently no specials available")

      }

  }

}
