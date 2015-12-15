package com.gvolpe.cluster.actors

import akka.actor.{Actor}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

import scala.concurrent.Await
import scala.util.Try

import scala.concurrent.duration._

/**
 * @author <a link="mailto:damian.albrun@paddypower.com">dalbrun</a>
 */
class TestActor extends Actor  {
  val cluster = Cluster(context.system)
  cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
    classOf[MemberEvent], classOf[UnreachableMember])
  cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
    classOf[MemberEvent], classOf[MemberRemoved])
  cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
    classOf[MemberEvent], classOf[MemberExited])
  cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
    classOf[MemberEvent], classOf[LeaderChanged])

  println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> testActor")

  override def receive: Receive = {
    case state:CurrentClusterState =>
      println(s">>>>>>>>>>> State ${state}")
    case ev:MemberRemoved =>
      println(s">>>>>>>>>>>>>>>>> member removed : ${ev.member.address}")
    case ev:MemberExited =>
      println(s">>>>>>>>>>>>>>>>> member exited : ${ev.member.address}")
    case ev:LeaderChanged =>
      println(s">>>>>>>>>>>>>>>>> leader changed to : ${ev.getLeader}")

      if(ev.getLeader == null) {
        Thread.sleep(1000)
        context.system.registerOnTermination(System.exit(0))
        context.system.terminate()
        new Thread {
          override def run(): Unit = {
            if (Try(Await.ready(context.system.whenTerminated, 20.seconds)).isFailure)
              System.exit(-1)
          }
        }.start()
      }
    case a =>
      println(s">>>>>>>>>>>>>>>>> other ${a.getClass.getName}")
  }
}
