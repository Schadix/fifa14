import com.tinkerpop.rexster.client.RexsterClientFactory
import play.api.libs.ws.{Response, WS}
import play.api.mvc.{Results, Result}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent._
import com.tinkerpop.rexster._

println("hello")

//val client= RexsterClientFactory.open("localhost", "graph")

//val result = client.execute("g.V.name").toArray()

val source = scala.io.Source.fromFile("/Users/schadix/workspace/fifa14/conf/test.json")
val lines = source.mkString













































source.close()

