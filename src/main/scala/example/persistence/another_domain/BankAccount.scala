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
package example.persistence.another_domain

import example.persistence.another_domain.BankAccountEvents._

import java.time.Instant

sealed trait BankAccountError
object BankAccountError {
  case object LimitOverError extends BankAccountError
}

object BankAccount {
  def add(validAccount: ValidAccount, amount: Money): Either[BankAccountError, BankAccountEvent] =
    validAccount.add(amount)

  def subtract(validAccount: ValidAccount, amount: Money): Either[BankAccountError, BankAccountEvent] = {
    validAccount.subtract(amount)
  }
}

sealed trait BankAccount {
  def applyEvent: PartialFunction[BankAccountEvent, BankAccount]

}

case object EmptyAccount extends BankAccount {
  def applyEvent: PartialFunction[BankAccountEvent, ValidAccount] = {
    case BankAccountCreated(accountId, _) => ValidAccount(accountId = accountId)
  }

}
final case class ValidAccount(
    accountId: BankAccountId,
    limit: Money = Money(100000, Money.JPY),
    balance: Money = Money(0, Money.JPY)
) extends BankAccount {

 def applyEvent: PartialFunction[BankAccountEvent, BankAccount] = {
    case CashDeposited(_, _, balance, _) =>
      copy(balance = balance)
    case CashWithdrew(_, _, balance, _) =>
      copy(balance = balance)
  }

  private[another_domain] def add(amount: Money): Either[BankAccountError, BankAccountEvent] =
    if (limit < (balance + amount))
      Left(BankAccountError.LimitOverError)
    else
      Right(
        CashDeposited(accountId = accountId, amount = amount, balance = balance + amount, occurredAt = Instant.now())
      )

  private[another_domain] def subtract(amount: Money): Either[BankAccountError, BankAccountEvent] = {
    if (Money(0, Money.JPY) > (balance - amount))
      Left(BankAccountError.LimitOverError)
    else
      Right(
        CashWithdrew(accountId = accountId, amount = amount, balance = balance - amount, occurredAt = Instant.now())
      )
  }

}
