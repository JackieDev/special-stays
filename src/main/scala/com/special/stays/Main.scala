package com.special.stays

import cats.effect._
import cats.syntax.all._
import com.special.stays.config.ServiceConfig
import com.special.stays.consumers.SpecialDealConsumer
import com.special.stays.database.{PostgresStore, SchemaMigration, Store}
import com.special.stays.routing.Routes
import com.typesafe.scalalogging.Logger
import doobie.free.connection.ConnectionIO
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Main extends IOApp {

  implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val logger: Logger = Logger(getClass)

  def databaseResource(config: ServiceConfig): Resource[IO, Store[IO, ConnectionIO]] =
    for {
      _ <- Resource.eval(logger.info("------- Loading special-stays database...").pure[IO])
      _ <- Resource.eval(SchemaMigration[IO](config.specialStays.db))
      block <- Blocker[IO]
      dbES = Executors.newFixedThreadPool(config.specialStays.db.maxConnectionPoolSize)
      dbEC = ExecutionContext.fromExecutorService(dbES)
      db <- PostgresStore.resource(config.specialStays.db, dbEC, block, LiftIO.liftK[ConnectionIO])
    } yield db


  private def runServer(service: HttpRoutes[IO], host: String, port: Int, ec: ExecutionContext, consumer: Stream[IO, Unit])(
    implicit timer: Timer[IO],
    cs: ContextShift[IO]
  ): Stream[IO, ExitCode] =
    BlazeServerBuilder[IO](ec)
      .bindHttp(port, host)
      .withHttpApp(service.orNotFound)
      .serve
      .concurrently(consumer)


  def stream: Stream[IO, ExitCode] =
    for {
      configs <- Stream.eval(ServiceConfig.loadApplicationConfig.onError {
        case err => logger.error(s"The Special Stays service is unable to load config, aborting. Failed with error: $err").pure[IO]
      })

      (config, _) = configs

      appExecutorService = Executors.newFixedThreadPool(8)
      appExecutionContext = ExecutionContext.fromExecutorService(appExecutorService)

      db <- Stream.resource(databaseResource(config))
      consumer = SpecialDealConsumer.stream[IO, ConnectionIO](db)

      httpService = new Routes[IO]().routes

      server <- runServer(httpService, config.specialStays.httpd.host, config.specialStays.httpd.port, appExecutionContext, consumer)
    } yield server


  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

}
