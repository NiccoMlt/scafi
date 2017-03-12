package it.unibo.scafi.core

import scala.util.control.Exception._

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
 * This trait defines a component that extends Core and Language
 * It starts concretising the framework by implementing the key element of field-calculus semantics, namely:
 * - An export is a map from paths to values, and a value is a list of slots
 * - An Execution template implementing the whole operational semantics
 * - A basic Factory
 * - Additional ops to Context and Export, realised by family polymorphism
 *
 * This is still abstract in that we do not dictate how Context and Export are implemented and optimised internally
 */


trait Semantics extends Core with Language {

  override type CONTEXT <: Context with ContextOps
  override type EXPORT <: Export with ExportOps
  override type EXECUTION <: ExecutionTemplate

  trait Slot extends Serializable
  sealed case class Nbr[A](index: Int) extends Slot
  sealed case class Rep[A](index: Int) extends Slot
  sealed case class FunCall[A](index: Int, funId: Any) extends Slot

  trait Path {
    def push(slot: Slot): Path
    def pull(): Path
    def matches(path: Path): Boolean
    def isRoot: Boolean

    def /(slot: Slot) = push(slot)
  }

  trait ExportOps { self: EXPORT =>
    def put[A](path: Path, value: A): A
    def get[A](path: Path): Option[A]
  }

  trait ContextOps { self: CONTEXT =>
    def readSlot[A](i: ID, p: Path): Option[A]
  }

  trait Factory {
    def emptyPath(): Path
    def emptyExport(): EXPORT
    def path(slots: Slot*): Path
    def export(exps: (Path,Any)*): EXPORT
  }

  implicit val factory: Factory

  trait AggregateProgramSpecification { constructs: Constructs =>
    type MainResult
    def main(): MainResult
  }

  /**
   * It implements the whole operational semantics.
   */
  trait ExecutionTemplate extends (CONTEXT => EXPORT) with Constructs with AggregateProgramSpecification {
    self:Constructs =>

    import ExecutionTemplate._

    class RoundVM(val context: CONTEXT, var export: EXPORT, var status: Status){}
    
    @transient private var context: CONTEXT = _
    @transient private var export: EXPORT = _
    @transient private var status: Status = _

    def apply(c: CONTEXT): EXPORT = {
      round(c,main())
    }

    def round(c: CONTEXT, e: =>Any = main()): EXPORT = {
        context = c
        export = factory.emptyExport
        status = Status()
        export.put(factory.emptyPath, e)
        this.export
    }

    def mid(): ID = context.selfId

    def neighbour(): Option[ID] = status.neighbour

    def rep[A](init: A)(fun: (A) => A): A = {
      ensure(status.neighbour.isEmpty, "can't nest rep into fold")

      nest(Rep[A](status.index)) {
        fun(context.readSlot(context.selfId, status.path).getOrElse(init))
      }
    }

    def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = {
      ensure(status.neighbour.isEmpty, "can't nest fold constructs")

      try {
        val v = alignedNeighbours()
        val res = v.map { i =>
          handling(classOf[OutOfDomainException]) by (_ => init) apply {
            frozen { status = status.foldInto(i); expr }
          }
        }
        res.fold(init)(aggr)
      } finally {
        status = status.foldOut()
        // FIX: increment index for correct sequencing of NBRs
        // NOTE: it increments the index even though NBR is not used
        status = status.incIndex()
      }
    }

    // Works only if aligned yields self as last element..
    // Why? Because nest performs 'exp.put(status.path, expr)'
    // So the export must be overridden by the current device (at last).
    def nbr[A](expr: => A): A = {
      ensure(status.isFolding, "nbr should be nested into fold")
      nest(Nbr[A](status.index)) {
        if (status.neighbour.get == context.selfId){
          status = status.foldOut(); expr
        } else {
          context.readSlot[A](status.neighbour.get, status.path)
             .getOrElse(throw new OutOfDomainException(context.selfId, status.neighbour.get, status.path))
        }
      }
    }

    def aggregate[T](f: => T): T = {
      var funId = Thread.currentThread().getStackTrace()(3)

      nest(FunCall[T](status.index, funId)) { f }
    }

    def sense[A](name: LSNS): A = context.sense[A](name).getOrElse(throw new SensorUnknownException(context.selfId, name))

    def nbrvar[A](name: NSNS): A = {
      val nbr = status.neighbour.get
      context.nbrSense(name)(nbr).getOrElse(throw new NbrSensorUnknownException(context.selfId, name, nbr))
    }

    private[this] def nest[A](slot: Slot)(expr: => A): A = {
      try {
        status = status.push().nest(slot)  // prepare nested call
        export.put(status.path, expr) // function return value is result of expr
      } finally {
        status = status.pop().incIndex(); // do not forger to restore the status
      }
    }

    private[this] def frozen[A](expr: => A): A = {
      try {
        status = status.push()
        expr
      } finally {
        status = status.pop()
      }
    }

    private[this] def alignedNeighbours(): List[ID] =
      context.exports
        .filter(p => p._1 != context.selfId && (status.path.isRoot || p._2.get(status.path).isDefined))
        .map(_._1)
        .toList
        .++(List(context.selfId))
  }

  private[scafi] object ExecutionTemplate extends Serializable {

    trait Status extends Serializable {
      val path: Path
      val index: Int
      val neighbour: Option[ID]

      def isFolding: Boolean
      def foldInto(id: ID): Status
      def foldOut(): Status
      def nest(s: Slot): Status
      def incIndex(): Status
      def push(): Status
      def pop(): Status
    }

    private case class StatusImpl(
        path: Path = factory.emptyPath(),
        index: Int = 0,
        neighbour: Option[ID] = None,
        stack: List[(Path, Int, Option[ID])] = List()) extends Status {

      def isFolding: Boolean = neighbour.isDefined
      def foldInto(id: ID): Status = StatusImpl(path, index, Some(id), stack)
      def foldOut(): Status = StatusImpl(path, index, None, stack)
      def push(): Status = StatusImpl(path, index, neighbour, (path, index, neighbour) :: stack)
      def pop(): Status = stack match {
        case (p, i, n) :: s => StatusImpl(p, i, n, s)
        case _           => throw new Exception()
      }
      def nest(s: Slot): Status = StatusImpl(path.push(s), 0, neighbour, stack)
      def incIndex(): Status = StatusImpl(path, index + 1, neighbour, stack)
    }

    object Status {
      def apply(): Status = StatusImpl()
    }

    def ensure(b: => Boolean, s: String): Unit = {
      b match {
        case false => throw new Exception(s)
        case _     =>
      }
    }

    case class OutOfDomainException(selfId: ID, nbr: ID, path: Path) extends Exception() {
      override def toString: String = s"OutOfDomainException: $selfId , $nbr, $path"
    }

    case class SensorUnknownException(selfId: ID, name: LSNS) extends Exception() {
      override def toString: String = s"SensorUnknownException: $selfId , $name"
    }

    case class NbrSensorUnknownException(selfId: ID, name: NSNS, nbr: ID) extends Exception() {
      override def toString: String = s"NbrSensorUnknownException: $selfId , $name, $nbr"
    }

  }

}
