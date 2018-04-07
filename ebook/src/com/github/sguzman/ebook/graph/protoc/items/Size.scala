// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.github.sguzman.ebook.graph.protoc.items

@SerialVersionUID(0L)
final case class Size(
    size: _root_.scala.Float = 0.0f,
    `type`: com.github.sguzman.ebook.graph.protoc.items.Size.Types = com.github.sguzman.ebook.graph.protoc.items.Size.Types.Kb
    ) extends scalapb.GeneratedMessage with scalapb.Message[Size] with scalapb.lenses.Updatable[Size] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      if (size != 0.0f) { __size += _root_.com.google.protobuf.CodedOutputStream.computeFloatSize(1, size) }
      if (`type` != com.github.sguzman.ebook.graph.protoc.items.Size.Types.Kb) { __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(2, `type`.value) }
      __size
    }
    final override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
      {
        val __v = size
        if (__v != 0.0f) {
          _output__.writeFloat(1, __v)
        }
      };
      {
        val __v = `type`
        if (__v != com.github.sguzman.ebook.graph.protoc.items.Size.Types.Kb) {
          _output__.writeEnum(2, __v.value)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.github.sguzman.ebook.graph.protoc.items.Size = {
      var __size = this.size
      var __type = this.`type`
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 13 =>
            __size = _input__.readFloat()
          case 16 =>
            __type = com.github.sguzman.ebook.graph.protoc.items.Size.Types.fromValue(_input__.readEnum())
          case tag => _input__.skipField(tag)
        }
      }
      com.github.sguzman.ebook.graph.protoc.items.Size(
          size = __size,
          `type` = __type
      )
    }
    def withSize(__v: _root_.scala.Float): Size = copy(size = __v)
    def withType(__v: com.github.sguzman.ebook.graph.protoc.items.Size.Types): Size = copy(`type` = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = size
          if (__t != 0.0f) __t else null
        }
        case 2 => {
          val __t = `type`.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PFloat(size)
        case 2 => _root_.scalapb.descriptors.PEnum(`type`.scalaValueDescriptor)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.github.sguzman.ebook.graph.protoc.items.Size
}

object Size extends scalapb.GeneratedMessageCompanion[com.github.sguzman.ebook.graph.protoc.items.Size] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.github.sguzman.ebook.graph.protoc.items.Size] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.github.sguzman.ebook.graph.protoc.items.Size = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.github.sguzman.ebook.graph.protoc.items.Size(
      __fieldsMap.getOrElse(__fields.get(0), 0.0f).asInstanceOf[_root_.scala.Float],
      com.github.sguzman.ebook.graph.protoc.items.Size.Types.fromValue(__fieldsMap.getOrElse(__fields.get(1), com.github.sguzman.ebook.graph.protoc.items.Size.Types.Kb.javaValueDescriptor).asInstanceOf[_root_.com.google.protobuf.Descriptors.EnumValueDescriptor].getNumber)
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.github.sguzman.ebook.graph.protoc.items.Size] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.github.sguzman.ebook.graph.protoc.items.Size(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Float]).getOrElse(0.0f),
        com.github.sguzman.ebook.graph.protoc.items.Size.Types.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(com.github.sguzman.ebook.graph.protoc.items.Size.Types.Kb.scalaValueDescriptor).number)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ItemsProto.javaDescriptor.getMessageTypes.get(1)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ItemsProto.scalaDescriptor.messages(1)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 2 => com.github.sguzman.ebook.graph.protoc.items.Size.Types
    }
  }
  lazy val defaultInstance = com.github.sguzman.ebook.graph.protoc.items.Size(
  )
  sealed trait Types extends _root_.scalapb.GeneratedEnum {
    type EnumType = Types
    def isKb: _root_.scala.Boolean = false
    def isMb: _root_.scala.Boolean = false
    def isGb: _root_.scala.Boolean = false
    def companion: _root_.scalapb.GeneratedEnumCompanion[Types] = com.github.sguzman.ebook.graph.protoc.items.Size.Types
  }
  
  object Types extends _root_.scalapb.GeneratedEnumCompanion[Types] {
    implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[Types] = this
    @SerialVersionUID(0L)
    case object Kb extends Types {
      val value = 0
      val index = 0
      val name = "Kb"
      override def isKb: _root_.scala.Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Mb extends Types {
      val value = 1
      val index = 1
      val name = "Mb"
      override def isMb: _root_.scala.Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Gb extends Types {
      val value = 2
      val index = 2
      val name = "Gb"
      override def isGb: _root_.scala.Boolean = true
    }
    
    @SerialVersionUID(0L)
    final case class Unrecognized(value: _root_.scala.Int) extends Types with _root_.scalapb.UnrecognizedEnum
    
    lazy val values = scala.collection.Seq(Kb, Mb, Gb)
    def fromValue(value: _root_.scala.Int): Types = value match {
      case 0 => Kb
      case 1 => Mb
      case 2 => Gb
      case __other => Unrecognized(__other)
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = com.github.sguzman.ebook.graph.protoc.items.Size.javaDescriptor.getEnumTypes.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = com.github.sguzman.ebook.graph.protoc.items.Size.scalaDescriptor.enums(0)
  }
  implicit class SizeLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.github.sguzman.ebook.graph.protoc.items.Size]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.github.sguzman.ebook.graph.protoc.items.Size](_l) {
    def size: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Float] = field(_.size)((c_, f_) => c_.copy(size = f_))
    def `type`: _root_.scalapb.lenses.Lens[UpperPB, com.github.sguzman.ebook.graph.protoc.items.Size.Types] = field(_.`type`)((c_, f_) => c_.copy(`type` = f_))
  }
  final val SIZE_FIELD_NUMBER = 1
  final val TYPE_FIELD_NUMBER = 2
}
