package com.mazeboard.macros

import com.mazeboard.kafka.{DataStreamVariable, Window}

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
//import scala.reflect.api.Types
//import scala.reflect.api.TypeTags
import scala.reflect.runtime.universe._

object Macros {
  def withinTransaction(window: Window, dss: (() => DataStreamVariable[_, _])*)(code: => Unit): Unit = macro withinTransaction_Impl
  def withinTransaction_Impl(c: Context)(tag: c.Expr[String])(code: c.Expr[Function0[Unit]]): c.universe.Tree = {
    import c.universe._
    val name = tag match { case Expr(Literal(Constant(xval: String))) => xval }
    val sym = TypeName(c.freshName(s"_Tag_${name}_"))
    val typetag = TypeName(name)
    val termtag = TermName(name)
    val q"..$stats" =  q"""
    """
    println(q"$stats")
    println(q"..$stats")
    q"$stats"
  }
}
