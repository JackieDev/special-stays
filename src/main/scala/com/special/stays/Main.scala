package com.special.stays

import cats.effect._
import cats.syntax.all._
import com.special.stays.consumers.SpecialDealConsumer
import fs2.Stream

object Main extends IOApp {

  def stream: Stream[IO, Unit] =
    for {
      consumer <- SpecialDealConsumer.stream[IO]
    } yield consumer

  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

}
