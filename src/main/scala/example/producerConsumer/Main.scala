package example.producerConsumer

import akka.actor.typed.ActorSystem
import akka.actor.typed.delivery.{ ConsumerController, ProducerController }
import akka.actor.typed.scaladsl.Behaviors

import java.util.UUID

object Main extends App {
  val behavior = Behaviors.setup[Any] { context =>
    val consumerController = context.spawn(ConsumerController[FibonacciConsumer.Command](), "consumerController")
    context.spawn(FibonacciConsumer(consumerController), "consumer")

    val producerId = s"fibonacci-${UUID.randomUUID()}"
    val producerController = context.spawn(
      ProducerController[FibonacciConsumer.Command](producerId, durableQueueBehavior = None),
      "producerController"
    )
    context.spawn(FibonacciProducer(producerController), "producer")

    consumerController ! ConsumerController.RegisterToProducerController(producerController)

    Behaviors.same
  }
  ActorSystem(behavior, "app")
}
