package com.special.stays.consumers

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.special.stays.models.SpecialDeal
import fs2.kafka.vulcan._
import vulcan.Codec
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, KafkaConsumer, RecordDeserializer, commitBatchWithin}

import scala.concurrent.duration.DurationInt

object SpecialDealConsumer {

  val topic = "special-deals"

  def stream[F[_]: ConcurrentEffect: ContextShift: Timer](): fs2.Stream[F, Unit] = {

    val avroSettings = AvroSettings[F](
      SchemaRegistryClientSettings[F]("http://0.0.0.0:8081")
        .withAuth(Auth.Basic("username", "password")))

    implicit val keys: RecordDeserializer[F, SpecialDeal] = AvroDeserializer[SpecialDeal].using(avroSettings)

    val consumerSettings =
      ConsumerSettings[F, String, SpecialDeal]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("hyperion-hotel")

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic)
      .records
      .map { committable =>
        println(s"------------ Received the following from topic: $topic: ${committable.record}")
        committable.offset
      }
      .through(commitBatchWithin(50, 10.seconds))

  }

}
