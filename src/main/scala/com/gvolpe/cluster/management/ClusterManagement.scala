package com.gvolpe.cluster.management

import akka.actor.ActorSystem
import akka.cluster.{Member, MemberStatus, Cluster}
import com.gvolpe.cluster.actors.GracefulShutdownActor

import scala.collection.immutable.SortedSet
import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.duration._

class ClusterManagement(system: ActorSystem, port: Int) extends ClusterManagementMBean {

  override def leaveClusterAndShutdown(): Unit = {
    println(s"INVOKING MBEAN ${system.name}")

    val shutdownActor = system.actorOf(GracefulShutdownActor.props)
//    shutdownActor ! GracefulShutdownActor.LeaveAndShutdownNode
    cluster.leave(cluster.selfAddress)
  }


  private val cluster:Cluster = {
    val cluster:Cluster = Cluster(system)
/*
    cluster.registerOnMemberRemoved {
      println(s">>>>>>>>>>>>>>>>>> State: ${cluster.state.members.find(_.address == cluster.selfAddress).get.status}")
      println(s">>>>>>>>>>>>>>>>>> is Terminated?: ${cluster.isTerminated}")
      do {
        Thread.sleep(1000)
      } while(!isReallyTerminated(cluster))

      system.registerOnTermination(System.exit(0))
      system.terminate()
      new Thread {
        override def run(): Unit = {
          if (Try(Await.ready(system.whenTerminated, 20.seconds)).isFailure)
            System.exit(-1)
        }
      }.start()
    }
    */
    cluster
  }

  def isReallyTerminated(cluster:Cluster): Boolean = {
    val members: SortedSet[Member] = cluster.state.members
    val status: MemberStatus = members.find(_.address == cluster.selfAddress).get.status
//    println(s">>>>>>>>>>>>>> $status Members ${members}")
    println(s"${Cluster(system).state}")
    status == MemberStatus.removed
  }

}
