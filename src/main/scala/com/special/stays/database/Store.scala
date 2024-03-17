package com.special.stays.database

import cats.arrow.FunctionK
import cats.effect._
import cats.syntax.all._
import cats.~>
import com.special.stays.config.DatabaseConfig
import com.special.stays.models._
import com.typesafe.scalalogging.Logger
import doobie.{ConnectionIO, Transactor}
import doobie.hikari.HikariTransactor
import doobie.implicits._

import scala.concurrent.ExecutionContext

trait Store[F[_], G[_]] {
  def liftK: F ~> G
  def commit[A](f: G[A]): F[A]
  def insertSpecial(specialDeal: SpecialDeal): F[DBResult]
  def getAllSpecials(): F[List[SpecialDeal]]
  def getAllSpecialsByCity(city: String): F[List[SpecialDeal]]

}

final class PostgresStore[F[_]: Sync](transactor: Transactor[F], val liftK: FunctionK[F, ConnectionIO])(implicit b: Bracket[F, Throwable])
  extends Store[F, ConnectionIO] {

  val logger: Logger = Logger(getClass)

  override def commit[A](f: ConnectionIO[A]): F[A] = f.transact(transactor)

  override def insertSpecial(specialDeal: SpecialDeal): F[DBResult] =
    commit(SQLQueries.insertSpecial(specialDeal).run.attempt.map {
      case Right(rows) => {
        rows match {
          case 1 =>
            println(s"---------- inserting special into db was successful")
            SuccessfulDBInsert
          case _ =>
            println(s"---------- inserting special into db was not successful")
            FailedDBInsert
        }
      }
      case Left(e) =>
        println(s"---------- inserting special into db was not successful, error: $e")
        FailedDBInsert
    })

  override def getAllSpecials(): F[List[SpecialDeal]] =
    SQLQueries.getSpecials().transact(transactor)

  override def getAllSpecialsByCity(city: String): F[List[SpecialDeal]] =
    SQLQueries.getSpecialsInCity(city).transact(transactor)

}

object PostgresStore {
  def resource[F[_]: Async: ContextShift](config: DatabaseConfig,
                                          ec: ExecutionContext,
                                          blocker: Blocker,
                                          lift: FunctionK[F, ConnectionIO]
                                         ): Resource[F, Store[F, ConnectionIO]] =
    HikariTransactor
      .newHikariTransactor[F](config.driver, config.url, config.username, config.password, ec, blocker)
      .map(trans => new PostgresStore[F](trans, lift))
}
