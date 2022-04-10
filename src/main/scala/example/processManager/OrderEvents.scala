package example.processManager

import akka.actor.typed.ActorRef

import java.time.Instant
import java.util.UUID

object OrderEvents {
  sealed trait Event extends CborSerializable {
    def id: UUID
    def orderId: OrderId
    def occurredAt: Instant
  }

  case class OrderBegan(
      id: UUID,
      orderId: OrderId,
      orderItems: OrderItems,
      replyTo: ActorRef[OrderProtocol.CommandRequest],
      occurredAt: Instant
  ) extends Event

  case class StockSecured(id: UUID, orderId: OrderId, occurredAt: Instant)    extends Event
  case class BillingFailed(id: UUID, orderId: OrderId, occurredAt: Instant)   extends Event
  case class OrderCommitted(id: UUID, orderId: OrderId, occurredAt: Instant)  extends Event
  case class OrderRollbacked(id: UUID, orderId: OrderId, occurredAt: Instant) extends Event
}