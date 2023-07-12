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

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import example.persistence.another_domain._

import java.time.Instant

/** このスタイルの問題
  *
  *   - デメリット
  *     - Behaviorを使ったアクタープログラミングができない。
  *   - メリット
  *     - 記述するコード量が少ない
  *       - 複雑なステートマシンもドメインロジックとして実装できる
  *       - ドメインロジック(状態遷移部分)のミラー実装をアクターに書かなくてよくなる
  */
object BankAccountAggregate {

  def apply(accountId: BankAccountId): Behavior[BankAccountCommands.Command] = {
    EventSourcedBehavior.withEnforcedReplies(
      persistenceId = PersistenceId.ofUniqueId(accountId.asString),
      emptyState = EmptyAccount,
      commandHandler,
      eventHandler
    )
  }

  private def commandHandler
      : (BankAccount, BankAccountCommands.Command) => ReplyEffect[BankAccountEvent, BankAccount] = {
    // 口座残高の取得
    case (account: ValidAccount, BankAccountCommands.GetBalance(aggregateId, replyTo)) =>
      Effect.reply(replyTo)(BankAccountCommands.GetBalanceReply(aggregateId, account.balance))
    // 口座開設コマンド
    case (EmptyAccount, BankAccountCommands.CreateBankAccount(aggregateId, replyTo)) =>
      Effect.persist(BankAccountEvents.BankAccountCreated(aggregateId, Instant.now())).thenReply(replyTo) { _ =>
        BankAccountCommands.CreateBankAccountSucceeded(aggregateId)
      }
    // 現金の入金
    case (account: ValidAccount, BankAccountCommands.DepositCash(aggregateId, amount, replyTo)) =>
      // NOTE: コマンドはドメインロジックを呼び出す
      BankAccount
        .add(account, amount).fold(
          error => Effect.reply(replyTo)(BankAccountCommands.DepositCashFailed(aggregateId, error)),
          event =>
            Effect
              .persist(event)
              .thenReply(replyTo)(_ => BankAccountCommands.DepositCashSucceeded(aggregateId))
        )
    // 現金の出金
    case (account: ValidAccount, BankAccountCommands.WithdrawCash(aggregateId, amount, replyTo)) =>
      BankAccount
        .subtract(account, amount).fold(
        error => Effect.reply(replyTo)(BankAccountCommands.WithdrawCashFailed(aggregateId, error)),
        event =>
          Effect
            .persist(event)
            .thenReply(replyTo)(_ => BankAccountCommands.WithdrawCashSucceeded(aggregateId))
      )
    case _ => throw new MatchError("Invalid state")
  }

  private def eventHandler: (BankAccount, BankAccountEvent) => BankAccount = {
    (account, event) => account.applyEvent(event)
  }

}
