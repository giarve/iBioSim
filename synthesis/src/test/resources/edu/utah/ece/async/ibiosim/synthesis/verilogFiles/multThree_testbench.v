
// ----------------------------
// This module contains both the instantiation and the testbench for the
// multThree_imp.v circuit.
//
// Note:
// - This module compiles and simulates with Verilog synthesizer
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module multThree_testbench();

  wire parity0, parity1;
  reg bit0, bit1, next;

  initial begin
    bit0 = 1'b0;
    bit1 = 1'b0;
    next = 1'b0;
  end

  multthree_imp mt_instance(
  .in0(bit0),
  .in1(bit1),
  .parity0(parity0),
  .parity1(parity1)
  );

  always begin
    #5 next = $random%2;
    if (!next) begin
      #5 bit0 = 1'b1;
    end else begin
      #5 bit1 = 1'b1;
    end

    wait (parity0 == 1'b1 || parity1 == 1'b1) #5;

    if (bit0 == 1'b1) begin
      #5 bit0 = 1'b0;
    end else if(bit1 == 1'b1) begin
      #5 bit1 = 1'b0;
    end

    wait (parity0 != 1'b1 && parity1 != 1'b1) #5;
  end


endmodule
