/*
 * Copyright 2023 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.persistence.styleAnother

import akka.actor.typed.ActorRef
import example.persistence.another_domain.{BankAccountError, BankAccountId, Money}

object BankAccountCommands {
  sealed trait Command

  final case class CreateBankAccount(
      accountId: BankAccountId,
      replyTo: ActorRef[CreateBankAccountReply]
  ) extends Command
  sealed trait CreateBankAccountReply
  final case class CreateBankAccountSucceeded(accountId: BankAccountId) extends CreateBankAccountReply

  // ---

  final case class DepositCash(
      accountId: BankAccountId,
      amount: Money,
      replyTo: ActorRef[DepositCashReply]
  ) extends Command
  sealed trait DepositCashReply
  final case class DepositCashSucceeded(accountId: BankAccountId) extends DepositCashReply
  final case class DepositCashFailed(accountId: BankAccountId, error: BankAccountError)
      extends DepositCashReply

  // ---

  final case class WithdrawCash(
      accountId: BankAccountId,
      amount: Money,
      replyTo: ActorRef[WithdrawCashReply]
  ) extends Command

  sealed trait WithdrawCashReply

  final case class WithdrawCashSucceeded(accountId: BankAccountId) extends WithdrawCashReply

  final case class WithdrawCashFailed(accountId: BankAccountId, error: BankAccountError)
      extends WithdrawCashReply

  // ---

  final case class GetBalance(
      accountId: BankAccountId,
      replyTo: ActorRef[GetBalanceReply]
  ) extends Command

  final case class GetBalanceReply(accountId: BankAccountId, balance: Money)

}
