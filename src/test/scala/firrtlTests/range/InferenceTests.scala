package firrtlTests.range
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import firrtl.range.Inference._
import firrtl.ir._
import firrtl.PrimOps._

class opTypeTests extends FlatSpec with Matchers {
  val U0 = Reference("x", UIntType(IntWidth(0)))
  val U1 = Reference("x", UIntType(IntWidth(1)))
  val U2 = Reference("x", UIntType(IntWidth(2)))
  val U3 = Reference("x", UIntType(IntWidth(3)))
  val U4 = Reference("x", UIntType(IntWidth(4)))
  val U5 = Reference("x", UIntType(IntWidth(5)))
  val S0 = Reference("x", SIntType(IntWidth(0)))
  val S1 = Reference("x", SIntType(IntWidth(1)))
  val S2 = Reference("x", SIntType(IntWidth(2)))
  val S3 = Reference("x", SIntType(IntWidth(3)))
  val S4 = Reference("x", SIntType(IntWidth(4)))
  val S5 = Reference("x", SIntType(IntWidth(5)))
  // Sub tests
  "opType of sub(U<0>, U<0>)" should "be S<0>" in {
    opType(DoPrim(Sub, Seq(U0, U0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of sub(U<0>, S<0>)" should "be S<0>" in {
    opType(DoPrim(Sub, Seq(U0, S0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of sub(S<0>, U<0>)" should "be S<0>" in {
    opType(DoPrim(Sub, Seq(S0, U0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of sub(S<0>, S<0>)" should "be S<0>" in {
    opType(DoPrim(Sub, Seq(S0, S0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of sub(S<3>, U<3>)" should "be S<5>" in {
    opType(DoPrim(Sub, Seq(S3, U3), Seq.empty, UnknownType)) should be (S5.tpe);
  }
  "opType of sub(S<1>, U<3>)" should "be S<4>" in {
    opType(DoPrim(Sub, Seq(S1, U3), Seq.empty, UnknownType)) should be (S4.tpe);
  }
  // Add tests
  "opType of add(U<0>, U<0>)" should "be U<0>" in {
    opType(DoPrim(Add, Seq(U0, U0), Seq.empty, UnknownType)) should be (U0.tpe);
  }
  "opType of add(U<0>, S<0>)" should "be S<0>" in {
    opType(DoPrim(Add, Seq(U0, S0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of add(S<0>, U<0>)" should "be S<0>" in {
    opType(DoPrim(Add, Seq(S0, U0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of add(S<0>, S<0>)" should "be S<0>" in {
    opType(DoPrim(Add, Seq(S0, S0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of add(U<1>, S<0>)" should "be S<2>" in {
    opType(DoPrim(Add, Seq(U1, S0), Seq.empty, UnknownType)) should be (S2.tpe);
  }
  "opType of add(U<0>, S<1>)" should "be S<1>" in {
    opType(DoPrim(Add, Seq(U0, S1), Seq.empty, UnknownType)) should be (S1.tpe);
  }
  "opType of add(U<3>, S<1>)" should "be S<4>" in {
    opType(DoPrim(Add, Seq(U3, S1), Seq.empty, UnknownType)) should be (S4.tpe);
  }
  // Mul tests
  "opType of mul(U<0>, U<0>)" should "be U<0>" in {
    opType(DoPrim(Mul, Seq(U0, U0), Seq.empty, UnknownType)) should be (U0.tpe);
  }
  "opType of mul(U<0>, S<0>)" should "be S<0>" in {
    opType(DoPrim(Mul, Seq(U0, S0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of mul(S<0>, U<0>)" should "be S<0>" in {
    opType(DoPrim(Mul, Seq(S0, U0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of mul(S<0>, S<0>)" should "be S<0>" in {
    opType(DoPrim(Mul, Seq(S0, S0), Seq.empty, UnknownType)) should be (S0.tpe);
  }
  "opType of mul(U<2>, U<3>)" should "be U<5>" in {
    opType(DoPrim(Mul, Seq(U2, U3), Seq.empty, UnknownType)) should be (U5.tpe);
  }
  "opType of mul(U<2>, S<2>)" should "be S<4>" in {
    opType(DoPrim(Mul, Seq(U2, S2), Seq.empty, UnknownType)) should be (S4.tpe);
  }
  "opType of mul(S<3>, S<2>)" should "be S<5>" in {
    opType(DoPrim(Mul, Seq(S3, S2), Seq.empty, UnknownType)) should be (S5.tpe);
  }
  "opType of mul(U<2>, U<0>)" should "be U<0>" in {
    opType(DoPrim(Mul, Seq(U2, U0), Seq.empty, UnknownType)) should be (U0.tpe);
  }
}

// vim: set ts=4 sw=4 et: