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
package example.persistence.domain

import java.util.Currency

sealed trait BankAccountError
object BankAccountError {
  case object LimitOverError extends BankAccountError
}

final case class BankAccount(
    bankAccountId: BankAccountId,
    limit: Money = Money(100000, Money.JPY),
    balance: Money = Money(0, Money.JPY)
) {

  def add(amount: Money): Either[BankAccountError, BankAccount] =
    if (limit < (balance + amount))
      Right(coyp(balance = balance + amount))
    else Left(BankAccountError.LimitOverError)

  def subtract(amount: Money): BankAccount = copy(balance = balance - amount)

}