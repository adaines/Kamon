/* =========================================================================================
 * Copyright © 2013-2016 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.instrumentation.jdbc

import java.util.concurrent.Callable

import kamon.util.instrumentation.KamonInstrumentation
import net.bytebuddy.implementation.MethodDelegation._
import net.bytebuddy.implementation.bind.annotation._
import net.bytebuddy.matcher.ElementMatchers._

import scala.beans.BeanProperty

class PreparedStatementInstrumentation extends KamonInstrumentation {

  forSubtypeOf("java.sql.PreparedStatement")

  addMixin(classOf[PreparedStatementInjector])

  addTransformation { (builder, _) ⇒
    builder
      .method(named("execute").or(named("executeUpdate").or(named("executeQuery"))))
      .intercept(to(PreparedStatementInterceptor).filter(NotDeclaredByObject))
  }


  class PreparedStatementInjector extends PreparedStatementExtension {
    @BeanProperty var sql:String = _
  }

  object PreparedStatementInterceptor {
    @RuntimeType
    def execute(@SuperCall callable: Callable[_], @This ps: PreparedStatementExtension): Unit = {
      SqlProcessor.processStatement(callable, ps.getSql)
    }
  }
}

trait PreparedStatementExtension {
  def getSql: String
  def setSql(sql: String): Unit
}